# Low e High Level

Pensa no LangChain4j como um jogo com duas formas de jogar: **modo LEGO livre** e **modo Playmobil já montado**.

A frase que você trouxe está dizendo justamente isso.

---

### 1. **Nível baixo (Low-level): modo LEGO livre**

Aqui você mexe diretamente com os **blocos mais fundamentais** da ferramenta.

Coisas como:

* `ChatModel` → a instância do modelo de linguagem (OpenAI, Local, etc.)
* `UserMessage` / `AiMessage` → representações explícitas das mensagens enviadas e recebidas
* `EmbeddingStore` → onde você grava ou busca embeddings
* `Embedding` → o vetor numérico que representa um texto
* Entre outras partes modulares

Nesse nível:

* Você decide **como** juntar tudo.
* Você escreve o fluxo: recebe input → busca contexto → chama modelo → interpreta resposta → etc.
* Total liberdade, mas também **mais código manual**.

É como montar um carro começando pelas peças: você tem como fazer qualquer carro, mas também pode montar tudo torto se não souber o que está fazendo ainda.

Quando você vai trabalhar com **RAG do zero** (vector store, chunking, recuperação customizada, prompt custom, etc.), esse nível faz sentido porque você controla cada passo.

---

### 2. **Nível alto (High-level): modo Playmobil já montado**

Aqui o LangChain4j oferece **APIs de alto nível** chamadas *AI Services*:

```java
@AiService
interface SalesAssistant {
    String recommendProduct(String userQuestion);
}
```

Essa interface é suficiente. O framework:

* Cria o prompt
* Chama o modelo
* Recebe a resposta
* Formata o output

Você só usa o assistente como uma “função” qualquer.

A complexidade fica *escondida*, mas não perdida:

* Se quiser, você pode customizar prompts, contexto, chamadas, parâmetros
* Só que faz isso declarativamente, **sem escrever cola manual**.

É ótimo para protótipos, chatbots, microserviços, endpoints estáveis, «coisas que não exigem arquitetura de IA full manual».

---

### Em outras palavras

| Nível          | Nome “real”                                    | Vantagem             | Desvantagem                                    | Quando usar                                                                       |
| -------------- | ---------------------------------------------- | -------------------- | ---------------------------------------------- | --------------------------------------------------------------------------------- |
| **Low-level**  | Trabalhar com modelos e embeddings manualmente | Total controle       | Mais código e responsabilidade                 | Quando você quer construir algo custom, tipo um RAG com lógica própria ou agentes |
| **High-level** | AI Services                                    | Fácil, limpo, direto | Menos flexibilidade fina (mas ainda ajustável) | Quando você quer produtividade rápido e um fluxo previsível                       |

---


