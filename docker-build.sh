#!/usr/bin/env bash
# Builds the debug APK in Docker and drops it in ./out/app-debug.apk
# Requires Docker with BuildKit (default on modern Docker) and internet access.
set -euo pipefail

mkdir -p out
DOCKER_BUILDKIT=1 docker build --target export --output type=local,dest=./out .

echo ""
echo "Done. APK is at: ./out/app-debug.apk"
echo "Copy it to an Android phone and tap it to install (allow 'install unknown apps')."
