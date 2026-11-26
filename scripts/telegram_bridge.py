import os
import re
import sys
import time
import base64
import logging
from datetime import datetime
from pathlib import Path
import requests

# Configurações do Bot & APIs
TOKEN = "8269078374:AAEnVBu1jbVZf5sfL5brg7GEia---yeV6gE"
AUTHORIZED_CHAT_ID = 1178791446
BASE_URL = f"https://api.telegram.org/bot{TOKEN}"
FILE_BASE_URL = f"https://api.telegram.org/file/bot{TOKEN}"

OPENROUTER_KEY = os.getenv("OPENROUTER_API_KEY", "")
# Carrega de autobot_v2/.env se não estiver no ambiente
if not OPENROUTER_KEY:
    try:
        env_path = Path("D:/-Arquivos/Miqueias/Next wave/Projetos/autobot_v2/.env")
        if env_path.exists():
            for line in env_path.read_text(encoding="utf-8").splitlines():
                if line.startswith("OPENROUTER_API_KEY="):
                    OPENROUTER_KEY = line.split("=", 1)[1].strip()
                    break
    except Exception:
        pass
OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"
VISION_MODEL = "nvidia/nemotron-3.5-super-vl:free"
POLLINATIONS_URL = "https://text.pollinations.ai/"

MEMORY_FILE = Path("C:/Users/mique/.memory/MEMORY.md")
STORAGE_DIR = Path("D:/-Arquivos/Miqueias/Next wave/Projetos/open-brain/storage/telegram_media")
STORAGE_DIR.mkdir(parents=True, exist_ok=True)

LOG_FILE = Path("D:/-Arquivos/Miqueias/Next wave/Projetos/open-brain/telegram_chat_history.md")

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")

KEYBOARD_MARKUP = {
    "keyboard": [
        [{"text": "🛑 Encerrar Ponte"}, {"text": "📊 Status OpenBrain"}],
        [{"text": "💡 Dica Termux / Rish"}, {"text": "🧠 Resumo da Pauta"}]
    ],
    "resize_keyboard": True,
    "persistent": True
}

def load_system_memory():
    """Carrega dinamicamente a memória persistente do NextWave / MEMORY.md"""
    memory_text = ""
    try:
        if MEMORY_FILE.exists():
            with open(MEMORY_FILE, "r", encoding="utf-8") as f:
                memory_text = f.read()[:3500]
    except Exception as e:
        logging.warning(f"Erro ao ler MEMORY.md: {e}")
        
    return f"""Você é o Co-Piloto da Next Wave (Tech Lead & Arquiteto de IA), conversando diretamente com o Miquéias (Fundador da Next Wave / @MrKiller047) via Telegram enquanto ele está longe do PC.

Você tem ACESSO DIRETO à Memória Persistente do Sistema (NextWaveBrain):
--- MEMÓRIA PERSISTENTE OFICIAL ---
{memory_text}
-----------------------------------

Situação Exata do Projeto OpenBrain & APKs:
- Versão 1: `OpenBrain_v0.1.apk` (debug inicial com todas as permissões).
- Versão 2: `OpenBrain_Release.apk` (versão podada errada que o agente anterior gerou ao tentar agradar o Play Protect. NÃO deve ser usada).
- Versão 3 (ATUAL & DEFINITIVA): `OpenBrain_v0.1_Soberano.apk` (4.66 MB). Esta é a versão correta oficial!
  * Tem 100% dos poderes e permissões brutas restauradas: WRITE_SETTINGS, CALL_PHONE, Storage, Acessibilidade, Shizuku, Câmera, Notificações.
  * Assinada com a nossa Keystore oficial (openbrain.keystore).
  * Como instalar: com o Play Protect desativado na Play Store (ou forçando via Termux com 'rish -> pm install -r -d -g /sdcard/Download/OpenBrain_v0.1_Soberano.apk').

Regras Sagradas:
- Soberania Total: Não podamos funções para agradar Google.
- Diálogo Antes de Codar: Sempre debater e alinhar com o Miquéias antes de alterações.
- Você é capaz de analisar fotos/prints e links da web em tempo real.
- Responda sempre em português, com tom de liderança técnica, direto, seguro e conciso.
"""

conversation_history = [
    {"role": "system", "content": load_system_memory()}
]

def register_bot_commands():
    url = f"{BASE_URL}/setMyCommands"
    commands = [
        {"command": "start", "description": "Menu principal e atalhos"},
        {"command": "status", "description": "Status do OpenBrain e da Ponte"},
        {"command": "encerrar", "description": "Desligar e fechar a ponte no PC"}
    ]
    try:
        requests.post(url, json={"commands": commands}, timeout=10)
    except Exception as e:
        logging.warning(f"Erro ao registrar comandos: {e}")

