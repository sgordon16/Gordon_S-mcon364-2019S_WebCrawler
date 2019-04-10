import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Program {

	public static void main(String[] args) throws Exception {
		
		List<String> urls = new LinkedList<String>();
		urls.add("https://www.touro.edu/");
		urls.add("https://www.touro.edu/directory/");
		urls.add("https://www.touro.edu/news--events/");
		urls.add("https://www.touro.edu/contact/");
		urls.add("https://las.touro.edu/men/course-schedule/#/collapseTwo");
		TouroWebScraper scraper = new TouroWebScraper(urls);
		scraper.crawl();
		System.out.println("\nURLs within Touro domain:\n");
		for(String s : scraper.getURLsWithinTouroDomain())
			System.out.println(s);
		System.out.println("\nURLs outside of Touro domain:\n");
		for(String s : scraper.getURLsOutsideOfDomain())
			System.out.println(s);
		System.out.println("\nEmail adresses:\n");
		for(String s : scraper.getEmails())
			System.out.println(s);
		System.out.println("\nCourses:\n");
		for(String s : scraper.getCourses())
			System.out.println(s);
		
	}
	public static String getHtmlFromUrl(String url) throws Exception {
		
//		if(!url.matches("*touro.edu*"))
//			throw new Exception("Only Touro's website allowed");
		
		Document doc = Jsoup.connect(url).get();
		
		Elements links = doc.select("a[href]");
		StringBuilder sb = new StringBuilder();
		for(Element e : links) {
			sb.append(e.attr("abs:href") + "\n");
		}
		return sb.toString();
		
//	   try {
//		   
//		   BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
//		   StringBuilder output = new StringBuilder();
//		   String next;
//		   while ((next = in.readLine()) != null)
//			   output.append(next + "\n");
//		   in.close();
//		   return output.toString();
//	   }
//	   catch(Exception e) {
//		   return "ERROR";
//	   }
	}   
}
