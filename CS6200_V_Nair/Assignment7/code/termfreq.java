import java.io.*;
import java.util.*;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;


public class termfreq {
	ArrayList<String> list=new ArrayList<>();
	ArrayList<String> term_test=new ArrayList<>();
	ArrayList<String> term_train=new ArrayList<>();
	termfreq ec=null;
    Client client = null;
    HashMap<String,TreeMap<Integer,Double>> map = new HashMap<>();

    String readAddress = "";

    public termfreq() {
        readAddress = "/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/";
        try {
            Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true).put("number_of_shards", 1).build();
            client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300)).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
            queryReader();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


   
    public static void main(String arg[]){
        new termfreq();
    }

    
       public void queryReader() {
           try {
    
           	BufferedReader bf = new BufferedReader(new FileReader(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/features_unigrams.txt")));
               String read = "";
               while ((read = bf.readLine()) != null) {
                   list.add(read);
                   }
               searchTerm(list);
               bf.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       


       public void searchTerm(ArrayList<String> str) {
           try {
        	   TreeMap<Integer,Double> id_freq = null;
        	   HashMap<String,String> id_spam = new HashMap<>();
        	   HashMap<String,String> id_label = new HashMap<>();
               for (int i = 1; i < str.size(); i++) {
                   
                       String t = str.get(i);
                       
                       SearchResponse resp = client.prepareSearch("spam_vineet").setTypes("document").setExplain(true)
                       		.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setSize(75419)
                       		.setQuery(QueryBuilders.termQuery("text", t)).addFields("label","split").setQueryCache(Boolean.TRUE).execute().actionGet();
                      
                       for (SearchHit hit : resp.getHits().getHits()) {
                    	   String val = hit.getExplanation().toString();
                           int start = val.indexOf("termFreq=") + 9;
                           double tf = Double.parseDouble(val.substring(start, start + 3));
                           String id = hit.getId();
                           String spam_ham = hit.field("label").getValue();
                          // System.out.println(test_train);
                           String test_train = hit.field("split").getValue();
                           id_spam.put(id, spam_ham);
                           id_label.put(id, test_train);
                           if(map.containsKey(id))
                           map.get(id).put(i, tf);
                           else
                           {
                                id_freq=new TreeMap<>();
                               id_freq.put(i, tf);
                               map.put(id,id_freq);
                           }
                       }
                      
               }
              	
              writeToFile("train_tf_featmat.txt","test_tf_featmat", id_spam,id_label);    


           } catch (Exception e) {
               e.printStackTrace();
           }
       }



    
    public void writeToFile(String fileName,String fileName2, HashMap<String,String>i_spam,HashMap<String,String>i_label) {
        BufferedWriter writer = null;
        BufferedWriter writer2 = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/" + fileName)));
            writer2 = new BufferedWriter(new FileWriter(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/" + fileName2)));
            for(Map.Entry<String,TreeMap<Integer,Double>> e: map.entrySet())
	   	    {
            	if(i_label.get(e.getKey()).equals("train"))
            	  {
            		term_train.add(e.getKey());
            		if(i_spam.get(e.getKey()).equals("spam"))
            			writer.write("1" +" ");
            		else writer.write("0"+ " ");
            		for(Map.Entry<Integer,Double> en: e.getValue().entrySet())
            	  
      	   	    {	
            		  writer.write(en.getKey()+ ":" + en.getValue()+" ");
      	   	    }
            	  writer.write("\n");
            	  }
            	else
            	{
            		term_test.add(e.getKey());
            		if(i_spam.get(e.getKey()).equals("spam"))
            			writer2.write("1" +" ");
            		else writer2.write("0"+ " ");
            		for(Map.Entry<Integer,Double> en: e.getValue().entrySet())
            	  
      	   	    {	
            		  writer2.write(en.getKey()+ ":" + en.getValue()+" ");
      	   	    }
            	  writer2.write("\n");
            	  }
	   	    }
  		  writer.flush();
  		  writer.close();
  		  writer2.flush();
  		  writer2.close();

  		  writer = new BufferedWriter(new FileWriter(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/term_train_part1")));
          writer2 = new BufferedWriter(new FileWriter(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/term_test_part1")));
          for(int i =0 ;i<term_train.size();i++)
        	  writer.write(term_train.get(i)+"\n");
          for(int i =0 ;i<term_test.size();i++)
        	  writer2.write(term_test.get(i)+"\n");
          writer.flush();
  		  writer.close();
  		  writer2.flush();
  		  writer2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}    
