package org.example.memory;

import dev.langchain4j.memory.*;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import redis.clients.jedis.Jedis;

public class ChatPersistenceService {

    private final ChatMemory chatMemory;

    public ChatPersistenceService() {
        Jedis jedis = new Jedis("localhost", 6379);

        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(20)
                .chatMemoryStore(new RedisChatMemoryStore(jedis))
                .build();
    }

    public ChatMemory memory() {
        return chatMemory;
    }
}
