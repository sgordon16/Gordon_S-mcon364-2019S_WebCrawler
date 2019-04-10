import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TouroWebScraper {
	
	private int counter = 0;
//	private List<Document> docs = new LinkedList<Document>();
	private HashSet<String> urls = new HashSet<String>();
	private HashSet<String> emails = new HashSet<String>();
	private HashSet<String> scrapedURLs = new HashSet<String>();
	private int responseTime15sec = 0;
    private String touroURL = "https?://[^/]*touro.edu/.*";
    private HashSet<String> courses = new HashSet<String>();
    private Pattern courseNumber = Pattern.compile("\\b\\d{3}\\b");
    private Pattern CRN = Pattern.compile("\\b\\d{5}\\b");
    private Pattern subject = Pattern.compile("\\b[A-Z]{4}\\b");
    private Pattern course = Pattern.compile("\\b\\d{5} \\b[A-Z]{4} \\d{3}\\b");
//	private Pattern urlGroups = Pattern.compile("(https?://)([^/\\.]*).([^/\\.]*).([^/]*)");
//	private Pattern email = Pattern.compile("\\b[a-zA-Z0-9.-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z0-9.-]+\\b");
	
	public TouroWebScraper(String url) throws Exception {
		if(!matchesTouroDomain(url)) {
			throw new Exception("Error: Cannot except URLs outside of Touro domain");
		}
		urls.add(url);
	}

	public TouroWebScraper(List<String> urls) throws Exception {
		this.urls.addAll(urls.stream().filter(url -> matchesTouroDomain(url)).collect(Collectors.toList()));
	}
	
	public void addURL(String url) {
		urls.add(url);
	}
	
	private void safeRequestProtocol(long startTime, long endTime) throws Exception {
		TimeUnit.SECONDS.sleep(3);
		if((endTime / 1000) - (startTime / 1000) >= 15)
			responseTime15sec++;
		if(responseTime15sec == 3) {
			throw new Exception("Error: Response timed out");
		}
	}
	private boolean matchesTouroDomain(String url) {
		return url.matches(touroURL);
	}
	
	public void crawl() throws Exception {
		System.out.println("Crawling...");
		LinkedList<String> list = new LinkedList<String>(urls);
		for(String url : list)
			crawl(url);
		if(counter == 10)
			return;
	}
	 
	private void crawl(String url) throws Exception {
		if(counter == 10)
			return;
		System.out.println(++counter);
		try {
			urls.add(url);
			long startTime = System.currentTimeMillis();
			Document doc = Jsoup.connect(url).get();
			List<String> list = extractURLs(doc);
			extractEmails(doc);
			extractCourses(doc);
			long endTime = System.currentTimeMillis();
			safeRequestProtocol(startTime, endTime);
			scrapedURLs.add(url);
			for(String s : getURLsWithinTouroDomain(list)) {
				if(!scrapedURLs.contains(s))
					crawl(s);
			}
		}
		catch(SocketTimeoutException e) {}
		catch(UnsupportedMimeTypeException e) {}
		catch(HttpStatusException e) {}
		catch(ConnectException e) {}
		
	}
	
	private List<String> extractURLs(Document doc) {
		HashSet<String> set = new HashSet<String>();
		for(Element e : doc.select("a")) {
			set.add(e.absUrl("href"));
		}
		urls.addAll(set);
		return new LinkedList<String>(set);
	}

	public List<String> getURLs() throws Exception {
		return new LinkedList<String>(urls);
	}
	
	private List<String> getURLsWithinTouroDomain(List<String> list) throws Exception {
		return list.stream().filter(url -> matchesTouroDomain(url)).collect(Collectors.toList());
	}
	public List<String> getURLsWithinTouroDomain() throws Exception {
		return getURLs().stream().filter(url -> matchesTouroDomain(url)).collect(Collectors.toList());
	}
	
	public List<String> getURLsOutsideOfDomain() throws Exception {
		return getURLs().stream().filter(url -> !matchesTouroDomain(url)).collect(Collectors.toList());
	}
	
	public List<String> getEmails() {
		return new LinkedList<String>(emails);
	}
	public void extractEmails(Document doc) {
		HashSet<String> set = new HashSet<String>();
		for(Element e : doc.select("a[href~=mailto:.*]")) 
			set.add(e.text());
		emails.addAll(set);
	}
	
	public List<String> getCourses() {
		return new LinkedList<String>(courses);
	}
	public void extractCourses(Document doc) {
		for(Element e : doc.getAllElements()) {
        	Matcher subjectMatcher = subject.matcher(e.text());
            Matcher crnMatcher = CRN.matcher(e.text());
            Matcher courseNumberMatcher = courseNumber.matcher(e.text());
            if(subjectMatcher.find() && crnMatcher.find() && courseNumberMatcher.find()) {
            	courses.add(crnMatcher.group() + " " + subjectMatcher.group() + " " + courseNumberMatcher.group());
            } 	
        }
		
	}
}
