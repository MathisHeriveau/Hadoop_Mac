@echo off
setlocal enabledelayedexpansion

echo 🚀 Installation complète Hadoop + WordCount — MODE AUTOMATIQUE

REM ================================
REM 0) Vérifs de base
REM ================================
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Docker n'est pas installé.
    exit /b 1
)

docker --help | findstr /I "compose" >nul
if %errorlevel% neq 0 (
    echo ❌ Docker Compose n'est pas installé.
    exit /b 1
)

echo ✔️ Docker OK

REM ================================
REM 1) Lancer Hadoop Cluster
REM ================================
echo 🐳 Lancement du cluster Hadoop...
docker compose up -d

echo ⏳ Attente 5 sec que les conteneurs Hadoop démarrent...
timeout /t 5 >nul

REM ================================
REM 2) Lancer Dev Container
REM ================================
echo 🐧 Lancement du conteneur Dev...
docker compose -f docker-compose.dev.yml up -d

timeout /t 3 >nul

REM ================================
REM 3) Installer Java dans Dev
REM ================================
echo ☕ Installation de Java dans le conteneur dev...

docker exec -i hadoop-dev bash -c "apt update -y && apt install -y openjdk-11-jdk && javac -version"

echo ✔️ Java installé

endlocal
exit /b 0
