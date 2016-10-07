package inlinker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
public class Inlinker {
	private static File file ;
	private static FileWriter fw ;
	private static BufferedWriter bw;
	private static PrintWriter pw ;
	private static HashMap<String, String> inlinks = new HashMap<String, String>();
	public static void main(String[] args) throws IOException {
		try{
			FileReader fileReader = new FileReader("/Users/jenny/Desktop/Outlinks/merged2.txt");
			BufferedReader br = new BufferedReader(fileReader);
			String line="";
			while((line=br.readLine())!=null) {
				String[] comp = line.split(":-");
				if(comp.length>1) {
					String[] urls = comp[1].trim().split(" ");
					for(String ul : urls) {
						if(inlinks.containsKey(ul)) {
							String links = inlinks.get(ul);
							inlinks.remove(ul);
							links=links + " " + comp[0];
							inlinks.put(ul, links);
						} else {
							String links ="";
							links=links + " " + comp[0];
							inlinks.put(ul,links);
						}
					}
				}
				
			}
			br.close();
			fileReader.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Printing inlinks file");
		file = new File("/Users/jenny/Desktop/Outlinks/inlinks.txt");
		fw = new FileWriter(file,true);
		bw= new BufferedWriter(fw);
		pw = new PrintWriter(bw);
		if(!file.exists()) {
			file.createNewFile();
		}
		Iterator it = inlinks.entrySet().iterator();
		while(it.hasNext()) {
			System.out.println("here");
			Entry entry = (Entry) it.next();
			String key = (String) entry.getKey();
			String values = (String) entry.getValue();
			
			pw.println(key + ":-" + values.trim());
			System.out.println(key);
		}
		
		pw.close();
		fw.close();
		bw.close();
	}
	
	
	
}
