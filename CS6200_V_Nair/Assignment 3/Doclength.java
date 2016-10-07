import java.io.*;
import java.util.*;
import java.net.*;
//import java.sql.*;

//import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

import robots.BaseRobotRules;
import robots.SimpleRobotRules;
import robots.SimpleRobotRules.RobotRulesMode;
import robots.SimpleRobotRulesParser;

import org.apache.commons.io.IOUtils;
public class Doclength
{
	ArrayList<String> robotList = new ArrayList<String>();
	HashMap<String,ArrayList<String>> robot_map = new HashMap<>();
	public static void main(String args[]) throws IOException, InterruptedException, URISyntaxException
	{
		
		int crawlcount = 0;
		Doclength c =new Doclength();
		System.out.println("Crawling Starts:"+ crawlcount);
	//	HashMap<String, String> store_text = new HashMap<>();
	//	HashMap<String, String> store_raw = new HashMap<>();
		LinkedHashMap<String,Integer> frontier = new LinkedHashMap<>();
		Queue<String> visited_nodes = new LinkedList<String>();
//		HashMap<String,Integer> tp = new HashMap<>();
//		HashMap<String,Integer> time = new HashMap<>();

	//	LinkedHashMap<String, LinkedHashSet<String>> matrix = new LinkedHashMap<>();
		LinkedHashSet<String> matrix_children = new LinkedHashSet<>();
		frontier.put("http://en.wikipedia.org/wiki/Harvard_University".toLowerCase(), 10000);
		frontier.put("http://www.harvard.edu",10000);
		frontier.put("http://colleges.usnews.rankingsandreviews.com/best-colleges/harvard-university-166027/overall-rankings",10000);
		frontier.put("http://colleges.usnews.rankingsandreviews.com/best-colleges/harvard-university-2155",10000);
		frontier.put("http://www.webometrics.info/en/world",10000);
		frontier.put("http://www.timeshighereducation.co.uk/world-university-rankings/2014-15/world-ranking/",10000);
		

//		Response response = null;
		
		//String robot_url = "http://www.webometrics.info/robots.txt";
		//c.writeFile();
		while (!frontier.isEmpty()&& visited_nodes.size()<20001)
		{
			boolean check=false;
				String page=GetFrontier(frontier);
			
			URL url = new URL(page);
		
			String robot_url = "";
			robot_url = url.getProtocol() + "://" + url.getHost();
	
			//System.out.println(robot_url);
			if(!robot_url.contains("mailto")||!robot_url.contains("tfrrs"))
			{
				 check = c.createRobotstxtList(robot_url, "Mozilla/5.0");	
			}
			//System.out.println(check);
			else
			{
				 check = false;		
			}
			
			if (!visited_nodes.contains(url.toString()) && check)
			{
				boolean content =false;
				crawlcount+=1;
				System.out.println(crawlcount + " " + url);
				Thread.sleep(2000);
				HttpURLConnection httpcon = (HttpURLConnection) url.openConnection();
				httpcon.setRequestProperty("User-Agent", "Mozilla/5.0");
				httpcon.setRequestProperty("Accept", "text/html");
				httpcon.setConnectTimeout(10000);
				//httpcon.addRequestProperty("Accept-Language", "en-US,en");

				httpcon =c.redirect(httpcon);
				//System.out.println();
							
				if(httpcon.getContentType() != null){
					content = httpcon.getContentType().contains("text/html");
				//	System.out.println(content);
				}
				
					if(content &&  httpcon.HTTP_OK==200)
				{
					visited_nodes.add(url.toString());

					//if(Jsoup.connect(url.toString()).execute().statusCode()==200)
					//{
					 //response=Jsoup.connect(url.toString()).execute();
					try {
					Document doc=Jsoup.connect(url.toString()) .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
				               .timeout(10000).get(); 
					//int status = response.statusCode();
					 //if(status ==200||status ==301||status ==302)
				//	 {
					 //Document pages = Jsoup.parse(page);
//						 Document doc = Jsoup.connect(url.toString()).get();
							
				Elements links = doc.select("a[href]");
				String text = doc.body().text();
				String raw = doc.toString();

				//	if (frontier.contains(url.toString()))
				//	{
					//	tp.put(url.toString(), 0);
					//}
				//System.out.println(raw);

				//tp = new HashMap<>();
				matrix_children = null;
				matrix_children = new LinkedHashSet<>();			
				
				for (Element l: links)
				{
					String link =  l.attr("abs:href");
					//System.out.println("BOY "+link);
					if(link!= "") 					
					link = canonicalise(link.toLowerCase(),url);
					{
					if(!visited_nodes.contains(link.toLowerCase()) && 
							!frontier.containsKey(link.toLowerCase())&& link!="")
					{
						//System.out.println(link);
						frontier.put(link.toLowerCase(),1);
						//tp.put(link, 0);
						//time.put(link, getTime());
					}
					else 
						if (frontier.containsKey(link.toLowerCase()))
				//	{
							//System.out.println("Hello");
					//	if(tp.containsKey(link))
					//		tp.put(link, tp.get(link)+1);
					//	else tp.put(link, 0);
					frontier.put(link.toLowerCase(), frontier.get(link.toLowerCase())+1);
				//	}
					
					matrix_children.add(link);
					}	
//					for (Map.Entry<String, Integer> entry : time.entrySet())
	//				{
		//			time.put(entry.getKey(), entry.getValue() +1);
			//		}
					}
					 
				
				//matrix.put(url.toString(), matrix_children);
				
				writeToFile(text, raw, matrix_children, url.toString());
				
				/*if (content){
					store_text.put(url.toString(),text);
					store_raw.put(url.toString(),raw);
					//System.out.println(text);
				}*/
					 } catch (NullPointerException e) {
					        // TODO Auto-generated catch block
					        e.printStackTrace();
					    } catch (HttpStatusException e) {
					        e.printStackTrace();
					    } catch (IOException e) {
					        // TODO Auto-generated catch block
					        e.printStackTrace();
					    }
					catch (Exception e) {
				        // TODO Auto-generated catch block
				        e.printStackTrace();
				    }
					httpcon.disconnect();
						
				}


								
			}
			//		canonicalise();
		
			frontier.remove(url.toString());
			}
		
		} 
	
