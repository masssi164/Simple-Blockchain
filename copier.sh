# export_project_files.sh
TMPDIR=$(mktemp -d)
OUTFILE="$TMPDIR/export.txt"

append_file() {
  local src="$1"
  local relpath="$2"
  echo "// filepath: $relpath" >> "$OUTFILE"
  cat "$src" >> "$OUTFILE"
  echo -e "\n" >> "$OUTFILE"
}

find blockchain-node/src/main -type f -name "*.java" ! -path "*/test/*" | while read -r file; do
  relpath="${file#blockchain-node/}"
  append_file "$file" "blockchain-node/$relpath"
done

find blockchain-core/src/main -type f -name "*.java" ! -path "*/test/*" | while read -r file; do
  relpath="${file#blockchain-core/}"
  append_file "$file" "blockchain-core/$relpath"
done

find blockchain-ui/src \( -name "*.ts" -o -name "*.tsx" \) ! -path "*/__tests__/*" 2>/dev/null | while read -r file; do
  relpath="${file#blockchain-ui/}"
  append_file "$file" "blockchain-ui/$relpath"
done

for mod in blockchain-node blockchain-core blockchain-ui; do
  if [ -f "$mod/build.gradle" ]; then
    append_file "$mod/build.gradle" "$mod/build.gradle"
  fi
done

cat "$OUTFILE" | pbcopy
rm -rf "$TMPDIR"
echo "Fertig! Exportierte Dateien sind in der Zwischenablage."