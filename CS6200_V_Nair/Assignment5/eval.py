import sys
import operator
import math

def main(args):
    check = 0
    if len(args)<3 or len(args)>4:
        sys.exit("Usage:  trec_eval.py [-q] <qrel_file> <trec_file>\n\n")

    if len(args) == 4:
        args.pop(1)
        check=1

    qrel_file = args[1]
    trec_file = args[2]
    qrel={}
    num_rel={}
    trec={} 
    length={}   
    try:
        f = open(qrel_file)
        data = f.read().splitlines()
        f.close()
    
	while(data!=[]):
	   
	    b=data.pop(0)
	    topic,dummy,doc_id,rel= b.split()


            try:
		if doc_id in qrel[topic]:
		    length[topic][doc_id]+=1	
		    qrel[topic][doc_id] += float(rel)
		else:
		    length[topic][doc_id]=1
		    qrel[topic][doc_id]=float(rel)   	    
	    except KeyError:
        	qrel[topic]={}
		qrel[topic][doc_id]=float(rel)
		length[topic]={}
		length[topic][doc_id]=1


	for k in qrel.keys():
		for j in qrel[k].keys():
			if round(float(qrel[k][j])/length[k][j]) >=1:
				qrel[k][j]=1
			else: 	qrel[k][j]=0



	for k in qrel.keys():
		for j in qrel[k].keys():
			try:	
				num_rel[k]+= float(qrel[k][j])
			except KeyError:
       	    			num_rel[k]=float(qrel[k][j])

    except IOError:
	print "Cannot open"

    	
    try:
        
        f = open(trec_file)
        data = f.read().splitlines()
        f.close()
	#print(len(data))
        for d in data:
	    try:
	    	#b=data.pop(0)
            	topic,dummy,doc_id,dummy,score,dummy= d.split()
            	trec[topic][doc_id]=float(score)
    	    except KeyError:
		trec[topic]={}
            	trec[topic][doc_id]=float(score)
		
            
    except IOError:
        print "Cannot open"

    


    recalls = [0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0]
    cutoffs = [5,10,20,50,50,100]

    num_topics=0;
    tot_num_ret =0
    tot_num_rel=0
    tot_num_ret_rel =0
    sum_prec_at_cutoffs =[0]*1001
    sum_rec_at_cutoffs =[0]*1001
    sum_prec_at_recalls =[0]*1001
    avg_prec_at_cutoffs = [0]*1001
    avg_rec_at_cutoffs = [0]*1001
    avg_prec_at_recalls = [0]*1001
    sum_avg_prec = 0
    sum_r_prec =0
    list_ndcg=[]
   
    
    for topic in sorted(trec):
	    if (num_rel[topic]):
       	        num_topics+=1
        	href= trec[topic]       
		prec_list = [0]*1001
        	rec_list = [0]*1001
		relev =[]
	        num_ret =0
	        num_rel_ret =0
	        sum_prec=0

	        for doc_id in sorted(href,key=lambda x:href[x],reverse=True):
	            num_ret+=1
		    try:
		        rel= qrel[topic][doc_id]
			relev.append(rel)
		    except KeyError:   	
			rel=0.0
			relev.append(rel)
			
		
		     
	
	            if(rel):
	                sum_prec+=float(rel*(1+num_rel_ret))/num_ret
	                num_rel_ret+=rel
	
		    
	
	            prec_list[num_ret]= num_rel_ret/float(num_ret)
	            rec_list[num_ret] = num_rel_ret/float(num_rel[topic])
	
		   
	            if(num_ret >=1000):
			break
		    
		
		dcg=relev[0]
		for i in range(1,len(relev)):
		    dcg+=relev[i]/float(math.log(i+1,2))

		relev.sort(reverse=True)
	        dcg1=relev[0]
		for i in range(1,len(relev)):
		    dcg1+= relev[i]/float(math.log(i+1,2))
		
		ndcg=dcg/float(dcg1)
		
		list_ndcg.append(ndcg)
		
		avg_prec= sum_prec/float(num_rel[topic])


    		final_recall= num_rel_ret/float(num_rel[topic])

	
        	for i in range(num_ret+1,1001):
        	    prec_list[i]=num_rel_ret/float(i)
        	    rec_list[i]= final_recall
	
	
        	prec_at_cutoffs= []
        	for cutoff in cutoffs:
        	    prec_at_cutoffs.append(prec_list[cutoff])
		
		rec_at_cutoffs= []
		for cutoff in cutoffs:
        	    rec_at_cutoffs.append(rec_list[cutoff])
	
        	if num_rel[topic]>num_ret:
        	    r_prec= num_rel_ret/float(num_rel[topic])
	
        	else:
        	    int_num_rel = int(num_rel[topic])
        	    frac_num_rel = num_rel[topic] - int_num_rel
        	    if(frac_num_rel >0):
			r_prec= (1- frac_num_rel)*prec_list[int_num_rel]+ frac_num_rel*prec_list[int_num_rel+1] 
		    else:
			r_prec= prec_list[int_num_rel]
	
        	max_prec =0
        	for i in range(1000,0,-1):
        	    if prec_list[i] > max_prec:
        	        max_prec= prec_list[i]
        	    else:
        	        prec_list[i]=max_prec
	
        	prec_at_recalls = []
        	i =1

        	for recall in recalls:
        	    while(i <=1000 and rec_list[i] < recall):
        	        i+=1
        	    if(i <=1000):
        	        prec_at_recalls.append(prec_list[i])
        	    else:
        	        prec_at_recalls.append(0.0)

		
		if(check):
		    eval_print(topic,num_ret,num_rel[topic],num_rel_ret,prec_at_recalls,avg_prec,prec_at_cutoffs,rec_at_cutoffs,r_prec,ndcg)
	
        	tot_num_ret +=num_ret
        	tot_num_rel += num_rel[topic]
        	tot_num_ret_rel += num_rel_ret
	
	
	
	
        	for i in range(0,len(cutoffs)):
        	    sum_prec_at_cutoffs[i]+= prec_at_cutoffs[i]
		

		for i in range(0,len(cutoffs)):
        	    sum_rec_at_cutoffs[i]+= rec_at_cutoffs[i]
		    
	
        	for i in range(0,len(recalls)):
        	    sum_prec_at_recalls[i]+= prec_at_recalls[i]
        	
	
        	sum_avg_prec+=avg_prec
        	sum_r_prec+=r_prec

        


  
    for i in range(0,len(cutoffs)):
	    avg_prec_at_cutoffs[i] = sum_prec_at_cutoffs[i]/float(num_topics)


    for i in range(0,len(cutoffs)):
	    avg_rec_at_cutoffs[i] = sum_rec_at_cutoffs[i]/float(num_topics)


    for i in range(0,len(recalls)):
	    avg_prec_at_recalls[i] = sum_prec_at_recalls[i]/float(num_topics)
    
    mean_dcg= sum(list_ndcg)/float(num_topics)
    mean_avg_prec = sum_avg_prec/float(num_topics)
    avg_r_prec = sum_r_prec/float(num_topics)
    	
    eval_print(num_topics,tot_num_ret,tot_num_rel,tot_num_ret_rel,avg_prec_at_recalls,mean_avg_prec,avg_prec_at_cutoffs,avg_rec_at_cutoffs,avg_r_prec,mean_dcg)