	public static String GetFrontier(LinkedHashMap<String,Integer> frontier)
	{
		Map.Entry<String,Integer> maxEntry = null;

		for (Map.Entry<String,Integer> entry : frontier.entrySet())
		{
		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) >0)
		    {
		        maxEntry = entry;
		    }
		}
	//	System.out.println( maxEntry.getKey()+ " D");
		return maxEntry.getKey();
	}
	
	public static void writeToFile(String text,String raw, LinkedHashSet matrix, String url)
	{
	    
			try {
				String content = "";
				File file = new File("/home/vineet/Documents/Crawling.txt");
				// if file doesnt exists, then create it
				
				//System.out.println(1);
				if (!file.exists()) {
					file.createNewFile();
				}
				FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(url + "$$$" + text + "$$$" + raw + "$$$" +matrix.toString()+"$$$");
			
				bw.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
	}

	public boolean createRobotstxtList(String page_url,String user_agent) throws IOException{
		try {
			if(!page_url.contains("mailto"))
			{
			URL urlObj = new URL(page_url);
			String hostId = urlObj.getProtocol() + "://" + urlObj.getHost()
					+ (urlObj.getPort() > -1 ? ":" + urlObj.getPort() : "");
			//System.out.println(hostId);
			Map<String, BaseRobotRules> robotsTxtRules = new HashMap<String, BaseRobotRules>();
			BaseRobotRules rules = robotsTxtRules.get(hostId);
			if (rules == null) {
				String robotsContent = getContents(hostId + "/robots.txt");
				if (robotsContent == null) {
					rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
				} else {
					SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
					rules = robotParser.parseContent(hostId,
							robotsContent.getBytes(), "text/plain",
							user_agent);
				}
			}
			return rules.isAllowed(page_url);
		
			}
			} catch (MalformedURLException e) {
			e.printStackTrace();
			
		}
		catch (Exception e)
		{e.printStackTrace();
		
		}
		return false;
	}

	public String getContents(String page_url) {
		InputStream is = null;
		try {

				URLConnection openConnection = new URL(page_url).openConnection();
			//HttpURLConnection openConnection = (HttpURLConnection)new URL(page_url).openConnection();
			
			openConnection
					.addRequestProperty("User-Agent",
							"Mozilla/5.0");
		
			
			openConnection.setConnectTimeout(10000);
			is = openConnection.getInputStream();
		
			String theString = IOUtils.toString(is);
			
			return theString;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public HttpURLConnection redirect(HttpURLConnection conn) throws IOException
	{
		boolean redirect = false;
		try{ 
		// normally, 3xx is redirect
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status ==HttpURLConnection.HTTP_MOVED_PERM
					)
			redirect = true;
		}
		
		
	 
		//System.out.println("Response Code ... " + status);
	 
		if (redirect) {
	 
			// get redirect url from "location" header field
			String newUrl = conn.getHeaderField("Location");
	 
			// get the cookie if need, for login
		//	String cookies = conn.getHeaderField("Set-Cookie");
	
	 
			// open the new connnection again
			conn = (HttpURLConnection) new URL(newUrl).openConnection();
			conn.setConnectTimeout(20000);
		
		
			//conn.setRequestProperty("Cookie", cookies);
		//	conn.addRequestProperty("Accept-Language", "en-US,en");
			conn.addRequestProperty("User-Agent", "Mozilla/5.0");
		//	conn.addRequestProperty("Referer", "google.com");
		}
			//System.out.println("Redirect to URL : " + newUrl);


		}
		catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		return conn;

	}
	public static String canonicalise(String url,URL parent) throws MalformedURLException, URISyntaxException
	{
		url = url.toLowerCase();

		//System.out.println(url);
		
		while(url.endsWith("/")||url.endsWith("#"))
		{
			url = url.substring(0,url.length()-1);
			//System.out.println(url);
		}


		if(url.startsWith("../"))
		{
		URL baselink = new URL(parent, url);
		url= baselink.toString();
		//System.out.println(url);
		}
	
		
		int secondindex = -1;
		//Removing duplicate slashes
		do{
		int firstindex =url.indexOf("//", 0);
		secondindex = url.indexOf("//", firstindex+1);
		if(secondindex!= -1){
			url = url.substring(0, secondindex) + url.substring(secondindex+1, url.length());
			//System.out.println(url);
		}
		}while(secondindex!=-1);

		
		String ulink  = "";
		//System.out.println(url);
		URL link=null;
		//if(new URL(url).getProtocol().toString()=="")
		//{
			//link = new URL(new URL("http://") , url);
		//}
		
		link = new URL(url);
		
		String a = link.getRef();
		//System.out.println(a);
		if(a!=null)
		{
			int l = link.toString().indexOf("#");	
			//System.out.println(l);
			ulink = link.toString();
			link = new URL(ulink.substring(0, l));
		//	System.out.println(link);	
		}
		
		if(link.getPort()==80 && link.getProtocol().contentEquals("http")||
				link.getPort()==443 && link.getProtocol().contentEquals("https"))
		{

		//	System.out.println(link.getPort());	
			int l = url.indexOf(":80");
			if (l==-1)
			{
				l = url.indexOf(":443");
				ulink = link.toString();
				ulink = ulink.substring(0, l)+ulink.substring(l+4, ulink.length());
			}
			else 
			{
				ulink = link.toString();
				ulink = ulink.substring(0, l)+ulink.substring(l+3, ulink.length());
			}
			//System.out.println(ulink);	
		}
	
		else ulink = link.toString();
		
		
		return ulink;
	}
	
}