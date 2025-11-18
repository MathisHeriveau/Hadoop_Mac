# Anagramme -- Hadoop MapReduce

Ce projet identifie automatiquement les anagrammes dans un texte en
utilisant le modèle MapReduce.

## 🔧 Lancer le programme

``` bash
./compile_and_run.sh -mainClass=src.anagramme.AnagramDriver ./data/100-0.txt
```

## Mapper 

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
            <td>Signature</td>
            <td>Mot</td>
        </tr>
    </tbody>
</table>

### Traitement (Mapper)

-   Lire la ligne
-   Extraire les mots
-   Nettoyer (minuscules, suppression ponctuation)
-   Trier les lettres du mot pour obtenir la signature
-   Émettre (signature, mot)

## 🧮 Reducer

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
            <td>Signature</td>
            <td>Liste des mots (text)</td>
        </tr>
        <tr>
            <td>Output</td>
            <td>Signature</td>
            <td>Liste d'anagrammes (text)</td>
        </tr>
    </tbody>
</table>

### Traitement (Reducer)

-   Récupérer la signature et tous les mots associés
-   Construire une liste de mots distincts
-   Vérifier s'il y a au moins deux mots différents
-   Émettre la signature avec la liste d'anagrammes
