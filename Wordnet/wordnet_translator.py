import xml.etree.ElementTree as ET
import os
import deepl
from tqdm import tqdm
import copy

auth_key = "b9b21679-6916-e9a7-e65f-8b1afa40a979"
translator = deepl.Translator(auth_key)

os.chdir("/home/luc/Desktop/Completer")

class Word:
	def __init__(self, form, type):
		self.form = form
		self.type = type

	form : str
	type : str

	def id(self):
		return "oewn-" + self.form + "-" + self.type

def cutWordnet():
	#Mots francais - traduction - type
	sow_en = open("sow_en.txt")
	sow_fr = open("sow_fr.txt")
	sow_types = open("sow_types.txt")

	#Fichier Wordnet
	tree = ET.parse("wn.xml")
	root = tree.getroot()[0]

	new_id = {}
	new_sense_id = {}
	existing_words = {}
	synsets = set()

	#Stats
	merge_syns = {}
	merge_set = set()
	word_relation_count = {}
	word_relation_count_cut = {}
	synset_relation_count = {}
	synset_relation_count_cut = {}
	synset_relation_count_inter = {}

	print('Parsing du sow :')

	#Ensemble des lignes des fichier pour respectivement : anglais - français - type
	lines_en = sow_en.readlines()
	lines_fr = sow_fr.readlines()
	lines_types = sow_types.readlines()

	print("Etape 1")

	#On parcours les Set Of Words
	for i in tqdm(range(len(lines_en))):
		#word = trad(mot)
		word = lines_en[i]
		mot = lines_fr[i]
		type = lines_types[i]

		#Retire les retours à la ligne
		if len(word) > 0:
			word = word[0: len(word) - 1]
		if len(mot) > 0:
			mot = mot[0: len(mot) - 1]
		if len(type) > 0:
			type = type[:len(type)-1]

		#On rassemble mot et type
		word = "oewn-" + word + "-" + type
		mot = Word(mot, type)

		#Regroupe par word
		if not word in existing_words:
			existing_words[word] = []
		existing_words[word].append(mot)

		#Stats
		if word in merge_syns:
			merge_syns[word].append(mot)
			merge_set.add(word)
		else:
			merge_syns[word] = [mot]

	print("Etape 2")

	#On parcours l'ensemble des mots de wn en les traduisant
	lexical_entry_list = root.findall("LexicalEntry")
	for i in tqdm(range(len(lexical_entry_list))):
		lexical_entry = lexical_entry_list[i]

		old_id = lexical_entry.get("id")

		if old_id in existing_words:

			liste  = existing_words[old_id]

			new_id[old_id] = []

			for mot in liste:
				nid = mot.id()
				cp = copy.deepcopy(lexical_entry);

				cp.set("id", nid)
				lemma = cp.find("Lemma")
				lemma.set("writtenForm", mot.form)

				new_id[old_id].append(nid)

				sense_i = 0
				for s in cp.findall("Sense"):
					nsense_id = nid + "-" + str(sense_i)
					new_sense_id[s.get("id")] = nsense_id
					s.set("id", nsense_id)
					sense_i += 1

				root.append(cp)

		root.remove(lexical_entry)

	print("Etape 3")

	#On parcours une nouvelle fois l'ensemble des mots pour purger les relations inutiles et adapter les identifiants des sens
	lexical_entry_list = root.findall("LexicalEntry")
	for i in tqdm(range(len(lexical_entry_list))):
		lexical_entry = lexical_entry_list[i]

		for sense in lexical_entry.findall("Sense"):
			sense_id = sense.get("id")
			rel_i = 0

			for relation in sense.findall("SenseRelation"):
				type = relation.get("relType")

				#Stats
				if not type in word_relation_count:
					word_relation_count[type] = 0
					word_relation_count_cut[type] = 0
				word_relation_count[type] += 1

				target_old_id = relation.get("target")

				#Vérifie si le sens cible existe toujours supprime la relation si non
				if not target_old_id in new_sense_id:
					sense.remove(relation)
				else:
					#Met à jour l'identifiant de la cible
					target_nid = new_sense_id[target_old_id]
					relation.set("target", target_nid)

					#Stats
					word_relation_count_cut[type] += 1

	print("Etape 4")

	#On fait le tri dans les synsets en supprimant ceux auquels aucun mot ne se réfère
	synset_list = root.findall("Synset")
	for i in tqdm(range(len(synset_list))):
		synset = synset_list[i]
		synset_id = synset.get("id")

		member_names_initial = synset.get("members").split(' ')
		members = []

		for name in member_names_initial:
			if name in new_id:
				for mid in new_id[name]:
					members.append(mid)

		for relation in synset.findall("SynsetRelation"):
			type = relation.get("relType")

			if not type in synset_relation_count:
				synset_relation_count[type] = 0
				synset_relation_count_cut[type] = 0
				synset_relation_count_inter[type] = 0
			synset_relation_count[type] += 1

		if len(members) == 0:
			root.remove(synset)
		else:
			synsets.add(synset_id)

			member_names_final = members[0]
			for i in range(1, len(members)):
				m = members[i]
				member_names_final += " " + m
			synset.set("members", member_names_final)

	print("Etape 5")

	#On reparcourt l'ensemble des synsets pour supprimer les relation inutiles
	synset_list = root.findall("Synset")
	for i in tqdm(range(len(synset_list))):
		synset = synset_list[i]

		for relation in synset.findall("SynsetRelation"):
			type = relation.get("relType")

			#Stats
			synset_relation_count_inter[type] += 1

			target_id = relation.get("target")

			#Vérifie si le synset cible existe toujours supprime la relation si non
			if not target_id in synsets:
				synset.remove(relation)
			else:
				#Stats
				synset_relation_count_cut[type] += 1

	sow_fr.close()
	sow_en.close()

	#On enregistre les modifications
	with open("wn_cut.xml", 'wb') as wn_cut:
		wn_cut.write(ET.tostring(root))

	#Enregistrement des statistiques
	print("Enregistrement des statistiques")

	stats = []
	stats.append("Relations entre mots\t&\tOriginal\t&\tTraduit \\\\ \n")
	for t in word_relation_count:
		stats.append(t)
		stats.append(f"\t&\t{word_relation_count[t]}\t&\t{word_relation_count_cut[t]}\n")

	stats.append("\n\n")

	stats.append("Relations entre synsets\t&\tOriginal\t&\tIntermédiaire\t&\tTraduit \\\\ \n")
	for t in synset_relation_count:
		stats.append(t)
		stats.append(f"\t&\t{synset_relation_count[t]}\t&\t{synset_relation_count_inter[t]}\t&\t{synset_relation_count_cut[t]}\\\\ \n")

	stats.append("\n\nListe des mots fusionnés à la traduction : \n")
	for w in merge_set:
		stats.append(w + " : " + merge_syns[w][0].form + "-" +  merge_syns[w][0].type)
		for i in range(1, len(merge_syns[w])):
			m = merge_syns[w][i].form
			t = merge_syns[w][i].type
			stats.append(", " + m)
		stats.append("\n")

	#Fichier de sortie des statistiques
	with open("wn_stats.txt", "w") as wn_stats:
		wn_stats.write("".join(stats))

	print("Fin d'éxécution")

def translateSOW():
	translator.translate_document_from_filepath("sow_fr.txt", "sow_en.txt", source_lang="fr", target_lang="en-us")
