# Modelo Mental LangChain4j

Pensa no LangChain4j como um **conjunto de peças** para construir aplicações em que um modelo de linguagem não é só chamado uma vez, mas **faz parte do fluxo do sistema**. Ele organiza a bagunça.

A biblioteca não “faz IA”.
Ela **orquestra** como o modelo conversa, lembra, usa dados externos e chama funções do seu código.

O modelo mental é este:

---

### 1) **Modelo (LLM)**

É o cérebro.
Ele só sabe **gerar texto** a partir de texto.

Ex:

* GPT da OpenAI
* Mistral/LLama
* Modelo local (Ollama)

Sem mais nada, ele só responde perguntas.
E esquece tudo logo em seguida.

---

### 2) **Prompt / Instrução**

É como você enquadra o modelo.

Sem prompt: ele responde qualquer coisa.
Com prompt: ele assume papéis, estilos, objetivos.

LangChain4j permite:

* **Prompt fixo** (estático)
* **Prompt com variáveis** (template)
* **Prompt encapsulado em uma interface** (`@AiService`)

A sacada:
Você transforma o modelo em **funções** do seu sistema, do tipo:

```java
String resumir(String texto);
```

Assim ele vira parte do código, não um brinquedo solto.

---

### 3) **Memória**

O modelo é amnésico.
Se você quer que ele “lembre” do que foi dito antes, alguém precisa guardar esse contexto.

LangChain4j oferece:

* `InMemoryChatMemory` → simples
* `RedisChatMemory` → persistente / produção

Memória dá **continuidade** à conversa.
Sem ela, cada mensagem é isolada.

---

### 4) **Embeddings**

Aqui é onde você dá **conhecimento real** para o modelo.

Você pega um texto (PDF, docs, artigos, notas suas)
→ transforma em vetores (números que representam significado)
→ guarda isso num banco vetorial.

Esse vetor é como uma “impressão semântica”.

O modelo não lê o PDF inteiro toda vez, ele busca **trechos relevantes** pelo sentido.

---

### 5) **Vector Store (Banco Vetorial)**

É onde você guarda esses vetores.
Pode ser:

* Local (Map, HNSW, SQLite)
* Milvus
* Weaviate
* Pinecone
* Qdrant

Ele serve pra responder coisas como:

> “Me acha o trecho mais parecido com essa pergunta.”

Isso é **RAG** (Retrieval Augmented Generation):

1. Usuário pergunta
2. A pergunta vira embedding
3. Banco busca trechos parecidos
4. LangChain4j manda trechos → modelo
5. Modelo responde baseado **no seu conteúdo**, não só no que ele já sabe

Isso é o que diferencia **brincar com IA** de **construir um sistema sério**.

---

### 6) **Ferramentas (Tools)**

Aqui a coisa fica interessante.

Você pode dar ao modelo **funções Java reais** para ele chamar quando quiser.

Ex:

* Fazer consulta no banco
* Calcular algo
* Chamar outra API
* Atualizar dados

Você anota métodos com `@Tool`.

O modelo passa a poder **agir**, não só responder.

---

### 7) **Agente**

Um agente é quando você deixa o modelo **decidir sozinho** quando usar ferramentas.

Ele olha o objetivo → pensa → escolhe o que fazer → executa → responde.

É literalmente:

> “Resolva o problema, usando meus recursos quando achar necessário.”

Esse é o topo da cadeia.

Sem `@Tool`, o agente só fala.
Com `@Tool`, ele age.

Ferramentas são o que permitem o agente:

* consultar dados,

* modificar estado,

* interagir com o sistema.

---

### Modelo Mental Resumido

```
LLM → responde
+ Prompt → responde direito
+ Memória → conversa com continuidade
+ Embeddings + Vector Store → responde baseado em dados reais
+ Tools → pode agir
+ Agent → sabe quando agir
```

Ou seja:
Você começa com um **papagaio eloquente**
e termina com **um assistente operacional** que navega seu sistema.

---

## As Peças da API

Entendi — você quer *as peças concretas da API*, não só a ideia.
Vamos montar isso como um quadro de “peça → para quê serve → onde aparece no código”.

Fica simples quando dividimos em **6 blocos**.

---

## **BLOCO 1 — Modelo**

### **ChatModel**

É o **objeto Java** que representa o LLM em si.

