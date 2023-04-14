# Projet Scientifique Collectif 2022 - Caractérisation de l'écriture artiste

Contributeurs : Emma Verdier - Tanguy Couderc - Loman Sezestre - Luc Buysse

Organisation du dépot :
- le dossier <b> src </b> contient l'intégralité du code source java, le code écrit spécifiquement pour le projet est contenu dans le package PSC. Les autres packages sont des librairies que nous n'avons pas codées nous même.
- les fichiers <b> sensations.txt </b> et <b> art_words.txt </b> contiennent respectivement les listes de mots du vocabulaire des sensations et du vocabulaire de l'art que nous avons utilisées
- le dossier <b> Wordnet </b> contient :
  - un fichier <b> wn_cut.xml </b> qui est Wordnet traduit
  - un script python <b> wordnet_translator.py </b> qu permet de traduire le wordnet. Le script est rapide à éxécuter et son code est très lisible. Si vous voulez l'éxécuter sur votre machine, il faut remplacer le nom du répertoire de travail et placer 4 fichiers dans ce répertoire : <b> wn.xml </b> (Wordnet anglais disponible à l'adresse suivante <a> https://en-word.net/static/english-wordnet-2022.xml.gz </a>) et...
  - les fichiers <b> sow_fr.txt </b>, <b> sow_en.txt </b> et <b> sow_types.txt </b> qui sont respectivement la liste des mots du corpus, leur traduction et leur type
- le fichier <b> wn_merge.txt </b> est la liste des mots fusionnés à la traduction, il permet d'évaluer la qualité et les limites de la traduction
