# 🎯 Matriz de Paridade & Habilidades Oficiais do OpenBrain
> **Diretriz Mandatória da Next Wave:** O OpenBrain DEVE executar **100% das funções que os assistentes comerciais modernos (Siri, Google Assistente, Gemini, Alexa) já executam**, para então expandir com os seus superpoderes exclusivos.

---

## 📱 Bloco 1: Controle de Hardware & Sistema (Device Control)
Funções que o usuário usa diariamente por voz ou comando rápido:

| Habilidade | Implementação Técnica Android (OpenBrain) | Status |
| :--- | :--- | :--- |
| **Lanterna (Ligar / Desligar)** | `CameraManager.setTorchMode(cameraId, enabled)` | 📋 A Fazer |
| **Volume (Mídia, Toque, Alarme)** | `AudioManager.setStreamVolume(...)` + presets rápidos | 📋 A Fazer |
| **Brilho da Tela** | `Settings.System.putInt(..., SCREEN_BRIGHTNESS, ...)` ou Shizuku `settings put system screen_brightness` | 📋 A Fazer |
| **Wi-Fi & Bluetooth (Toggle / Painel)** | `Settings.Panel.ACTION_WIFI` / `ACTION_INTERNET_CONNECTIVITY` ou Shizuku `svc wifi enable/disable` | 📋 A Fazer |
| **Modo Não Perturbe / Silencioso** | `NotificationManager.setInterruptionFilter(...)` | 📋 A Fazer |
| **Bateria & Telemetria do Aparelho** | `BatteryManager` (Nível, temperatura, tempo restante de carga) | 📋 A Fazer |
| **Bloquear Tela / Desligar Display** | `DevicePolicyManager.lockNow()` ou Shizuku `input keyevent 26` | 📋 A Fazer |
| **Abrir Configurações Específicas** | Intents explícitos para Telas de Apps, Armazenamento, Acessibilidade, Bateria | 📋 A Fazer |

---

## ⏰ Bloco 2: Tempo, Alarmes & Produtividade
O coração utilitário do cotidiano:

| Habilidade | Implementação Técnica Android (OpenBrain) | Status |
| :--- | :--- | :--- |
| **Criar Alarme (Horário / Dias)** | `AlarmClock.ACTION_SET_ALARM` com hora, minuto, mensagem e skip-ui | 📋 A Fazer |
| **Iniciar Timer / Cronômetro** | `AlarmClock.ACTION_SET_TIMER` com duração em segundos e auto-start | 📋 A Fazer |
| **Criar Evento no Calendário** | `Intent(Intent.ACTION_INSERT).setData(Events.CONTENT_URI)` | 📋 A Fazer |
| **Lembretes com Alarme/Notificação** | Banco de dados local + `AlarmManager.setExactAndAllowWhileIdle()` | 📋 A Fazer |
| **Lista de Compras / Notas Rápidas** | Sincronizado nativamente no `UniversalMemory` (OpenBrain Memory) | 📋 A Fazer |

---

## 📞 Bloco 3: Comunicação & Chamadas
Ações sociais essenciais:

| Habilidade | Implementação Técnica Android (OpenBrain) | Status |
| :--- | :--- | :--- |
| **Fazer Ligação Telefônica** | `Intent(Intent.ACTION_CALL, Uri.parse("tel:..."))` | 📋 A Fazer |
| **Mandar Mensagem no WhatsApp** | Deep-link `https://api.whatsapp.com/send?phone=...&text=...` + fallback Acessibilidade | 📋 A Fazer |
| **Enviar SMS** | `Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:..."))` | 📋 A Fazer |
| **Abrir Câmera / Tirar Foto** | `MediaStore.ACTION_IMAGE_CAPTURE` ou `CameraX` silencioso | 📋 A Fazer |

---

## 🎵 Bloco 4: Mídia & Entretenimento
Controle de reprodução de áudio e streaming:

| Habilidade | Implementação Técnica Android (OpenBrain) | Status |
| :--- | :--- | :--- |
| **Tocar Música / Artista no Spotify** | Intent URI `spotify:search:...` + simulação de Play via `MediaSession` | 📋 A Fazer |
| **Tocar no YouTube / YouTube Music** | Intent URI `vnd.youtube:...` ou deep link direto | 📋 A Fazer |
| **Controle de Mídia Global** | `AudioManager.dispatchMediaKeyEvent(...)` (Play, Pause, Próxima, Anterior) | 📋 A Fazer |

---

## 🧭 Bloco 5: Navegação, Clima & Utilidades Rápidas
Orientação e consultas rápidas:

| Habilidade | Implementação Técnica Android (OpenBrain) | Status |
| :--- | :--- | :--- |
| **Navegar para Endereço / Traçar Rota** | `google.navigation:q=...` ou `waze://?q=...` | 📋 A Fazer |
| **Previsão do Tempo em Tempo Real** | Open-Meteo / wttr.in (API leve e sem necessidade de chave) | 📋 A Fazer |
| **Cotação de Moedas & Conversões** | Conversor financeiro offline + API de câmbio (Dólar, Euro, Bitcoin, Real) | 📋 A Fazer |
| **Calculadora Matemática Rápida** | Interpretador matemático nativo no Core | 📋 A Fazer |

---

## 🗣️ Bloco 6: Voz Bidirecional (Falar e Ouvir)
A experiência de um assistente moderno:

| Habilidade | Implementação Técnica Android (OpenBrain) | Status |
| :--- | :--- | :--- |
| **Entrada de Voz (Speech-to-Text)** | Android `SpeechRecognizer` nativo + Whisper API como fallback | 📋 A Fazer |
| **Resposta Falada (Text-to-Speech)** | Android `TextToSpeech` nativo com vozes neurais em português brasileiro | 📋 A Fazer |

---

## 🚀 Bloco 7: SUPERPODERES EXCLUSIVOS (O que NENHUM outro assistente faz)
Onde o OpenBrain ultrapassa a Big Tech:

| Superpoder | Mecanismo Técnico |
| :--- | :--- |
| **Mexer em QUALQUER App da Tela** | `AccessibilityService` (clica, rola e extrai dados mesmo sem API) |
| **Executar Terminal e Shell sem PC** | `Shizuku API` (execução com privilégios de ADB nativo no smartphone) |
| **Instalação e Atualização de APKs** | `PackageInstaller` + `FileProvider` autônomos |
| **Memória Universal Sincronizada** | O que você fez no PC no Windows é lembrado no celular em tempo real |
| **Modo Kali Linux / Pentest / Dev** | Ferramentas de terminal técnico e integração com ambientes de desenvolvimento |
