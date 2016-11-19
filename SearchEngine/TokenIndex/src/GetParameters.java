//import commonValues.Values;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 *
 * @author vineet
 */
public class GetParameters implements Values {

    int docFrequency = 0, totalTermFrequency = 0;
    public RandomAccessFile raf = null;
    public String positions;
    File indexFile = null;
    HashMap<String, String> idMapper = null;
    public HashMap<String, String> temp2;

    public GetParameters(HashMap<String, String> id) throws IOException {
        idMapper = id;
        indexFile = new File(writeAddress + "index/finalIndex.txt");
        raf = new RandomAccessFile(indexFile, "r");
    }

    public void closeReaders() throws IOException {
        raf.close();
    }

    public HashMap<String, Double> seekBytes(String offset) throws IOException {
        docFrequency = 0;
        totalTermFrequency = 0;
        raf.seek(0);
        String split[] = offset.split(";");
        long start = (long) Double.parseDouble(split[0]);
       // System.out.println(start);
        int size = (int) Double.parseDouble(split[1]);
        raf.seek(start);
        byte b[] = new byte[size];
        raf.read(b);
        String data = new String(b, "UTF-8");
        //System.out.println(data);
        return extractValues(data);
    }

    public HashMap<String, Double> extractValues(String data) {
        int start = 0, end = 0;
        HashMap<String, Double> temp = new HashMap<String, Double>();
        temp2 = new HashMap<String, String>();

        while ((end = data.indexOf(":", start)) >= 0) {
            String sector = data.substring(start, end);
            //System.out.println(sector);
            sector = decryptColumnIds(sector);
            //System.out.println("SEctor  "+sector);
            int firstIndex = sector.indexOf(";");
            int secondIndex = sector.indexOf(";", firstIndex + 1);
            double frequencyCount = Double.parseDouble(sector.substring(0, firstIndex));
            String id = sector.substring(firstIndex + 1, secondIndex);
            String pos = sector.substring(secondIndex + 1, sector.length()).trim();
            docFrequency++;
            // System.out.println(frequencyCount+" "+pos);
            totalTermFrequency += frequencyCount;
            String originalId = idMapper.get(id);
            temp.put(originalId, frequencyCount);
            temp2.put(originalId, pos);
            start = end + 1;
        }
        return temp;
    }

    public String decryptColumnIds(String dBlock) {
        String split[]=dBlock.split(";");
        String getDocID=getColumnNumber(split[1]);
        String getposIds=getColumnNumber(split[2]);
        return split[0]+";"+getDocID+";"+getposIds;
    }
    public String getColumnNumber(String val){
        String split[]=val.split(",");
        String result="";
        for(int i=0;i<split.length;i++){
            result=result+","+getXMLColumnNumber(split[i]);
        }
        return result.substring(1,result.length());
    }
    public int getXMLColumnNumber(String column){
        int result = 0;
        for (int i = 0; i < column.length(); i++) {
            result *= 44;
            result += column.charAt(i) - '<'+1;
        }
        return result-1;
    }
}