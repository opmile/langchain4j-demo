package org.example.memory;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.service.AiServices;

public class MainChatMemory {
    public static void main(String[] args) {

        ChatPersistenceService chatService = new ChatPersistenceService();

        ChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .build();

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatService.memory())
                .build();

        String answer = assistant.chat("Hello! Once I've told you my name, can you recall it for me?.");
        System.out.println(answer);
    }
}
