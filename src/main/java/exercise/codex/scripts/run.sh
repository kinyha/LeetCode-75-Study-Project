#!/usr/bin/env bash
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Usage: $0 <exercise_dir> [MainClass]" >&2
  echo "Example: $0 ex01_threads" >&2
  exit 1
fi

ROOT_DIR=$(cd "$(dirname "$0")/.." && pwd)
EX_DIR="$ROOT_DIR/$1"

if [ ! -d "$EX_DIR/src" ]; then
  echo "No src directory in $EX_DIR" >&2
  exit 2
fi

OUT_DIR="$EX_DIR/out"
rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

find "$EX_DIR/src" -name "*.java" -print0 | xargs -0 javac -d "$OUT_DIR"

MAIN_CLASS=${2:-Main}
cd "$OUT_DIR"
java "$MAIN_CLASS"

