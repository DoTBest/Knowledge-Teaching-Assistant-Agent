package com.ai.agent.app;

import com.ai.agent.advisor.MyLoggerAdvisor;
import com.ai.agent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * AI 知识教学助手应用
 * 基于 RAG 的 Java/Spring/AI 技术知识教学，支持多轮对话、知识检索、练习题生成等功能
 */
@Component
@Slf4j
public class TeachingAssistantApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = """
            你是一位专业的技术知识教学助手，专注于 Java 编程、Spring 框架和 AI 开发领域的教学辅导。

            【身份定位】
            你拥有丰富的教学经验，能够根据学习者的水平调整讲解深度，让复杂的技术概念变得易于理解。

            【核心能力】
            1. 知识点解释：根据用户指定的层次调整讲解方式
               - 入门（beginner）：使用类比和生活化例子，避免术语堆砌，给出简单代码示例
               - 进阶（intermediate）：结合代码示例，讲解原理和最佳实践
               - 专家（advanced）：深入源码、设计模式、性能优化和架构决策
            2. 练习题生成：根据知识点生成配套练习，包含题目、提示和参考答案
            3. 学习路径推荐：根据当前水平和目标，制定个性化学习计划
            4. 知识点总结：将复杂知识体系梳理为结构化的总结

            【回答规范】
            - 代码示例必须完整可运行，附带注释说明
            - 使用 Markdown 格式，合理使用标题、代码块、列表
            - 每次回答后，主动提示相关联的知识点或下一步学习建议
            - 如果问题超出知识库范围，诚实说明并给出学习资源建议

            【知识库使用】
            优先从知识库中检索相关内容作为回答依据，确保知识的准确性和系统性。
            """;

    public TeachingAssistantApp(ChatModel dashscopeChatModel) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    // ==================== RAG 知识库 ====================

    @Resource
    private VectorStore teachingVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 基础对话（支持多轮对话记忆）
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("doChat content: {}", content);
        return content;
    }

    /**
     * 流式对话（RAG + 多轮记忆，SSE 流式传输）
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        return chatClient.prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .advisors(new QuestionAnswerAdvisor(teachingVectorStore))
                .stream()
                .content();
    }

    /**
     * 知识点解释（按难度层次）
     *
     * @param concept 要解释的概念
     * @param level   难度层次：beginner / intermediate / advanced
     * @param chatId  会话ID
     */
    public Flux<String> doExplain(String concept, String level, String chatId) {
        String levelDesc = switch (level) {
            case "beginner" -> "入门级别，使用生活化类比，避免术语堆砌，给出最简单的代码示例";
            case "advanced" -> "专家级别，深入源码实现，讨论设计决策、性能考量和最佳实践";
            default -> "进阶级别，讲解核心原理，给出完整代码示例和常见使用场景";
        };
        String prompt = String.format(
                "请以【%s】解释以下技术概念：**%s**\n\n要求：%s",
                levelDesc, concept, levelDesc
        );
        return chatClient.prompt()
                .user(prompt)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .advisors(new QuestionAnswerAdvisor(teachingVectorStore))
                .stream()
                .content();
    }

    /**
     * 生成练习题
     *
     * @param topic      主题（java / spring / ai）
     * @param difficulty 难度（easy / medium / hard）
     * @param chatId     会话ID
     */
    public Flux<String> doGenerateExercise(String topic, String difficulty, String chatId) {
        String prompt = String.format(
                "针对【%s】主题，生成3道【%s】难度的编程练习题。\n\n" +
                "每道题的格式：\n" +
                "**题目N**：题目描述\n" +
                "**提示**：解题思路提示\n" +
                "**参考答案**：完整可运行的代码\n\n" +
                "题目要有梯度，由易到难。",
                topic, difficulty
        );
        return chatClient.prompt()
                .user(prompt)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    /**
     * 学习路径推荐
     *
     * @param topic        学习主题
     * @param currentLevel 当前水平（beginner / intermediate / advanced）
     * @param chatId       会话ID
     */
    public Flux<String> doRecommendLearningPath(String topic, String currentLevel, String chatId) {
        String prompt = String.format(
                "我目前是【%s】水平，想系统学习【%s】。\n\n" +
                "请给出个性化学习路径，包含：\n" +
                "1. 当前阶段重点和薄弱点分析\n" +
                "2. 推荐学习顺序（分阶段列出）\n" +
                "3. 每个阶段的里程碑和验收标准\n" +
                "4. 推荐学习资源（书籍、文档、项目实战）",
                currentLevel, topic
        );
        return chatClient.prompt()
                .user(prompt)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .advisors(new QuestionAnswerAdvisor(teachingVectorStore))
                .stream()
                .content();
    }

    /**
     * 知识点总结
     *
     * @param topic  要总结的主题
     * @param chatId 会话ID
     */
    public Flux<String> doSummarize(String topic, String chatId) {
        String prompt = String.format(
                "请对【%s】进行系统性总结，包含：\n\n" +
                "1. **核心概念清单**：列出最重要的知识点\n" +
                "2. **知识体系结构**：各概念之间的关联关系\n" +
                "3. **常见误区**：初学者容易犯的错误\n" +
                "4. **最佳实践**：实际开发中的推荐做法\n" +
                "5. **与其他技术的关联**：上下游技术栈",
                topic
        );
        return chatClient.prompt()
                .user(prompt)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .advisors(new QuestionAnswerAdvisor(teachingVectorStore))
                .stream()
                .content();
    }

    // 结构化输出：学习报告
    record LearningReport(String topic, String level, List<String> keyPoints, String nextStep) {}

    /**
     * 生成结构化学习报告
     */
    public LearningReport generateLearningReport(String topic, String chatId) {
        LearningReport report = chatClient.prompt()
                .system(SYSTEM_PROMPT + "\n每次对话后生成学习报告，包含主题、当前水平、关键知识点列表和下一步建议。")
                .user("请为【" + topic + "】生成一份学习报告")
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId))
                .call()
                .entity(LearningReport.class);
        log.info("learningReport: {}", report);
        return report;
    }
}
