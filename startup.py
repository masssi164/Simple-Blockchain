import subprocess
import os
import time

# Backend starten (Spring Boot Node)
backend_jar = os.path.join("blockchain-node", "build", "libs", "blockchain-node-0.0.1-SNAPSHOT.jar")
backend_cmd = ["java", "-jar", backend_jar]

print("Starte Backend...")
backend_proc = subprocess.Popen(backend_cmd)

# Kurze Wartezeit, damit das Backend initialisiert
print("Warte auf Backend-Initialisierung...")
time.sleep(5)

# Frontend starten (Vite)
frontend_dir = os.path.join("ui")
print("Starte Frontend (Vite)...")
frontend_proc = subprocess.Popen(["npm", "run", "dev"], cwd=frontend_dir)

print("Beide Prozesse laufen. Mit STRG+C beenden.")

try:
    backend_proc.wait()
    frontend_proc.wait()
except KeyboardInterrupt:
    print("\nBeende Prozesse...")
    backend_proc.terminate()
    frontend_proc.terminate()
    backend_proc.wait()
    frontend_proc.wait()
    print("Fertig.")
