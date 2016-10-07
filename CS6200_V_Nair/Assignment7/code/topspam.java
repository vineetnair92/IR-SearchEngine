import java.io.*;
import java.util.*;

public class topspam
{
	TreeMap<String,Double> score_id= new TreeMap<>();
	public static void main(String args[])
	{
		topspam r = new topspam();
		r.createrankfile();
	
	}
	public void createrankfile()
	{
		int count=1;
		double id =1;
		String result="";
		HashMap<Double,Double> score=new HashMap<>();
		HashMap<Double,String> id_query=new HashMap<>();
		String line = "";
		String file="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/train_spam_linear.txt";
	    BufferedReader bufferedReader = null;
	    try {
	      bufferedReader = new BufferedReader(new FileReader(new File(file)));
	      while ((line = bufferedReader.readLine()) != null) {
	    	  if(count<7)
	    	  {
	    		  count+=1;
	    		  continue;
	    	  }
	    	  else
	    		  {
	    		  score.put(id,Double.parseDouble(line));
	    		  id+=1;
	    		  }
	      }
	      bufferedReader.close();
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
	    
		String file1="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/term_id.txt";
	    	 try {
	   	      bufferedReader = new BufferedReader(new FileReader(new File(file1)));
//	   	      System.out.println(score.size());
	   	      while ((line = bufferedReader.readLine()) != null) {
	   	    	    String[] lines= line.split(":");
	   	 	    String a="";
	   		    String b="";

	   	    	    for(int i=0;i<lines.length;i++)
	   	    	    {
	   	    	    	if(lines[i].matches("[0-9]*"))
	   	    	    
	   	    	    		a=lines[i];
	   	    	    	else b+=lines[i];
	   	    	    }
	   	    	   // System.out.println(b);  
	   	    		  id_query.put(Double.parseDouble(a),b);
	   	      }
	   	      
	   	      bufferedReader.close();
	    	 }catch(Exception e)
	 	    {
	 	    	e.printStackTrace();
	 	    }
	    	 
	   	      for(double i=0.0;i<score.size();i++)
	   	      {
	   	    	  if (id_query.containsKey(i))
	   	    		{
	   	    	  score_id.put(id_query.get(i), score.get(i));
	   	    	  }
	   	      }
        	
	   	      result+= top1000(score_id);
	   	    
	   	    writeFile(result);
	    
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

	

    public String top1000(TreeMap tm) {
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
            Map.Entry me = (Map.Entry) si.next();
            if (++k == 101) {
                break;
            }
            String doc_id=me.getKey()+"";             
//            String Qnumber= lines[0];
            	ranking += k + " " + doc_id + " " + me.getValue() + "\n";

        }
        
        return ranking;
    }

    
    public void writeFile(String score_ids)
    {
   	 
   	 BufferedWriter bw=null;
   	 String file2="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/trec_test_rank_spam.txt";
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