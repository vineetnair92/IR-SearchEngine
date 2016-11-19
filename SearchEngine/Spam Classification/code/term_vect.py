from elasticsearch import Elasticsearch 

es=Elasticsearch()

split = unicode('split')
source = unicode('_source')
label = unicode('label')
train = unicode('train')
test = unicode('test')
tv = unicode('term_vectors')
text = unicode('text')
terms = unicode('terms')
tf = unicode('term_freq')
spam=unicode('spam')
ham=unicode('ham')

train_term_id={}
test_term_id={}
train_term_tf={}
test_term_tf={}
term_id={}

count=0
f1 =open("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/train_spam_feature.txt","w+")
f2 =open("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/test_spam_feature.txt","w+")
f6 =open("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/train_term_id.txt","w+")
f5 =open("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/test_term_id.txt","w+")
f4 =open("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/term_id.txt","w+")
for i in range(1,75420):
	try:

		x = es.termvector(index='spam_vineet', doc_type='document', id='inmail.'+ str(i))
		l=x[tv][text][terms].items()
		y = es.get(index='spam_vineet', doc_type='document', id='inmail.'+ str(i))
		types= y[source][split]
		labels= y[source][label]
		check=unicode(types)
		c=unicode(labels)
		if(check==test):
			test_id_tf={}
			for entry in l:
				key=entry[0]
				keys = str(entry[0])
				test_term_tf[keys]=entry[1][tf]
				if(term_id.has_key(keys)):
					test_id_tf[term_id[keys]]= entry[1][tf]
				else:	
					count=count+1
					test_id_tf[count]=entry[1][tf]
					term_id[keys]=count
			if(c==spam):	
				f2.write(str(0)+ " ")
			else:
				f2.write(str(1)+" ")
			
			f5.write('inmail.'+ str(i)+"\n")

			for key in sorted(test_id_tf):
				f2.write(str(key) + ":"+ str(test_id_tf[key])+" ")
			f2.write("\n")    	
		else:
			train_id_tf={}
			for entry in l:
				key=entry[0]
				keys = str(entry[0])
				train_term_tf[keys]=entry[1][tf]
				if(term_id.has_key(keys)):
					train_id_tf[term_id[keys]]= entry[1][tf]
				else:	
					count=count+1
					train_id_tf[count]=entry[1][tf]
					term_id[keys]=count
		
			if(c==spam):
				f1.write(str(0)+ " ")
			else:
				f1.write(str(1)+" ")

			f6.write('inmail.'+ str(i)+"\n")
			for key in sorted(train_id_tf):		
				f1.write(str(key) + ":"+ str(train_id_tf[key])+" ")
				
			f1.write("\n")    	
	
	except KeyError:
		print(i)
	
for (k,v) in term_id.items():
	f4.write(str(k) + ":"+ str(v)+"\n")

#for (k,v) in train_term_id.items():
#	f6.write(str(k) + ":"+ str(v)+"\n")

#for (k,v) in train_term_id.items():
#	f5.write(str(k) + ":"+ str(v)+"\n")


f1.close()
f2.close()
#f3.close()
f4.close()
f5.close()
f6.close()