def send_telegram_message(chat_id, text, reply_markup=KEYBOARD_MARKUP):
    url = f"{BASE_URL}/sendMessage"
    payload = {
        "chat_id": chat_id,
        "text": text,
        "parse_mode": "Markdown",
        "reply_markup": reply_markup
    }
    try:
        r = requests.post(url, json=payload, timeout=15)
        if not r.json().get("ok"):
            payload.pop("parse_mode", None)
            requests.post(url, json=payload, timeout=15)
    except Exception as e:
        logging.error(f"Erro ao enviar mensagem: {e}")

def send_chat_action(chat_id, action="typing"):
    try:
        requests.post(f"{BASE_URL}/sendChatAction", json={"chat_id": chat_id, "action": action}, timeout=5)
    except Exception:
        pass

def log_interaction(sender, message):
    try:
        now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        with open(LOG_FILE, "a", encoding="utf-8") as f:
            f.write(f"\n**[{now}] {sender}:**\n{message}\n\n---\n")
    except Exception as e:
        logging.error(f"Erro ao registrar log: {e}")

def download_telegram_file(file_id, ext="dat"):
    try:
        r = requests.get(f"{BASE_URL}/getFile?file_id={file_id}", timeout=15).json()
        if r.get("ok"):
            file_path = r["result"]["file_path"]
            download_url = f"{FILE_BASE_URL}/{file_path}"
            
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            local_name = f"media_{timestamp}.{ext}"
            local_path = STORAGE_DIR / local_name
            
            content = requests.get(download_url, timeout=30).content
            with open(local_path, "wb") as f:
                f.write(content)
                
            logging.info(f"Arquivo salvo em {local_path} ({len(content)} bytes)")
            return local_path, content
    except Exception as e:
        logging.error(f"Erro ao baixar arquivo do Telegram: {e}")
    return None, None

def scrape_link(url):
    try:
        headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"}
        resp = requests.get(url, headers=headers, timeout=8)
        if resp.status_code == 200:
            html = resp.text
            text = re.sub(r'<script.*?</script>', '', html, flags=re.DOTALL)
            text = re.sub(r'<style.*?</style>', '', html, flags=re.DOTALL)
            text = re.sub(r'<[^>]+>', ' ', text)
            clean_text = ' '.join(text.split())[:2000]
            return f"\n\n[Conteúdo extraído da URL {url}]:\n{clean_text}\n"
    except Exception as e:
        logging.warning(f"Não foi possível raspar o link {url}: {e}")
    return ""

def ask_llm(user_content, is_multimodal=False):
    # Recarrega a memória viva no system prompt
    conversation_history[0]["content"] = load_system_memory()
    
    if is_multimodal:
        conversation_history.append({"role": "user", "content": user_content})
    else:
        conversation_history.append({"role": "user", "content": user_content})
        
    recent_messages = [conversation_history[0]] + conversation_history[-10:]
    
    try:
        headers = {
            "Authorization": f"Bearer {OPENROUTER_KEY}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": VISION_MODEL,
            "messages": recent_messages
        }
        r = requests.post(OPENROUTER_URL, headers=headers, json=payload, timeout=50)
        if r.status_code == 200:
            data = r.json()
            reply = data["choices"][0]["message"]["content"].strip()
            conversation_history.append({"role": "assistant", "content": reply})
            return reply
        else:
            logging.warning(f"OpenRouter falhou ({r.status_code}): {r.text[:100]}")
    except Exception as e:
        logging.warning(f"OpenRouter error: {e}")

    if not is_multimodal:
        try:
            payload = {
                "messages": recent_messages,
                "model": "openai"
            }
            r = requests.post(POLLINATIONS_URL, json=payload, timeout=30)
            if r.status_code == 200:
                reply = r.text.strip()
                conversation_history.append({"role": "assistant", "content": reply})
                return reply
        except Exception as e:
            logging.error(f"Pollinations fallback error: {e}")
            
    return "⚠️ Tive uma instabilidade momentânea na conexão de IA, mas o arquivo foi salvo com segurança no PC!"

