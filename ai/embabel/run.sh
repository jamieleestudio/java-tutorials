#!/usr/bin/env bash
# Embabel 教程一键脚本（bash）
#
# 用法：
#   ./run.sh list                 列出所有模块与端口
#   ./run.sh docker               启动 Postgres + LiteLLM + Ollama，并拉取所需模型
#   ./run.sh chat                 启动指定模块（自动识别是否需要指向 LiteLLM）
#
# 与 run.ps1 等价；52 个模块、52 个端口、两种环境变量组合，手工敲很容易出错。
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODULE="${1:-list}"
SKIP_BUILD="${SKIP_BUILD:-}"

# 需要嵌入/视觉的模块：必须走 LiteLLM（DeepSeek 没有 embedding 接口）
LITELLM_MODULES="embabel-embeddings embabel-vector-store embabel-document-ingest embabel-memory embabel-multimodal"

list_modules() {
  printf "%-8s %-34s %s\n" "端口" "模块" "分类"
  for catdir in "$ROOT"/*/; do
    cat="$(basename "$catdir")"
    [ "$cat" = "docker" ] && continue
    for moddir in "$catdir"*/; do
      [ -d "$moddir" ] || continue
      name="$(basename "$moddir")"
      yml="$moddir/src/main/resources/application.yml"
      port="?"
      [ -f "$yml" ] && port="$(grep -oE 'port:[[:space:]]*[0-9]+' "$yml" | head -1 | grep -oE '[0-9]+' || echo '?')"
      printf "%-8s %-34s %s\n" "$port" "$name" "$cat"
    done
  done
  echo
  echo "用法：./run.sh <模块名>"
  echo "需要 Docker 的模块：embeddings / vector-store / document-ingest / memory / multimodal / persistence"
}

start_docker() {
  cd "$ROOT/docker"
  if [ ! -f .env ]; then
    cp .env.example .env
    echo "已生成 docker/.env —— 请填入 DEEPSEEK_API_KEY 后重跑"
  fi
  docker compose up -d
  echo "拉取 Ollama 模型（已存在会跳过）..."
  docker compose exec -T ollama ollama pull nomic-embed-text
  docker compose exec -T ollama ollama pull qwen2.5vl:3b
  echo "Docker 组件就绪：LiteLLM :4000 / Ollama :11434 / Postgres :5433"
}

start_module() {
  local name="$1"
  local moddir
  moddir="$(find "$ROOT" -maxdepth 2 -type d -name "$name" | head -1)"
  if [ -z "$moddir" ]; then
    echo "找不到模块 '$name'。用 ./run.sh list 查看全部。" >&2
    exit 1
  fi

  if echo " $LITELLM_MODULES " | grep -q " $name "; then
    export OPENAI_BASE_URL="http://localhost:4000"
    export OPENAI_API_KEY="sk-1234"
    echo "已把 OPENAI_BASE_URL/OPENAI_API_KEY 指向 LiteLLM（本模块需要 embedding/视觉）"
  elif [ -z "${DEEPSEEK_API_KEY:-}" ] && [ -z "${OPENAI_API_KEY:-}" ]; then
    echo "提示：未检测到 DEEPSEEK_API_KEY / OPENAI_API_KEY 环境变量。"
  fi

  if [ -z "$SKIP_BUILD" ]; then
    (cd "$ROOT" && mvn -q -pl ":$name" package -DskipTests)
  fi

  local jar
  jar="$(find "$moddir/target" -maxdepth 1 -name "$name-*.jar" ! -name '*original' | head -1)"
  if [ -z "$jar" ]; then
    echo "未找到 $name 的 jar，请先构建（不要设置 SKIP_BUILD）。" >&2
    exit 1
  fi
  echo "启动 $name ..."
  exec java -jar "$jar"
}

case "$MODULE" in
  list)   list_modules ;;
  docker) start_docker ;;
  *)      start_module "$MODULE" ;;
esac
