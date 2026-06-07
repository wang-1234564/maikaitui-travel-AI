package com.maikaitui.ai.config;

import com.maikaitui.ai.tool.AdminToolService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * 管理员 AI 配置 — 创建独立的 adminChatClient Bean
 *
 * <p>与面向游客的 chatClient（CommonConfiguration）完全分离：
 * <ul>
 *   <li>独立的系统提示词</li>
 *   <li>通过 {@code defaultTools(adminToolService)} 自动扫描所有 {@code @Tool} 方法</li>
 * </ul>
 *
 * <p><b>扩展新工具：在 {@link AdminToolService} 中添加一个 @Tool 方法即可，无需修改此类。</b>
 */
@Configuration
public class AdminAiConfiguration {

    @Bean
    ChatClient adminChatClient(OpenAiChatModel openAiChatModel,
                               AdminToolService adminToolService) {
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(new ClassPathResource("prompts/admin-system-prompt.txt"))
                .defaultTools(adminToolService)
                .build();
    }
}
