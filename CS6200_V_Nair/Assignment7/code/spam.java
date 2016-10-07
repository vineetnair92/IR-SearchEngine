import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import org.tartarus.snowball.ext.PorterStemmer;

public class spam {
    PorterStemmer ps=null;
    HashMap<String,String> label = new HashMap<>();
    spam ec=null;
    ArrayList<String> stopWords=null;
    double corpusLength=0;
    Client client = null;
    
    
    String readAddress = "";

    public spam() {
        readAddress = "/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/";
        try {
            ps = new PorterStemmer();
            Settings settings = ImmutableSettings.settingsBuilder().put("client.transport.sniff", true).put("number_of_shards", 1).build();
            client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300)).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
            CreateIndexRequestBuilder createIndexRequestBuilder = client.admin().indices().prepareCreate("spam_vin");
            processIndexing();
            //  readFile("");
            // client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String readFile(String file) {
        String result = "",results="";
	String text = "";
        try {
	   	InputStream mailFileInputStream = new FileInputStream(new File (file));
	   	Properties props = new Properties();
	   	Session session = Session.getDefaultInstance(props, null);
	   	MimeMessage m = new MimeMessage(session, mailFileInputStream);
	   	System.setProperty("mail.mime.parameters.strict", "false");
	   	System.setProperty("mail.mime.decodetext.strict", "false");
	   	System.setProperty("mail.mime.base64.ignoreerrors", "true");
	   	System.setProperty("mail.mime.ignoreunknownencoding", "true");
	  	Object contentObject = m.getContent();
		if(contentObject instanceof Multipart)
            	{
                BodyPart clearTextPart = null;
                BodyPart htmlTextPart = null;
                Multipart content = (Multipart)contentObject;
                int count = content.getCount();
                for(int i=0; i<count; i++)
                {
                    BodyPart part =  content.getBodyPart(i);
                    //Object contents = part.getContent();
                    if(part.isMimeType("text/plain"))
                    {
                     	clearTextPart = part;
                        
                    }
                    else if(part.isMimeType("text/html"))
                    {
                     	htmlTextPart = part;
                    }
                    else  
                    	result += m.getSubject()+"\n"+ dumpPart(content.getBodyPart(0));

                }
                

                if(clearTextPart!=null)
                {
                    result += m.getSubject()+"\n"+(String) clearTextPart.getContent();
                }
                 if (htmlTextPart!=null)
                {
                    String html = (String) htmlTextPart.getContent();
                    results = m.getSubject()+"\n"+Jsoup.parse(html).text();
                }
                
                
            }
            
		else if (contentObject instanceof String) // a simple text message
            {
			Pattern p1 = Pattern.compile("text/html", Pattern.CASE_INSENSITIVE);
         	Matcher m1 = p1.matcher(m.getContentType());
         	if(m1.find())
         	{
         		Document doc = Jsoup.parse((String)contentObject);
         		result+= m.getSubject()+"\n"+doc.body().text();
    			
         	}
         	else
         			result += m.getSubject()+"\n"+(String) contentObject;
            }

	  	mailFileInputStream.close();

            }catch(Exception e)
	 	    {
	 	    	e.printStackTrace();
	 	    System.out.println(file);
	 	    }
       
        return result+ results;
	   	    
    }

    
    private String dumpPart(Part p) throws Exception {

        InputStream is = p.getInputStream();
        // If "is" is not already buffered, wrap a BufferedInputStream
        // around it.
        if (!(is instanceof BufferedInputStream)) {
        	is = new BufferedInputStream(is);
        }
        return getStringFromInputStream(is);
    }

private String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String html="";
        String line="",text="";
        boolean flag=false;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
            	line=line.replaceAll("=","");
            	 
            	Pattern p = Pattern.compile("[A-Za-z0-9]^|-.*-|^-|-$");
            	 Matcher m = p.matcher(line);
            	 if(!m.find())
            	 {
                 	Pattern p1 = Pattern.compile("text/html", Pattern.CASE_INSENSITIVE);
                 	Matcher m1 = p1.matcher(line);
                 	Pattern p2 = Pattern.compile("text/plain", Pattern.CASE_INSENSITIVE);
                 	Matcher m2 = p2.matcher(line);

            		 if (m1.find())
            		 {
                	flag=true;
            		 continue;
            		 }
            		  if (m2.find())
                		 continue;
            		  
            		  if(flag)
            			  text+=line;
            		  else
            		  {
            			  sb.append(line);
            			  
            		  }
          }
            }
            //System.out.println(flag);
            if (flag)
            {
            //	System.out.println(text);
				Document doc = Jsoup.parse(text);
				html = doc.body().text();
			//	html = doc.text();
            }
            
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }          
        
        return sb.toString()+"\n" + html;
    }
    
    


    public void processIndexing() {
        try {
        	BufferedReader br = null;
        	String line="";
        	File f = new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/trec07p/full/index");
        	try
        	{
        		 br = new BufferedReader(new FileReader(f));
                 while ((line = br.readLine()) != null) {
                 String lines[]= line.split(" ");
                 lines[1]= lines[1].replaceAll("../data/", "");
                 label.put(lines[1],lines[0]);
                 }
                 
             br.close();    
        	}catch(IOException e)
        	{
        		e.printStackTrace();
        	}
            String str="";
            File folder = new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/trec07p/data/");
            File[] listOfFiles = folder.listFiles();
          int count=0;
            for (File file : listOfFiles) {
            	
            	String docfile= file.getName();
            	
            	if (file.isFile()) {
            		String  files= file.toString();
                     str = readFile(files);
                     String check= removeRegExp(str);
                     buildIndexes(docfile, check);
                     //writeToFile(check,docfile);
            	
            }
            }
            
//            writeToFile(corpusLength);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

       public static void main(String arg[]){
        new spam();
    }
   

       public String removeRegExp(String doc) {
           String text = doc;
           text = text.replaceAll("[^\\w\\s\\-\\$\\:]", "");
         //System.out.println(text);
           return text;
           
       }

       
       public void buildIndexes(String id, String text) {
        try {
        		IndexResponse resp=null;
        		Random randomGenerator = new Random();
        		float rand = randomGenerator.nextFloat();
        		if(rand<=0.2)
        		   resp = client.prepareIndex("spam_vin", "document", id).setSource(jsonBuilder().startObject().field("file_name", id).field("text",text).field("label", label.get(id)).field("split","test").endObject()).execute().actionGet();
        		else
        		   resp = client.prepareIndex("spam_vin", "document", id).setSource(jsonBuilder().startObject().field("file_name", id).field("text",text).field("label", label.get(id)).field("split","train").endObject()).execute().actionGet();
        	 
        } catch (Exception e) {
            System.out.println("Error" + e.getMessage());
        }

    }

    
    public void writeToFile(String Length,String name){
 	   BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/corpus.txt"),true));
            writer.write(Length);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            writer = new BufferedWriter(new FileWriter(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/name"),true));
            writer.write(name);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}    
