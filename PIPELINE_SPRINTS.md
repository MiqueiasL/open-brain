# 📋 OpenBrain — Pipeline de Sprints & Quadro Ágil (Kanban / Scrum)

> **Foco Inicial:** Engenharia Reversa dos Assistentes (Sprint 0) & Android Pocket Edition (Sprint 1)  
> **Status:** Sprint 0 em Andamento  
> **Última Atualização:** 23/11/2026

---

## 📊 Matriz de Esforço & Recursos (Legenda)

- ⏱️ **Tempo Dev Solo**: Estimativa de tempo se desenvolvido manualmente por um desenvolvedor humano experiente (pesquisa técnica, leitura de docs e engenharia reversa).
- 🪙 **Tokens Agente Solo**: Média estimada de tokens consumidos (prompt + pesquisa profunda + síntese arquitetural + código).
- 🎯 **DoD (Definition of Done)**: Critério estrito de aceitação da tarefa.

---

## 🔬 Sprint 0: Engenharia Reversa & Dossiê Comparativo dos Grandes Assistentes
**Meta da Sprint:** Mapear a fundo como Siri (Apple), Google Assistente/Gemini (Android) e Alexa (Amazon) fazem suas requisições, como se comunicam com apps e onde está a barreira que eles não conseguem ultrapassar (e que o OpenBrain irá romper).  
**Prazo Total Estimado:** 1 dia (Dev Solo) · 1 sessão focada (Agente Solo)

### 📌 Backlog da Sprint 0

| ID | Tarefa | Responsável | ⏱️ Dev Solo | 🪙 Tokens IA | Status | Prazo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **OB-R01** | **Engenharia Reversa Apple Siri & Apple Intelligence**<br>Mapear App Intents, SiriKit, Entity Queries, execução out-of-process e limites de sandboxing do iOS. | Agente | 3h 00m | ~40.000 | 🟡 Em Progresso | D+0 |
| **OB-R02** | **Engenharia Reversa Google Assistente & Gemini Android**<br>Mapear `VoiceInteractionService`, `shortcuts.xml` (App Actions), `onProvideAssistStructure` e por que o Gemini não consegue clicar em apps. | Agente | 3h 30m | ~45.000 | 📋 A Fazer | D+0 |
| **OB-R03** | **Engenharia Reversa Amazon Alexa (ASK & Mobile Links)**<br>Mapear modelo Cloud-to-Cloud via AWS Lambda, deep-linking mobile e limitações de controle de dispositivo. | Agente | 2h 00m | ~25.000 | 📋 A Fazer | D+1 |
| **OB-R04** | **Dossiê Comparativo & Vantagem Competitiva do OpenBrain**<br>Consolidar em `docs/RESEARCH_ASSISTANTS_ECOSYSTEM.md`: Abordagem Cooperativa (Big Tech) vs Abordagem Soberana por Acessibilidade/Shizuku (OpenBrain). | Agente + Dev | 2h 30m | ~35.000 | 📋 A Fazer | D+1 |

**Subtotal Sprint 0:**  
⏱️ **Dev Solo:** ~11 horas  
🪙 **Tokens Agente Solo:** ~145.000 tokens  

---

## 🏃 Sprint 1: Fundação Android, Execução Elevada & Acessibilidade
**Meta da Sprint:** Ter a casca Android instalada no celular, capaz de rodar comandos de sistema via Shizuku (sem PC) e ler elementos na tela via Acessibilidade.  
**Prazo Total Estimado:** 3 a 4 dias (Dev Solo) · 1 a 2 sessões focadas (Agente Solo)  
**Status da Sprint:** 🟢 **100% CONCLUÍDA** (APK v0.1 Compilado com Sucesso: `OpenBrain_v0.1.apk`)

### 📌 Backlog da Sprint 1

