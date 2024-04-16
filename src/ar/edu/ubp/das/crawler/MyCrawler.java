package ar.edu.ubp.das.crawler;

import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import ar.edu.ubp.das.beans.MetadataBean;
import ar.edu.ubp.das.elastic.ElasticSearch;
import ar.edu.ubp.das.logger.MyLogger;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ar.edu.ubp.das.utils.Utils;

public class MyCrawler extends WebCrawler {
	private static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|mid|mp2|mp3|mp4|json|wav|avi|flv|mov|mpeg|ram|m4v|rm|smil|wmv|swf|wma|zip|rar|gz|xml|bmp|gif|png|svg|svgz|ico|jpg|jpeg|jpe|jif|jfif|jfi|webp|tiff|tif|psd|raw|arw|cr2|nrw|k25|bmp|dib))$");
	private int idUser;
	private int idWeb;
	MyLogger logger;
	private String tags;
	private String filters;
	
	private String domain;
	private ElasticSearch elastic;
	
	public MyCrawler(String domain, int idUser, int idWeb,String tags, String filters) {
		this.idUser = idUser;
		this.idWeb = idWeb;
		this.elastic = new ElasticSearch();
		this.domain = domain;
		this.tags = tags;
		this.filters = filters;
		logger = new MyLogger("MyCrawler");
		
	}
	
	/**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not
     */
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {		
		String href = url.getURL().toLowerCase();
        return !FILTERS.matcher(href).matches()
        		&& url.getDomain().equals(this.domain);
	}
	
	/**
     * This function is called when a page is fetched and ready to be processed by your program.
     */
	@Override
	public void visit(Page page) {
		try {			
		    MetadataBean metadata = new MetadataBean();
		    metadata.setUserId(this.idUser);
		    metadata.setWebsiteId(this.idWeb);
		    metadata.setApproved(false);
		    metadata.setUrl(page.getWebURL().getURL());
		    
		    //SET TAGS AND FILTERS
		    String[] tags = this.tags.split(",");
		    String[] filters = this.filters.split(",");
		    
		    System.out.print(tags);
		    System.out.print(filters);
		    
		    
		    Date date = new Date();
		    metadata.setDate(date.toString());
		    
		    metadata.setTags(tags);
		    metadata.setFilters(filters);
		
		    String mime = Utils.mimeTypes.get(page.getContentType().split(";")[0]);
		    if (mime == null) {
				this.logger.log(MyLogger.INFO, "Saltando página con mime " + page.getContentType());
				return;
			}
		    metadata.setType(mime);
		    
		    try {
				if (page.getParseData() instanceof HtmlParseData) {
					System.out.println("Parsing HTML");
					
					HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
					
					Document doc = Jsoup.parse(htmlParseData.getHtml());
					
					String description = "";
					if (doc.select("meta[name=description]").size() > 0) {
						description = doc.select("meta[name=description]").get(0).attr("content");
					}
					
					doc.prependText(description);
					
					String text = doc.text();
					
					metadata.setTitle(doc.title());
					
					text = Utils.cleanText(text);
					
					metadata.setTextLength(text.length());
					
					metadata.setText(Utils.removeStopWords(text));

				} else {
					Utils.parseDoc(metadata);
					if (metadata.getType().equals("pdf")) {
						metadata.setTitle(Utils.getPDFTitle(page.getContentData()));
					}
				}
				metadata.setTopWords(Utils.topWords(metadata.getText()));
				
				System.out.print(metadata);
				
				this.elastic.indexPage(metadata);
		    } catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
