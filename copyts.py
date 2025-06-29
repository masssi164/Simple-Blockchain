import os
import pyperclip

src_dir = os.path.join(os.getcwd(), "ui", "src")
tsx_contents = []

for root, dirs, files in os.walk(src_dir):
    # Testverzeichnisse überspringen
    if '__tests__' in root.lower():
        continue
    for file in files:
        # Nur .ts und .tsx, aber keine Testdateien
        if (file.endswith(".ts") or file.endswith(".tsx")) and not file.endswith(".test.ts") and not file.endswith(".test.tsx"):
            file_path = os.path.join(root, file)
            rel_path = os.path.relpath(file_path, src_dir)
            with open(file_path, "r", encoding="utf-8") as f:
                tsx_contents.append(f"// {rel_path}\n" + f.read())

all_tsx = "\n\n".join(tsx_contents)
pyperclip.copy(all_tsx)
print("Alle TypeScript/TSX-Dateien (ohne Tests) ab 'ui/src' wurden in die Zwischenablage kopiert.")
