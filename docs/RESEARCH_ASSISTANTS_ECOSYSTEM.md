# 🔬 Dossiê de Engenharia Reversa: O Ecossistema dos Grandes Assistentes
> **Documento de Pesquisa Arquitetural — OpenBrain (Sprint 0 / Tasks OB-R01 a OB-R04)**  
> *Como Siri, Google Assistente, Gemini e Alexa realmente funcionam por baixo do capô, por que eles fracassaram em autonomia real e como o OpenBrain rompe essa barreira.*

---

## 1. Apple Siri & Apple Intelligence (iOS / macOS)

### 1.1. Arquitetura Subjacente
Historicamente baseada no **SiriKit** (`Intents.framework`), a Apple migrou todo o ecossistema a partir do iOS 16/17 e do Apple Intelligence (iOS 18) para o framework **App Intents**:

```
[ Usuário fala/digita ]
        │
        ▼
[ Siri NLU & Semantic Index (Local / Nuvem) ]
        │  (Mapeia a intenção para uma AppIntent registrada)
        ▼
[ XPC Daemon (launchd) ]
        │  (Comunicação interprocessos isolada da Apple)
        ▼
[ App Extension Sandbox (Out-of-Process) ]
        │  Executa: perform() async throws -> some IntentResult
        ▼
[ Retorno: Diálogo, Snippet ou Deep-Link ]
```

### 1.2. Como o Desenvolvedor se Integra
O desenvolvedor do app é **obrigado** a escrever código Swift explícito declarando cada ação:
```swift
struct CriarPedidoIntent: AppIntent {
    static var title: LocalizedStringResource = "Fazer Pedido"
    
    @Parameter(title: "Item")
    var item: String
    
    func perform() async throws -> some IntentResult {
        // O código do app executa aqui dentro
        return .result(dialog: "Pedido de \(item) realizado com sucesso!")
    }
}
```

### 1.3. Limitações Críticas & Por que a Siri não tem Autonomia Real:
1. **O Modelo Cooperativo Obrigatório**: Se o desenvolvedor do app não programou uma `AppIntent`, a Siri é **100% incapaz** de interagir com o app.
2. **Isolamento de Sandbox Absoluto**: A Siri nunca vê a tela real de outro aplicativo nem pode clicar em botões que estão visíveis. Ela só executa blocos de código pré-autorizados.
3. **Impossibilidade de Automação de Sistema**: A Siri não instala apps por fora da App Store, não altera configurações restritas e não roda scripts de linha de comando no iPhone.

---

## 2. Google Assistente & Gemini on Android

### 2.1. Arquitetura Subjacente
No Android, a integração de assistentes é dividida em três pilares:
1. **`VoiceInteractionService` & `VoiceInteractionSession`**: O serviço oficial do sistema Android que define quem é o assistente padrão (ativado ao segurar o botão de energia ou falar "Ok Google").
2. **App Actions & BIIs (Built-in Intents)**: Declarados no arquivo `res/xml/shortcuts.xml` dentro do APK do desenvolvedor.
3. **Assist Structure API (`onProvideAssistStructure` / `onProvideAssistData`)**: Como o assistente "enxerga" o que está na tela.

```
[ Usuário aciona o Gemini / Assistente ]
        │
        ▼
[ VoiceInteractionSessionManager (System Server) ]
        │
        ├─► Solicita a árvore de nós sanitizada: Activity.onProvideAssistStructure()
        │   (Se o app tiver FLAG_SECURE como apps de banco, o Google recebe tela preta)
        │
        └─► Dispara Intent com parâmetros mapeados em shortcuts.xml
                │
                ▼
        [ App de Terceiro abre na tela via Deep Link ]
```

### 2.2. Como o Gemini se Comporta Hoje no Android:
- O Gemini substituiu a interface do Google Assistente por um overlay flutuante.
- Quando você clica em *"Perguntar sobre esta tela"*, o Gemini tira um screenshot temporário via `MediaProjection`, extrai o texto/visão via multimodalidade na nuvem e responde sua dúvida.
- **Mas ele para por aí**: ele gera um texto explicativo ou sugere um link. Ele **NUNCA clica nos botões pelo usuário**.

