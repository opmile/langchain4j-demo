# **LangChain4j + Redis: Persistência de Memória Conversacional com LLMs Locais (Ollama)**

Este repositório documenta meus estudos sobre como implementar memória conversacional persistente em aplicações Java utilizando **LangChain4j**, **Redis** e modelos locais servidos via **Ollama**.
O objetivo foi entender como o framework organiza mensagens, como a memória é estruturada internamente e como criar uma implementação realista de `ChatMemoryStore` usando Redis.

---

## **1. Motivação e Contexto Geral**

Meu ponto de partida foi simples: integrar o **Ollama**, rodando localmente como daemon HTTP, com LangChain4j. O Ollama expõe modelos como o Llama 3 em `http://localhost:11434`, o que torna o consumo do modelo trivial via HTTP.

Extrair respostas simples é fácil. O desafio real é **construir conversas com continuidade**, mantendo coerência e estado entre chamadas. A partir daí, investiguei profundamente como o LangChain4j lida com memória, persistência e contexto.

---

## **2. Componentes Low-Level do LangChain4j**

Para entender a estrutura interna, analisei os blocos fundamentais:

* **`ChatModel`** → interface central para comunicação com a LLM.
* **`ChatMessage`** → abstração de mensagens (`UserMessage`, `AiMessage`, etc.).
* **`ChatResponse`** → encapsula a resposta do modelo.
* **`ChatResponseMetadata`** → inclui token usage, dados do provider, tempo de execução.
* **Método-chave: `chat()`** → recebe lista de `ChatMessage` e retorna `ChatResponse`.

Implementar o fluxo manualmente rapidamente gera **boilerplate** significativo. A cada interação a lista de mensagens cresce, e você passa a ter que gerenciar janelas, contagem de tokens e acúmulo de histórico, o que é pouquíssimo eficiente e produtivo.

---

## **3. O Papel Crítico do ChatMemory**

É nesse momento que entra o conceito central: **ChatMemory**.

Um erro comum é confundir memória com histórico. O histórico apenas registra todas as mensagens; a memória é **um mecanismo inteligente de redução, condensação e filtragem**.
Isso é feito considerando eviction policy, dessa maneira, a depender do algoritmo implementado, a memória decide **o que permanece** na janela de contexto e o que é despejado, já que toda LLM tem limite de tokens.

O LangChain4j oferece duas implementações relevantes:

* **`MessageWindowChatMemory`**: mantém apenas as N últimas mensagens.
* **`TokenWindowChatMemory`**: mantém mensagens até um limite total de tokens, sendo considerada uma alternativa mais sofiticada.

Ambas usam estratégias de **sliding window** para garantir coerência sem estourar o limite do modelo.

---

## **4. Persistência com ChatMemoryStore**

Para que a memória sobreviva a reinicializações, distribuições ou múltiplas instâncias, existe a interface:

### **`ChatMemoryStore`**

O framework traz uma única implementação:

* **`InMemoryChatMemoryStore`** → volátil, só para estudos.

Em produção, isso é inutilizável: restartou, perdeu memória. Isso inviabiliza:

* múltiplos usuários,
* sessões persistentes,
* escalonamento horizontal,
* deploys sem perda de contexto.

Por isso implementei meu próprio store usando Redis, o que não foi algo extraordinário. A documentação do próprio framework já te mostra o caminho das pedras para qualquer implementação que você deseja.

---

## **5. Por que Redis é o Memory Store Ideal**

Apesar de ser um key-value database orientado a cache, Redis se encaixa perfeitamente como camada de memória conversacional:

* muito rápido (mantêm o conjunto de trabalho em DRAM rápida, em detrimento de acesso ao disco)
* baixa latência
* suporta milhares de operações paralelas
* sobrevivência a restart
* perfeito para armazenar pequenos payloads JSON
* fácil containerização

No contexto de aplicações com **RAG**, o Redis ganha ainda mais força:

* banco vetorial → guarda conhecimento
* modelo (Ollama) → gera raciocínio
* Redis → sustenta o estado da conversa (cache)

