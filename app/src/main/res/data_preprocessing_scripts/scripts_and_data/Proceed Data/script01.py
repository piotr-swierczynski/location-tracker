import data_structure as ds

#for i in range(18):
	#file = "C" + str(i+1) + ".txt"

file = "C14.txt"
load_from_json = True

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

if not load_from_json:
	path = "Gathering Data/"+file

	k = 0
	mac = []
	signals = []
	with open(path, 'r') as f:
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

	print "Ilosc wszytkich probek dla pliku " + file + ": " + str(len(mac))

	# Przerabiamy strukture tak by miec liste mac adresow pod ktora kryje sie lista zczytanych sygnalow dla danego mac adresu

	mac_dict = {}
	for i in range(len(mac)):
		for j in range(len(mac[i])):
			if key_exist(mac_dict, mac[i][j]):
				mac_dict[mac[i][j]].append(signals[i][j])
			else:
				mac_dict[mac[i][j]] = [signals[i][j]]
else:
	#wczytanie z jsona slownika zawierajacego wpisy z tego etapu skryptu
	mac_dict = ds.File.load_info_from_json_file("Gathering Data/"+file)

# Obcinamy sygnaly ponizej progu
threshold = -80
mac_dict_after_threshold = {}
for key, signals in mac_dict.iteritems():
	new_signals = []
	for s in signals:
		s = int(s)
		if s >= threshold:
			new_signals.append(s)
	if new_signals == []:
		continue
	new_signals.sort()
	mac_dict_after_threshold[key] = new_signals
mac_dict = mac_dict_after_threshold

# Robimy alpha trimming
alpha = 0.2
mac_dict_after_trimming = {}
for key, signals in mac_dict.iteritems():
	new_signals = []
	lenght = len(signals)
	number_to_cut = int(lenght*alpha)
	cut_from_the_beginning = int(number_to_cut / 2)
	cut_from_the_end = int(number_to_cut / 2)
	signal_without_beginning = []
	for s in signals:
		if cut_from_the_beginning > 0:
			cut_from_the_beginning -= 1;
			continue
		signal_without_beginning.append(s)
	for i in reversed(range(len(signal_without_beginning))):
		if cut_from_the_end > 0:
			cut_from_the_end -= 1;
			continue
		new_signals.append(signal_without_beginning[i])
	if new_signals == []:
		continue
	new_signals.sort()
	mac_dict_after_trimming[key] = new_signals
mac_dict = mac_dict_after_trimming

# Sposrod puli dostepnych wybieramy te z najwieksza srednia
number_of_significant_signals = 10
minimum_number_of_signals_to_consider_access_point = 25

def avg_calculate(dict):
	avg_dict = {}
	for key, signals in dict.iteritems():
		if len(signals) < minimum_number_of_signals_to_consider_access_point:
			continue
		sum = 0
		for s in signals:
			sum += int(s)
		avg = sum/float(len(signals))
		avg_dict[key] = avg
	return avg_dict

def get_the_most_significant_mac_addresses(avg_dict, number_to_get):
	significant_mac_list = []
	while number_to_get > 0:
		last_signal_avg = -100
		mac_address = ""
		for key, avg in avg_dict.iteritems():
			if avg > last_signal_avg and not exist_in_list(significant_mac_list, key):
				last_signal_avg = avg
				mac_address = key
		if mac_address == "":
			break
		significant_mac_list.append(mac_address)
		number_to_get -= 1
	return significant_mac_list

avg_dict = avg_calculate(mac_dict)
significant_mac_list = get_the_most_significant_mac_addresses(avg_dict, number_of_significant_signals)

final_data_dict = {}
for key, signals in mac_dict.iteritems():
	if exist_in_list(significant_mac_list, key):
		if signals == []:
			continue
		final_data_dict[key] = signals

