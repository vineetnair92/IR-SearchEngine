import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author vineet
 */
public class StopIndex {

    WriteFiles write = null;
    String oldId = "";
    HashMap<String, String> map = null;
    boolean check = false;

    public StopIndex() {
        write = new WriteFiles();
        map = new HashMap<String, String>();
    }

    public int buildHashMap(String data, HashSet<String> stopWords, String id) throws IOException {
        int startIndex = 0, endIndex = 0;
        int docLength=0;
        Matcher m = Pattern.compile("\\w+(\\.?\\w+)*").matcher(data);
        
        while (m.find()) {
            String token = m.group().trim();
            
            //System.out.println(token);
            if (map.size() == 1000) {
                write.writeFile(map, id, check);
                map = new HashMap<String, String>();
//                System.out.println("File dumped till " + oldId);
//                System.out.println("New Map building started for token " + token + " " + id);
                check = true;
                System.gc();
            }
           if (!stopWords.contains(token)) {
                fillMap(token, startIndex, id);
                docLength+=token.length()+1;
            }

            startIndex+=token.getBytes().length+1;
            
        }
        //System.out.println(map);
        oldId = id;
        return docLength;
    }

    public void fillMap(String token, int index, String id) {

        String val = map.get(token);
        if (map.containsKey(token)) {
            val = buildNewVal(val, index, id, token);
            map.put(token, val);
        } else {
            String temp = 1 + ";" + id + ";" + index + ":";
            map.put(token, temp);
        }
    }

    public String buildNewVal(String val, int index, String id, String key) {
        int start = 0, end = 0;
        String token = "";
        boolean check = false;
        while ((end = val.indexOf(":", start)) >= 0) {
            String temp = val.substring(start, end);
            int startIndex = temp.indexOf(";");
            int endIndex = temp.indexOf(";", startIndex + 1);
            String tempID = temp.substring(startIndex + 1, endIndex);
            if (tempID.equalsIgnoreCase(id)) {
                int count = Integer.parseInt(temp.substring(0, temp.indexOf(";"))) + 1;
                String pos = temp.substring(temp.lastIndexOf(";") + 1, temp.length());
                pos = pos + "," + index;
                token = token + count + ";" + id.trim() + ";" + pos.trim() + ":";
                check = true;

            } else {
                token += temp + ":";
            }
            start = end + 1;
        }
        if (!check) {
            val = val + 1 + ";" + id + ";" + index + ":";
            // System.out.println(" Match Not Found " + val);
            return val.trim();
        } else {
            // System.out.println(" Match Found " + token);
            return token.trim();

        }
    }
}