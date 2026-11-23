# OpenBrain — Sincronizador Automático com o GitHub Projects
# Este script cria o repositório remoto, sincroniza os commits e cria os Issues/Cards no GitHub Projects.

$ErrorActionPreference = "Stop"

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "   🧠 OPENBRAIN — SINCRONIZADOR GITHUB PROJECTS      " -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar se o GitHub CLI está instalado
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    Write-Host "[ERRO] GitHub CLI ('gh') não foi encontrado no PATH." -ForegroundColor Red
    Write-Host "Instale via 'winget install GitHub.cli' ou baixe em cli.github.com" -ForegroundColor Yellow
    exit 1
}

# 2. Verificar autenticação
Write-Host "[1/4] Verificando login no GitHub..." -ForegroundColor Yellow
$authStatus = gh auth status 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[AVISO] Você ainda não está logado no GitHub CLI!" -ForegroundColor Red
    Write-Host "Por favor, rode o comando abaixo no terminal para fazer o login no seu navegador:" -ForegroundColor Yellow
    Write-Host "  gh auth login" -ForegroundColor Green
    Write-Host ""
    Write-Host "Depois de logar, execute este script novamente para criar o projeto e as issues automaticamente." -ForegroundColor Cyan
    exit 1
}

Write-Host "[+] GitHub CLI autenticado com sucesso!" -ForegroundColor Green

# 3. Criar repositório remoto se ainda não existir
Write-Host "[2/4] Verificando repositório remoto no GitHub..." -ForegroundColor Yellow
$remotes = git remote -v
if (-not $remotes) {
    Write-Host "[*] Criando repositório no seu GitHub pessoal..." -ForegroundColor Cyan
    gh repo create "open-brain" --public --source=. --remote=origin --push
} else {
    Write-Host "[*] Enviando commits para o repositório remoto..." -ForegroundColor Cyan
    git push -u origin master
}

# 4. Criar as Issues do Sprint 0 e Sprint 1
Write-Host "[3/4] Sincronizando Pipeline e Tarefas com GitHub Issues..." -ForegroundColor Yellow

$issues = @(
    @{ title = "OB-R01: Engenharia Reversa Apple Siri & Apple Intelligence"; body = "Mapear App Intents, SiriKit, Entity Queries, execução out-of-process e limites de sandboxing do iOS."; label = "research,sprint-0" },
    @{ title = "OB-R02: Engenharia Reversa Google Assistente & Gemini Android"; body = "Mapear VoiceInteractionService, shortcuts.xml (App Actions), onProvideAssistStructure e limites."; label = "research,sprint-0" },
    @{ title = "OB-R03: Engenharia Reversa Amazon Alexa (ASK & Mobile Links)"; body = "Mapear modelo Cloud-to-Cloud via AWS Lambda, deep-linking mobile e limitações de controle."; label = "research,sprint-0" },
    @{ title = "OB-R04: Dossiê Comparativo & Vantagem Competitiva do OpenBrain"; body = "Consolidar pesquisa em docs/RESEARCH_ASSISTANTS_ECOSYSTEM.md: Abordagem Cooperativa vs Soberana."; label = "research,sprint-0" },
    @{ title = "OB-01: Scaffold do Módulo Android (drivers/android)"; body = "Estruturar Gradle 8.4, SDK 34, Kotlin e AndroidManifest.xml no Disco D."; label = "android,sprint-1" },
    @{ title = "OB-02: Integração Shizuku API (Wireless ADB sem PC)"; body = "Adicionar dependência rikka.shizuku:api, binding IPC e permissões para rodar comandos shell de sistema."; label = "android,sprint-1" },
    @{ title = "OB-03: Motor de Acessibilidade (OpenBrainAccessibilityService)"; body = "Captura em tempo real da árvore de nós da UI (AccessibilityNodeInfo), extração de textos/botões e injeção de toques."; label = "android,sprint-1" },
    @{ title = "OB-04: Cápsula Flutuante Minimalista (Floating HUD Overlay)"; body = "Janela flutuante (TYPE_APPLICATION_OVERLAY) compacta com modo bolha recolhida e modo expandido sobre qualquer app."; label = "android,sprint-1" },
    @{ title = "OB-05: Ponte de Instalação Autônoma de APKs (OTA Installer)"; body = "PackageInstaller e FileProvider permitindo que o OpenBrain baixe e instale APKs sozinho sem PC."; label = "android,sprint-1" },
    @{ title = "OB-06: Módulo de Controle de Hardware Básico (Paridade Siri/Google)"; body = "Lanterna, Volume, Brilho, Wi-Fi/Bluetooth panel, Não Perturbe e Telemetria de Bateria."; label = "android,sprint-1" },
    @{ title = "OB-07: Módulo de Produtividade & Tempo (Alarmes, Timers, Agenda)"; body = "Alarmes, cronômetros, eventos de calendário e lembretes com notificação nativa."; label = "android,sprint-2" },
    @{ title = "OB-08: Módulo de Mídia, Comunicação & Navegação"; body = "Spotify / YouTube play, envio rápido no WhatsApp, discagem e rotas no Waze/Google Maps."; label = "android,sprint-2" },
    @{ title = "OB-09: Motor de Voz Bidirecional (STT & TTS Neural PT-BR)"; body = "Entrada de voz rápida com SpeechRecognizer + resposta falada natural via TextToSpeech."; label = "android,sprint-2" },
    @{ title = "OB-10: Ponte de Sincronização AI-Memory (Universal Memory Sync)"; body = "Conector local/remoto para sincronizar as notas e regras de D:\ai-memory-data diretamente no smartphone."; label = "android,sprint-2" }
)

foreach ($issue in $issues) {
    Write-Host "  -> Criando issue: $($issue.title)" -ForegroundColor Cyan
    gh issue create --title $issue.title --body $issue.body --label $issue.label
}

Write-Host ""
Write-Host "[4/4] Pipeline 100% sincronizado com o GitHub!" -ForegroundColor Green
Write-Host "Acesse o seu repositório para criar o GitHub Project Kanban com 1 clique." -ForegroundColor Cyan
