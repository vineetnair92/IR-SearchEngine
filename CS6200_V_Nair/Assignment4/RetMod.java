import java.io.*;
import java.util.*;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.SimpleQueryParser.Settings;
import org.elasticsearch.node.Node;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import org.elasticsearch.search.SearchHit;


public class RetMod {
    TreeMap res= null;
    String result="";
    private static Node node;

    RetMod ec=null;
    Client client = null;

    public RetMod() {
        try {
            	node = nodeBuilder().client(true).clusterName("harvardcluster").node();
		client = node.client();
      
                searchTerm("Harvard");
                writeFiles(); 
            }
        catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public static void main(String arg[]){
 //       new RetMod();
  //  }


 public void writeFiles(){
       writeToFile("Harvard_ESsearch.txt", result);    
        }

       public void searchTerm(String str) {
           try {
                   /* SearchResponse resp = client.prepareSearch("harvard").setTypes("document").setExplain(true)
                		.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                       		.setQuery(QueryBuilders.termQuery("text", str)).setQueryCache(Boolean.TRUE).execute().actionGet();
                     */ 
               SearchResponse resp = client.prepareSearch().setExplain(true).execute().actionGet();
       //  System.out.println(resp.toString());
               System.out.println(resp.getHits().getHits().length);
                    
                     for (SearchHit hit : resp.getHits().getHits()) {
                           String id = hit.getId();
                           String val = hit.getExplanation().toString();
                           int start= val.indexOf("out_links")+9; 
                           String outlinks = val.substring(start);
                           res.put(id, outlinks);
                       }
//System.out.println(tf.size());
                       result += top1000(res, str);

           } catch (Exception e) {
               e.printStackTrace();
           }
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

            ranking +=  me.getKey() + " " + me.getValue() + "\n";

        }
        
        return ranking;
    }
    
    public void writeToFile(String fileName, String file) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File("/Users/abhisheksawarkar/Desktop/" + fileName)));
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
