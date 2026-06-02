# RAG技术原理与实践

## 适用人群
已了解LLM基础，希望深入理解RAG（检索增强生成）技术的开发者。

## 什么是RAG
RAG（Retrieval-Augmented Generation，检索增强生成）是一种将外部知识库与LLM结合的技术。

**核心思想：** 不把所有知识塞进Prompt，而是在需要时从知识库中检索相关内容，动态注入到Prompt中。

**解决的问题：**
- LLM知识截止日期问题（无法获取最新信息）
- 幻觉问题（基于真实文档回答，减少编造）
- 上下文窗口限制（按需检索，不超出Token限制）
- 私有知识问题（企业内部文档、专业领域知识）

---

## RAG完整流程

**离线阶段（知识库构建）：**
1. 文档加载（PDF、Markdown、网页等）
2. 文本切分（按段落、Token数量切分）
3. 向量化（Embedding模型将文本转为向量）
4. 存储（向量数据库：Pinecone、PgVector、Milvus等）

**在线阶段（查询检索）：**
1. 查询向量化（将用户问题转为向量）
2. 相似度检索（在向量数据库中找最相似的文档片段）
3. 上下文构建（将检索结果拼接到Prompt）
4. LLM生成（基于检索内容生成回答）

---

## 向量化与相似度检索
向量化是RAG的核心，将文本转为高维向量，语义相似的文本在向量空间中距离更近。

```java
// Spring AI 中的向量化和检索
@Configuration
public class VectorStoreConfig {

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 创建内存向量存储
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        // 加载并向量化文档
        List<Document> docs = List.of(
            new Document("Spring IoC是控制反转，由容器管理对象创建"),
            new Document("Spring AOP是面向切面编程，处理横切关注点")
        );
        store.add(docs); // 自动调用EmbeddingModel向量化
        return store;
    }
}

// 检索相似文档
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.builder()
        .query("什么是Spring IoC")
        .topK(3)                    // 返回最相似的3个文档
        .similarityThreshold(0.5)   // 相似度阈值
        .build()
);
```

**相似度算法：** 余弦相似度（Cosine Similarity）是最常用的，计算两个向量夹角的余弦值，范围[-1, 1]，值越大越相似。

---

## 文档切分策略
切分粒度直接影响检索质量，太大检索不精准，太小丢失上下文。

```java
// 方式1：按Token数量切分（推荐）
TokenTextSplitter splitter = new TokenTextSplitter(
    300,  // 每块最大Token数
    50,   // 块间重叠Token数（保留上下文连续性）
    5,    // 最小块大小
    10000,// 最大块大小
    true  // 保留分隔符
);
List<Document> chunks = splitter.apply(documents);

// 方式2：按Markdown标题/水平线切分（结构化文档）
MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
    .withHorizontalRuleCreateDocument(true) // --- 分隔符创建新文档
    .withIncludeCodeBlock(true)
    .build();
```

---

## 查询优化技术

**查询重写（Query Rewriting）：** 用LLM将用户的口语化问题改写为更适合检索的标准化查询。

```java
@Component
public class QueryRewriter {
    private final ChatClient chatClient;

    public String rewrite(String originalQuery) {
        return chatClient.prompt()
            .system("将用户问题改写为更适合文档检索的标准化查询，保留核心语义，去除口语化表达")
            .user(originalQuery)
            .call()
            .content();
    }
}
// "Spring IoC是啥" → "Spring IoC控制反转概念和工作原理"
```

**关键词增强（Keyword Enrichment）：** 为文档片段自动提取关键词作为元数据，提升检索召回率。

**混合检索（Hybrid Search）：** 结合向量检索（语义相似）和关键词检索（精确匹配），取两者结果的并集或加权融合。

---

## 元数据过滤
通过元数据缩小检索范围，提升精准度。

```java
// 为文档添加元数据
Document doc = new Document(
    "Spring IoC容器管理Bean的生命周期...",
    Map.of(
        "topic", "spring",
        "level", "intermediate",
        "source", "spring-core.md"
    )
);

// 检索时按元数据过滤
SearchRequest request = SearchRequest.builder()
    .query("IoC原理")
    .topK(5)
    .filterExpression("topic == 'spring' && level == 'intermediate'")
    .build();
```

---

## 常见问题
Q: RAG检索结果不相关怎么办？
A: 排查顺序：1）检查文档切分是否合理；2）尝试查询重写；3）降低相似度阈值；4）增加topK数量；5）检查Embedding模型是否支持中文。

Q: RAG和Fine-tuning（微调）如何选择？
A: RAG适合知识频繁更新、需要引用来源的场景，成本低；Fine-tuning适合需要改变模型行为风格、特定领域术语理解的场景，成本高。大多数企业应用优先选RAG。

---

## 学习建议
RAG是AI应用开发的核心技术，建议结合本项目的Spring AI实现深入理解，并尝试调整各参数观察效果变化。
