# Uma Introdução ao Redis

> https://www.geeksforgeeks.org/system-design/introduction-to-redis-server/

---

## O que é Redis, em termos simples

Redis é um **banco chave→valor que guarda os dados na memória RAM**, não no disco.
Por isso ele é **absurdamente rápido** para ler e escrever.

---

## Para que serve (na prática)

Você vai usar Redis principalmente para:

1. **Cache** (evitar bater no banco principal toda hora)
2. **Sessões** (guardar dados temporários de usuários logados)
3. **Contadores e rate-limiting** (controle de requisições, likes etc.)
4. **Filas simples / processamento assíncrono** (Streams ou lists)
5. **Dados que precisam ser obtidos rápido** (ranking, features de chat, etc.)

Redis não é para guardar tudo.
Ele é para **coisas rápidas e voláteis**, não para dados definitivos.

---

## Por que ele é rápido

Porque:

* Ele **não lê disco** para cada operação, lê **RAM** (que é muito mais rápida)
* O servidor é **single-threaded por shard**, evitando locks complexos
* As estruturas internas são simples e otimizadas

Resultado: operações na casa de micro a milissegundos.

---

## Como se usa no Java (o essencial)

Você não interage com Redis diretamente — você usa um cliente.

Os mais comuns são:

* **Lettuce** (padrão atual, não bloqueante, funciona bem com Spring Boot)
* **Redisson** (fornece abstrações como mapas distribuídos, locks etc.)

No Spring Boot normalmente:

* Você configura o Redis no `application.yml`
* Você injeta um `RedisTemplate` ou uma lib tipo `Spring Cache`

Exemplo simples (conceito):

```java
redis.set("user:42:name", "Milena");
String nome = redis.get("user:42:name");
```

---

## Quando escolher Redis

Use Redis quando:

* Você precisa **responder rápido**
* Os dados **podem expirar** (cache) ou **são temporários**
* Você quer reduzir carga no banco principal

**Não** use Redis para:

* Dados críticos que **não podem se perder**
* Tabelas grandes que **não cabem em memória**

---

## Se fosse resumir em uma frase

Redis é um **armazenamento em memória extremamente rápido**, perfeito para **cache e dados temporários**, que você usa ao lado do seu banco principal, **não no lugar dele**.

---

Vamos deixar isso claro de forma bem direta, sem florear.

---

## Redis vs H2 — os dois são “em memória”, mas com **propósitos totalmente diferentes**

### **Redis**

* **É um banco chave→valor** (com algumas estruturas de dados).
* Pensado para **alta velocidade** e **dados temporários**.
* Operações simples: set, get, increment, listas, sets etc.
* Geralmente usado **ao lado** de um banco principal.
* Pode (ou não) persistir — **não é sua função principal**.

**Use Redis para:**

* **Cache** (respostas de API, consultas caras)
* **Sessões de usuário** (dados transitórios)
* **Rate limiting** (contagem de requisições)
* **Filas simples / mensagens rápidas**
* Contadores, rankings, estados voláteis

**Resumo:** Redis é para **dados que podem ser perdidos** sem quebrar a aplicação.
Ele existe para **performance**.

---

### **H2**

* **É um banco relacional** completo (SQL).
* Pode rodar **em memória** ou **em arquivo**.
* Modela tabelas, colunas, constraints — igual PostgreSQL/MySQL.
* Usado principalmente para **desenvolvimento e testes**.
* Gerenciado pelo Spring Boot facilmente.

**Use H2 para:**

* **Testar sua aplicação local** sem instalar um banco real
* **Substituir PostgreSQL/MySQL** durante desenvolvimento
* Rodar testes automatizados com estado limpo

**Resumo:** H2 é para **dados estruturados**, quando você quer **SQL**, **joins**, e comportamento semelhante ao seu banco real.

---

## A analogia certeira

| Ferramenta | Pensa como…                              | Para…                                               |
| ---------- | ---------------------------------------- | --------------------------------------------------- |
| **Redis**  | Um *cofre aberto na mesa*                | Pegar e guardar coisas rápido. Se perder, tudo bem. |
| **H2**     | Uma *versão de treino do seu banco real* | Testar e desenvolver como se fosse produção.        |

---

## Como isso aparece num backend Java

### Você usa Redis **junto** com PostgreSQL/MySQL/Mongo, para performance:

* Spring Boot → Banco real (persistência de verdade)
* Redis → Cache rapidão na frente

### Você usa H2 **no lugar** do PostgreSQL/MySQL **durante dev/teste**:

* Para não precisar subir docker ou instalar banco local
* Para rodar testes limpos e repetíveis

---

## Regra prática para não errar

```
Se você precisa de SQL → H2 (em dev) e PostgreSQL/MySQL (em prod).

Se você precisa de velocidade e pode perder o dado → Redis.
```

