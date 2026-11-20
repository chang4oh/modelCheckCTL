#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build
javac -encoding UTF-8 -d build Main.java model/*.java controller/*.java view/*.java util/*.java
printf 'Built successfully. GUI: java -cp build modelCheckCTL.Main --gui\n'
