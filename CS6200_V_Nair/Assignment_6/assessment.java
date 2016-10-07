import java.io.*;
import java.util.*;
import java.util.Map.Entry;

//import de.bwaldvogel.liblinear.Linear;

public class assessment
{
	HashMap<String,ArrayList<String>> pair = new HashMap<>();

	public static void main(String args[])
	{
		assessment a = new assessment();
		a.run();
		a.write();
		a.buildMatrix();
	}	
	
	public void run()
	{
		String line = "";
		String file="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/qrels.adhoc.51-100.AP89.txt";
	    BufferedReader bufferedReader = null;
	    try {
	      bufferedReader = new BufferedReader(new FileReader(new File(file)));
	      while ((line = bufferedReader.readLine()) != null) {
	    	String lines[]= line.split(" ");
	    	if(pair.containsKey(lines[0]))
	    	{
	    		ArrayList<String> a = pair.get(lines[0]);
	    		a.add(lines[2]);
	    		a.add(lines[3]);
	    		pair.put(lines[0],a);
	      }
	    	else
	    		{
	    		ArrayList<String> a =new ArrayList<String>();
	    		a.add(lines[2]);
	    		a.add(lines[3]);
	    		pair.put(lines[0], a);
	    		
	    		}
	    	}
	    } catch (FileNotFoundException e)
	    {
	    	e.printStackTrace();
	
	}
	    catch (IOException e)
	    {
	    	e.printStackTrace();
	}
	   
	}
	
	public void write()
	{
		String file="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/query_filename.txt";
	    BufferedWriter bufferedWriter = null;
	    try {
	    	bufferedWriter = new BufferedWriter(new FileWriter(new File(file)));
//	    	System.out.println(pair.size());
	    	for(Entry<String, ArrayList<String>> e: pair.entrySet())
	    	{
	    		for(int i = 0;i < e.getValue().size()-1;i=i+2)
	    		bufferedWriter.write(e.getKey() + " " + e.getValue().get(i)+ " " + e.getValue().get(i+1)+ "\n");
	    	}
	    	
	    	bufferedWriter.close();
	    	}catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	}
	
	public void buildMatrix()
	{
		ArrayList<String> sb =new ArrayList<>();
		for(Entry<String, ArrayList<String>> e: pair.entrySet())
    	{
			System.out.println(e.getKey());
			for(int i = 0;i < e.getValue().size()-1;i=i+2)
			{
				int counter=0;
				String line = "";
			String file="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/tf_result.txt";
			String file1="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/tfidf_result.txt";
			String file2="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/bm25_result.txt";
			String file3="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/laplace_result.txt";			
			String file4="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/mercer_result.txt";
			BufferedReader bufferedReader = null;
		    try {
		      bufferedReader = new BufferedReader(new FileReader(new File(file)));
		      while ((line = bufferedReader.readLine()) != null) {
			    	String lines[]= line.split(" ");
			    	if(e.getKey().equals(lines[0]) && e.getValue().get(i).equals(lines[2]))
			    	{
			    	//	System.out.println("Found");
			    		sb.add(e.getKey());
			    		sb.add(e.getValue().get(i));
			    		sb.add("1:"+ lines[4]);
			    	break;
			    	}
    	}
		      bufferedReader.close();

		      if (line == null)		    	  
		      {
		    	  sb.add(e.getKey());
		    	  sb.add(e.getValue().get(i));
		      		counter++;
		    	  	//continue;
		      } 
		      bufferedReader = new BufferedReader(new FileReader(new File(file1)));
				      while ((line = bufferedReader.readLine()) != null) {
					    	String lines[]= line.split(" ");
					    	if(e.getKey().equals(lines[0]) && e.getValue().get(i).equals(lines[2]))
					    	{
					    		sb.add("2:"+lines[4]);
					    		break;
					    	}
		    	} 
				      
				      bufferedReader.close();

				      if(line==null)
				      {
				    	counter++;  	
				      }
		    

				      
				bufferedReader = new BufferedReader(new FileReader(new File(file2)));
						      while ((line = bufferedReader.readLine()) != null) {
							    	String lines[]= line.split(" ");
							    	if(e.getKey().equals(lines[0]) && e.getValue().get(i).equals(lines[2]))
							    	{
							    		sb.add("3:"+lines[4]);
							    		break;
							    	}
				    	}
						      bufferedReader.close();
						      
						      if(line==null)
						      	{
						    	  counter++;
						    	  //continue;
						      	}	
						      
						      bufferedReader = new BufferedReader(new FileReader(new File(file3)));
							      while ((line = bufferedReader.readLine()) != null) {
								    	String lines[]= line.split(" ");
								    	if(e.getKey().equals(lines[0]) && e.getValue().get(i).equals(lines[2]))
								    	{
								    		sb.add("4:"+lines[4]);
								    		break;
								    	}
					    	}
							      bufferedReader.close();

							      if(line==null)
							      {
							    	  	counter++;
							    	  	//continue;
							      }
					    
	     
									bufferedReader = new BufferedReader(new FileReader(new File(file4)));
								      while ((line = bufferedReader.readLine()) != null) {
									    	String lines[]= line.split(" ");
									    	if(e.getKey().equals(lines[0]) && e.getValue().get(i).equals(lines[2]))
									    	{
									    		sb.add("5:"+lines[4]);
									    		sb.add(e.getValue().get(i+1));
									    		break;
									    	}
						    	}
								      bufferedReader.close();
								      
								      if(line==null)
								      {
								    	  sb.add(e.getValue().get(i+1));
								    	  counter++;
								    	  	//	continue;
								      }
								      
								      if(counter==5)
								      {
								    	  sb.clear();
								    	  continue;
								      }
								      else
								      {
								  	String wfile="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/Lib_matrix_sparse.txt";
									    BufferedWriter bufferedWriter = null;
									    try {
									    	bufferedWriter = new BufferedWriter(new FileWriter(new File(wfile),true));
									    	bufferedWriter.write(sb.get(sb.size()-1));
									    	for(int j = 2; j<sb.size()-1;j++)
									    	bufferedWriter.write(" "+ sb.get(j));
									    	bufferedWriter.write("\n");
									    	bufferedWriter.close();
									    //	sb.clear();
									    }
									    catch (IOException ex)
								    	{
								    		ex.printStackTrace();
								    	}
									    
									  	String wwfile="/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/Lib_ids_query.txt";
									    BufferedWriter bw = null;
									    try {
									    	bw = new BufferedWriter(new FileWriter(new File(wwfile),true));
									    	bw.write( sb.get(0) +" "+ sb.get(1)+"\n");
									    	bw.close();
									    	sb.clear();
									    }
									    catch (IOException ex)
								    	{
								    		ex.printStackTrace();
								    	}

									    
								      }
								      
								  //    System.exit(0);
									    
    	}catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}
			}
			
		    }
		  
	}
}