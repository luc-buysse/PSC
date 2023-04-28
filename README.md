# Projet Scientifique Collectif 2022 - Caractérisation de l'écriture artiste

Contributeurs : Emma Verdier - Tanguy Couderc - Loman Sezestre - Luc Buysse

Organisation du dépot :
- le dossier <b> src </b> contient l'intégralité du code source java, le code écrit spécifiquement pour le projet est contenu dans le package PSC. Les autres packages sont des librairies.
- les fichiers <b> sensations.txt </b> et <b> art_words.txt </b> contiennent respectivement les listes de mots du vocabulaire des sensations et du vocabulaire de l'art que nous avons utilisées
- le dossier <b> Wordnet </b> contient :
  - un fichier <b> wn_cut.xml </b> qui est le Wordnet traduit
  - un script python <b> wordnet_translator.py </b> qui permet de traduire le Wordnet. Le script est rapide à éxécuter et son code est très lisible. Si vous voulez l'éxécuter sur votre machine, il faut remplacer le nom du répertoire de travail et placer 4 fichiers dans ce répertoire : <b> wn.xml </b> (Wordnet anglais disponible à l'adresse suivante <a> https://en-word.net/static/english-wordnet-2022.xml.gz </a>) et...
  - les fichiers <b> sow_fr.txt </b>, <b> sow_en.txt </b> et <b> sow_types.txt </b> qui sont respectivement la liste des mots du corpus, leur traduction et leur type
- le fichier <b> wn_merge.txt </b> contient la liste des mots fusionnés à la traduction, il permet d'évaluer la qualité et les limites de la traduction
- le fichier <b> neo.txt </b> contient la liste des néologismes détectés dans le corpus en utilisant un dictionnaire français
- le fichier <b> freq.txt </b> contient la liste des nombre d'occurences des mots dans le corpus
- le dossier <b> SVM </b> contient :
  - un script python <b> svm.py </b> qui crée une SVM de classification des textes d'écriture artiste
  - un fichier <b> weights.csv </b> qui contient la liste des coefficients de la SVM normalisée en norme 1 (somme = 1)

Pour éxécuter le code java sur votre propre machine :
- le charger dans eclipse
- modifier les chemins d'accès disponibles dans le fichier Settings.java
- lancer le programme et éxécuter une des commandes décrite dans le fichier Head.java
