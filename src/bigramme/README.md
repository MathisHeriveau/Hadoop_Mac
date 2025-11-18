# Bigramme --- Hadoop MapReduce

Ce projet détecte automatiquement les **bigrammes** (paires de mots
consécutifs) dans un texte en utilisant **Hadoop MapReduce**.\
Le pipeline utilise **deux jobs MapReduce** :\
1. Extraction & comptage des bigrammes\
2. Tri des bigrammes par fréquence décroissante

------------------------------------------------------------------------

## 🚀 Exécution du programme

``` bash
./compile_and_run.sh -mainClass=src.bigramme.BGCDriver ./data/100-0.txt
```

------------------------------------------------------------------------

# 1️⃣ Premier Job --- Extraction & Comptage

## 🧩 Mapper 1

<table border="1">
    <thead>
        <tr>
            <th></th>
            <th>Clé</th>
            <th>Valeur</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Input</td>
            <td>Index de ligne</td>
            <td>Contenu de la ligne</td>
        </tr>
        <tr>
            <td>Output</td>
            <td>Bigramme</td>
            <td>1</td>
        </tr>
    </tbody>
</table>

### 🔍 Traitement

-   Nettoyer + découper la ligne en mots\
-   Générer toutes les paires `(mot_i, mot_{i+1})`\
-   Émettre : **(bigramme, 1)**

------------------------------------------------------------------------

## 🌀 Shuffle & Sort

Hadoop regroupe automatiquement toutes les valeurs pour une même clé
(bigramme).

------------------------------------------------------------------------

## 🧮 Reducer 1

<table border="1">
    <thead>
        <tr>
            <th></th>
            <th>Clé</th>
            <th>Valeur</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Input</td>
            <td>Bigramme</td>
            <td>Occurrences</td>
        </tr>
        <tr>
            <td>Output</td>
            <td>Bigramme</td>
            <td>NB occurrences (IntWritable)</td>
        </tr>
    </tbody>
</table>

### 🔍 Traitement

-   Additionner toutes les occurrences\
-   Émettre : **(bigramme, total)**

------------------------------------------------------------------------

# 2️⃣ Deuxième Job --- Tri par Fréquence

## 🧩 Mapper 2

<table border="1">
    <thead>
        <tr>
            <th></th>
            <th>Clé</th>
            <th>Valeur</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Input</td>
            <td>Bigramme</td>
            <td>NB occurrences (Text)</td>
        </tr>
        <tr>
            <td>Output</td>
            <td>NB occurrences (IntWritable)</td>
            <td>Bigramme</td>
        </tr>
    </tbody>
</table>

### 🔍 Traitement

-   Inverser clé / valeur\
-   Permettre au tri Hadoop de fonctionner sur les occurrences

------------------------------------------------------------------------

## 🧮 Reducer 2

<table border="1">
    <thead>
        <tr>
            <th></th>
            <th>Clé</th>
            <th>Valeur</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Input</td>
            <td>NB occurrences (IntWritable)</td>
            <td>Bigramme</td>
        </tr>
        <tr>
            <td>Output</td>
            <td>Bigramme</td>
            <td>NB occurrences (IntWritable)</td>
        </tr>
    </tbody>
</table>

### 🔍 Traitement

-   Parcourir les bigrammes pour une même fréquence\
-   Ré‑émettre le bigramme avec sa fréquence

------------------------------------------------------------------------

## ⚙️ Comparator (Tri décroissant)

Un comparateur personnalisé trie les clés `IntWritable` **du plus grand
au plus petit**,\
→ résultat : **bigrammes les plus fréquents en premier**.

------------------------------------------------------------------------

# 📦 Résultat final

Un fichier contenant :

    (bigramme)   (nb_occurrences)

Trié automatiquement du plus fréquent au moins fréquent.
