import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class ranking
{
	TreeMap<String,Double> score_id= new TreeMap<>();
	public static void main(String args[])
	{
		ranking r = new ranking();
		r.createrankfile();
	
	}
	public void createrankfile()
	{
		String result="";
		ArrayList<Double> score=new ArrayList<>();
		ArrayList<String> id_query=new ArrayList<>();
		ArrayList<String> id_test=new ArrayList<>();
		String line = "";
		String file="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/out_train_s11.txt";
	    BufferedReader bufferedReader = null;
	    try {
	      bufferedReader = new BufferedReader(new FileReader(new File(file)));
	      while ((line = bufferedReader.readLine()) != null) {
	      score.add(Double.parseDouble(line));
	      }
	      bufferedReader.close();
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
	    }

		String file1="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/train_id_query.txt";
	    	 try {
	   	      bufferedReader = new BufferedReader(new FileReader(new File(file1)));
//	   	      System.out.println(score.size());
	   	      while ((line = bufferedReader.readLine()) != null) {
	   	    	    String[] lines= line.split(" ");
	   	    	 System.out.println(lines[0]+lines[1]);  
	   	    		  id_query.add(lines[0]+"-"+lines[1]);
	   	      }
	   	      
	   	      bufferedReader.close();
	    	 }catch(Exception e)
	 	    {
	 	    	e.printStackTrace();
	 	    }
	    	 
	    	 BufferedWriter bw=null;
	    	 String file2="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/score_train_id_query.txt";
	    	 try {
	   	      bw = new BufferedWriter(new FileWriter(new File(file2)));
	   	     System.out.println(id_query.size());
	   	      for(int i=0;i<score.size();i++)
	   	      {
	   	    	  score_id.put(id_query.get(i), score.get(i));
	   	    	  bw.write(id_query.get(i)+ " " + score.get(i)+ "\n");
	   	      }
	   	      bw.close();
	   	    for(Map.Entry<String,Double> e: score_id.entrySet())
	   	    {
	   	   String doc_id= e.getKey();             
           String lines[]= doc_id.split("-");
           String Qnumber= lines[0]; 
           if(!id_test.contains(Qnumber))
           {
        	   id_test.add(Qnumber);
        	   result+= top1000(score_id, Qnumber);
	   	    }
	   	    }
	   	    writeFile(result);
	    	 }catch(Exception e)
	    	 {
	    		 e.printStackTrace();
	    	 }
	    
	}
	
	
	class ValueCompare implements Comparator<String> {

	    Map<String, Double> base;

	    public ValueCompare(Map<String, Double> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	    
	}    

	

    public String top1000(TreeMap tm, String Qno) {
        ValueCompare v = new ValueCompare(tm);
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

            String doc_id=me.getKey()+"";             
            String lines[]= doc_id.split("-");
//            String Qnumber= lines[0];
            String doc_name=lines[1]+"-"+lines[2];
            if(Qno.equals(lines[0]))
            	ranking += Qno + " Q0 " + doc_name + " " + k + " " + me.getValue() + " Exp" + "\n";

        }
        
        return ranking;
    }

    
    public void writeFile(String score_ids)
    {
   	 
   	 BufferedWriter bw=null;
   	 String file2="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/trec_train_result.txt";
   	 try {
  	      bw = new BufferedWriter(new FileWriter(new File(file2)));
  	      bw.write(score_ids);
  	      
  	      bw.close();

    }catch(IOException e)
    {
    	e.printStackTrace();
    }
	    
    }
}