import java.io.*;
import java.util.*;
import org.apache.lucene.analysis.util.CharArrayMap.EntrySet;

public class HubsAuth
{
   
      Set<String> Roots;
    Set<String> Pages;
    Map<String, Integer> outLinkesFromQ;
    Map<String, Double> Auth;
    Map<String, Double> Hubs;
    RankSorted rankSorted;
    SortedMap<String, Double> rankSortedPages;

     int[] combinedPerplexity;
    int combinedPages;
    double perplexity;
    int c= 0;
    
    
    public HubsAuth()
    {
    Roots = new HashSet<String>();
    Pages = new HashSet<String>();
    Hubs = new HashMap<String, Double>();
    Auth = new HashMap<String, Double>();
    outLinkesFromQ = new HashMap<String, Integer>();
    rankSorted = new RankSorted(Hubs);
    rankSortedPages = new TreeMap<String, Double>(rankSorted);
   }
     
  public class RankSorted implements Comparator<String> {

    Map<String, Double> p;

    public RankSorted(Map<String, Double> pair) {
      p = pair;
    }

    public int compare(String to, String from) {
    	if(p.get(to) >= p.get(from))
    	return -1;
    	else return 1;	
    }
  }  
  
  public double calculatePerplexity(Map<String, Double> map) {
    return Math.pow(2.0, entropy(map));
  }

  
  public static double entropy(Map<String, Double> map) {
    double val = 0.0;
    for (String page : map.keySet()) {
      val += ((Math.log(map.get(page))) / (Math.log(2.0)) * map.get(page));
    }
    return -val;
  }

    public void readFile()
    {
         String line = "";
    BufferedReader bufferedReader = null;
    try {
    
        bufferedReader = new BufferedReader(new FileReader(new File("/Users/abhisheksawarkar/Desktop/outlinks&url.txt")));
      while ((line = bufferedReader.readLine()) != null) {
        String[] lines= line.split(" ");
        Roots.add(lines[0]);
        for (int i=1 ; i<= lines.length;i++)
       {
        Roots.add(lines[i]);
      }
      
      }
      
      for (String page : Roots)
      {
          Hubs.put(page, 1.00);
          Auth.put(page, 1.00);
      }
      int i=0;
      i+=1;
      while(notConverged(i))
      {
          Double norm=0.00;
//         Auth.get(i)
      }
      

    
    }catch(Exception e)
    {
        e.printStackTrace();
    }
    
  }

  public boolean notConverged(int i)
  {
       boolean result = true;
	    int change=0;

	    perplexity = calculatePerplexity(Hubs);
	     
	    if (i >0) {

	      combinedPerplexity[0] = combinedPerplexity[1];
	      combinedPerplexity[1] = (int) perplexity;

	       change = Math.abs(combinedPerplexity[1]- combinedPerplexity[0]);
	
	      if (change < 1 && c <= 4) {
	        c+=1;
	        result = false;
	      }
	      
	      else {
	        result = true;
	      }
	    }
	    
	    else {
	      combinedPerplexity[0] = (int) perplexity;
	      result = false;
	    }
	    
	    return result;
	   
      
  }

}