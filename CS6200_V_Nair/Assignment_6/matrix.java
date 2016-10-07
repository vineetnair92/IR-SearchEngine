import java.io.*;
import java.util.*;

public class matrix
{
	public static void main(String args[]) throws IOException
	{	
		String file= "/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/train_mat.txt";
		String file1= "/home/vineet/Documents/elasticsearch-1.5.2/config/AP_DATA/Lib_train.txt";
		String line="";
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(file1)));
	    try {
	      bufferedReader = new BufferedReader(new FileReader(new File(file)));
	      while ((line = bufferedReader.readLine()) != null) {
		    	String lines[]=line.split(" ");
		    	bufferedWriter.write(lines[0]+" "+ lines[2]+" "+ lines[3]+
		    			" "+ lines[4]+" "+ lines[5] +lines[6]+"\n");
	      		}
	      bufferedReader.close();
	      bufferedWriter.close();
	    }catch(Exception e)
	    {
	    	e.printStackTrace();
	    }

		
	}
}
