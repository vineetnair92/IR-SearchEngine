import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.lang.Math.*;

public class trial {

 double df = 0.85;
 int count = 1;
 String currentPage;
 double sinkRank;

 Set<String> Pages;
 Set<String> SinkNodes;

 Map<String, String> pagesHavingLinks;
 Map<String, Integer> outLinkesFromQ;

 Map<String, Double> pageRank;
 Map<String, Double> newPageRank;

 int[] combinedPerplexity;
 int combinedPages;
 double perplexity;
 int c= 0;
 RankSorted rankSorted;
 SortedMap<String, Double> rankSortedPages;

  public trial() {
    Pages = new HashSet<String>();
    SinkNodes = new HashSet<String>();
    pagesHavingLinks = new HashMap<String, String>();
    pageRank = new HashMap<String, Double>();
    newPageRank = new HashMap<String, Double>();
    outLinkesFromQ = new HashMap<String, Integer>();
    rankSorted = new RankSorted(pageRank);
    rankSortedPages = new TreeMap<String, Double>(rankSorted);
    combinedPerplexity = new int[4];
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

  
  
  public void calculateRank() {
  
	 
	  while (hasConverged(count)) {
      System.out.println(count + " " + perplexity);

      sinkRank = 0.0;
      
      for (String sinkPage : SinkNodes) {
        sinkRank += pageRank.get(sinkPage);
      }

      for (String page : Pages) {
        newPageRank.put(page, (1.0 - df)/ combinedPages);
        newPageRank.put(page, newPageRank.get(page)
        		+ (df * sinkRank / combinedPages));
        
        if (pagesHavingLinks.containsKey(page)) {
        	for (String link : pagesHavingLinks.get(page).split(" ")) {
              newPageRank.put(page,
                  newPageRank.get(page) + df
                      * pageRank.get(link) / outLinkesFromQ.get(link));
            }
        }
      }

      for (String page : pageRank.keySet()) {
        pageRank.put(page, newPageRank.get(page));
      }
      count++;
    }
  }

  private boolean hasConverged(int i) {
	    boolean result = true;
	    int change=0;

	    perplexity = calculatePerplexity(pageRank);
	     
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
  
  public void splitPagesLinks(String line) {
    String[] currentPageLinks = line.split(":- ", 2);

    if (currentPageLinks.length > 1) {

    	pagesHavingLinks.put(currentPageLinks[0], currentPageLinks[1]);
    	currentPage = currentPageLinks[0];

    	if (!Pages.contains(currentPage)) {
        Pages.add(currentPage);
      }

      String[] links = currentPageLinks[1].split(" ");
     
      for (String link : links) {
      
    	  if (!Pages.contains(link)) {
          Pages.add(link);
        }

    	  if (outLinkesFromQ.containsKey(link)) {
          outLinkesFromQ.put(link, 1 + outLinkesFromQ.get(link));
        } else {
          outLinkesFromQ.put(link, 1);
        }
      }
    } 
    else {
      Pages.add(currentPageLinks[0]);
    }
  }

  
  public void sortedByRank() {
    rankSortedPages.putAll(pageRank);
    int count = 1;
    System.out.println("Top 50 pages sorted by Pagerank");
    for (Entry<String, Double> entry : rankSortedPages.entrySet()) {
      if (count > 500) {
        break;
      } else
        System.out.println((count++) + " - " + entry.getKey() + " - "
            + entry.getValue());
    }
  }



  
  public void CreateGraph(String file) {
    String line = "";
    BufferedReader bufferedReader = null;
    try {
      bufferedReader = new BufferedReader(new FileReader(new File(file)));
      while ((line = bufferedReader.readLine()) != null) {
        splitPagesLinks(line);
      }
      combinedPages = Pages.size();

      double start_page_rank = 1.0 / combinedPages;

      for (String page : Pages) {
        if (!outLinkesFromQ.containsKey(page)) {
          SinkNodes.add(page);
        }
        pageRank.put(page, start_page_rank);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    trial pageRankImpl = new trial();

    pageRankImpl.CreateGraph("C:\\Users\\vineet\\Desktop\\inlinks_merged.txt");
    pageRankImpl.calculateRank();
    pageRankImpl.sortedByRank();  
    
  }

}