# Tworzymy histogram wystapien sygnalow w celu zapisania do pliku
string_to_be_put_into_file = ""
for key, signals in final_data_dict.iteritems():
	string_to_be_put_into_file += key + "\n"
	last_signal = signals[0]
	counter = 0
	added = False
	for s in signals:
		if s == last_signal:
			counter += 1
			added = False
		else:
			string_to_be_put_into_file += str(last_signal) + "\t" + str(counter) + "\n"
			counter = 1
			last_signal = s
			added = True
	if not added:
		string_to_be_put_into_file += str(last_signal) + "\t" + str(counter) + "\n"

import os

#outputPath = "Result_"+filename
#fout = open(outputPath, 'w')
#fout.write(string_to_be_put_into_file)
#fout.close()

# mac /tab liczba wszystkich sygnalow /enter sila /tab liczba wystapien

# Liczymy odchylenie standardowe

import numpy as np
from scipy.optimize import curve_fit

string_to_be_put_into_file = ""
string_ft_to_be_put_into_file = ""
for key, signals in final_data_dict.iteritems():
	#regular_method
	mean = np.mean(signals)
	std = np.std(signals)
	string_to_be_put_into_file += key + "\t" + str(mean) + "\t" + str(std) + "\n"

	#fitting_curve
	hist, bin_edges = np.histogram(signals, len(set(signals)), density=True)
	bin_centres = (bin_edges[:-1] + bin_edges[1:])/2

	# Define model function to be used to fit to the data above:
	def gauss(x, *p):
		A, mu, sigma = p
		return A*np.exp(-(x-mu)**2/(2.*sigma**2))

	# p0 is the initial guess for the fitting coefficients (A, mu and sigma above)
	p0 = [0.5, mean, std]
	#p0 = [1.,0.,1.]
	try:
		coeff, var_matrix = curve_fit(gauss, bin_centres, hist, p0 = p0)
	except RuntimeError as err:
		string_ft_to_be_put_into_file += key + "\t" + str(mean) + "\t" + str(std) + "\n"
		continue


	string_ft_to_be_put_into_file += key + "\t" + str(coeff[1]) + "\t" + str(coeff[2]) + "\n"
	# Finally, lets get the fitting parameters, i.e. the mean and standard deviation:
	#print 'Fitted mean = ', coeff[1]
	#print 'Fitted standard deviation = ', coeff[2]
	"""
	if key == '1c:aa:07:6e:31:a2':
		import matplotlib.pyplot as plt
		import matplotlib.mlab as mlab
		import math
		x = np.linspace(-80, -40, 100)
		plt.plot(x,mlab.normpdf(x, mean, std))
		plt.hist(signals, bins=len(set(signals)), normed=True)
		plt.show()
	
		#Druga metoda
		from scipy.optimize import curve_fit
		hist, bin_edges = np.histogram(signals, len(set(signals)), density=True)
	
		bin_centres = (bin_edges[:-1] + bin_edges[1:])/2
	
		# Define model function to be used to fit to the data above:
		def gauss(x, *p):
			A, mu, sigma = p
			return A*np.exp(-(x-mu)**2/(2.*sigma**2))

		# p0 is the initial guess for the fitting coefficients (A, mu and sigma above)
		p0 = [1, mean, std]
		coeff, var_matrix = curve_fit(gauss, bin_centres, hist, p0 = p0)

		# Get the fitted curve
		hist_fit = gauss(bin_centres, *coeff)

		#new_hist_fit = gauss(new_bin_centers, *coeff)
		x_fit = np.linspace(bin_centres[0], bin_centres[-1], 50)
		y_fit = gauss(x_fit, *coeff)
		plt.plot(x_fit, y_fit, lw=1, color="r")

		# Finally, lets get the fitting parameters, i.e. the mean and standard deviation:
		print 'Fitted mean = ', coeff[1]
		print 'Fitted standard deviation = ', coeff[2]
		plt.plot(x,mlab.normpdf(x, coeff[1], coeff[2]), color="y")
		plt.show()
	"""
	
outputPath = "Gathering Data/StdResult_"+file
fout = open(outputPath, 'w')
fout.write(string_to_be_put_into_file)
fout.close()

outputPath = "Gathering Data/FtStdResult_"+file
fout = open(outputPath, 'w')
fout.write(string_ft_to_be_put_into_file)
fout.close()