def main():
    logging.info("Iniciando Telegram Bridge com Memória Sincronizada...")
    register_bot_commands()
    
    offset = None
    
    while True:
        try:
            url = f"{BASE_URL}/getUpdates?timeout=25"
            if offset:
                url += f"&offset={offset}"
                
            resp = requests.get(url, timeout=35).json()
            
            if resp.get("ok"):
                for update in resp.get("result", []):
                    offset = update["update_id"] + 1
                    
                    message = update.get("message")
                    if not message:
                        continue
                        
                    chat_id = message.get("chat", {}).get("id")
                    if chat_id != AUTHORIZED_CHAT_ID:
                        continue
                        
                    user_text = message.get("text", "").strip()
                    caption = message.get("caption", "").strip()
                    effective_text = user_text or caption

                    # Comandos Rápidos
                    if effective_text in ["🛑 Encerrar Ponte", "/encerrar", "/stop", "/fechar"]:
                        bye_msg = (
                            "🛑 **Ponte Telegram Encerrada!**\n\n"
                            "O processo no PC foi desligado. Todo o histórico foi preservado no Disco D.\n"
                            "Até breve, Miquéias!"
                        )
                        send_telegram_message(chat_id, bye_msg, reply_markup={"remove_keyboard": True})
                        log_interaction("Sistema", "Ponte encerrada a pedido do usuário.")
                        sys.exit(0)

                    if effective_text in ["📊 Status OpenBrain", "/status"]:
                        send_telegram_message(chat_id, (
                            "📊 **Status OpenBrain Oficial:**\n\n"
                            "• **Versão Correta**: `OpenBrain_v0.1_Soberano.apk` (4.66 MB assinado com keystore oficial e permissões brutas).\n"
                            "• **Repositório**: Sincronizado no GitHub (`master` autoria MiqueiasL).\n"
                            "• **Ponte**: Sincronizada com `MEMORY.md` e multimodalidade ativa."
                        ))
                        continue

                    if effective_text == "💡 Dica Termux / Rish":
                        send_telegram_message(chat_id, (
                            "💡 **Comando de Instalação Forçada (Termux + Rish):**\n\n"
                            "```bash\nrish\npm install -r -d -g /sdcard/Download/OpenBrain_v0.1_Soberano.apk\n```\n\n"
                            "*(Ou clique direto no APK com o Play Protect desativado na Play Store)*."
                        ))
                        continue

                    # Processamento de Imagens
                    if "photo" in message:
                        send_chat_action(chat_id, "upload_photo")
                        photo_id = message["photo"][-1]["file_id"]
                        local_path, img_bytes = download_telegram_file(photo_id, ext="jpg")
                        
                        prompt_text = caption if caption else "Por favor, analise este print/imagem em detalhes e me dê a orientação técnica exata."
                        log_interaction("Miquéias [FOTO ENVIADA]", f"Salvo em: {local_path} | Prompt: {prompt_text}")
                        
                        b64_img = base64.b64encode(img_bytes).decode("utf-8")
                        data_url = f"data:image/jpeg;base64,{b64_img}"
                        
                        user_content = [
                            {"type": "text", "text": prompt_text},
                            {"type": "image_url", "image_url": {"url": data_url}}
                        ]
                        
                        send_chat_action(chat_id, "typing")
                        reply = ask_llm(user_content, is_multimodal=True)
                        log_interaction("Co-Piloto [VISÃO]", reply)
                        send_telegram_message(chat_id, reply)
                        continue

                    # Áudios
                    if "voice" in message or "audio" in message:
                        send_chat_action(chat_id, "record_audio")
                        media_obj = message.get("voice") or message.get("audio")
                        local_path, _ = download_telegram_file(media_obj["file_id"], ext="ogg")
                        msg = f"🎙️ **Áudio salvo no PC:** `{local_path.name}` no Disco D."
                        log_interaction("Miquéias [ÁUDIO]", f"Salvo em: {local_path}")
                        send_telegram_message(chat_id, msg)
                        continue

                    # Mensagens de Texto e Links
                    if user_text:
                        log_interaction("Miquéias", user_text)
                        
                        urls = re.findall(r'https?://[^\s]+', user_text)
                        extra_web_content = ""
                        for u in urls:
                            send_chat_action(chat_id, "find_location")
                            logging.info(f"Lendo conteúdo da URL: {u}")
                            extra_web_content += scrape_link(u)
                            
                        final_prompt = user_text + extra_web_content
                        
                        send_chat_action(chat_id, "typing")
                        reply = ask_llm(final_prompt, is_multimodal=False)
                        log_interaction("Co-Piloto", reply)
                        send_telegram_message(chat_id, reply)

            time.sleep(1)
        except SystemExit:
            break
        except Exception as e:
            logging.error(f"Erro no loop do Telegram: {e}")
            time.sleep(5)

if __name__ == "__main__":
    main()
