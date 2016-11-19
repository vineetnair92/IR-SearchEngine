
import java.io.*;
import java.util.*;

import org.tartarus.snowball.ext.EnglishStemmer;

/**
 *
 * @author vineet
 */
public class ReadQuery implements Values {

    HashSet<String> stopWords = null;
    TreeMap<String, Double> proximityMap,tf, tfIDF, bm25, laplace;
    HashMap<String, String> proximity;
    EnglishStemmer ps = null;
    HashMap<String, String> catalogue = null;
    public GetParameters gp = null;
    double vocabSize = 0, corpusLength = 0;
    public String tf_result = "", idf_result = "", bm_result = "", laplace_result = "",proximity_result="";
    TreeMap<Long, Integer> docs = null;
    HashMap<String,Long> docids = null;
    HashMap<Long,String> getdocname = null;

    public ReadQuery(HashSet<String> stop, HashMap<String, String> ids, int vocab, int corp, TreeMap<Long, Integer> doc, HashMap<String,Long> dup, HashMap<Long,String> docname) throws IOException {
        vocabSize = vocab;
        docs = doc;
        docids=dup;
        getdocname= docname;
        stopWords = stop;
        corpusLength = corp;
        ps = new EnglishStemmer();
        gp = new GetParameters(ids);
        catalogue = new HashMap<String, String>();
        buildCatalogue();

    }

