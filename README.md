# 🧠 OpenBrain

> **The Universal Autonomous AI Brain for Every Screen**  
> *O cérebro autônomo de código aberto que você carrega no bolso e no PC — com memória universal contínua e controle total sobre qualquer dispositivo.*

---

## 🌟 A Visão

Hoje, no computador desktop, as inteligências artificiais ganharam superpoderes: executam comandos em shell, editam arquivos, realizam builds, acessam ferramentas via MCP e consultam memórias persistentes. No entanto, nos smartphones — os aparelhos mais presentes no nosso dia a dia —, a IA foi aprisionada em caixas de chat isoladas ou assistentes de voz limitados (Siri, Google Assistente), incapazes de instalar aplicativos sozinhos, manipular interfaces sem APIs públicas ou agir com autonomia real.

**OpenBrain** nasceu para romper esse isolamento.

O OpenBrain é um ecossistema de código aberto que fornece uma **camada de inteligência unificada, autônoma e portátil**. Ele combina um cérebro de raciocínio com motores de execução locais no **Android, Windows, Mac e iOS**, mantendo uma **Memória Universal Persistente** compartilhada entre todos os seus dispositivos.

---

## 🚀 Pilares Fundamentais

1. **Liberdade Real de Execução (No Walled Gardens)**:
   - Sem restrições artificiais de ecossistema. O OpenBrain possui ferramentas nativas para interagir com o sistema operacional, executar linhas de comando, instalar pacotes e manipular a interface do usuário.
2. **Memória Universal Unificada**:
   - Uma regra, decisão, projeto ou contexto aprendido no PC está imediatamente disponível quando você aciona o OpenBrain no smartphone ou no notebook.
3. **Pocket Edition (Formato Ultra-Compacto e Portátil)**:
   - No celular, o OpenBrain opera como um HUD flutuante (overlay minimalista / cápsula tática), acessível em cima de qualquer app com 1 toque, sem precisar de telas cheias intrusivas.
4. **Independência e Privacidade (Agnóstico de Modelos)**:
   - Funciona tanto com APIs de ponta na nuvem (Gemini 2.5/Flash, Claude 3.7, GPT-4o) quanto com modelos abertos locais (Ollama, vLLM, NPUs Snapdragon/Apple Silicon).

---

## 🏗️ Arquitetura do Sistema

```
                      ┌──────────────────────────┐
                      │     OPENBRAIN CORE       │
                      │  (Planner + Raciocínio)  │
                      └─────────────┬────────────┘
                                    │
               ┌────────────────────┼────────────────────┐
               │                    │                    │
        [ Memória Universal ] [ Protocolo MCP ] [ Voice / Chat UI ]
               │                    │                    │
 ┌─────────────┴───────────┬────────┴────────────┬───────┴─────────────┐
 │                         │                     │                     │
 ▼                         ▼                     ▼                     ▼
[ Android Adapter ]     [ Windows Adapter ]   [ Mac Adapter ]       [ iOS Adapter ]
• Shizuku (Wireless ADB) • PowerShell / Win32  • AppleScript / Zsh   • Shortcuts & Intents
• Accessibility Service  • UI Automation API  • AXUIElement (A11y)  • Local Sideload Bridge
• PackageInstaller       • Background Daemons  • LaunchAgents        • Vision & Actions
```

Para detalhes aprofundados sobre a arquitetura de software, consulte [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## 📱 Foco Imediato: OpenBrain Mobile (Android Pocket Edition)

A primeira esteira de desenvolvimento do OpenBrain foca em resolver a maior dor do ecossistema móvel: **autonomia real no Android sem depender de cabos ou de um computador intermediário**:

- **Execução Elevada sem Root**: Integração com **Shizuku** via Wireless ADB para rodar comandos de terminal de sistema nativamente dentro do smartphone.
- **Manipulação de Telas**: Motor nativo baseado em **Accessibility Service** para ler árvores de nós da interface (`AccessibilityNodeInfo`), encontrar botões, rolar páginas e clicar em qualquer app.
- **Instalação Autônoma de APKs**: Capacidade de baixar e atualizar aplicativos over-the-air (OTA) via `PackageInstaller` e `FileProvider`.
- **Sincronização de Memória**: Cliente conectado em tempo real com o repositório de memória persistente (`ai-memory` / Obsidian Vault).

---

## 🗺️ Roadmap do Projeto

- [x] **Fase 0**: Manifesto, concepção arquitetural e definição da marca oficial.
- [ ] **Fase 1 (Android Core)**:
  - Criação do esqueleto Android Nativo (Kotlin / Gradle).
  - Implementação da cápsula flutuante (Floating HUD Overlay).
  - Módulo de conexão com Shizuku (Wireless ADB Runner).
  - Módulo do Accessibility Service para controle de tela e toques.
- [ ] **Fase 2 (Universal Memory Bridge)**:
  - Cliente local no Android para sincronizar memória com o `ai-memory` do PC.
- [ ] **Fase 3 (Desktop Agent)**:
  - Cliente unificado para Windows e Mac permitindo orquestração cruzada entre PC e celular.
- [ ] **Fase 4 (iOS & Open Ecosystem)**:
  - Extensão para atalhos e automações iOS e lançamento público da documentação open-source.

---

## 📄 Licença

Distribuído sob a licença **MIT**. Veja `LICENSE` para mais informações.
