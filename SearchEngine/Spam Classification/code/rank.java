import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class rank
{
	TreeMap<String,Double> score_id= new TreeMap<>();
	TreeMap<String,String> spam_doc= new TreeMap<>();

	public static void main(String args[])
	{
		rank r = new rank();
		r.creat_spam_doc();
		r.createrankfile();
	
	}
	public void creat_spam_doc()
	{
		String line ="";
	
		String file="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/trec07p/full/index";
	    BufferedReader bufferedReader = null;
	    try {
	      bufferedReader = new BufferedReader(new FileReader(new File(file)));
	      while ((line = bufferedReader.readLine()) != null) {
	    	  String lines[]= line.split(" ");
	    	  lines[1]=lines[1].replaceAll("../data/", "");
	    	  spam_doc.put(lines[1], lines[0]);
	      }
	      
	      }catch(Exception e)
	      {
	    	  e.printStackTrace();
	      }
	}
	public void createrankfile()
	{
		String result="";
		ArrayList<Double> score=new ArrayList<>();
		ArrayList<String> id_query=new ArrayList<>();
		ArrayList<String> id_test=new ArrayList<>();
		String line = "";
		String file="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/test_tf_linear.txt";
	    BufferedReader bufferedReader = null;
	    try {
	    	int b=0;
	      bufferedReader = new BufferedReader(new FileReader(new File(file)));
	      while ((line = bufferedReader.readLine()) != null) {
	    	  if(b==0)
   	    	  { 
   	    		  b+=1;
   	    		  continue;
   	    	  }
	    	  else
	    	  {String lines[]=line.split(" ");
	      score.add(Double.parseDouble(lines[1]));
	      }
	      }
	      bufferedReader.close();
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
	    }

		String file1="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/term_test_part1";
	    	 try {
	   	      bufferedReader = new BufferedReader(new FileReader(new File(file1)));
//	   	      System.out.println(score.size());
	   	      while ((line = bufferedReader.readLine()) != null) {
	   	    	  // String[] lines= line.split(" ");
//	   	    	 	 System.out.println(lines[0]+lines[1]);  
	   	    		  id_query.add(line);
	   	      }
	   	      
	   	      bufferedReader.close();
	    	 }catch(Exception e)
	 	    {
	 	    	e.printStackTrace();
	 	    }
	    	 
	    	 BufferedWriter bw=null;
	    	 String file2="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/score_test_id_tf.txt";
	    	 try {
	   	      bw = new BufferedWriter(new FileWriter(new File(file2)));
	   	     System.out.println(id_query.size());
	   	     
	   	      for(int i=0;i<score.size();i++)
	   	      {
	   	    	  score_id.put(id_query.get(i), score.get(i));
	   	    	  bw.write(id_query.get(i)+ " " + score.get(i)+ "\n");
	   	      }
	   	      bw.close();          
        	
	   	      result+= top1000(score_id);
	   	    
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
            if (++k == 1001) {
                break;
            }
            String doc_id=me.getKey()+""; 
            String Qnumber= spam_doc.get(doc_id);
            	ranking += k + " "+doc_id+" " + Qnumber + " "  + me.getValue() + "\n";

        }
        
        return ranking;
    }

    
    public void writeFile(String score_ids)
    {
   	 
   	 BufferedWriter bw=null;
   	 String file2="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/trec_test_rank_tf.txt";
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