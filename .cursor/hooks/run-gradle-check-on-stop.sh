#!/usr/bin/env bash
# Cursor `stop` hook: run ./gradlew check javadoc when the agent run ends with status "completed".
# Runs javadoc unconditionally (no need to infer whether docs changed); output goes to tracked docs/.
# Gradle output goes to stderr; stdout is always `{}` (valid JSON for Cursor). Exits with Gradle's
# exit code so a failed check marks the hook as failed (use failClosed on the hook entry).
# Disable temporarily: SKIP_GRADLE_CHECK_ON_STOP=1 (not read by Cursor by default).

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
./gradlew check javadoc --no-daemon >&2
gradle_exit=$?
set -e

echo '{}'
exit "$gradle_exit"
