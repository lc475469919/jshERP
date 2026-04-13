#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$ROOT_DIR/jshERP-boot"
FRONTEND_DIR="$ROOT_DIR/jshERP-web"
JAVA_BIN="${JAVA_BIN:-java}"

BACKEND_LOG="$ROOT_DIR/backend.log"
FRONTEND_LOG="$ROOT_DIR/frontend.log"
BACKEND_PID_FILE="$ROOT_DIR/backend.pid"
FRONTEND_PID_FILE="$ROOT_DIR/frontend.pid"

is_running() {
  local pid_file="$1"
  if [ ! -f "$pid_file" ]; then
    return 1
  fi

  local pid
  pid="$(cat "$pid_file")"
  [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null
}

start_backend() {
  if is_running "$BACKEND_PID_FILE"; then
    echo "Backend already running, PID $(cat "$BACKEND_PID_FILE")"
    return
  fi

  if [ ! -f "$BACKEND_DIR/target/jshERP.jar" ]; then
    echo "Backend jar not found. Build it first:"
    echo "  cd $BACKEND_DIR && mvn package -DskipTests"
    exit 1
  fi

  echo "Starting backend on http://localhost:9999/jshERP-boot ..."
  (
    cd "$BACKEND_DIR"
    nohup "$JAVA_BIN" -jar target/jshERP.jar > "$BACKEND_LOG" 2>&1 &
    echo $! > "$BACKEND_PID_FILE"
  )
  echo "Backend PID: $(cat "$BACKEND_PID_FILE")"
  echo "Backend log: $BACKEND_LOG"
}

start_frontend() {
  if is_running "$FRONTEND_PID_FILE"; then
    echo "Frontend already running, PID $(cat "$FRONTEND_PID_FILE")"
    return
  fi

  echo "Starting frontend on http://localhost:3000/ ..."
  (
    cd "$FRONTEND_DIR"
    nohup npm run serve > "$FRONTEND_LOG" 2>&1 &
    echo $! > "$FRONTEND_PID_FILE"
  )
  echo "Frontend PID: $(cat "$FRONTEND_PID_FILE")"
  echo "Frontend log: $FRONTEND_LOG"
}

start_backend
start_frontend

echo
echo "Local jshERP is starting."
echo "Backend:  http://localhost:9999/jshERP-boot"
echo "Frontend: http://localhost:3000/"
echo "Mini program default API: http://localhost:9999/jshERP-boot"
echo
echo "If startup fails, inspect:"
echo "  tail -100 $BACKEND_LOG"
echo "  tail -100 $FRONTEND_LOG"
