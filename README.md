# 🚀 TP Hadoop — WordCount avec Docker

Setup complet + Exécution MapReduce sous Hadoop 3.x

Ce projet installe un **cluster Hadoop complet**, compile automatiquement un **WordCount en Java**, l’envoie dans le **namenode**, crée l’input dans **HDFS**, exécute le job, et lit le résultat.

Vous avez deux modes :
- **INSTALLATION AUTOMATIQUE**
- **INSTALLATION MANUELLE**


---

# ⚡️ INSTALLATION AUTOMATIQUE (RECOMMANDÉE)

Deux scripts :
- `./install.sh` → installe et lance Hadoop + Dev + Java + copie WordCount
- `./wordcount/run_wordcount.sh` → compile + jar + HDFS + exécution WordCount

## 1) Installation complète

```bash
chmod +x install.sh
./install.sh
```

## 2) Lancer WordCount automatiquement

```bash
cd wordcount
chmod +x run_wordcount.sh
./run_wordcount.sh
```

---

# 🧱 INSTALLATION MANUELLE

## 1) Pré-requis

- Docker + Docker Compose
- Structure :
```
hadoop/
├── docker-compose.yml
├── docker-compose.dev.yml
├── wordcount/
│   ├── WCDriver.java
│   ├── WCMapper.java
│   ├── WCReducer.java
│   └── file.txt
```

---

## 2) Lancer Hadoop

```bash
docker compose up -d
```

---

## 3) Lancer le conteneur Dev

```bash
docker compose -f docker-compose.dev.yml up -d
docker exec -it hadoop-dev bash
```

---

## 4) Installer Java dans Dev

```bash
apt update
apt install -y openjdk-11-jdk
javac -version
```

---

## 5) Copier WordCount dans le namenode

```bash
docker cp wordcount namenode:/root/
docker exec -it namenode bash
ls /root/wordcount
```

---

## 6) Compiler WordCount

```bash
cd /root/wordcount
javac -cp "$(hadoop classpath)" -d . wordcount/*.java
```

---

## 7) Créer le JAR

```bash
jar -cvf wc.jar wordcount
```

---

## 8) Préparer HDFS

```bash
hdfs dfs -mkdir -p /data
hdfs dfs -put -f /root/file.txt /data
```

---

## 9) Exécuter WordCount

```bash
hadoop jar wc.jar wordcount.WCDriver /data /output
```

---

## 10) Lire le résultat

```bash
hdfs dfs -cat /output/part-r-00000
```

---

# 🎉 FIN
WordCount fonctionne à 100%.
