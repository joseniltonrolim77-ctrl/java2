#!/bin/bash
# Script de demonstração manual da API para apresentação (equivalente ao antigo scripts/demo.js).
#
# Como usar:
#   1) Em um terminal: mvn spring-boot:run   (ou docker compose up --build)
#   2) Em outro terminal: bash scripts/demo.sh
#
# Chama, em sequência, todos os endpoints implementados contra o servidor real e imprime
# no console a requisição enviada e a resposta recebida da API. Requer `curl`; usa `python3`
# ou `jq` (se disponíveis) apenas para formatar o JSON de saída — não é obrigatório.

set -u

BASE_URL="${DEMO_BASE_URL:-http://localhost:${SERVER_PORT:-3577}/api}"

GREEN='\033[0;32m'
RED='\033[0;31m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
BOLD='\033[1m'
RESET='\033[0m'

pretty() {
  if command -v jq >/dev/null 2>&1; then
    jq . 2>/dev/null || cat
  elif command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool 2>/dev/null || cat
  else
    cat
  fi
}

call() {
  local method="$1"; local path="$2"; local body="${3:-}"
  echo -e "\n${BOLD}${CYAN}-> ${method} ${path}${RESET}"

  local status body_file
  body_file=$(mktemp)

  if [ -n "$body" ]; then
    echo -e "${CYAN}Body enviado:${RESET}"
    echo "$body" | pretty
    status=$(curl -s -o "$body_file" -w '%{http_code}' -X "$method" "${BASE_URL}${path}" \
      -H 'Content-Type: application/json' -d "$body")
  else
    status=$(curl -s -o "$body_file" -w '%{http_code}' -X "$method" "${BASE_URL}${path}")
  fi

  if [ -z "$status" ] || [ "$status" = "000" ]; then
    echo -e "${RED}Não foi possível conectar em ${BASE_URL}.${RESET}"
    echo -e "${YELLOW}Certifique-se de que o servidor está rodando (mvn spring-boot:run) antes de executar o demo.${RESET}"
    rm -f "$body_file"
    exit 1
  fi

  local color="$RED"
  [ "$status" -ge 200 ] && [ "$status" -lt 300 ] && color="$GREEN"
  echo -e "${color}<- Status ${status}${RESET}"
  echo "Resposta:"
  cat "$body_file" | pretty
  LAST_BODY_FILE="$body_file"
}

json_field() {
  local file="$1"; local field="$2"
  if command -v python3 >/dev/null 2>&1; then
    python3 -c "import json,sys; d=json.load(open('$file')); print(d.get('$field',''))" 2>/dev/null
  else
    grep -o "\"$field\":[0-9]*" "$file" | head -1 | cut -d: -f2
  fi
}

echo -e "${BOLD}\n=== Demonstracao da DevShowcase API (${BASE_URL}) ===${RESET}"

echo -e "\n${BOLD}${YELLOW}--- 1) Profiles - cadastro e busca por id ---${RESET}"
TS=$(date +%s)
call POST /profiles "{\"name\":\"Ana Souza\",\"email\":\"ana.${TS}@example.com\",\"bio\":\"Dev backend apaixonada por APIs\",\"avatarUrl\":\"https://example.com/ana.png\"}"
PROFILE_ID=$(json_field "$LAST_BODY_FILE" id)
call GET "/profiles/${PROFILE_ID}"

echo -e "\n${BOLD}${YELLOW}--- 2) Technologies - cadastro e listagem ---${RESET}"
TECH_NAME="Node.js-${TS}"
call POST /technologies "{\"name\":\"${TECH_NAME}\"}"
TECH_ID=$(json_field "$LAST_BODY_FILE" id)
call GET /technologies

echo -e "\n${BOLD}${YELLOW}--- 3) Projects - cadastro (vinculando profile + technology) e listagem ---${RESET}"
call POST /projects "{\"title\":\"DevShowcase API\",\"description\":\"Backend do projeto apresentado em aula\",\"repositoryUrl\":\"https://github.com/ana/devshowcase\",\"profileId\":${PROFILE_ID},\"technologyIds\":[${TECH_ID}]}"
PROJECT_ID=$(json_field "$LAST_BODY_FILE" id)
call GET /projects

echo -e "\n${BOLD}${YELLOW}--- 4) Relacionamento Profile 1:N Project ---${RESET}"
call GET "/profiles/${PROFILE_ID}"

echo -e "\n${BOLD}${YELLOW}--- 5) Feedbacks - nota (1 a 5) + comentario ---${RESET}"
call POST "/projects/${PROJECT_ID}/feedbacks" '{"rating":5,"comment":"Excelente projeto!"}'
call POST "/projects/${PROJECT_ID}/feedbacks" '{"rating":3,"comment":"Bom, mas pode melhorar."}'

echo -e "\n${BOLD}${YELLOW}--- 6) Upvote ---${RESET}"
call PUT "/projects/${PROJECT_ID}/upvote"

echo -e "\n${BOLD}${YELLOW}--- 7) Filtragem por tecnologia e paginacao ---${RESET}"
call GET "/projects?technology=${TECH_NAME}&page=1&limit=5"

echo -e "\n${BOLD}${YELLOW}--- 8) Validacao de DTOs - exemplos de erro (400) ---${RESET}"
call POST /profiles '{"email":"nao-e-um-email-valido"}'
call POST /projects '{"title":"","repositoryUrl":"nao-e-uma-url","profileId":999999}'
call POST "/projects/${PROJECT_ID}/feedbacks" '{"rating":10,"comment":"Nota invalida"}'

echo -e "${BOLD}${GREEN}\n=== Demonstracao concluida ===\n${RESET}"
