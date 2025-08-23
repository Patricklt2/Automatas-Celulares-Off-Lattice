#!/bin/bash

SRC_DIR="simulation/src"
OUT_DIR="simulation/out"

mkdir -p "$OUT_DIR"

javac -d "$OUT_DIR" "$SRC_DIR"/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful. Running program..."
    java -cp "$OUT_DIR" FrontEndGui
else
    echo "Compilation failed."
fi