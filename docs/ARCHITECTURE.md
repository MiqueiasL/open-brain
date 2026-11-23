# 📐 OpenBrain — Especificação Arquitetural

## 1. Visão Geral

O **OpenBrain** adota uma arquitetura em camadas desacopladas (*Decoupled Layered Architecture*), garantindo que a inteligência central e a camada de memória persistente permaneçam 100% universais e agnósticas de plataforma, enquanto cada sistema operacional (Android, Windows, Mac, iOS) implementa um **Device Driver** específico.

---

## 2. Diagrama de Componentes

```
+-------------------------------------------------------------+
|                       OPENBRAIN CORE                        |
|  +--------------------+  +-------------------------------+  |
|  | Task Planner (LLM) |  | Tool Calling Registry (MCP)   |  |
|  +--------------------+  +-------------------------------+  |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                  UNIVERSAL MEMORY LAYER                     |
|  +--------------------+  +-------------------------------+  |
|  | ai-memory Client   |  | Local Cache (SQLite / JSON)   |  |
|  +--------------------+  +-------------------------------+  |
+-------------------------------------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                   PLATFORM DRIVER BRIDGE                    |
|  +------------------+ +------------------+ +-------------+  |
|  | Android Adapter  | | Windows Adapter  | | Mac/iOS Adap|  |
|  +------------------+ +------------------+ +-------------+  |
+-------------------------------------------------------------+
```

---

## 3. Detalhamento dos Módulos

### 3.1. OpenBrain Core
- **Função**: Recebe solicitações em linguagem natural (voz ou texto), elabora planos de execução multi-etapas (*Chain-of-Thought* / *ReAct*) e invoca ferramentas padronizadas.
- **Formato das Ferramentas**:
  - `run_shell(command: string) -> string`
  - `inspect_screen() -> UIHierarchy`
  - `click(target: TargetQuery) -> boolean`
  - `input_text(target: TargetQuery, text: string) -> boolean`
  - `install_package(package_source: string) -> InstallResult`
  - `query_memory(query: string) -> MemoryContext`
  - `save_memory(key: string, content: string) -> void`

---

### 3.2. Android Pocket Edition Driver
O driver Android é projetado para operar com **zero necessidade de computador ou cabo USB**:

1. **Floating HUD Service (`FloatingHudService`)**:
   - Renderiza uma bolha/cápsula flutuante compacta usando `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
   - Permite expandir uma mini-interface tática ou recolher em 1 toque.
2. **Accessibility Engine (`OpenBrainAccessibilityService`)**:
   - Analisa o `AccessibilityNodeInfo` da janela em primeiro plano.
   - Localiza botões, campos de texto e listas por `viewId`, `text` ou `contentDescription`.
   - Dispara gestos (`dispatchGesture`) e ações de rolagem/toque em qualquer app.
3. **Elevated Execution Engine (`ShizukuRunner`)**:
   - Conecta-se à API do Shizuku via binder IPC.
   - Executa comandos com privilégios de `shell/adb` (ex: `pm install -r`, `am start`, `screencap`, `input keyevent`).
4. **OTA Package Installer (`PackageInstallerBridge`)**:
   - Instala atualizações e novos apps via `FileProvider` e chamadas de sistema.

---

### 3.3. Universal Memory Layer
- Mantém coerência de contexto entre dispositivos.
- Sincroniza regras de operação, preferências do usuário e histórico de tarefas através do protocolo `ai-memory` e arquivos markdown estruturados.
