# 🚀 TP Hadoop – WordCount avec Docker

Ce README explique **toute l’installation** du cluster Hadoop sous Docker, la configuration, la compilation du programme Java WordCount, le déploiement dans Hadoop, l’exécution du job MapReduce, la création du dossier `/data` dans HDFS, et la lecture des résultats.

Ce guide reproduit exactement l’environnement utilisé dans le projet.

---

# 📦 1. Pré-requis

* Docker installé
* Docker Compose installé
* Java installé sur le **conteneur dev** (on l’installera après)
* Un dossier local contenant :

  * `docker-compose.yml` (cluster Hadoop)
  * `docker-compose.dev.yml` (conteneur Dev Ubuntu)
  * Un dossier `wordcount/` avec vos fichiers Java

Arborescence recommandée :

```
hadoop/
├── docker-compose.yml
├── docker-compose.dev.yml
├── hadoop.env
├── file.txt (optionnel)
└── wordcount/
    ├── WCDriver.java
    ├── WCMapper.java
    └── WCReducer.java
```

---

# 🐳 2. Lancer le cluster Hadoop

Placez-vous dans le dossier contenant `docker-compose.yml`.

```bash
cd /Users/.../hadoop
```

Démarrer le cluster :

```bash
docker compose up -d
```

Les services suivants seront lancés :

* namenode
* datanode1 / datanode2 / datanode3
* resourcemanager
* nodemanager
* historyserver

Vous pouvez vérifier :

```bash
docker ps
```

---

# 🖥️ 3. Lancer le conteneur Dev pour compiler WordCount

Le conteneur Dev est une Ubuntu utilisée pour compiler votre code Java.

Démarrer le conteneur Dev :

```bash
docker compose -f docker-compose.dev.yml up -d
```

Entrer dans le conteneur :

```bash
docker exec -it hadoop-dev bash
```

Vous arriverez dans :

```
root@xxxx:/workspace
```

Ce dossier `/workspace` est automatiquement lié au dossier `wordcount/` sur votre machine.

---

# ☕ 4. Installer Java dans le conteneur Dev

Dans le conteneur Dev :

```bash
apt update
apt install -y openjdk-11-jdk
```

Vérifier :

```
javac -version
```

---

# 📝 5. Déposer les fichiers WordCount dans le conteneur Hadoop (namenode)

Les fichiers `.java` sont sur votre machine, dans `wordcount/`.

Copiez-les dans le namenode :

```bash
docker cp wordcount namenode:/root/
```

Puis entrez dans le namenode :

```bash
docker exec -it namenode bash
```

Vérifiez :

```bash
ls -la /root/wordcount
```

Vous devez voir :

```
WCDriver.java
WCMapper.java
WCReducer.java
```

---

# 🧱 6. Compilation du WordCount dans le Namenode

Toujours dans `/root/wordcount` :

```bash
cd /root/wordcount
```

Compiler avec le classpath Hadoop :

```bash
javac -cp "$(hadoop classpath)" -d . wordcount/*.java
```

Si tout est correct, vous obtiendrez :

```
wordcount/WCDriver.class
wordcount/WCMapper.class
wordcount/WCReducer.class
```

---

# 📦 7. Création du JAR WordCount

Toujours dans `/root/wordcount` :

```bash
jar -cvf wc.jar wordcount
```

Votre JAR est maintenant prêt pour Hadoop.

---

# 📂 8. Préparer le dossier d’entrée HDFS `/data`

Créer le dossier `/data` dans HDFS :

```bash
hdfs dfs -mkdir /data
```

Créer un fichier texte d’entrée dans le namenode :

```bash
echo "hello hadoop world this is a big test hello hello" > /root/file.txt
```

Envoyer ce fichier dans HDFS :

```bash
hdfs dfs -put /root/file.txt /data
```

Vérifier :

```bash
hdfs dfs -ls /data
```

---

# 🚀 9. Exécuter le WordCount sur Hadoop

Dans le namenode :

```bash
hadoop jar wc.jar wordcount.WCDriver /data /output
```

Vous verrez Hadoop lancer :

* un job Map
* un job Reduce
* avec statistiques

Si tout se passe bien :

```
Job completed successfully
```

---

# 📖 10. Lire le résultat du WordCount

Lire le fichier de sortie dans HDFS :

```bash
hdfs dfs -cat /output/part-r-00000
```

Exemple :

```
a       1
big     1
hadoop  1
hello   3
is      1
test    1
this    1
world   1
```

---

# 🧹 11. Nettoyage (optionnel)

Supprimer un dossier HDFS :

```bash
hdfs dfs -rm -r /output
```

Arrêter tous les conteneurs Docker :

```bash
docker compose down
```

Arrêter aussi le Dev :

```bash
docker compose -f docker-compose.dev.yml down
```

---

# 🎉 FIN — Votre WordCount Hadoop fonctionne !

Vous avez :

* un vrai cluster Hadoop
* un programme MapReduce Java compilé
* un JAR exécutable
* un fichier d’entrée dans HDFS
* une exécution complète MapReduce
* un résultat final affiché depuis HDFS

Vous avez entièrement validé le TP Hadoop WordCount.

Vous pouvez maintenant passer tranquillement au TP2 ou Spark 😎🔥

---

# 🤖 Script d'automatisation : `compile_and_run.sh`

Un script `compile_and_run.sh` est fourni pour automatiser toutes les étapes décrites ci-dessus (compilation, création du JAR, exécution du job Hadoop).

## Utilisation

Exécutez simplement le script depuis votre terminal :

```bash
./compile_and_run.sh [options] [fichier_entree]
```

### Arguments Positionnels

*   `fichier_entree`: Chemin vers le fichier d'entrée local.
    *   **Défaut** : `./src/wordcount/file.txt`

### Options

*   `-container=<nom>`: Nom du conteneur Docker où exécuter les commandes.
    *   **Défaut** : `namenode`
*   `-jarName=<nom>`: Nom du fichier `.jar` à créer.
    *   **Défaut** : `wc.jar`
*   `-mainClass=<classe>`: Classe Java principale à exécuter.
    *   **Défaut** : `src.wordcountenseignant.WCDriver`
*   `-h, --help`: Affiche le message d'aide.

## Exemple

Pour lancer le WordCount sur un fichier différent avec la classe enseignante :

```bash
./compile_and_run.sh -mainClass=src.wordcountenseignant.WCDriver ./mon_fichier.txt
```