| ID | Tarefa | Responsável | ⏱️ Dev Solo | 🪙 Tokens IA | Status | Prazo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **OB-01** | **Scaffold do Módulo Android (`drivers/android`)**<br>Estruturar Gradle 8.4, SDK 34, Kotlin e `AndroidManifest.xml` no Disco D. | Agente + Dev | 2h 30m | ~35.000 | 🟢 Concluído | D+0 |
| **OB-02** | **Integração Shizuku API (Wireless ADB sem PC)**<br>Adicionar dependência `rikka.shizuku:api`, binding IPC e permissões para rodar comandos shell de sistema. | Agente | 4h 00m | ~55.000 | 🟢 Concluído | D+0 |
| **OB-03** | **Motor de Acessibilidade (`OpenBrainAccessibilityService`)**<br>Captura em tempo real da árvore de nós da UI (`AccessibilityNodeInfo`), extração de textos/botões e injeção de toques. | Agente | 5h 30m | ~70.000 | 🟢 Concluído | D+0 |
| **OB-04** | **Cápsula Flutuante Minimalista (Floating HUD Overlay)**<br>Janela flutuante (`TYPE_APPLICATION_OVERLAY`) compacta com modo bolha recolhida e modo expandido sobre qualquer app. | Agente | 3h 30m | ~45.000 | 🟢 Concluído | D+0 |
| **OB-05** | **Ponte de Instalação Autônoma de APKs (OTA Installer)**<br>`PackageInstaller` e `FileProvider` permitindo que o OpenBrain baixe e instale APKs sozinho sem PC. | Agente + Dev | 3h 00m | ~40.000 | 🟢 Concluído | D+0 |
| **OB-06** | **Módulo de Controle de Hardware Básico (Paridade Siri/Google)**<br>Lanterna, Volume, Brilho, Wi-Fi/Bluetooth panel, Não Perturbe e Telemetria de Bateria. | Agente | 3h 30m | ~45.000 | 🟢 Concluído | D+0 |

---

## 🏃 Sprint 2: Habilidades do Cotidiano, Voz & Ponte de Memória
**Meta da Sprint:** Cobrir 100% das funções do dia a dia (Alarmes, Timers, WhatsApp, Spotify, Mapas, Voz STT/TTS) e conectar a Memória Universal (`ai-memory`).  
**Prazo Total Estimado:** 3 a 4 dias (Dev Solo) · 1 a 2 sessões focadas (Agente Solo)

### 📌 Backlog da Sprint 2

| ID | Tarefa | Responsável | ⏱️ Dev Solo | 🪙 Tokens IA | Status | Prazo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **OB-07** | **Módulo de Produtividade & Tempo (Alarmes, Timers, Agenda)**<br>Alarmes, cronômetros, eventos de calendário e lembretes com notificação nativa. | Agente | 3h 00m | ~40.000 | 📋 Backlog | D+6 |
| **OB-08** | **Módulo de Mídia, Comunicação & Navegação**<br>Spotify / YouTube play, envio rápido no WhatsApp, discagem e rotas no Waze/Google Maps. | Agente | 3h 30m | ~45.000 | 📋 Backlog | D+7 |
| **OB-09** | **Motor de Voz Bidirecional (STT & TTS Neural PT-BR)**<br>Entrada de voz rápida com SpeechRecognizer + resposta falada natural via TextToSpeech. | Agente | 3h 30m | ~50.000 | 📋 Backlog | D+8 |
| **OB-10** | **Ponte de Sincronização AI-Memory (Universal Memory Sync)**<br>Conector local/remoto para sincronizar as notas e regras de `D:\ai-memory-data` diretamente no smartphone. | Agente | 4h 00m | ~60.000 | 📋 Backlog | D+9 |

---

## 🏃 Sprint 3: Validação de Campo & Casos de Uso Reais
**Meta da Sprint:** Colocar o OpenBrain para resolver tarefas diárias reais no celular sem intervenção manual (ex: atualizar apps, extrair dados de telas, automatizar rotinas).

| ID | Tarefa | Responsável | ⏱️ Dev Solo | 🪙 Tokens IA | Status | Prazo |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **OB-09** | **Teste E2E: Auto-Instalação e Auto-Update Sem PC**<br>Disparar pelo OpenBrain a compilação/download de um APK de teste e auto-instalação no Android. | Dev + Agente | 2h 00m | ~30.000 | 📋 Backlog | D+9 |
| **OB-10** | **Teste E2E: Navegação e Ação em App de Terceiros**<br>Abrir um app pelo nome, navegar até uma tela específica e extrair o texto para a memória universal. | Dev + Agente | 3h 00m | ~40.000 | 📋 Backlog | D+10 |
