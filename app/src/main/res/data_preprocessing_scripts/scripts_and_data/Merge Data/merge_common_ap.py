import data_structure as ds

#TODO przerobic by z automatu przerabialo wszystkie pliki

def exist_in_list(list, item):
	exist = False
	for i in list:
		if i == item:
			exist = True
	if exist:
		return True
	return False

def give_index_if_exist(list, item):
	for i in range(len(list)):
		if list[i] == item:
			return i
	return -1

def add_if_not_exist(list, item):
	if not exist_in_list(list, item):
		list.append(item)
	return list

def key_exist(dict, key):
	keys = dict.iterkeys()
	for k in keys:
		if k == key:
			return True
	return False

def load_data(filename):
	k = 0
	mac = []
	signals = []
	with open(filename, 'r') as f:
		for line in f:
			k += 1
			if (k+1)%3 == 0:
				#adresy mac w danym odczycie
				mac.append(line.split())
			if k%3 == 0:
				#sila sygnalu do poprzednio zczytanych sygnalow
				signals.append(line.split())

	if len(mac) != len(signals):
		print "Blad pliku"
		exit(0)

	for i in range(len(mac)):
		if len(mac[i]) != len(signals[i]):
			print "Blad pliku"
			exit(0)

	print "Ilosc wszytkich probek dla pliku " + filename + ": " + str(len(mac))

	# Przerabiamy strukture tak by miec liste mac adresow pod ktora kryje sie lista zczytanych sygnalow dla danego mac adresu

	mac_dict = {}
	for i in range(len(mac)):
		for j in range(len(mac[i])):
			if key_exist(mac_dict, mac[i][j]):
				mac_dict[mac[i][j]].append(signals[i][j])
			else:
				mac_dict[mac[i][j]] = [signals[i][j]]
				
	return mac_dict

#for i in range(18):
	#filename = "C" + str(i+1) + ".txt"
filename = "C3.txt"
mac_dict = load_data("input/" + filename)
pattern = load_data("pattern/C4.txt")# + filename)
dict_result = {}
print "Plik: " + filename
print "Ilosc adresow przed przesiewe: " + str(len(mac_dict))
k = 0
for key, signals in mac_dict.iteritems():
	if key_exist(pattern, key):
		dict_result[key] = signals
		k += 1

print "Ilosc adresow po przesiewie: " + str(len(dict_result))
ds.File.save_data_as_json("output/" + filename, dict_result)

