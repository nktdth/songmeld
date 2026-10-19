#!/usr/bin/env bash
# Pure Kotlin catalog tests with actual models, parser, search and bundled TSV assets.
set -euo pipefail
kotlin_distribution=${1:?Usage: bash tools/check_icon_catalog.sh /path/to/kotlinc}
repo_root=$(cd "$(dirname "$0")/.." && pwd)
check_dir=$(mktemp -d)
trap 'rm -rf "$check_dir"' EXIT
java -cp "$kotlin_distribution/lib/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -kotlin-home "$kotlin_distribution" \
  -classpath "$kotlin_distribution/lib/kotlinx-coroutines-core-jvm.jar" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/ui/icons/IconModels.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/ui/icons/IconCatalogQuery.kt" \
  "$repo_root/tools/tests/IconCatalogCheck.kt" -d "$check_dir/checks.jar"
java -cp "$check_dir/checks.jar:$kotlin_distribution/lib/*" \
  moe.ktandth.songmeld.ui.icons.IconCatalogCheckKt "$repo_root/app/src/main/assets/icon_catalog"
