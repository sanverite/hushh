#!/bin/sh
# Regenerate design/exports/ from design/hushh.figcraft.
# Each screen page has a dark row and a light copy with the same frame names;
# figcraft-cli writes the light copies as "<name> 2.png", which we sort into light/.
set -e
cd "$(dirname "$0")"
rm -rf exports
for pair in "Android 360:android" "iPhone 390:iphone"; do
  page=${pair%%:*}; dir=exports/${pair##*:}
  mkdir -p "$dir/dark" "$dir/light"
  figcraft-cli export --page "$page" --out "$dir" hushh.figcraft >/dev/null
  for f in "$dir"/*" 2.png"; do mv "$f" "$dir/light/$(basename "$f" " 2.png").png"; done
  mv "$dir"/*.png "$dir/dark/"
done
echo "exported $(find exports -name '*.png' | wc -l | tr -d ' ') screens"
