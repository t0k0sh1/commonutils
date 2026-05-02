#!/usr/bin/env bash
# Cursor `stop` hook: run ./gradlew check when the agent run ends with status "completed".
# Logs go to stderr; stdout must be JSON for Cursor.
# Disable temporarily: SKIP_GRADLE_CHECK_ON_STOP=1 (e.g. in shell profile — not read by Cursor by default).

set -euo pipefail

INPUT=$(cat || true)
if [[ -n "${SKIP_GRADLE_CHECK_ON_STOP:-}" ]]; then
  echo '{}'
  exit 0
fi

STATUS="$(
  printf '%s' "$INPUT" | python3 -c 'import json,sys
try:
  d=json.load(sys.stdin)
  print(d.get("status") or "")
except Exception:
  print("")
' 2>/dev/null || echo ""
)"

if [[ "$STATUS" != "completed" ]]; then
  echo '{}'
  exit 0
fi

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

set +e
./gradlew check --no-daemon >&2
set -e

echo '{}'
exit 0
