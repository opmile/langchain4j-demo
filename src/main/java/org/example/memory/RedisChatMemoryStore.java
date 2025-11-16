package org.example.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import redis.clients.jedis.Jedis;

import java.util.List;

public class RedisChatMemoryStore implements ChatMemoryStore {

    private final Jedis jedis;
    private static final String PREFIX = "chat-memory: ";

    public RedisChatMemoryStore(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String json = jedis.get(PREFIX + memoryId);
        if (json == null) return List.of();
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = ChatMessageSerializer.messagesToJson(messages);
        jedis.set(PREFIX + memoryId, json);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        jedis.del(PREFIX + memoryId);
    }
}
