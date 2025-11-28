#!/bin/bash

set -e

# === CONFIG ===
CONTAINER="namenode"
DEFAULT_INPUT_FILE="./src/wordcount/file.txt"
JAR_NAME="wc.jar"
MAIN_CLASS="src.wordcountenseignant.WCDriver"

REMOTE_SRC_DIR="/root/src"

# All files will be uploaded here
HDFS_INPUT="/data"
HDFS_OUTPUT="/output"

HEAD_COUNT=""

# --- Functions ---
usage() {
    echo "Usage: $0 [options] [input_files...]"
    echo ""
    echo "Runs a Hadoop job in a Docker container with ANY number of input files."
    echo ""
    echo "Examples:"
    echo "  $0 file1.txt"
    echo "  $0 produit.csv concerner.csv commande.csv magasin.csv"
    echo ""
    echo "Options:"
    echo "  -container=<name>  Docker container name. (Default: $CONTAINER)"
    echo "  -jarName=<name>    Name of the .jar file. (Default: $JAR_NAME)"
    echo "  -mainClass=<class> Java main class to run. (Default: $MAIN_CLASS)"
    echo "  -head=<n>          Show only the first n lines of output."
    echo "  -h, --help         Show this help."
    exit 1
}

# --- Argument Parsing ---
INPUT_FILES=()

while [[ "$#" -gt 0 ]]; do
    case $1 in
        -container=*)
            CONTAINER="${1#*=}"
            shift
            ;;
        -jarName=*)
            JAR_NAME="${1#*=}"
            shift
            ;;
        -mainClass=*)
            MAIN_CLASS="${1#*=}"
            shift
            ;;
        -head=*)
            HEAD_COUNT="${1#*=}"
            shift
            ;;
        -h|--help)
            usage
            ;;
        -*)
            echo "Error: Unknown option: $1"
            usage
            ;;
        *)
            INPUT_FILES+=("$1")
            shift
            ;;
    esac
done

# If no input files → use default
if [ ${#INPUT_FILES[@]} -eq 0 ]; then
    INPUT_FILES=("$DEFAULT_INPUT_FILE")
fi

# Validate each file
for f in "${INPUT_FILES[@]}"; do
    if [ ! -f "$f" ]; then
        echo "❌ Error: File '$f' not found."
        exit 1
    fi
done

echo "🚀 Déploiement Hadoop Big Data"
echo "   Container:  $CONTAINER"
echo "   MainClass: $MAIN_CLASS"
echo "   Jar:       $JAR_NAME"
echo "   Inputs:"
for f in "${INPUT_FILES[@]}"; do
    echo "      - $f"
done
echo ""

# 1) Copy source code
echo "📁 Copie du dossier src vers le conteneur..."
docker cp ./src "$CONTAINER:/root/"

# 2) Copy input files
for f in "${INPUT_FILES[@]}"; do
    echo "📄 Copie de $(basename "$f") dans le conteneur..."
    docker cp "$f" "$CONTAINER:/root/"
done

# 3) Compile Java
echo "🛠️ Compilation Java..."
docker exec -it $CONTAINER bash -c "
    rm -rf /root/build && mkdir -p /root/build &&
    find $REMOTE_SRC_DIR -name '*.java' > /root/sources.txt &&
    javac -cp \"\$(hadoop classpath)\" -d /root/build @/root/sources.txt
"

# 4) Build jar
echo "📦 Création du JAR..."
docker exec -it $CONTAINER bash -c "
    jar -cvf $JAR_NAME -C /root/build .
"

# 5) Prepare HDFS output
echo "🗑️ Suppression de /output si existe..."
docker exec -it $CONTAINER hdfs dfs -rm -r -f $HDFS_OUTPUT || true

# 6) Upload input files to HDFS
echo "📁 Upload des inputs dans HDFS ($HDFS_INPUT)..."
docker exec -it $CONTAINER hdfs dfs -mkdir -p $HDFS_INPUT

for f in "${INPUT_FILES[@]}"; do
    BASENAME=$(basename "$f")
    echo "   → Upload $BASENAME"
    docker exec -it $CONTAINER hdfs dfs -put -f "/root/$BASENAME" $HDFS_INPUT
done

# Build HDFS argument list
HDFS_ARGS=""
for f in "${INPUT_FILES[@]}"; do
    BASENAME=$(basename "$f")
    HDFS_ARGS="$HDFS_ARGS $HDFS_INPUT/$BASENAME"
done

echo ""
echo "⚙️ Arguments Hadoop transmis au programme Java :"
echo "   $HDFS_ARGS"
echo ""

# 7) Run Hadoop job
echo "🚀 Exécution du job Hadoop..."
docker exec -it $CONTAINER bash -c "
    hadoop jar $JAR_NAME $MAIN_CLASS $HDFS_ARGS $HDFS_OUTPUT
"

# 8) Display output
echo "📊 Résultat final :"

# Trouver automatiquement le dernier dossier de sortie
LAST_OUTPUT_DIR=$(docker exec -it $CONTAINER hdfs dfs -ls $HDFS_OUTPUT \
  | grep "^d" \
  | awk '{print $NF}' \
  | tr -d '\r' \
  | sort \
  | tail -n 1)

if [ -z "$LAST_OUTPUT_DIR" ]; then
    echo "❌ Aucun dossier output trouvé."
    exit 1
fi

echo "📁 Dossier final détecté : $LAST_OUTPUT_DIR"

# Lire le part file
if [ -z "$HEAD_COUNT" ]; then
    docker exec -it $CONTAINER hdfs dfs -cat "$LAST_OUTPUT_DIR/part-r-00000"
else
    docker exec -it $CONTAINER hdfs dfs -cat "$LAST_OUTPUT_DIR/part-r-00000" | head -n $HEAD_COUNT
fi

