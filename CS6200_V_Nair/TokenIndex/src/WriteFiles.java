import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author vineet
 */
public class WriteFiles implements Values {

    String merge = "";
    int counter = 1;
    BufferedReader bf = null;
    FileMerger fm=null;
    int temp=1;
    double last=0;
    BufferedWriter writer,writer2;
    public WriteFiles() {
        
    }
      public void writeFile(HashMap<String, String> map,String id,boolean check) throws IOException {

        String writerr = "";
        String offsetBuilder="";
       double start=0;
       double end=0;
            writer = new BufferedWriter(new FileWriter(new File(writeAddress+"index/" +"index.txt"),check));
            writer2 = new BufferedWriter(new FileWriter(new File(writeAddress+"offset/" +"offset.txt"),check));
            
       //FileOutputStream indexWriter=new FileOutputStream(new File(writeAddress+"index\\" +"index.txt"),true);
       //FileOutputStream offsetWriter=new FileOutputStream(new File(writeAddress+"index\\" +"offset.txt"),check);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String term = entry.getKey();
                String val = entry.getValue();
                start=start+term.trim().getBytes().length+bitsTransfer+end+last;
                end=val.trim().getBytes().length;
                String offsetTemp=term.trim()+";"+start+";"+end+"\n";
                //offsetBuilder=offsetBuilder+term.trim()+";"+start+";"+end+"\n";
                String temp= term.trim() + ":"+ val.trim()+"\n" ;
                //indexWriter.write(temp.getBytes());
                //offsetWriter.write(offsetTemp.getBytes());
                writer.write(temp);
                writer2.write(offsetTemp);
                //writerr = writerr + temp+ "\n";
                end=end+1;
                last=0;
            }
            //fileWriter(writerr,offsetBuilder,id,check);
            last=start+end;
            //writer.write("One merging done "+last+"\n");
            //writer2.write("One merging done "+last+"\n");
            writer.flush();
            writer.close();;
            writer2.flush();
            writer2.close();;
//            
//            offsetWriter.flush();
//            offsetWriter.close();
            //indexWriter.flush();
            //indexWriter.close();
            System.gc();
         }

    
    
    

    public void fileWriter(String write,String offset,String id,boolean check) {
        
        try {
            
//            writer = new FileWriter(new File(writeAddress+"index\\" +"index.txt"),check);
//            writer2 =new FileWriter(new File(writeAddress+"offset\\" +"offset.txt"),check);
            
            //byte[] b=write.getBytes();
            writer.write(write.trim()+"\n");
            writer2.write(offset.trim()+"\n");
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}