    public void buildCatalogue() throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(new File(writeAddress + "offset/finalOffset.txt")));
        String read = "";
        while ((read = bf.readLine()) != null) {
            int index = read.indexOf(";");
            String id = read.substring(0, index).trim();
            catalogue.put(id, read.substring(index + 1, read.length()));
        }
    }

    public void queryReader() {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/query_desc.51-100.short.txt")));
            String read = "",str="";
            while ((read = bf.readLine()) != null) {
                str = removeRegExp(read).trim();
                searchTerm(str);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
            
            
    public void buildString(String queryId) {
        tf_result += top1000(tf, queryId);
        bm_result += top1000(bm25, queryId);
        laplace_result += top1000(laplace, queryId);
        proximity_result+=top1000(proximityMap, queryId);


    }

    public void initialiseMaps() {
        tf = new TreeMap<String, Double>();
        bm25 = new TreeMap<String, Double>();
        laplace = new TreeMap<String, Double>();
        proximity = new HashMap<String, String>();
    }

    
    public void searchTerm(String str) {
        try {
 
        	initialiseMaps();
        	String[] words = str.split(" ");	
        	int ttf = 0; //total word frequency
        	 ArrayList<String> id_list = null;
             HashMap<String, ArrayList<String>> hits_map = new HashMap<String, ArrayList<String>>(); 
             TreeMap cloneset = new TreeMap();
             TreeMap freq = new TreeMap();
             TreeMap<String, Double> vocab = null;
             TreeMap doc = new TreeMap();
             int tfq=0;

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
                     
                     
                     String offsets = catalogue.get(t);
                    if (offsets != null) {
                         HashMap<String, Double> map = gp.seekBytes(offsets);
                         double docFrequency = gp.docFrequency;
                      
                         for (Map.Entry<String, Double> entry : map.entrySet()) {
          	
                             String id = entry.getKey();
                             long id_no = docids.get(id);
                             
                             int docLen = docs.get(id_no);
                             cloneset.put(id,docLen);     
          
                             double termFrequency = entry.getValue();
                             vocab.put(id, termFrequency);
                             id_list.add(id);
                         }
                         }

                     freq.put(t, vocab);
                     hits_map.put(t, id_list);

            if (offsets != null) {
                HashMap<String, Double> map = gp.seekBytes(offsets);
                double docFrequency = gp.docFrequency;
                HashMap<String, String> positions = gp.temp2;
           
                for (Map.Entry<String, Double> entry : map.entrySet()) {
 	
                    String id = entry.getKey();
                    long id_no = docids.get(id);
                    
                    String position = positions.get(id);
                   
                    int docLen = docs.get(id_no);
                    double termFrequency = entry.getValue();

                    double okapitf = calculateOkapiTF(termFrequency, docLen);
                    double o_bm25 = calculateBM25(termFrequency, docFrequency, tfq, docLen);
                   
                       if (tf.containsKey(id)) {
                        double balance = ((Double) tf.get(id));
                        tf.put(id, balance + okapitf);
                    } else {
                        tf.put(id, okapitf);
                    }
                    
                    
                    if (bm25.containsKey(id)) {
                        double balance = ((Double) bm25.get(id));
                        bm25.put(id, balance + o_bm25);

                    } else {
                        bm25.put(id, o_bm25);
                    }

                    if (proximity.containsKey(id)) {
                        proximity.put(id, proximity.get(id) + position + ";");
                    } else {
                        proximity.put(id, position + ";");
                    }

                    
                    }
            }
            }	   
             }

             Set<String> keys = cloneset.keySet();
             for (int i = 1; i < words.length; i++) {
          	   if (!stopWords.contains(words[i].toLowerCase())) {
          		    ps.setCurrent(words[i].toLowerCase());
                      ps.stem();
                      String t = ps.getCurrent();
                      
                      String offsets = catalogue.get(t);
                      
                      if (offsets != null) {
                          
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
             }
             findBlurbs(proximity, words.length);
             buildString(words[0]);
             }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double calculateOkapiTF(double tf, int length) {
        double okapitf;
        //System.out.println(corpusLength);
        double val = (tf + 0.5 + (1.5 * (length / (corpusLength / 84678.0))));

        okapitf = tf / val;

        return okapitf;
    }

    public double calculateULM(double tf, int length) {

        double plaplace = (tf + 1) / (length + vocabSize);
        ////System.out.println(tf+ "    "+ Math.log(plaplace));
        return Math.log(plaplace);
    }

    public double calculateBM25(double tf, double docfreq, int tfq, int doclen) {
        double k1 = 1.2, k2 = 10, b = 0.75;
        return ((Math.log((84678 + 0.5) / (docfreq + 0.5))) * ((tf + (k1 * tf)) / (tf + (k1 * ((1 - b) + (b * (doclen / (corpusLength / 84678))))))) * (tfq + (k2 * tfq) / (tfq + k2)));

    }

    
    

    public TreeMap<String,Double> findBlurbs(HashMap<String, String> prox, int count) {
        proximityMap=new TreeMap<String,Double>();
        for (Map.Entry<String, String> entry : prox.entrySet()) {
            String positions[] = entry.getValue().split(";");
            
            //if (positions.length >=(count*9)/10) {
                int blurb=computeBlurb(positions);
                long docLeng=docids.get(entry.getKey());
                int docLength =docs.get(docLeng);
                double tempresult=Math.pow(0.9, ((blurb*1.0)-(count*1.0))/count*1.0);
                double result=((1600+tempresult)*(positions.length*1.0))/(docLength+count+vocabSize*1.0);
               
                proximityMap.put(entry.getKey(),result);
                
            //}
        }
        return proximityMap;
    }

    public int computeBlurb(String[] positions) {
        int minDistance = Integer.MAX_VALUE;
        int[] array = new int[positions.length];
        String[][] D2array = new String[array.length][];
        boolean[] checks = new boolean[positions.length];
        for (int i = 0; i < array.length; i++) {
            D2array[i] = positions[i].split(",");
            array[i] = 0;
            checks[i] = false;
        }
        while (true) {
            int tempMin = Integer.MAX_VALUE, minPos = -1;
            int tempMax = Integer.MIN_VALUE;

            for (int i = 0; i < array.length; i++) {
                if(array[i]>=D2array[i].length){
                    int val = Integer.parseInt(D2array[i][array[i]-1]);
                    if (val < tempMin) {
                        tempMin = val;
                    }
                    if (val > tempMax) {
                        tempMax = val;
                    }
                }
                else {
                    int val = Integer.parseInt(D2array[i][array[i]]);
                     
                    
                    if (val < tempMin) {
                        tempMin = val;
                        minPos = i;
                    }
                    if (val > tempMax) {
                        tempMax = val;
                    }
                  // System.out.println(val+" "+tempMin+"  "+tempMax);
                }
            }
            //System.out.println("\nNew\n");
            if(minPos==-1){
                break;
            }else{
            minDistance = Math.min(minDistance, (tempMax-tempMin));
            array[minPos] = array[minPos] + 1;}
        }
       // System.out.println(minDistance);
        return minDistance;
    }

    
    public String removeRegExp(String doc) {
        String text = doc;
        text = text.replaceAll("U.S.", "UNITEDSTATES");
        text = text.replaceAll("[^\\w\\s\\-\\$\\:]", "");
        return text;
// [^a-Z0-9]
    }

    
    public String top1000(TreeMap tmap, String Qnumber) {
        String ranking = "";
        ValueComparator comparator = new ValueComparator(tmap);
        TreeMap<String, Double> tmap1 = new TreeMap<String, Double>(comparator);
        tmap1.putAll(tmap);
        Set set = tmap1.entrySet();
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

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}