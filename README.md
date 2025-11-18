# 🚀 TP Hadoop — WordCount avec Docker (Nouvelle Version 2025)

Setup 100% automatique d’un cluster **Hadoop 3.x** + compilation + exécution d’un **WordCount Java**.
Tu peux tout lancer en *AUTO* ou gérer toi-même en *MANUEL*.

Structure du projet :

```
hadoop/
├── install.sh
├── compile_and_run.sh
├── docker-compose.yml
├── docker-compose.dev.yml
├── src/
│   ├── wordcount/
│   │   ├── WCDriver.java
│   │   ├── WCMapper.java
│   │   └── WCReducer.java
│   └── wordcountenseignant/
│   │   ├── WCDriver.java
│   │   ├── WCMapper.java
│   │   └── WCReducer.java
└── data/
    └── file.txt
```

---

# ⚡️ INSTALLATION AUTOMATIQUE (RECOMMANDÉE)

## 1) Installation complète Hadoop + Dev + Java

```bash
chmod +x install.sh
./install.sh
```

Lance automatiquement :
- le cluster Hadoop
- le conteneur Dev
- l'installation de Java 11

---

## 2) Compilation + JAR + HDFS + WordCount

```bash
chmod +x compile_and_run.sh
./compile_and_run.sh
```

Ce script :
- compile le WordCount
- génère le `.jar`
- upload le fichier dans HDFS
- exécute le job
- affiche le résultat

---

# 🎯 Options avancées

Exemple pour changer la classe main + fichier :

```bash
./compile_and_run.sh -mainClass=src.wordcountenseignant.WCDriver ./data/file.txt
```

### Paramètres disponibles

| Option | Description | Défaut |
|--------|-------------|--------|
| `-container=<nom>` | Conteneur Hadoop | namenode |
| `-jarName=<nom>` | Nom du JAR généré | wc.jar |
| `-mainClass=<classe>` | Classe Java principale | src.wordcountenseignant.WCDriver |
| `-head=<n>` | Afficher seulement les n premières lignes du résultat | (tout afficher) |
| `input_file` | Fichier local à envoyer dans HDFS | `./src/wordcount/file.txt` |

Afficher l’aide :

```bash
./compile_and_run.sh --help
```

---

# 🧱 INSTALLATION MANUELLE

## 1) Lancer Hadoop

```bash
docker compose up -d
```

## 2) Lancer Dev

```bash
docker compose -f docker-compose.dev.yml up -d
docker exec -it hadoop-dev bash
```

## 3) Installer Java

```bash
apt update
apt install -y openjdk-11-jdk
javac -version
```

## 4) Copier le code Java

```bash
docker cp ./src namenode:/root/
docker cp ./data/file.txt namenode:/root/
```

## 5) Compiler

```bash
find /root/src -name '*.java' > sources.txt
javac -cp "$(hadoop classpath)" -d build @sources.txt
```

## 6) JAR

```bash
jar -cvf wc.jar -C build .
```

## 7) HDFS

```bash
hdfs dfs -mkdir -p /data
hdfs dfs -put -f /root/file.txt /data
```

## 8) Exécution

```bash
hadoop jar wc.jar src.wordcount.WCDriver /data /output
```

## 9) Lire résultat

```bash
hdfs dfs -cat /output/part-r-00000
```

---

# 🎉 Résultat

Tu as :
- un cluster Hadoop fonctionnel
- un MapReduce Java compilé
- un `.jar` exécutable
- un fichier injecté dans HDFS
- un WordCount qui tourne

Parfait pour valider ton TP et passer à Spark 🔥😎

