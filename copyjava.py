import os
import pyperclip

src_dir = os.getcwd()
java_contents = []

for root, dirs, files in os.walk(src_dir):
    # Verzeichnisse mit 'test' im Namen überspringen
    if 'test' in root.split(os.sep):
        continue
    for file in files:
        if file.endswith(".java"):
            file_path = os.path.join(root, file)
            rel_path = os.path.relpath(file_path, src_dir)
            with open(file_path, "r", encoding="utf-8") as f:
                java_contents.append(f"// {rel_path}\n" + f.read())

all_java = "\n\n".join(java_contents)
pyperclip.copy(all_java)
print("Alle Java-Dateien wurden in die Zwischenablage kopiert (mit relativem Pfad, ohne Testdateien).")