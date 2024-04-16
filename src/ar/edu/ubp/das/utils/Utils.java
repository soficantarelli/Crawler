package ar.edu.ubp.das.utils;

import org.docear.pdf.PdfDataExtractor;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;

import ar.edu.ubp.das.beans.MetadataBean;
import ar.edu.ubp.das.beans.WebSiteBean;
import ar.edu.ubp.das.crawler.MyCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.CrawlController.WebCrawlerFactory;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

public class Utils {
	
	public static boolean pingURL(String url, int timeout) {
		url = url.replaceFirst("^https", "http");
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			return (200 <= responseCode && responseCode <= 399);
		} catch (IOException exception) {
			return false;
		}
	}
	
	public static CrawlController startCrawler(WebSiteBean website) {
		CrawlController controller = null;
		CrawlConfig config = new CrawlConfig();
        
		File dir = getFolderName(0);
        
		config.setIncludeHttpsPages(true);
        config.setPolitenessDelay(200);
        config.setMaxDepthOfCrawling(2);
        config.setMaxPagesToFetch(10);
        config.setMaxDownloadSize(2097152);
        config.setIncludeBinaryContentInCrawling(true);
		config.setCrawlStorageFolder(dir.toString());
        config.setResumableCrawling(false);
       
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		
        try {
			controller = new CrawlController(config, pageFetcher, robotstxtServer);
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Lanzando Crawler para: " + website.getUrl());
		WebCrawlerFactory<MyCrawler> factory = () -> new MyCrawler(getDomainName(website.getUrl()), website.getIdUser(), website.getIdWebSite(), website.getTags(), website.getFilters());
        controller.addSeed(website.getUrl());
        controller.startNonBlocking(factory, 8);
        return controller;
	}
	
	public static File getFolderName(int i) {
		String path = "/tmp/crawler4j";
		File dir = new File(path + "/crawler" + i);

		if (!dir.exists()) {
			return dir;
		} else {
			return getFolderName(i + 1);
		}
	}

	public static String getDomainName(String url) {
		WebURL weburl = new WebURL();
		weburl.setURL(url);
		return weburl.getDomain();
	}
	
	public static final Map<String, String> mimeTypes = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
	{
	     put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "doc");
	     put("application/vnd.openxmlformats-officedocument.wordprocessingml", "doc");
	     put("application/vnd.oasis.opendocument.text", "doc");
	     put("application/msword", "doc");
	     put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "ppt");
	     put("application/vnd.openxmlformats-officedocument.presentationml", "ppt"); // pptx
	     put("application/vnd.ms-powerpoint", "ppt");
	     put("application/vnd.oasis.opendocument.presentation", "ppt");
	     put("application/pdf", "pdf");
	     put("text/html", "html");
	}};
	
	public static List<String> topWords(String text) {
    	try {
			List<String> stopwords = Files.readAllLines(Paths.get("words.txt"));
			
			
			ArrayList<String> allWords = Stream.of(text.replaceAll("[^\\p{L} ]", "").toLowerCase().split("\\s+"))
					.collect(Collectors.toCollection(ArrayList<String>::new));
			allWords.removeAll(stopwords);
			
			
			
			Map<String, Long> map = allWords.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));
			
			
			
			List<Map.Entry<String, Long>> result = map.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(5)
					.collect(Collectors.toList());
			
			
			
			List<String> words = new ArrayList<String>();
			
			
			
			for (Map.Entry<String, Long> entry : result) {
				words.add(entry.getKey());
			}
			System.out.println(words);
			
			return words;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
}

	public static String removeStopWords(String text) {
		try { 
			List<String> stopwords = Files.readAllLines(Paths.get("words.txt"));
			
			ArrayList<String> allWords = Stream.of(text.toLowerCase().split(" "))
					.collect(Collectors.toCollection(ArrayList<String>::new));
			
			allWords.removeAll(stopwords);
			
			return allWords.stream().collect(Collectors.joining(" "));
		} catch (IOException e) {
			return null;
		}
    }
    
    public static void parseDoc(MetadataBean data) throws Exception {
    	Metadata metadata = new Metadata();
    			
		URL net_url = new URL(data.getUrl());
    	
		InputStream input = TikaInputStream.get(net_url, metadata);
		
		//parameters of parse() method
	    Parser parser = new AutoDetectParser();
	    BodyContentHandler handler = new BodyContentHandler();
	    ParseContext context = new ParseContext();
	      
	    //Parsing the given file
	    parser.parse(input, handler, metadata, context);
		
		
		String text = handler.toString();
		text = text.replaceAll("[^\\p{L}0-9 ]", " ").replaceAll("\\s{2,}", " ");
		
		if (metadata.get("resourceName") != null) {
			data.setTitle(metadata.getValues("resourceName")[0]);
		} else {
			data.setTitle("Documento sin titulo." + data.getType());
		}
		
		data.setTextLength(text.length());
		data.setText(removeStopWords(text));
	}


	public static String getPDFTitle(MetadataBean data) throws Exception {
		String title = null;
		try {
			Metadata metadata = new Metadata();
			
    		URL net_url = new URL(data.getUrl());
    		
    		InputStream input = TikaInputStream.get(net_url, metadata);
    		
    		BodyContentHandler handler = new BodyContentHandler();
    		ParseContext context = new ParseContext();
    		PDFParser parser = new PDFParser();
    		
    		parser.parse(input, handler, metadata,context);
    		input.close();
    		
			title = metadata.get("title");
		} catch (Exception e) {
			throw e;
		}
		return title;
	}
    
    public static String getPDFTitle(byte[] data) throws Exception {
		String title = null;
		try {
			PdfDataExtractor extractor = new PdfDataExtractor(data);
			title = extractor.extractTitle();
		} catch (Exception e) {
			throw e;
		}
		return title;
	}
	
	public static final HttpClient MyHttpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
			.connectTimeout(Duration.ofSeconds(5)).build();
	
	/*public static HttpResponse<String> restCall(String url) throws IOException, InterruptedException {		
		HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(url)).build();
		return MyHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}*/
	
	
	public static String cleanText(String text) {
		return text.replaceAll("[^\\p{L}0-9 ]", " ").replaceAll("\\s{2,}", "  ");
	}
	
}
