#!/bin/bash

# OpenBrain CLI Launcher (Linux / Kali Linux / macOS / Android Termux)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if command -v node >/dev/null 2>&1; then
  node packages/cli/bin/openbrain.js "$@"
elif command -v bun >/dev/null 2>&1; then
  bun packages/cli/bin/openbrain.js "$@"
else
  echo "[OpenBrain] Erro: Node.js ou Bun não encontrado no PATH."
  exit 1
fi
