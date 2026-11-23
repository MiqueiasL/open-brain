# 📋 OpenBrain — Pipeline de Sprints & Quadro Ágil (Kanban / Scrum)

> **Foco Inicial:** Android Pocket Edition (Autonomia no Smartphone, Shizuku, Acessibilidade e Floating HUD)  
> **Status:** Sprint 1 Planejada  
> **Última Atualização:** 23/11/2026

---

## 📊 Matriz de Esforço & Recursos (Legenda)

- ⏱️ **Tempo Dev Solo**: Estimativa de tempo se desenvolvido manualmente por um desenvolvedor humano experiente (incluindo setup, leitura de docs, compilação e testes manuais).
- 🪙 **Tokens Agente Solo**: Média estimada de tokens consumidos (prompt + raciocínio + geração de código + testes automatizados) se executado autonomamente pela IA.
- 🎯 **DoD (Definition of Done)**: Critério estrito de aceitação da tarefa.

---

## 🏃 Sprint 1: Fundação Android, Execução Elevada & Acessibilidade
**Meta da Sprint:** Ter a casca Android instalada no celular, capaz de rodar comandos de sistema via Shizuku (sem PC) e ler elementos na tela via Acessibilidade.  
**Prazo Total Estimado:** 3 a 4 dias (Dev Solo) · 1 a 2 sessões focadas (Agente Solo)

### 📌 Backlog da Sprint 1

| ID | Tarefa | Responsável | ⏱️ Dev Solo | 🪙 Tokens IA | Status | Prazo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **OB-01** | **Scaffold do Módulo Android (`drivers/android`)**<br>Estruturar Gradle 8.4, SDK 34, Kotlin e `AndroidManifest.xml` no Disco D. | Agente + Dev | 2h 30m | ~35.000 | 📋 A Fazer | D+1 |
| **OB-02** | **Integração Shizuku API (Wireless ADB sem PC)**<br>Adicionar dependência `rikka.shizuku:api`, binding IPC e permissões para rodar comandos shell de sistema. | Agente | 4h 00m | ~55.000 | 📋 A Fazer | D+2 |
| **OB-03** | **Motor de Acessibilidade (`OpenBrainAccessibilityService`)**<br>Captura em tempo real da árvore de nós da UI (`AccessibilityNodeInfo`), extração de textos/botões e injeção de toques. | Agente | 5h 30m | ~70.000 | 📋 A Fazer | D+3 |
| **OB-04** | **Cápsula Flutuante Minimalista (Floating HUD Overlay)**<br>Janela flutuante (`TYPE_APPLICATION_OVERLAY`) compacta com modo bolha recolhida e modo expandido sobre qualquer app. | Agente | 3h 30m | ~45.000 | 📋 A Fazer | D+3 |
| **OB-05** | **Ponte de Instalação Autônoma de APKs (OTA Installer)**<br>`PackageInstaller` e `FileProvider` permitindo que o OpenBrain baixe e instale APKs sozinho sem PC. | Agente + Dev | 3h 00m | ~40.000 | 📋 A Fazer | D+4 |

**Subtotal Sprint 1:**  
⏱️ **Dev Solo:** ~18 horas e 30 minutos  
🪙 **Tokens Agente Solo:** ~245.000 tokens  

---

## 🏃 Sprint 2: Cérebro no Bolso & Ponte de Memória Universal
**Meta da Sprint:** Conectar o OpenBrain Android ao cérebro de IA (Gemini/Claude) e à base universal de memória (`ai-memory`), permitindo que ele lembre de tudo o que foi decidido no PC.  
**Prazo Total Estimado:** 3 dias (Dev Solo) · 1 sessão focada (Agente Solo)

### 📌 Backlog da Sprint 2

| ID | Tarefa | Responsável | ⏱️ Dev Solo | 🪙 Tokens IA | Status | Prazo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **OB-06** | **Cliente LLM Leve no Celular (Tool-Calling Engine)**<br>Execução de chamadas diretas de IA com streaming e suporte a ferramentas nativas via HTTP nativo. | Agente | 3h 30m | ~45.000 | 📋 Backlog | D+5 |
| **OB-07** | **Ponte de Sincronização AI-Memory (Universal Memory Sync)**<br>Conector local/remoto para sincronizar as notas e regras de `D:\ai-memory-data` diretamente no smartphone. | Agente | 4h 00m | ~60.000 | 📋 Backlog | D+6 |
| **OB-08** | **Módulo de Comandos de Voz & Whisper Local/API**<br>Reconhecimento de voz rápido ativado com 1 toque na bolha flutuante para comandos livres. | Agente | 4h 30m | ~65.000 | 📋 Backlog | D+7 |

**Subtotal Sprint 2:**  
⏱️ **Dev Solo:** ~12 horas  
🪙 **Tokens Agente Solo:** ~170.000 tokens  

---

## 🏃 Sprint 3: Validação de Campo & Casos de Uso Reais
**Meta da Sprint:** Colocar o OpenBrain para resolver tarefas diárias reais no celular sem intervenção manual (ex: atualizar apps, extrair dados de telas, automatizar rotinas).  
**Prazo Total Estimado:** 2 a 3 dias

| ID | Tarefa | Responsável | ⏱️ Dev Solo | 🪙 Tokens IA | Status | Prazo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **OB-09** | **Teste E2E: Auto-Instalação e Auto-Update Sem PC**<br>Disparar pelo OpenBrain a compilação/download de um APK de teste e auto-instalação no Android. | Dev + Agente | 2h 00m | ~30.000 | 📋 Backlog | D+8 |
| **OB-10** | **Teste E2E: Navegação e Ação em App de Terceiros**<br>Abrir um app pelo nome, navegar até uma tela específica e extrair o texto para a memória universal. | Dev + Agente | 3h 00m | ~40.000 | 📋 Backlog | D+9 |

---

## 📈 Resumo Geral do Plano

| Métrica | Estimativa Dev Solo | Estimativa Agente Solo (IA) |
| :--- | :--- | :--- |
| **Tempo Total de Desenvolvimento** | ~33 horas de codificação e testes | ~450.000 tokens (cerca de 3 a 4 sessões guiadas) |
| **Custo de Token (Gemini 2.5 Flash)** | $0.00 | ~$0.15 a $0.35 USD (menos de R$ 2,00) |
| **Velocidade de Entrega** | 2 a 3 semanas (ritmo parcial) | 2 a 4 dias (colaboração interativa) |
