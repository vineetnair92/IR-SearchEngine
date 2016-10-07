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

public class Index {
    PorterStemmer ps=null;
    
    Index ec=null;
    ArrayList<String> stopWords=null;
    double corpusLength=0;

    TreeMap docs;
    Client client = null;
    
    
    String readAddress = "";

    public Index() {
        readAddress = "/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/";
        try {

           
            initialiseStopWords();
            ps = new PorterStemmer();
            Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true).put("number_of_shards", 5).build();
            client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300)).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
            CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate("ap_vineet");
            processIndexing();
           // client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readText(String text, String type) {
        String documn = "";
        int startIndex = text.indexOf("<" + type + ">");
        int endIndex = text.indexOf("</" + type + ">");
        while (startIndex >= 0 && endIndex >= 0) {
            documn = documn + "\n" + text.substring(startIndex + 6, endIndex);
            startIndex = text.indexOf("<" + type + ">", startIndex + 1);
            endIndex = text.indexOf("</" + type + ">", endIndex + 1);
        }
        return documn;

    }

    public String readFile(File file) {
        String str = "";
        try {
        	
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            str = new String(data, "UTF-8");
           
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public String removeRegExp(String doc) {
        String text = doc;
        text = text.replaceAll("[^\\w\\s\\-\\$\\:]", "");
      //System.out.println(text);
        return text;
        
    }

    public String performStemming(String text[]) {
        String newText = "";
        for (String s : text) {
            if (!stopWords.contains(s.toLowerCase())) {
                ps.setCurrent(s.toLowerCase());
                ps.stem();
                String t = ps.getCurrent();

                newText += t + " ";
            }
        }
        return newText;
    }

    public void processIndexing() {
        docs = new TreeMap();
        int counter = 0;
        try {

            File folder = new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/ap89_collection");
            File[] listOfFiles = folder.listFiles();
          
            for (File file : listOfFiles) {
            	
            	//System.out.println(file);
            	String docfile= file.getName();

            	if (file.isFile()) {
                   
                    String str = readFile(file);
                   
                    //System.out.println(str);
                   
                    String split[] = str.split("</DOC>");
                  
                  //  System.out.println(split[0]);
                    
                    for (int i = 0; i < split.length - 1; i++) {
                        String text = "";
                        String documn = readText(split[i], "TEXT");
                        counter++;
                        // System.out.println(documn);
                        
                        String head = readText(split[i], "HEAD");
                        String id = split[i].substring(split[i].indexOf("<DOCNO>") + 7, split[i].indexOf("</DOCNO>")).trim();
                    
                       text = (head + " " + documn).trim();
                        String split1[] = removeRegExp(text).split(" ");
                         String newtext = performStemming(split1);
                     //    System.out.println(newtext);
                         buildIndexes(Integer.toString(counter), newtext);
                        // buildIndexes(id, newtext);
                         corpusLength += newtext.length();         
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

       public static void main(String arg[]){
        new Index();
    }
   
       public void buildIndexes(String id, String text) {
        try {
            IndexResponse resp = client.prepareIndex("ap_vineet", "document", id).setSource(jsonBuilder().startObject().field("message", text).endObject()).execute().actionGet();

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
            stopWords.add("Document");
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    
}    
