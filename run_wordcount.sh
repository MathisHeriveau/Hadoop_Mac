#!/bin/bash

set -e

# === CONFIG ===
CONTAINER="namenode"
LOCAL_WC_DIR="./wordcount"
REMOTE_WC_DIR="/root/wordcount"
INPUT_FILE="./wordcount/file.txt"
HDFS_INPUT="/data"
HDFS_OUTPUT="/output"
JAR_NAME="wc.jar"
MAIN_CLASS="wordcount.WCDriver"

echo "🚀 Déploiement WordCount Hadoop..."

# 1) Copie du code Java dans le container
echo "📁 Copie du dossier wordcount vers le conteneur..."
docker cp "$LOCAL_WC_DIR" "$CONTAINER:$REMOTE_WC_DIR"

# 2) Copie du fichier d'entrée
echo "📄 Copie du fichier d'entrée dans le conteneur..."
docker cp "$INPUT_FILE" "$CONTAINER:$REMOTE_WC_DIR"

# 3) Compilation Java
echo "🛠️ Compilation des fichiers Java..."
docker exec -it $CONTAINER bash -c "
    cd $REMOTE_WC_DIR &&
    javac -cp \"\$(hadoop classpath)\" -d . wordcount/*.java
"

# 4) Création du JAR
echo "📦 Création du JAR..."
docker exec -it $CONTAINER bash -c "
    cd $REMOTE_WC_DIR &&
    jar -cvf $JAR_NAME wordcount
"

# 5) Préparation HDFS
echo "🗑️ Nettoyage ancien /output si existe..."
docker exec -it $CONTAINER hdfs dfs -rm -r -f $HDFS_OUTPUT || true

echo "📁 Upload du fichier dans HDFS..."
docker exec -it $CONTAINER hdfs dfs -mkdir -p $HDFS_INPUT
docker exec -it $CONTAINER hdfs dfs -put -f $REMOTE_WC_DIR/file.txt $HDFS_INPUT

# 6) Exécution du job WordCount
echo "⚙️ Exécution du job Hadoop WordCount..."
docker exec -it $CONTAINER bash -c "
    cd $REMOTE_WC_DIR &&
    hadoop jar $JAR_NAME $MAIN_CLASS $HDFS_INPUT $HDFS_OUTPUT
"

# 7) Lecture du résultat
echo "📊 Résultat final :"
docker exec -it $CONTAINER hdfs dfs -cat $HDFS_OUTPUT/part-r-00000

echo "✅ FINI !"
