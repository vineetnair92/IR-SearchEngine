import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class FileMerger implements Values {

    HashSet<String> set = null;
    HashMap<String, ArrayList<String>> catalogue = null;
    FileOutputStream indexWriter = null;
    BufferedWriter offsetWriter = null;
    RandomAccessFile raf = null;
    File file;
    double last=0;
    boolean check=false;
    File indexRead=null,offsetRead=null;
    int tokenReplacement=0;
    public FileMerger() throws IOException {
        
        indexRead=new File(writeAddress + "index/index.txt");
        offsetRead=new File(writeAddress + "offset/offset.txt");
        //offsetRead.setWritable(true);
        set = new HashSet<String>();
        catalogue = new HashMap<String, ArrayList<String>>();
                raf = new RandomAccessFile(indexRead, "rw");
      // mergeFiles();
       
    }

    public int mergeFiles() throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(offsetRead));
        String read = "";
        while ((read = bf.readLine()) != null) {
            int index = read.indexOf(";");
            String token = read.substring(0, index);
            set.add(token.trim());;

        }
        bf.close();
        System.out.println(set.size());
        System.out.println("Catalogue started");
        startCataloging();
        return set.size();
    }

    public void startCataloging() throws IOException {
        Iterator i = set.iterator();
        while (i.hasNext()) {
            
            while (catalogue.size() <= 25000 && i.hasNext()) {
                String token = i.next().toString();
                catalogue.put(token.trim(), new ArrayList<String>());
                i.remove();
            }
            String read = "";
            
            BufferedReader bf = new BufferedReader(new FileReader(offsetRead));
            File tempFile=new File(writeAddress+"offset/tempOffset.txt");
            BufferedWriter tempWriter=new BufferedWriter(new FileWriter(tempFile));
            read=bf.readLine();
            while ((read = bf.readLine()) != null) {
                int index = read.indexOf(";");
                String token = read.substring(0, index);
                if (catalogue.containsKey(token)) {
                    ArrayList<String> val = catalogue.get(token.trim());
                    val.add(read.substring(index + 1, read.length()));
                    catalogue.put(token, val);
                }else{
                    tempWriter.write(read+"\n");
                }
            }
            bf.close();
            offsetRead.delete();
            tempWriter.flush();
            tempWriter.close();
            
            tempFile.renameTo(offsetRead);
            
            System.out.println("mergingStarted");
            indexWriter=new FileOutputStream(new File(writeAddress + "index/finalIndex.txt"),true);
            //indexWriter = new BufferedWriter(new FileWriter(new File(writeAddress + "index\\finalIndex.txt"),true));
            offsetWriter = new BufferedWriter(new FileWriter(new File(writeAddress + "offset/finalOffset.txt"),true));
            
            mergeCatalogue();
            catalogue = new HashMap<String, ArrayList<String>>();
            indexWriter.flush();
            indexWriter.close();
            offsetWriter.flush();
            offsetWriter.close();
            
            System.gc();
        }
        
       // indexRead.deleteOnExit();
        
        //offsetRead.deleteOnExit();
        
    }

    public void mergeCatalogue() throws IOException {
        double start = 0, end = 0;
        for (Map.Entry<String, ArrayList<String>> entry : catalogue.entrySet()) {
            tokenReplacement++;
            String tokenreq = entry.getKey();
            String token=tokenReplacement+"";
            ArrayList<String> offsets = entry.getValue();
            String val = buildNewIndexFile(token, offsets);
            start = start + end +last;//+ token.trim().getBytes().length + bitsTransfer+last;
            end = val.trim().getBytes().length;
            String index = val.trim()+"\n";//token.trim() + ":" + val.trim()+"\n";

            String offset = tokenreq + ";" + start + ";" + end+"\n";
            last=0;
            end = end + 1;
            ////System.out.println(index);
            indexWriter.write(index.getBytes());
            //indexWriter.write(index);
            offsetWriter.write(offset);
        }
//        indexWriter.write("MergeCompleted");
//        offsetWriter.write("MergeCompleted");
        last=start+end;

    }

    public String buildNewIndexFile(String token, ArrayList<String> offsets) throws IOException {
        String result = "";
        for (int i = 0; i < offsets.size(); i++) {
            raf.seek(0);
            String temp[] = offsets.get(i).split(";");
            ////System.out.println(temp[0]+"   "+temp[1]);
            long lon=Long.MAX_VALUE;
            ////System.out.println(lon);
            double seek=Double.parseDouble(temp[0]);
            if(seek>lon){
            while(seek-lon>=Long.MAX_VALUE){
                raf.seek(lon);
                seek=seek-lon;
                ////System.out.print(seek+" ");
                }
            }
           // //System.out.println(token+" "+(long)seek);;
            raf.seek((long)seek);
            int end =(int) Double.parseDouble(temp[1]);
            byte b[] = new byte[end];
            raf.read(b);
            String tempp = new String(b, "UTF-8");
            //System.out.println(tempp);
            //1;12345;123,123,1234:2;12333;123:
            tempp=compressString(tempp);
            //System.out.println("NEW "+tempp);
            //System.out.println(tempp);
            //tempp=compressPosition(tempp);
            //System.out.println(tempp);
            result += tempp;
        }

        return result;
    }
    
    public String compressString(String tempp){
        int start=0,end=0;
        String finalValue="";
        while((end=tempp.indexOf(":",start))>=0){
            String dBlock=tempp.substring(start,end); //colons are removed do append them at last
            String split[]=dBlock.split(";");
            String compressedID=buildXMLColumns(split[1]);
            String compressedlocations=buildXMLColumns(split[2]);
            String temp=split[0]+";"+compressedID+";"+compressedlocations+":";
            finalValue+=temp;
            start=end+1;
        }
        return finalValue;
                
    }
    public String buildXMLColumns(String val){
        String split[]=val.split(",");
        String allColumns="";
        for(int i=0;i<split.length;i++){
            int number=Integer.parseInt(split[i].trim());
            allColumns=allColumns+","+getEquivColumn(number);
        }
        return allColumns.substring(1,allColumns.length());
        
    }
    public String getEquivColumn(int number){
        String converted = "";
        // Repeatedly divide the number by 26 and convert the
        // remainder into the appropriate letter.
        while (number >= 0)
        {
            int remainder = number % 44;
            converted = (char)(remainder + '<') + converted;
            number = (number / 44)-1;
        }

        return converted;
    }
    public String compressPosition(String tempp){
        int start=0,end=0;
        String result="";
        while((end=tempp.indexOf(":",start))>=0){
            String sub=tempp.substring(start,end);
            int lastIndex=sub.lastIndexOf(";");
            String firstPart=sub.substring(0,lastIndex);
            String lastPart=sub.substring(lastIndex+1,sub.length());
            lastPart=modifyLastPart(lastPart);
            String temp=firstPart+";"+lastPart;
            result=result+temp+":";
            start=end+1;
        }
        return result;
    }
    public String modifyLastPart(String lastPart){
        //System.out.println(lastPart);
        String split[]=lastPart.split(",");
        String temp="";
        for(int i=split.length-1;i>0;i--){
            int big=Integer.parseInt(split[i]);
            int secondBig=Integer.parseInt(split[i-1]);
            temp=","+(big-secondBig)+temp;
        }
        temp=split[0]+temp;
        //System.out.println("Converted "+temp);
        return temp;
    }
public static void main(String arg[]) throws IOException{
    new FileMerger();
}
    
}