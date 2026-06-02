# Spring AI 开发指南

## 适用人群
已掌握Spring Boot，希望将AI能力集成到Java应用中的开发者。

## 什么是Spring AI
Spring AI是Spring生态中的AI集成框架，提供统一的API对接各大AI模型（OpenAI、阿里云DashScope、Ollama等），让Java开发者用熟悉的Spring方式开发AI应用。

核心能力：ChatClient、Advisor、RAG、工具调用、向量存储。

---

## ChatClient 基础使用
ChatClient是Spring AI的核心接口，封装了与AI模型的交互。

```java
@Component
public class MyAiApp {

    private final ChatClient chatClient;

    // 通过Builder构建ChatClient，注入模型
    public MyAiApp(ChatModel chatModel) {
        chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一个专业的Java教学助手")
                .build();
    }

    // 同步调用
    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    // 流式调用（SSE）
    public Flux<String> chatStream(String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
```

---

## Advisor 机制
Advisor是Spring AI的拦截器，可以在请求前后插入自定义逻辑，类似Spring MVC的拦截器。

```java
// 自定义日志Advisor
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest request, CallAroundAdvisorChain chain) {
        // 请求前：记录用户输入
        log.info("用户输入: {}", request.userText());
        AdvisedResponse response = chain.nextAroundCall(request);
        // 响应后：记录AI输出
        log.info("AI输出: {}", response.response().getResult().getOutput().getText());
        return response;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest request, StreamAroundAdvisorChain chain) {
        log.info("流式请求: {}", request.userText());
        return chain.nextAroundStream(request);
    }

    @Override
    public String getName() { return "MyLoggerAdvisor"; }

    @Override
    public int getOrder() { return 0; }
}

// 使用Advisor
ChatClient chatClient = ChatClient.builder(chatModel)
        .defaultAdvisors(
            MessageChatMemoryAdvisor.builder(chatMemory).build(), // 对话记忆
            new MyLoggerAdvisor()                                  // 自定义日志
        )
        .build();
```

---

## RAG（检索增强生成）
RAG将外部知识库与AI结合，让AI基于你的文档回答问题，避免幻觉。

**RAG流程：文档加载 → 切分 → 向量化 → 存储 → 检索 → 增强回答**

```java
// 1. 配置向量存储（加载知识库文档）
@Configuration
public class VectorStoreConfig {

    @Bean
    VectorStore myVectorStore(EmbeddingModel embeddingModel,
                               MyDocumentLoader documentLoader) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        List<Document> docs = documentLoader.loadMarkdowns();
        store.add(docs); // 文档自动向量化并存储
        return store;
    }
}

// 2. 使用QuestionAnswerAdvisor启用RAG
public String chatWithRag(String message, String chatId) {
    return chatClient.prompt()
            .user(message)
            .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
            .advisors(new QuestionAnswerAdvisor(vectorStore)) // RAG检索
            .call()
            .content();
}
```

---

## 工具调用（Tool Calling）
工具调用让AI能够执行真实操作，如搜索网页、读写文件、调用API。

```java
// 定义工具
public class WeatherTool {

    @Tool(description = "根据城市名称查询当前天气")
    public String getWeather(String city) {
        // 调用天气API
        return "北京今天晴，气温25°C";
    }
}

// 注册并使用工具
@Bean
public ToolCallback[] allTools() {
    return ToolCallbacks.from(new WeatherTool());
}

// 在ChatClient中启用工具
public String chatWithTools(String message) {
    return chatClient.prompt()
            .user(message)
            .toolCallbacks(allTools) // 注入工具
            .call()
            .content();
    // AI会自动决定是否调用工具，并将结果整合到回答中
}
```

---

## 多轮对话记忆
通过 `MessageChatMemoryAdvisor` 实现多轮对话上下文保持。

```java
// 初始化对话记忆（内存存储，最多保留20条消息）
MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
        .chatMemoryRepository(new InMemoryChatMemoryRepository())
        .maxMessages(20)
        .build();

// 每次对话传入相同的chatId，即可保持上下文
public Flux<String> chat(String message, String chatId) {
    return chatClient.prompt()
            .user(message)
            .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
            .stream()
            .content();
}
```

---

## 结构化输出
让AI直接返回Java对象，而不是字符串。

```java
// 定义输出结构
record LearningReport(String topic, List<String> keyPoints, String nextStep) {}

// 调用时指定返回类型
public LearningReport generateReport(String topic) {
    return chatClient.prompt()
            .user("为" + topic + "生成学习报告，包含关键知识点和下一步建议")
            .call()
            .entity(LearningReport.class); // 自动解析为Java对象
}
```

---

## 常见问题
Q: Spring AI 支持哪些模型？
A: 支持OpenAI（GPT系列）、阿里云DashScope（Qwen系列）、Ollama（本地模型）、Azure OpenAI、Google Gemini等，通过统一API切换模型只需修改配置。

Q: RAG 检索结果不准确怎么优化？
A: 优化方向：1）改善文档切分粒度；2）使用查询重写（QueryRewriter）；3）调整相似度阈值；4）增加关键词元数据（KeywordEnricher）；5）使用混合检索（向量+关键词）。

---

## 学习建议
Spring AI是本项目的核心技术，建议结合项目源码深入理解RAG管道和Advisor机制的实现细节。
