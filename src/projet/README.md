# Linux/MacOS

./compile_and_run.sh -mainClass=src.projet.Driver ./data/Produit.csv ./data/Concerner.csv ./data/Commande.csv ./data/Magasin.csv

# Windows
.\compile_and_run.bat -mainClass=src.projet.Driver ".\data\Produit.csv" ".\data\Concerner.csv" ".\data\Commande.csv" ".\data\Magasin.csv"

# Vérifie avec ca 
docker exec -it namenode bash
cd /root/build
find .