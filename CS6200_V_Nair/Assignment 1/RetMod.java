import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.tartarus.snowball.ext.PorterStemmer;


public class RetMod {
    int totalDocs = 84678;
    PorterStemmer ps=null;
    TreeMap tf, tfIDF, bm25,laplace, mercer = null;
    String tf_result="",idf_result="", bm25_result= "", laplace_result="", mercer_result = "";;
    
    RetMod ec=null;
    ArrayList<String> stopWords=null;
    TreeMap docs;
    Client client = null;
    private static HashMap<String,String> map = new HashMap<String,String>();
    String readAddress = "";

    public RetMod() {
        readAddress = "/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/";
        try {
            iniitialiseStopWords();
            ps = new PorterStemmer();
            Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true).put("number_of_shards", 5).build();
            client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300)).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
            queryReader();
            writeFiles(); 
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String removeRegExp(String doc) {
        String text = doc;
        text = text.replaceAll("U.S.", "UNITEDSTATES");
        text = text.replaceAll("[^\\w\\s\\-\\$\\:]", "");
        return text;
// [^a-Z0-9]
    }

   
    public static void main(String arg[]){
        new RetMod();
    }

    

    public void iniitialiseStopWords() {
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new FileReader(new File(readAddress + "stoplist.txt")));
            stopWords = new ArrayList<String>();
            String read = "";
            while ((read = bf.readLine()) != null) {
                stopWords.add(read.toLowerCase().trim());
            }
            stopWords.add("Document");
            bf.close();
           
        } catch (Exception e) {
        	 e.printStackTrace();}

    }
    
 public void writeFiles(){
       writeToFile("tf_result.txt", tf_result);    
       writeToFile("tfidf_result.txt", idf_result);
       writeToFile("bm25_result.txt", bm25_result);
       writeToFile("laplace_result.txt",laplace_result );
       writeToFile("mercer_result.txt", mercer_result);
        }

 


    
    public String readFile(File corpus)
    {
    	 String str = "";
         try {
         	
             FileInputStream fis = new FileInputStream(corpus);
             byte[] data = new byte[(int) corpus.length()];
             fis.read(data);
             fis.close();
             str = new String(data, "UTF-8");
            
         } catch (Exception e) {
             e.printStackTrace();
         }
         return str;
    }


       public void queryReader() {
           try {
            File f = new File(readAddress + "corpus.txt");
            double corpusLen = Double.parseDouble(readFile(f));

           	BufferedReader bf = new BufferedReader(new FileReader(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/query_desc.51-100.short.txt")));
               String read = "",str="";
               int counter = 0;
               while ((read = bf.readLine()) != null) {
                   str = removeRegExp(read).trim();
                   searchTerm(str,corpusLen);
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       

       public void initialiseMaps() {
           tf = new TreeMap();
          tfIDF = new TreeMap();
           bm25 = new TreeMap();
           laplace = new TreeMap();
           mercer = new TreeMap();
       }


       public void searchTerm(String str, double corpus) {
           try {

               initialiseMaps();
               String[] words = str.split(" ");
               int ttf = 0; //total word frequency
               int tfq = 0;
               ArrayList<String> id_list = null;
               HashMap<String, ArrayList<String>> hits_map = new HashMap<String, ArrayList<String>>(); 
               TreeMap cloneset = new TreeMap();
               TreeMap freq = new TreeMap();
               TreeMap<String, Double> vocab = null;
               TreeMap mercerdoc = new TreeMap();
               docs = new TreeMap();
               
               for (int i = 1; i < words.length; i++) {
            	   vocab = new TreeMap<String, Double>();
               	   id_list = new ArrayList<String>();
                   
               	   if (!stopWords.contains(words[i].toLowerCase())) {

                       String temp = str;
                       while (true) {
                           if (temp.contains(" " + words[i] + " ")) {
                               ++tfq;
                               temp = temp.replaceFirst(" " + words[i] + " ", " ");
                           } else {
                               break;
                           }
                       }
                       
                       
                       ps.setCurrent(words[i].toLowerCase());
                       ps.stem();
                       String t = ps.getCurrent();
                       
                       SearchResponse resp = client.prepareSearch("ap_vineet").setTypes("document").setExplain(true)
                       		.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setSize(84678)
                       		.setQuery(QueryBuilders.termQuery("message", t)).setQueryCache(Boolean.TRUE).execute().actionGet();
                      
                       for (SearchHit hit : resp.getHits().getHits()) {
                           int doclen = hit.getSource().toString().length();
                    	   String val = hit.getExplanation().toString();
                           int start = val.indexOf("termFreq=") + 9;
                           double tf = Double.parseDouble(val.substring(start, start + 3));
                        
                           
                           ttf += tf;
                           String id = hit.getId();
                           cloneset.put(id,doclen);
                           mercerdoc.put(id,doclen);
                           vocab.put(id, tf);
                           id_list.add(id);
                       }

                       docs.put(t, ttf);
                       freq.put(t, vocab);
                       hits_map.put(t, id_list);
                
                       for (SearchHit hit : resp.getHits().getHits()) {
                    	   int doclen = hit.getSource().toString().length();   
                           String val = hit.getExplanation().toString();
                           int start = val.indexOf("termFreq=") + 9;
                           double termFrequency = Double.parseDouble(val.substring(start, start + 3));
                           double docFrequency = Double.parseDouble((val.substring(val.indexOf("docFreq=") + 8, val.indexOf(", maxDocs="))).trim());
                           String id = hit.getId();
                                                      
                           double okapitf = calculateOkapiTF(termFrequency, doclen,corpus);
                           double tf_Idf = okapitf * Math.log(totalDocs / docFrequency);
                           double o_bm25 = calculateBM25(termFrequency, docFrequency, tfq, doclen, corpus);

                           if (tf.containsKey(id)) {
                               double balance = ((Double) tf.get(id));
                               tf.put(id, balance + okapitf);
                           } else {
                               tf.put(id, okapitf);
                           }
                           if (tfIDF.containsKey(id)) {
                               double balance = ((Double) tfIDF.get(id));
                               tfIDF.put(id, balance + tf_Idf);
                           } else {
                               tfIDF.put(id, tf_Idf);
                           }

                           if (bm25.containsKey(id)) {
                               double balance = ((Double) bm25.get(id));
                               bm25.put(id, balance + o_bm25);

                           } else {
                               bm25.put(id, o_bm25);
                           }

                   }
               }
               }
               
               Set<String> keys = cloneset.keySet();
               
               Set<String> mercerkey = mercerdoc.keySet();
                       
               for (int i = 1; i < words.length; i++) {
            	   if (!stopWords.contains(words[i].toLowerCase())) {
            		    ps.setCurrent(words[i].toLowerCase());
                        ps.stem();
                        String t = ps.getCurrent();
 
                        ArrayList<String> doclist = hits_map.get(t);
                        
                        for(Iterator<String> it = keys.iterator(); it.hasNext();)
                        {
                            String docid = (String) it.next();
                            
                        	if(doclist.contains(docid))
                        	{
                        		if(!laplace.containsKey(docid))
                        		{
                        			laplace.put(docid, calculateULM( ((TreeMap<String, Double>)freq.get(t)).get(docid),(int)cloneset.get(docid)));
                        		
                        		}
                        		else
                        		{
                        			laplace.put(docid, (Double)laplace.get(docid)+calculateULM( ((TreeMap<String, Double>)freq.get(t)).get(docid),(int)cloneset.get(docid)));
                        		
                        		}
                        	}
                        	else
                        	{
                        		if(!laplace.containsKey(docid))
                        		{
                        			laplace.put(docid, (calculateULM(0,(int)cloneset.get(docid))));
                        		
                        		}
                        		else
                        		{
                        			laplace.put(docid, (Double)laplace.get(docid)+calculateULM(0,(int)cloneset.get(docid)));
                        		
                        		}
                        		
                        	}
                       	}
            	   
               }
               }
               
               
       for (int i = 1; i < words.length; i++) {
    	   if (!stopWords.contains(words[i].toLowerCase())) {
    		    ps.setCurrent(words[i].toLowerCase());
                ps.stem();
                String t = ps.getCurrent();
                ArrayList<String> doclist = hits_map.get(t);
                
                for(Iterator<String> it = mercerkey.iterator(); it.hasNext();)
                {
                    String docid = (String) it.next();
                    
                	if(doclist.contains(docid))
                	{
                		if(!mercer.containsKey(docid))
                		{
                			mercer.put(docid, calculatejMercer( ((TreeMap<String, Double>)freq.get(t)).get(docid),(int)mercerdoc.get(docid),(int)docs.get(t),corpus));
                		}
                		else
                		{
                			mercer.put(docid, (Double)mercer.get(docid)+calculatejMercer( ((TreeMap<String, Double>)freq.get(t)).get(docid),(int)mercerdoc.get(docid),(int)docs.get(t),corpus));
                		}
                	}
                	else
                	{
                		if(!mercer.containsKey(docid))
                		{
                			mercer.put(docid, calculatejMercer(0,1,(int)docs.get(t), corpus));
                		}
                		else
                		{
                			mercer.put(docid, (Double)mercer.get(docid)+calculatejMercer(0,0,(int)docs.get(t),corpus));
                		}
                		
                	}
               	}
    	   
       }
       }
               tf_result += top1000(tf, words[0]);
               idf_result += top1000(tfIDF, words[0]);
               bm25_result += top1000(bm25, words[0]);
              laplace_result += top1000(laplace, words[0]);
              mercer_result += top1000(mercer, words[0]);


           } catch (Exception e) {
               e.printStackTrace();
           }
       }



    public double calculateOkapiTF(double tf, int length,double corpusLength) {
        double okapitf;
        okapitf = tf / (tf + 0.5 + (1.5 * (length/(corpusLength/totalDocs))));

        return okapitf;
    }
    
    public double calculateBM25(double tf, double docfreq, int tfq, int doclen, double corpusLength) {
        double k1 = 1.2, k2 = 10, b = 0.75;
        return ((Math.log((totalDocs + 0.5) / (docfreq + 0.5))) * ((tf + (k1 * tf)) / (tf + (k1 * ((1 - b) +
        		(b * (doclen /(corpusLength/totalDocs))))))) * (tfq + (k2 * tfq) / (tfq + k2)));

    }
    
    public double calculateULM(double tf, int length) {

        double plaplace = (tf + 1) / (length + 177290);
        return Math.log(plaplace);
    }

    
    public double calculatejMercer(double tf, int length, int ttf, double corpusLength) {
        double lambda = 1;
        double plaplace = (lambda * ((tf + 1) / (length + 177290)))
                + ((1 - lambda) * ((ttf - tf) / (corpusLength - length)));

        return Math.log(plaplace);
    }
    
    


    public String top1000(TreeMap tm, String Qnumber) {
        ValueComparator v = new ValueComparator(tm);
        TreeMap<String, Double> tm1 = new TreeMap<String, Double>(v);
        String ranking = "";
        tm1.putAll(tm);
        Set set = tm1.entrySet();
        // Get an iterator
        Iterator si = set.iterator();
        // Display elements
        int k = 0;
        while (si.hasNext()) {
            if (++k == 1001) {
                break;
            }
            Map.Entry me = (Map.Entry) si.next();

            ranking += Qnumber + " Q0 " + me.getKey() + " " + k + " " + me.getValue() + " Exp" + "\n";

        }
        
        return ranking;
    }
    
    public String readFile(String me) {
        String str = "";
     	String value= ""; 
           try {
       	 BufferedReader reader = new BufferedReader(new FileReader("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/doclist.txt"));
      
       	 String line = "";
         while ((line = reader.readLine()) != null)	 {
             String parts[] = line.split(" ");
             map.put(parts[0], parts[1]);
        }
         reader.close();
         
         Set<Entry<String, String>> mapSet = map.entrySet();
         Iterator<Entry<String, String>> mapIterator = mapSet.iterator();
        
         while (mapIterator.hasNext()) {
             Entry<String, String> mapEntry = mapIterator.next();
            
             String keyValue = mapEntry.getKey();
             if(keyValue.equals(me))
             {
             value = mapEntry.getValue();
             break;
             }
        }

           
        }
        catch (Exception e) {
            e.printStackTrace();
}

           return value;
    }

    public void writeToFile(String fileName, String file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/" + fileName)));
            writer.write(file);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ValueComparator implements Comparator<String> {

    Map<String, Double> base;

    public ValueComparator(Map<String, Double> base) {
        this.base = base;
    }

    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        }
    }
    
}    
