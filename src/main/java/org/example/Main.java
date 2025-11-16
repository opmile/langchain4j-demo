package org.example;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.ChatResponseMetadata;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.TokenUsage;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {

        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("llama3")
                .timeout(Duration.ofSeconds(60))
                .temperature(0.0)
                .build();

        ChatResponse response = model.chat(UserMessage.from("Qual a cidade mais populosa da China?"));

        System.out.println(response.aiMessage().text());
        TokenUsage tokensAsk1 = response.metadata().tokenUsage();

        UserMessage message = UserMessage.from(
                TextContent.from("Faça uma descrição pontual do seguinte gráfico:"),
                ImageContent.from("https://relatoriogestao.mpu.mp.br/imagens/tabela-exemplo.jpg/")
        );
        ChatResponse graphResponse = model.chat(message);

        System.out.println(graphResponse.aiMessage().text());
        TokenUsage tokensAsk2 = graphResponse.metadata().tokenUsage();

        System.out.println("Consumo de tokens de cada resposta:");
        System.out.println("1 = " + tokensAsk1);
        System.out.println("2 = " + tokensAsk2);




    }
}