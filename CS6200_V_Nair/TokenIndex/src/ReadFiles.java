import java.io.*;
import java.lang.String;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author vineet
 */
public class ReadFiles implements Values {
	static long docID = 0;
    BuildIndex buildIndex = null;
    public static LinkedHashMap<Long, String> docids = new LinkedHashMap<>();
    public static LinkedHashMap<Long, Integer> doclen = new LinkedHashMap<>();
    HashMap<Long,String> docname = new HashMap<>();
    public static HashMap<String,Long> dup = new HashMap<>();

    WriteFiles write = null;
    HashSet<String> stopWords = null;

    public ReadFiles() {
        buildIndex = new BuildIndex();
        write = new WriteFiles();
        initialiseStopWords();
        readFiles(new File(address));
    }

    public void readFiles(File folder) {
        try {
            int counter = 1;
           int kkk=1;
           HashMap<String,String> ids=new HashMap<String,String>();
           int corpusSize=0;
           double startTime=System.currentTimeMillis();
            File[] listOfFiles = folder.listFiles();
            for (File file : listOfFiles) {
                System.out.println(counter+" -> "+file.getName());
               
                int startIndex = 0, endIndex = 0;
                
                String data = readData(file);
                
                while ((endIndex = data.indexOf("</DOC>", startIndex)) >= 0) {
                    
                    String document = data.substring(startIndex, endIndex).trim();
                    String documn = readText(document, "TEXT");
                    //documn = removeRegExp(documn);
                    //documn = documn.replaceAll("\n", " ").replace("\r", " ");;
                    String id = document.substring(document.indexOf("<DOCNO>") + 7, document.indexOf("</DOCNO>")).trim();
                    assignandwritedocids(id);
                    int docLength=buildIndex.buildHashMap(documn.toLowerCase()+" ", stopWords, kkk+"");
                    writedoclength(id,docLength);
                    corpusSize+=docLength;
                    kkk++;
                    startIndex = endIndex + 1;
                   
                }
                
                
                counter++;
            }
            writedocid();
            writedoclen();
             System.out.println(corpusSize);
             System.out.println("Time for IndexBuilding "+(System.currentTimeMillis()-startTime));
             startTime=System.currentTimeMillis();
            FileMerger fileMerger =new FileMerger();
            
            int vocab= fileMerger.mergeFiles();
            ids=buildIDS();
            TreeMap<Long,Integer> docs=buildDocs();
            System.out.println("Time for merging "+(System.currentTimeMillis()-startTime));
            startTime=System.currentTimeMillis();
            ReadQuery rq= new ReadQuery(stopWords, ids , vocab ,corpusSize,docs,dup,docname);
            rq.queryReader();
           rq.writeToFile("tf_result.txt", rq.tf_result);
            rq.writeToFile("bm_result.txt", rq.bm_result);
           rq.writeToFile("laplace_result.txt", rq.laplace_result);
            rq.writeToFile("proximity_result.txt", rq.proximity_result);
            System.out.println("Time for query "+(System.currentTimeMillis()-startTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void assignandwritedocids(String docName) throws IOException {	
   		docID = docID + 1;	
   		docids.put(docID, docName);
   		dup.put(docName,docID);
   		docname.put(docID,docName);
   	}
    
    
    public void writedoclength(String id,int len) throws IOException {	
   		doclen.put(docID, len);
   		
   	}
    
    
    public static void writedocid() throws IOException{
		/*Writing docid to the file*/
		File file = new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/index/ids.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		for (Entry e: docids.entrySet()){
			bw.write(e.getKey() +";"+ e.getValue()+"\n");
		}
		bw.close();
		System.gc();
    }
    
    public static void writedoclen() throws IOException{
		/*Writing docid to the file*/
		File file = new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/index/docs.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		for (Entry e: doclen.entrySet()){
			bw.write(e.getKey() +";"+ e.getValue()+"\n");
		}
		bw.close();
		System.gc();
    }

    
    public HashMap<String,String> buildIDS() throws IOException{
        HashMap<String,String> temp=new HashMap<String,String>();
        BufferedReader bf=new BufferedReader(new FileReader(new File(writeAddress+"index/ids.txt")));
        String read="";
        while((read=bf.readLine())!=null){
            int index=read.indexOf(";");
            String key=read.substring(0,index);
            String val=read.substring(index+1,read.length());
            temp.put(key, val);
        }
        return temp;
    }
    
    
    public TreeMap<Long,Integer> buildDocs() throws IOException{
        TreeMap<Long,Integer> temp=new TreeMap<Long,Integer>();
        BufferedReader bf=new BufferedReader(new FileReader(new File(writeAddress+"index/docs.txt")));
        String read="";
        while((read=bf.readLine())!=null){
            int index=read.indexOf(";");
            long key=Long.parseLong(read.substring(0,index));
            int val=Integer.parseInt(read.substring(index+1,read.length()).trim());
            temp.put(key, val);
        }
        return temp;
    }
    
    public void initialiseStopWords() {
        double temp = System.nanoTime();
        stopWords = new HashSet();
        BufferedReader bf = null;
        try {
            String read;
            bf = new BufferedReader(new FileReader(new File("/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/stoplist.txt")));
            while ((read = bf.readLine()) != null) {
                stopWords.add(read.toLowerCase().trim());
            }
            System.out.println("Stop Words Building Time " + (System.nanoTime() - temp));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
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

    public String removeRegExp(String data) {
        data = data.replaceAll("[^\\w\\d\\s\\.]", "");
        return data;
    }

    public String readData(File file) {
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

    public static void main(String arg[]) {
        new ReadFiles();
    }
}