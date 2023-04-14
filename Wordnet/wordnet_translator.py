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

	#Fichier de sortie
	wn_cut = open("wn_cut.xml", 'wb')

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

	print('Parsing du sow :')

	#Ensemble des lignes des fichier pour resp. : anglais - français - type
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
		if not word in existing_words.keys():
			existing_words[word] = []
		existing_words[word].append(mot)

		#Stats
		if word in merge_syns.keys():
			merge_syns[word].append(mot)
			merge_set.add(word)
		else:
			merge_syns[word] = [mot]

	print("Nombre de mots parsés : " + str(len(existing_words.keys())) + "\nEtape 2")

	#On parcours l'ensemble des mots de wn en les traduisant
	lexical_entry_list = root.findall("LexicalEntry")
	for i in tqdm(range(len(lexical_entry_list))):
		child = lexical_entry_list[i]

		old_id = child.get("id")

		if old_id in existing_words.keys():

			liste  = existing_words[old_id]

			new_id[old_id] = []

			for mot in liste:
				nid = mot.id()
				cp = copy.deepcopy(child);

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

		root.remove(child)

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
				if not type in word_relation_count.keys():
					word_relation_count[type] = 0
					word_relation_count_cut[type] = 0
				word_relation_count[type] += 1

				target_old_id = relation.get("target")

				#Vérifie si le sens cible existe toujours supprime la relation si non
				if not target_old_id in new_sense_id.keys():
					sense.remove(relation)
				else:
					#Met à jour l'identifiant de la cible
					target_nid = new_sense_id[target_old_id]
					relation.set("target", target_nid)

					#Stats
					word_relation_count_cut[type] += 1

	print("Etape 4")

	#On fait le tri dans les synsets en supprimant ceux auquels aucun mot ne réfère
	synset_list = root.findall("Synset")
	for i in tqdm(range(len(synset_list))):
		synset = synset_list[i]
		synset_id = synset.get("id")

		member_names_initial = synset.get("members").split(' ')
		members = []

		for name in member_names_initial:
			if name in new_id.keys():
				for mid in new_id[name]:
					members.append(mid)

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
			if not type in synset_relation_count.keys():
				synset_relation_count[type] = 0
				synset_relation_count_cut[type] = 0
			synset_relation_count[type] += 1

			target_id = relation.get("target")

			#Vérifie si le synset cible existe toujours supprime la relation si non
			if not target_id in synsets:
				synset.remove(relation)
			else:
				#Stats
				synset_relation_count_cut[type] += 1

	#On enregistre les modifications
	wn_cut.write(ET.tostring(root))
	wn_cut.close()

	#Enregistrement des statistiques
	print("Enregistrement des statistiques")

	stats = "Relations entre mots\t\tavant épuration\taprès épuration\n"
	for t in word_relation_count.keys():
		stats += t
		stats += "\t\t\t\t" + str(word_relation_count[t]) + "\t\t" + str(word_relation_count_cut[t]) + "\n"

	stats += "\n\n"

	stats += "Relations entre synsets\t\tavant épuration\taprès épuration\n"
	for t in synset_relation_count.keys():
		stats += t
		stats += "\t\t\t\t" + str(synset_relation_count[t]) + "\t\t" + str(synset_relation_count_cut[t]) + "\n"
	stats += "Total de mots\t\t" + str(len(existing_words)) + "\n\n"

	stats += "Liste des mots fusionnés à la traduction : "
	for w in merge_set:
		stats += w + " : " + merge_syns[w][0].form + "-" +  merge_syns[w][0].type
		for i in range(1, len(merge_syns[w])):
			m = merge_syns[w][i].form
			t = merge_syns[w][i].type
			stats += ", " + m
		stats += "\n"

	#Fichier de sortie des statistiques
	wn_stats = open("wn_stats.txt", "w")
	wn_stats.write(stats)
	wn_stats.close()

	sow_fr.close()
	sow_en.close()

	print("Fin d'éxécution")

def translateSOW():
	translator.translate_document_from_filepath("sow_fr.txt", "sow_en.txt", source_lang="fr", target_lang="en-us")