Exemplos concretos:

```java
ChatModel model = OpenAiChatModel.builder()
        .apiKey("...")
        .modelName("gpt-4o")
        .build();
```

Esse `model` recebe mensagens e responde mensagens.

---

## **BLOCO 2 — Mensagens e Geração**

### **UserMessage / AiMessage / SystemMessage**

São *tipos de mensagens* que o modelo entende.

Exemplo:

```java
UserMessage msg = UserMessage.from("Explique recursão.");
AiMessage resposta = model.generate(msg);
System.out.println(resposta.text());
```

Relação:

* Você **sempre conversa** com o modelo via mensagens.
* O modelo retorna uma `AiMessage`.

---

## **BLOCO 3 — Services e Prompting**

### **@AiService**

Transforma o modelo em **uma interface Java com métodos normais**, o que é bem mais elegante.

```java
@AiService
interface Explicador {
    String explicar(String conceito);
}

Explicador explicador = AiServices.create(Explicador.class, model);

System.out.println(explicador.explicar("herança em OOP"));
```

Esse salto é importante:

* Você já não manipula diretamente `UserMessage`.
* Você começa a pensar em **funções inteligentes**.

---

## **BLOCO 4 — Memória**

### **ChatMemory**

Guarda histórico para conversas contínuas.

```java
ChatMemory memory = InMemoryChatMemory.builder().build();

ChatModel modelComMemoria = OpenAiChatModel.builder()
        .apiKey("...")
        .chatMemory(memory)
        .build();
```

**Sem memória:** cada chamada é isolada.
**Com memória:** contexto é mantido automaticamente no `@AiService`.

---

## **BLOCO 5 — RAG**

Aqui entram **embeddings** e **vector store**.

### **EmbeddingModel**

Converte texto → vetor numérico.

```java
EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
        .apiKey("...")
        .build();
```

### **EmbeddingStore (Vector Store)**

Guarda esses vetores. Pode ser local ou externo.

```java
EmbeddingStore store = new InMemoryEmbeddingStore();
```

### **Retriever**

Faz busca semântica na base.

```java
ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
        .embeddingModel(embeddingModel)
        .embeddingStore(store)
        .build();
```

### **RAGChain**

Une tudo: pergunta → busca → passa contexto → modelo responde.

```java
AiMessage resposta = RAGChain.builder()
        .chatModel(model)
        .contentRetriever(retriever)
        .build()
        .generate("Explique baseado nos documentos.");
```

**Relação:**

```
Texto → EmbeddingModel → EmbeddingStore → Retriever → RAGChain → ChatModel
```

---

## **BLOCO 6 — Ferramentas e Agentes**

### **@Tool**

Dá ao modelo **funções Java reais** para usar.

```java
class Utilidades {
    @Tool
    public int somar(int a, int b) { return a + b; }
}
```

### **Agent**

Coordena:

* objetivo
* modelo
* ferramentas
* memória
* RAG (opcional)

```java
Agent agent = Agent.builder()
        .chatModel(model)
        .tools(new Utilidades())
        .build();

System.out.println(agent.chat("Some 7 e 13"));
```

**Diferencial:**
O LLM decide **quando** chamar `somar()` sem você mandar.

---

## **VISÃO PROGRESSIVA**

Do mais simples ao mais poderoso:

| Etapa | Peça Central                                      | Código Focado Em                | O que Você Ganha                      |
| ----- | ------------------------------------------------- | ------------------------------- | ------------------------------------- |
| 1     | `ChatModel` + `UserMessage`                       | `model.generate()`              | Respostas diretas                     |
| 2     | `@AiService`                                      | Interfaces como funções         | Fluxo natural no código               |
| 3     | `ChatMemory`                                      | Conversas com contexto          | Persistência de diálogo               |
| 4     | `EmbeddingModel` + `EmbeddingStore` + `Retriever` | RAGChain                        | Responder baseado em documentos reais |
| 5     | `@Tool` + `Agent`                                 | Chamadas automáticas de funções | Assistente que **age**, não só fala   |

---

```
ChatModel  → responde
AiService  → responde como função Java
ChatMemory → lembra
EmbeddingModel + Store + Retriever → traz informações certas
Tool → permite agir
Agent → decide quando agir
```

Esse é o **modelo mental concreto + as peças da API**.

---

