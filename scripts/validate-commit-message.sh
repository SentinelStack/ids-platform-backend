#!/usr/bin/env bash

set -euo pipefail

usage() {
  cat <<'USAGE' >&2
Usage:
  validate-commit-message.sh --file <path>
  validate-commit-message.sh --message "<subject>"
USAGE
}

subject=""

case "${1:-}" in
  --file)
    if [[ -z "${2:-}" ]]; then
      usage
      exit 2
    fi
    subject="$(head -n 1 "$2" | tr -d '\r')"
    ;;
  --message)
    if [[ -z "${2:-}" ]]; then
      usage
      exit 2
    fi
    subject="$2"
    ;;
  *)
    usage
    exit 2
    ;;
esac

# Allow Git-generated merge commits.
if [[ "$subject" =~ ^Merge[[:space:]] ]]; then
  exit 0
fi

# Allow Git-generated reverts.
if [[ "$subject" =~ ^Revert[[:space:]]\" ]]; then
  exit 0
fi

pattern='^(feat|fix|chore|docs|style|refactor|perf|test|build|ci|revert)(\([a-z0-9._/-]+\))?(!)?: .+'

if [[ ! "$subject" =~ $pattern ]]; then
  cat <<EOF >&2
Invalid commit message:
  $subject

Expected Conventional Commit format:
  <type>(optional-scope): <description>

Allowed types:
  feat, fix, chore, docs, style, refactor, perf, test, build, ci, revert

Examples:
  feat(alert): add alert filter by protocol
  chore: update dependencies
EOF
  exit 1
fi

