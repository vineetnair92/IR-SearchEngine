import java.io.*;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import org.tartarus.snowball.ext.PorterStemmer;

public class IndexingCrawl {
    PorterStemmer ps=null;
    
    IndexingCrawl ec=null;
    ArrayList<String> stopWords=null;
    double corpusLength=0;
    //HashSet vocab;
    //TreeMap docs;
    Client client = null;
    
    
    String readAddress = "";

    public IndexingCrawl() {
        readAddress = "E://crawl//";
        try {

           
//            initialiseStopWords();
            ps = new PorterStemmer();
            Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true).put("number_of_shards", 5).build();
            client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300)).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
            CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate("harvard");
            processIndexing();
           // client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

      public void processIndexing() {
      //  docs = new TreeMap();
        int counter = 0;
        try {
        //  	 File file = new File("/home/vineet/cr.txt");
        	 BufferedReader bf = new BufferedReader(new FileReader(new File("E://crawl//Crawler")));
        	 String str = "";
        	 String text = "";
    		 String id ="";
    		 String outlinks ="";
    		 String raw_html ="";
    		 String field = "";
    		 str = bf.readLine();
        	 while (str != null) {
        		 //System.out.println(str);
        		 if(str.equals("$"))
                
                    {
                   	 //System.out.println(str);
                     counter = (counter + 1) % 4;
                    //System.out.println(counter);  
//                    	if(counter==5)
  //                  	{
    //                	}
                    	
                    	if(counter==1)
                    	{
                    		id = field;
                    		field = "";
                          	 System.out.println(id);
                    	}
                    	
                     	if(counter==2)
                    	{
                    		//text =str;
                     		text = field;
                     		field = "";
                    	}
                     	
                     	if(counter==3)
                    	{
                    		//raw_html =str;
                     		raw_html = field;
                     		field = "";
                    	}
                     	if(counter==0)
                    	{
                    		outlinks = field;
                    		field = "";
                       	//	writetoFile(id,text, raw_html,outlinks);
                  		 buildIndexes(id, text,raw_html,outlinks);	
//                  		 counter=0;
//                   	System.exit(0);	
                
                    	}
                     	}
                    	else
                    	{
    /*                		if(counter%4==1)
                        	{
                        		id += str; 
                        	}
      */                   	/*if(counter%4==2)
                        	{
                        		text += str; 
                        	}
                         	if(counter%4==3)
                        	{
                        		raw_html +=str; 
                        	}
                         	if(counter%4==0)
                        	{
                        		outlinks +=str; 
                        	}*/
                    		field += str;
                    		//if(counter == 4)
                    		//System.out.print(str);
                    	}
                     	str = bf.readLine();
                    
                    }
                              
           } catch (Exception e) {
               e.printStackTrace();
           }

                                    //}
            //}

            //writeToFile(corpusLength);
          
        
    }
    

	public static void writetoFile(String url,String text, String raw, String outlinks)
	{
	    
			try {
				String content = "";
				File file = new File("E://crawl//Crawl_TEST");
				// if file doesnt exists, then create it
				
				//System.out.println(1);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(url + "\n" + text + "\n" + raw + "\n" +outlinks+"\n");
			
				bw.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
	}


       public static void main(String arg[]){
        new IndexingCrawl();
    }
   
       public void writeToFile(double Length){
    	   BufferedWriter writer = null;
           try {
               writer = new BufferedWriter(new FileWriter(new File("E://crawl//Crawler" )));
               writer.write(Double.toString(Length));
               writer.flush();
               writer.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       
       
       
       public void buildIndexes(String id, String text,String raw,String outlinks) {
        try {
            IndexResponse resp = client.prepareIndex("harvard", "document", id).setSource(jsonBuilder().startObject().field("docno",id).field("text", text).field("raw_Source", raw).field("out_links",outlinks).endObject()).execute().actionGet();

        } catch (Exception e) {
            System.out.println("Error" + e.getMessage());
        }

    }

    

    public void initialiseStopWords() {
        BufferedReader bf = null;
        try {
            bf = new BufferedReader(new FileReader(new File(readAddress + "stoplist.txt")));
            stopWords = new ArrayList<String>();
            String read = "";
            while ((read = bf.readLine()) != null) {
                stopWords.add(read.toLowerCase().trim());
            }
//            stopWords.add("Document");
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    
}    
