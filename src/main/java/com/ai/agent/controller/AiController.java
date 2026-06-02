package com.ai.agent.controller;

import com.ai.agent.agent.AiManus;
import com.ai.agent.app.TeachingAssistantApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private TeachingAssistantApp teachingAssistantApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 同步调用教学助手
     */
    @GetMapping("/teaching/chat/sync")
    public String doChatSync(String message, String chatId) {
        return teachingAssistantApp.doChat(message, chatId);
    }

    /**
     * SSE 流式调用教学助手（RAG + 多轮记忆）
     */
    @GetMapping(value = "/teaching/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatSSE(String message, String chatId) {
        return teachingAssistantApp.doChatByStream(message, chatId);
    }

    /**
     * 知识点解释（按难度层次）
     *
     * @param concept 要解释的概念
     * @param level   难度：beginner / intermediate / advanced
     * @param chatId  会话ID
     */
    @GetMapping(value = "/teaching/explain", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doExplain(String concept, String level, String chatId) {
        return teachingAssistantApp.doExplain(concept, level, chatId);
    }

    /**
     * 生成练习题
     *
     * @param topic      主题（java / spring / ai）
     * @param difficulty 难度（easy / medium / hard）
     * @param chatId     会话ID
     */
    @GetMapping(value = "/teaching/exercise", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doGenerateExercise(String topic, String difficulty, String chatId) {
        return teachingAssistantApp.doGenerateExercise(topic, difficulty, chatId);
    }

    /**
     * 学习路径推荐
     *
     * @param topic        学习主题
     * @param currentLevel 当前水平（beginner / intermediate / advanced）
     * @param chatId       会话ID
     */
    @GetMapping(value = "/teaching/learning-path", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doRecommendLearningPath(String topic, String currentLevel, String chatId) {
        return teachingAssistantApp.doRecommendLearningPath(topic, currentLevel, chatId);
    }

    /**
     * 知识点总结
     *
     * @param topic  要总结的主题
     * @param chatId 会话ID
     */
    @GetMapping(value = "/teaching/summary", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doSummarize(String topic, String chatId) {
        return teachingAssistantApp.doSummarize(topic, chatId);
    }

    /**
     * 流式调用 Manus 超级智能体
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        AiManus aiManus = new AiManus(allTools, dashscopeChatModel);
        return aiManus.runStream(message);
    }
}
