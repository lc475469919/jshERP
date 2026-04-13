#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

stop_from_pid_file() {
  local name="$1"
  local pid_file="$2"

  if [ ! -f "$pid_file" ]; then
    echo "$name is not running: missing $pid_file"
    return
  fi

  local pid
  pid="$(cat "$pid_file")"
  if [ -z "$pid" ]; then
    echo "$name is not running: empty $pid_file"
    return
  fi

  if kill -0 "$pid" 2>/dev/null; then
    echo "Stopping $name, PID $pid"
    kill "$pid"
  else
    echo "$name process $pid is not running"
  fi
}

stop_from_pid_file "frontend" "$ROOT_DIR/frontend.pid"
stop_from_pid_file "backend" "$ROOT_DIR/backend.pid"
