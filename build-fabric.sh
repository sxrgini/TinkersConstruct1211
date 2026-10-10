#!/bin/sh
# Builds the Fabric 1.21.1 port (needs JDK 21). The root gradlew is the old Forge 1.20.1 build, do not use it.
# Usage: ./build-fabric.sh [tconstruct task, default: build]   e.g. ./build-fabric.sh runClient
set -e
cd "$(dirname "$0")"
(cd mantle-fabric && ./gradlew remapJar)
(cd tconstruct-fabric && ./gradlew "${1:-build}")
