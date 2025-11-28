@echo off
setlocal enabledelayedexpansion

REM ============================================
REM CONFIG
REM ============================================
set CONTAINER=namenode
set DEFAULT_INPUT_FILE=src\wordcount\file.txt
set JAR_NAME=wc.jar
set MAIN_CLASS=src.wordcountenseignant.WCDriver

set HDFS_INPUT=/data
set HDFS_OUTPUT=/output

set HEAD_COUNT=
set INPUT_FILES=

REM ============================================
REM PARSING ARGUMENTS
REM ============================================
:parse_args
if "%~1"=="" goto args_done

echo %1 | findstr /B "-container=" >nul && (
    set CONTAINER=%1:~11%
    shift
    goto parse_args
)

echo %1 | findstr /B "-jarName=" >nul && (
    set JAR_NAME=%1:~9%
    shift
    goto parse_args
)

echo %1 | findstr /B "-mainClass=" >nul && (
    set MAIN_CLASS=%1:~11%
    shift
    goto parse_args
)

echo %1 | findstr /B "-head=" >nul && (
    set HEAD_COUNT=%1:~6%
    shift
    goto parse_args
)

if "%~1"=="-h" (
    goto usage
)
if "%~1"=="--help" (
    goto usage
)

REM otherwise input file
set INPUT_FILES=!INPUT_FILES! "%~1"
shift
goto parse_args

:args_done

if "%INPUT_FILES%"=="" (
    set INPUT_FILES="%DEFAULT_INPUT_FILE%"
)

REM ============================================
REM VALIDATE FILES
REM ============================================
for %%F in (%INPUT_FILES%) do (
    if not exist %%F (
        echo ❌ File not found: %%F
        exit /b 1
    )
)

echo.
echo 🚀 Déploiement Hadoop Big Data
echo   Container: %CONTAINER%
echo   MainClass: %MAIN_CLASS%
echo   Jar:       %JAR_NAME%
echo   Inputs: %INPUT_FILES%
echo.

REM ============================================
REM 1) COPY SRC
REM ============================================
echo 📁 Copie du dossier src dans le conteneur...
docker cp src %CONTAINER%:/root/

REM ============================================
REM 2) COPY INPUT FILES
REM ============================================
for %%F in (%INPUT_FILES%) do (
    echo 📄 Copie de %%~nxF...
    docker cp %%F %CONTAINER%:/root/
)

REM ============================================
REM 3) COMPILE JAVA
REM ============================================
echo 🛠️ Compilation Java...
docker exec -i %CONTAINER% bash -c "rm -rf /root/build && mkdir -p /root/build && find /root/src -name '*.java' > /root/sources.txt && javac -cp \"\$(hadoop classpath)\" -d /root/build @/root/sources.txt"

REM ============================================
REM 4) BUILD JAR
REM ============================================
echo 📦 Création du JAR...
docker exec -i %CONTAINER% bash -c "jar -cvf %JAR_NAME% -C /root/build ."

REM ============================================
REM 5) CLEAN OUTPUT
REM ============================================
echo 🗑️ Suppression de /output...
docker exec -i %CONTAINER% hdfs dfs -rm -r -f %HDFS_OUTPUT%

REM ============================================
REM 6) UPLOAD INPUTS
REM ============================================
echo 📁 Upload dans HDFS...
docker exec -i %CONTAINER% hdfs dfs -mkdir -p %HDFS_INPUT%

for %%F in (%INPUT_FILES%) do (
    echo   → Upload %%~nxF
    docker exec -i %CONTAINER% hdfs dfs -put -f "/root/%%~nxF" %HDFS_INPUT%
)

REM BUILD HDFS ARGS
set HDFS_ARGS=
for %%F in (%INPUT_FILES%) do (
    set HDFS_ARGS=!HDFS_ARGS! %HDFS_INPUT%/%%~nxF
)

echo.
echo ⚙️ Arguments Hadoop :
echo    %HDFS_ARGS%
echo.

REM ============================================
REM 7) RUN JOB
REM ============================================
echo 🚀 Exécution Hadoop...
docker exec -i %CONTAINER% bash -c "hadoop jar %JAR_NAME% %MAIN_CLASS% %HDFS_ARGS% %HDFS_OUTPUT%"

REM ============================================
REM 8) OUTPUT
REM ============================================
echo 📊 Résultat :

docker exec -i %CONTAINER% hdfs dfs -ls %HDFS_OUTPUT% > temp_ls.txt

set LAST_OUTPUT_DIR=
for /f "tokens=5" %%A in (temp_ls.txt) do (
    set LAST_OUTPUT_DIR=%%A
)

if "%LAST_OUTPUT_DIR%"=="" (
    echo ❌ Aucun dossier output trouvé.
    exit /b 1
)

echo 📁 Dossier final : %LAST_OUTPUT_DIR%

if "%HEAD_COUNT%"=="" (
    docker exec -i %CONTAINER% hdfs dfs -cat "%LAST_OUTPUT_DIR%/part-r-00000"
) else (
    docker exec -i %CONTAINER% bash -c "hdfs dfs -cat \"%LAST_OUTPUT_DIR%/part-r-00000\" | head -n %HEAD_COUNT%"
)

del temp_ls.txt

exit /b 0

:usage
echo Usage : run_hadoop.bat [options] [input_files...]
echo.
exit /b 0
