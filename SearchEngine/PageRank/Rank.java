package rank;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
//import java.lang.Math.*;

public class Rank {

 double df = 0.85;
 int count = 1;
 String currentPage;
 double sinkRank;

 Set<Integer> Pages;
 Set<Integer> SinkNodes;

 Map<Integer, ArrayList<Integer>> pagesHavingLinks;
 Map<Integer, Integer> outLinkesFromQ;

 HashMap<String,Integer> Id;
 Map<Integer, Double> pageRank;
 Map<Integer, Double> newPageRank;

 int[] combinedPerplexity;
 int combinedPages;
 double perplexity;
 int c= 0;
 int url_id=1;
 
 RankSorted rankSorted;
 SortedMap<Integer, Double> rankSortedPages;
 ArrayList<Integer> url_inlinks;
 ArrayList<String> Crawled_Links ;
   
  
 public Rank() {
    Pages = new HashSet<Integer>();
    SinkNodes = new HashSet<Integer>();
    Id = new HashMap<String,Integer>();
     Crawled_Links = new ArrayList();
   
    pagesHavingLinks = new HashMap<Integer, ArrayList<Integer>>();
    pageRank = new HashMap<Integer, Double>();
    newPageRank = new HashMap<Integer, Double>();
    outLinkesFromQ = new HashMap<Integer, Integer>();
    rankSorted = new RankSorted(pageRank);
    rankSortedPages = new TreeMap<Integer, Double>(rankSorted);
    combinedPerplexity = new int[2];
  }

  
  public class RankSorted implements Comparator<Integer> {

    Map<Integer, Double> p;

    public RankSorted(Map<Integer, Double> pair) {
      p = pair;
    }

    public int compare(Integer to, Integer from) {
    	if(p.get(to) >= p.get(from))
    	return -1;
    	else return 1;	
    }
  }

  
  public double calculatePerplexity(Map<Integer, Double> map) {
    return Math.pow(2.0, entropy(map));
  }

  
  public static double entropy(Map<Integer, Double> map) {
    double val = 0.0;
    for (Integer page : map.keySet()) {
      val += ((Math.log(map.get(page))) / (Math.log(2.0)) * map.get(page));
    }
    return -val;
  }

  
  
  public void calculateRank() {
  
	 
	  while (hasConverged(count)) {
      System.out.println(count + " " + perplexity);

      sinkRank = 0.0;
      
      for (Integer sinkPage : SinkNodes) {
        sinkRank += pageRank.get(sinkPage);
      }

      for (Integer page : Pages) {
        newPageRank.put(page, (1.0 - df)/ combinedPages);
        newPageRank.put(page, newPageRank.get(page)
        		+ (df * sinkRank / combinedPages));
        
        if (pagesHavingLinks.containsKey(page)) {
        	for (Integer link : pagesHavingLinks.get(page)) {
              newPageRank.put(page,
                  newPageRank.get(page) + df
                      * pageRank.get(link) / outLinkesFromQ.get(link));
            }
        }
      }

      for (Integer page : pageRank.keySet()) {
        pageRank.put(page, newPageRank.get(page));
      }
      count++;
    }
              System.out.println("Done calculating rank");
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
      url_inlinks= new ArrayList<Integer>();

    //if(Crawled_Links.contains(currentPageLinks[0]))
    //{
        
        url_id+=1;
        Id.put(currentPageLinks[0], url_id);
    
    if (currentPageLinks.length > 1) {
    	
        String[] links = currentPageLinks[1].split(" ");
    	  for (String link : links) {
    //if(Crawled_Links.contains(link))
    //{
              if(!Id.containsKey(link))
    	  {
    			url_id+=1;
    			url_inlinks.add(url_id);
    	  }
    	  else
    		  url_inlinks.add(Id.get(link));
    		}
      //    }
          System.out.println("Done Conversion");
    	if (!Pages.contains(Id.get(currentPageLinks[0]))) {
        Pages.add(Id.get(currentPageLinks[0]));
      }

          /*	pagesHavingLinks.put(url_id, url_inlinks);
    	currentPage = currentPageLinks[0];

      for (Integer link : url_inlinks) {
      
    	  if (!Pages.contains(link)) {
          Pages.add(link);
        }

    	  if (outLinkesFromQ.containsKey(link)) {
          outLinkesFromQ.put(link, 1 + outLinkesFromQ.get(link));
        } else {
          outLinkesFromQ.put(link, 1);
        }
    	 
      }*/
    }
    
    else {
        
    	if (!Pages.contains(Id.get(currentPageLinks[0]))) {
        Pages.add(Id.get(currentPageLinks[0]));
      }
    }
    
  }

  
  public void sortedByRank() {
    rankSortedPages.putAll(pageRank);
    int count = 1;
    System.out.println("Top 50 pages sorted by Pagerank");
    for (Entry<Integer, Double> entry : rankSortedPages.entrySet()) {
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
       createMap();
        bufferedReader = new BufferedReader(new FileReader(new File(file)));
      while ((line = bufferedReader.readLine()) != null) {
        splitPagesLinks(line);
      }
      combinedPages = Pages.size();

      double start_page_rank = 1.0 / combinedPages;

      for (Integer page : Pages) {
        if (!outLinkesFromQ.containsKey(page)) {
          SinkNodes.add(page);
        }
        pageRank.put(page, start_page_rank);
      }
    System.out.println("Done Graph creation");
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void createMap()
  {
       String line = "";
    BufferedReader bufferedReader = null;
    try {
    
        bufferedReader = new BufferedReader(new FileReader(new File("/Users/abhisheksawarkar/Desktop/crawled_links.txt")));
      while ((line = bufferedReader.readLine()) != null) {
     Crawled_Links.add(line);
  }
     System.out.println("Done adding crawled links to array");
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    } 
    catch(IOException e)
    {
       e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException {
    Rank t = new Rank();

//  t.CreateGraph("C:\\Users\\vineet\\Desktop\\inlinks_merged.txt");
    t.CreateGraph("/Users/abhisheksawarkar/Desktop/inlinks_merged.txt");
  //  pageRankImpl.CreateGraph("/home/vineet/Desktop/wt2g_inlinks.txt");
    t.writeToFile();

   //t.calculateRank();
   // t.sortedByRank();  
     }
  
  
  
  
       public void writeToFile() throws IOException{
    	   BufferedWriter writer = null;
           try {
               writer = new BufferedWriter(new FileWriter(new File("/Users/abhisheksawarkar/Desktop/try.txt"),true));
                      Iterator it=Id.entrySet().iterator();
                      while(it.hasNext())
                      {
                          
                      Entry entry=(Entry) it.next();
                           writer.write(entry.getKey() + " " + entry.getValue()+"  \n");
                       
           }
                      writer.close();
           }
           catch (IOException e) {
				e.printStackTrace();
			}
       }
	

}