Ele pode inclusive armazenar resultados de consultas vetoriais mais frequentes, reduzindo latência.

---

## **6. Implementando `RedisChatMemoryStore`**

Como o LangChain4j não possui implementação nativa, escrevi a minha:

* **`getMessages()`**
* **`updateMessages()`**
* **`deleteMessages()`**

A serialização/deserialização é feita pelos utilitários:

* `ChatMessageSerializer`
* `ChatMessageDeserializer`

Como Redis é key-value, a implementação é essencialmente trabalhar com Strings, funcionando como um map, o que já traz certa familiriaridade no uso.

---

## **7. Arquitetura da Solução**

A estrutura final ficou assim:

* **`RedisChatMemoryStore`** → persistência distribuída, implementa a interface `ChatMemoryStore`
* **`ChatPersistenceService`** → gerencia conexão com Redis via Jedis, encapsula `MessageWindowChatMemory` com o controle da janela de contexto
* **`Assistant` (AI Service)** → camada high-level, substituindo o antigo `ConversationalChain`

Obs: O padrão dos AI Services lembra fortemente **Spring Data JPA**: você declarativamente define uma interface e o framework gera um proxy com uma implementação dinâmica carregando `ChatModel` e `ChatMemory`.

---

## **8. Recomendações Práticas**

### **Dependências**

Garanta no `pom.xml`:

* módulo principal do LangChain4j (`langchain4j`)
* módulo da integration API do provedor ((`langchain4j-{model-integration}`)
* driver Redis (Jedis ou Lettuce) (`jedis`)

### **Jedis vs Lettuce**

* **Jedis** → simples, direto, ótimo para testes.
* **Lettuce** → mais robustez, melhor para produção, suporta sync + async, threadsafe.

### **Gerenciamento de conexões**

Como `Jedis` implementa `Closeable`, é necessário:

* usar `JedisPool`, que garante o gerenciamento de conexões sem dor de cabeça de fazer isso manualmente
* fazer seu serviço implementar `Closeable` e a partir daí escrever um método de shutdown que força `jedis.close()`.

Manter a conexão aberta no construtor não é ideal. Isso foi usado apenas por fins de estudo, considerando uma aplicação muito simples.

### **Refinamentos possíveis**

* métodos do store poderiam retornar `Optional` para evitar listas vazias e acessos nulos
* log estruturado para acompanhar uso de tokens
* políticas de truncamento específicas por modelo (Llama 3, Qwen, Mistral etc.)

---

## **9. Considerações sobre Docker e Infraestrutura**

A decisão entre containerizar ou rodar um serviço no host tem que ser pragmática. A regra simples é: tudo que faz parte da arquitetura principal da aplicação tende a ficar melhor dentro de contêineres — sua aplicação Java, o Redis, o banco vetorial e quaisquer serviços auxiliares. Isso garante isolamento, reprodutibilidade e facilita subir o mesmo setup em qualquer máquina ou servidor.

Por outro lado, o Ollama é o típico serviço que não vale a dor de cabeça de empacotar em container quando você está estudando ou desenvolvendo localmente. Ele conversa direto com GPU, driver, aceleração nativa. Colocar isso em container gera overhead desnecessário.

Claro, faz total sentido containerizar em ambientes maiores tipo clusters Kubernetes com suporte a GPU, ou em times que exigem padronização total do ambiente.

Para estudos, honestamente, rodar tudo no host é mais rápido e menos burocrático. Containerizar começa a fazer diferença quando você quer replicabilidade real ou subir sua solução em produção.

---

## **10. Referências**

* [Doc LangChain4j](https://docs.langchain4j.dev/tutorials/chat-memory)
* [Doc Jedis](https://redis.io/docs/latest/develop/clients/jedis/)
* [Exemple persistent memory](https://github.com/langchain4j/langchain4j-examples/blob/main/other-examples/src/main/java/ServiceWithPersistentMemoryExample.java)
* [Example non-persistent memory](https://github.com/langchain4j/langchain4j-examples/blob/main/other-examples/src/main/java/ServiceWithMemoryExample.java)

