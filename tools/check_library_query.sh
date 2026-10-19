#!/usr/bin/env bash
# Pure Kotlin checks using an existing Kotlin compiler distribution; no SDK or downloads.
set -euo pipefail
kotlin_distribution=${1:?Usage: bash tools/check_library_query.sh /path/to/kotlinc}
repo_root=$(cd "$(dirname "$0")/.." && pwd)
check_dir=$(mktemp -d)
trap 'rm -rf "$check_dir"' EXIT
# Song's URI is opaque to querySongs. Stub only that Android type for standalone JVM checks.
cat > "$check_dir/Uri.kt" <<'KOTLIN'
package android.net
class Uri { companion object { fun parse(@Suppress("UNUSED_PARAMETER") value: String): Uri = Uri() } }
KOTLIN
java -cp "$kotlin_distribution/lib/*" org.jetbrains.kotlin.cli.jvm.K2JVMCompiler \
  -kotlin-home "$kotlin_distribution" \
  -classpath "$kotlin_distribution/lib/kotlinx-coroutines-core-jvm.jar" \
  "$check_dir/Uri.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/data/model/Song.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/ui/songs/LibraryQuery.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/ui/songs/LibraryLoadState.kt" \
  "$repo_root/app/src/main/java/moe/ktandth/songmeld/data/local/ArtworkSampling.kt" \
  "$repo_root/tools/tests/LibraryQueryCheck.kt" \
  -d "$check_dir/checks.jar"
java -cp "$check_dir/checks.jar:$kotlin_distribution/lib/*" \
  moe.ktandth.songmeld.ui.songs.LibraryQueryCheckKt