def eval_print(a,s,d,f,g,h,j,l,k,m):
  print ("Queryid (Num):    %5d" % int(a))
  print ("Total number of documents over all queries")
  print ("    Retrieved:    %5d" % int(s))
  print ("    Relevant:     %5d" % int(d))
  print ("    Rel_ret:      %5d" % int(f))
  print ("Interpolated Recall - Precision Averages:")
  print ("    at 0.00       %.4f"% float(g[0]))
  print ("    at 0.10       %.4f"% float(g[1]))
  print ("    at 0.20       %.4f"% float(g[2]))
  print ("    at 0.30       %.4f"% float(g[3]))
  print ("    at 0.40       %.4f"% float(g[4]))
  print ("    at 0.50       %.4f"% float(g[5]))
  print ("    at 0.60       %.4f"% float(g[6]))
  print ("    at 0.70       %.4f"% float(g[7]))
  print ("    at 0.80       %.4f"% float(g[8]))
  print ("    at 0.90       %.4f"% float(g[9]))
  print ("    at 1.00       %.4f"% float(g[10]))
  print ("Average precision (non-interpolated) for all rel docs(averaged over queries)")
  print ("                  %.4f"% float(h))
  print ("Precision:")
  print ("  At    5 docs:   %.4f"% float(j[0]))
  print ("  At   10 docs:   %.4f"% float(j[1]))
  print ("  At   20 docs:   %.4f"% float(j[2]))
  print ("  At   50 docs:   %.4f"% float(j[3]))
  print ("  At   100 docs:   %.4f"% float(j[4]))
  print ("Recalls:")
  print ("  At    5 docs:   %.4f"% float(l[0]))
  print ("  At   10 docs:   %.4f"% float(l[1]))
  print ("  At   20 docs:   %.4f"% float(l[2]))
  print ("  At   50 docs:   %.4f"% float(l[3]))
  print ("  At   100 docs:   %.4f"% float(l[4]))
  print ("DCG %.4f" % float(m))
  print ("R-Precision (precision after R (= num_rel for a query) docs retrieved):")
  print( "    Exact:        %.4f"% float(k))





if __name__ == '__main__':
    main(sys.argv)
