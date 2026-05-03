#!/usr/bin/env bash

set -euo pipefail

git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/pre-push .githooks/commit-msg scripts/validate-commit-message.sh

echo "Git hooks configured. commit-msg, pre-commit and pre-push are active."
