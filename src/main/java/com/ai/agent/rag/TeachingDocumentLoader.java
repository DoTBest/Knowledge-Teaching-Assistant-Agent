package com.ai.agent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 教学助手文档加载器
 * 扫描 classpath:document/teaching/** 下的所有 Markdown 文档
 * 从文件名中提取 topic（java/spring/ai）和 level（beginner/intermediate/advanced）元数据
 */
@Component
@Slf4j
public class TeachingDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public TeachingDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载教学知识库中的所有 Markdown 文档
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/teaching/**/*.md");
            log.info("发现教学文档数量: {}", resources.length);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) continue;

                // 从文件名解析 topic 和 level
                // 文件名格式：{topic}-{subtopic}-{level}.md，如 java-basics-beginner.md
                String nameWithoutExt = filename.replace(".md", "");
                String[] parts = nameWithoutExt.split("-");
                String topic = parts.length > 0 ? parts[0] : "general";
                String level = parts.length > 1 ? parts[parts.length - 1] : "intermediate";

                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(true)
                        .withIncludeBlockquote(true)
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("topic", topic)
                        .withAdditionalMetadata("level", level)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                List<Document> docs = reader.get();
                allDocuments.addAll(docs);
                log.info("加载文档: {} (topic={}, level={}, 片段数={})", filename, topic, level, docs.size());
            }
        } catch (IOException e) {
            log.error("教学文档加载失败", e);
        }
        log.info("教学知识库共加载文档片段: {}", allDocuments.size());
        return allDocuments;
    }
}
