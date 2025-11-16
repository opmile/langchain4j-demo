# Ollama

Ollama foi projetado para funcionar como um serviço em segundo plano (daemon) que gerencia modelos de linguagem grandes (LLMs).

* Isso permite que os usuários interajam com os modelos de forma transparente, sem a necessidade de gerenciar manualmente o carregamento e a execução dos modelos.

**Como funciona:**

* **Serviço em Segundo Plano (Daemon)**: Quando o Ollama é instalado e iniciado, ele roda como um serviço no sistema operacional (Windows, macOS, Linux). Esse serviço expõe uma API local (geralmente em http://localhost:11434).

* **Interação via API ou CLI**: Os usuários não precisam interagir diretamente com o processo principal do Ollama. Eles podem usar a interface de linha de comando (CLI) do Ollama ou qualquer cliente compatível (como bibliotecas Python, JavaScript, ou interfaces web de terceiros) para fazer solicitações à API.

* **Gerenciamento de Modelos**: Quando um usuário solicita um modelo específico (por exemplo, ollama run llama2), o serviço em segundo plano do Ollama verifica se o modelo já está baixado localmente. Se não estiver, ele baixa o modelo automaticamente e o carrega na memória (incluindo o uso de GPU, se configurado) para inferência.

* **Facilidade de Uso**: A beleza do Ollama reside na sua simplicidade. O usuário simplesmente "pede" o modelo e o Ollama lida com toda a complexidade de configuração e execução, agindo como um servidor de IA local.

Essa arquitetura simplifica a execução local de LLMs, tornando-os mais acessíveis para desenvolvedores e usuários que desejam rodar modelos off-line, com maior privacidade e segurança.

---

## Ollama no CLI

O panorama direto dos comandos do **Ollama** que você pode usar no terminal.

## Comandos principais do Ollama

### **1. Listar modelos instalados**

```
ollama list
```

### **2. Buscar modelos disponíveis no repositório oficial**

```
ollama search <nome>
```

Exemplo:

```
ollama search llama3
```

### **3. Baixar (pull) um modelo**

```
ollama pull <modelo>
```

Exemplo:

```
ollama pull llama3
```

### **4. Rodar um modelo diretamente no terminal**

```
ollama run <modelo>
```

Exemplo:

```
ollama run llama3
```

### **5. Executar um prompt direto no comando**

```
ollama run <modelo> "me explique recursão"
```

### **6. Parar um modelo em execução**

```
ollama stop <modelo>
```

### **7. Remover um modelo do seu PC**

```
ollama rm <modelo>
```

### **8. Ver logs do servidor Ollama**

```
ollama logs
```

### **9. Ver informações do sistema**

```
ollama ps
```

Mostra modelos rodando e processos ativos.

### **10. Instalar/atualizar servidor Ollama**

macOS (Homebrew):

```
brew install ollama
brew upgrade ollama
```


 