### 2.3. Por que o Gemini não Consegue Agir Sozinho:
1. **Medo de Responsabilidade & Processos**: O Google não permite que o Gemini execute transações automáticas dentro de apps de terceiros para evitar ações indesejadas (ex: compras acidentais ou transferências erradas).
2. **Políticas de SELinux e Segurança**: O aplicativo do Google roda como um app comum do sistema (`system_app` ou `priv-app`). Ele não tem permissão para emitir eventos de toque (`MotionEvent`) em janelas de outros apps sem passar pelo `AccessibilityService` ou `input tap` do ADB.
3. **Dependência de `shortcuts.xml`**: Se o app não registrou ações para o Google, o assistente só consegue abrir o app e deixar você na tela inicial.

---

## 3. Amazon Alexa (Mobile & Dispositivos)

### 3.1. Arquitetura Subjacente
A Alexa opera quase inteiramente no modelo **Cloud-to-Cloud**:
```
[ Áudio captado ] ──► [ Alexa Voice Service (Nuvem AWS) ]
                             │
                             ▼
                    [ AWS Lambda (Skill) ]
                             │
                             ▼
                    [ API Web da Empresa (iFood, Uber, etc.) ]
```

### 3.2. No Smartphone:
- A Alexa no celular é essencialmente um aplicativo comum.
- Ela se comunica com apps do smartphone apenas através de **Universal Links / Custom Schemes** (ex: `uber://...` ou `spotify://...`).
- Ela não tem nenhuma integração profunda com o sistema operacional móvel, não lê a tela e não executa ações locais.

---

## 4. O Choque de Paradigmas: Big Tech vs OpenBrain

Aqui está o diagnóstico exato que define a **vantagem competitiva e a tese de existência do OpenBrain**:

| Aspecto | Siri / Gemini / Alexa (Big Tech) | OpenBrain (Abordagem Soberana) |
| :--- | :--- | :--- |
| **Filosofia de Integração** | **Cooperativa & Sandboxed**<br>(Exige que o desenvolvedor crie APIs/Intents). | **Autônoma & Direta no SO**<br>(Funciona em 100% dos apps, mesmo sem API). |
| **Leitura da Interface** | Sanitizada ou apenas Screenshot OCR. | **Árvore de Nós Nativa em Tempo Real** via `AccessibilityNodeInfo`. |
| **Capacidade de Ação** | Apenas Deep Links e blocos pré-programados. | **Toques, Rolagens, Digitação e Gestos Reais** via `AccessibilityService`. |
| **Execução de Sistema** | Bloqueada (Zero shell, zero terminal). | **Shell Elevado Nativo** via Shizuku (Wireless ADB sem PC) e Termux. |
| **Instalação de Apps** | Apenas via App Store / Play Store. | **Instalação Autônoma de APKs** via `PackageInstaller` e `FileProvider`. |
| **Memória Contínua** | Ilhas isoladas dentro de contas fechadas. | **Memória Universal Aberta (`ai-memory`)** sincronizada entre PC e Celular. |
| **Independência** | Controlado por diretrizes corporativas restritivas. | **100% Open-Source e nas mãos do Usuário**. |

---

## 5. Diretrizes de Engenharia para a Construção do OpenBrain (Sprint 1)

Com base nessa engenharia reversa, derivamos as seguintes regras técnicas fundamentais para a implementação do OpenBrain no Android:

1. **Não depender de APIs públicas de terceiros**: O OpenBrain deve ser capaz de abrir qualquer tela, localizar qualquer botão por texto/ID e interagir com ele como um ser humano faria.
2. **Utilizar o Shizuku como nosso "Root sem Root"**: O Shizuku nos dá os mesmos poderes que o ADB pelo computador tinha, mas de forma 100% autônoma dentro do smartphone.
3. **Casca de Acessibilidade Silenciosa e Eficiente**: Nosso serviço de acessibilidade não deve consumir bateria excessiva; ele deve monitorar a tela apenas durante a execução de uma tarefa ativa ou quando convocado pela cápsula flutuante (HUD).
4. **Ponte de Confiança com o Usuário**: O OpenBrain mostrará na cápsula flutuante o que está lendo e o que vai clicar, mantendo o usuário com controle absoluto (Human-in-the-Loop) quando necessário.
