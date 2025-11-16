# Redis como Memory Store

Quando:

> “Minha memória de conversa vai ser persistida no Redis.”

Logo:

* Você **precisa ter um Redis rodando**
* Ou local
* Ou container (Docker)
* Ou gerenciado (Redis Cloud, AWS ElastiCache, etc.)

Não é plug-and-play sem backend.

---

## Por que Redis para memória?

Memória de conversa tem este perfil:

* Pequena
* Precisa ser lida e escrita rápido
* Deve sobreviver a múltiplas requisições paralelas (ex: API REST sendo escalada)
* Às vezes precisa sobreviver ao restart da aplicação

Redis é perfeito pra isso: chave-valor rápido, barato e simples.

---

## Então você *precisa* usar Redis?

**Não.**
Você tem duas opções principais:

### 1. **Prototipação / Desenvolvimento Local**

Use **InMemoryChatMemory**

```java
ChatMemory memory = new InMemoryChatMemory();
```

Ele guarda o histórico só **enquanto a aplicação está rodando**.
Se a API reiniciar → memória some.
Mas é ótimo para testar.

### 2. **Produção / Escalonamento**

Aí sim faz sentido **RedisChatMemory**

```java
ChatMemory memory = RedisChatMemory.builder()
        .redisHost("localhost")
        .redisPort(6379)
        .build();
```

Você instala/usa Redis via Docker:

```bash
docker run -p 6379:6379 redis
```

Ou usa renovável em cloud.

---

## E seu **banco vetorial** nessa história?

Totalmente **separado**.

Você pode ter:

* PostgreSQL + pgvector para embeddings (conhecimento)
* Redis para memória da conversa

Stack típico e saudável:

```
Spring Boot
   ↓
LangChain4j
   ↓
 + ChatMemory (Redis)
 + EmbeddingStore (pgvector)
   ↓
PostgreSQL (dados e vetores)
Redis (estado da conversa)
```

Nada confunde.
Cada um faz o que é bom.

---

## Pergunta chave para você agora:

Você quer que sua API **mantenha o contexto entre requisições**?

* Se **não precisa** → use **InMemoryChatMemory** e seja feliz.
* Se **precisa**, e principalmente:

    * API recebendo requests stateless (REST real)
    * Possível deploy futuro em múltiplas instâncias
    * Usuário logando depois de horas e ainda “lembrando”

→ **Aí você instala Redis.**

---

## Resumo direto

| Cenário                                                           | Use                                                     | Precisa instalar algo?                |
| ----------------------------------------------------------------- | ------------------------------------------------------- | ------------------------------------- |
| Desenvolvimento local                                             | `InMemoryChatMemory`                                    | Não                                   |
| Produção simples sem estado persistente                           | `InMemoryChatMemory` ++ “aceitar que não lembra depois” | Não                                   |
| Produção com continuidade real entre requisições e escalabilidade | `RedisChatMemory`                                       | **Sim, você instala/configura Redis** |

---
