package ar.edu.ubp.das.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import org.apache.commons.io.FileUtils;

import ar.edu.ubp.das.beans.WebSiteBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.logger.MyLogger;
import ar.edu.ubp.das.resources.WebSite;
import ar.edu.ubp.das.utils.Utils;

public class Crawler {
	
	/*public static void main(String[] args) throws Exception {
		MyLogger logger = new MyLogger("Crawler");
		WebSite websitesList = new WebSite();
		
		websitesList.updateServices();
		List<WebSiteBean> listByUser = websitesList.getWebsitesByUser();
		
		if (listByUser != null && listByUser.size() > 0) {
			crawl(listByUser);
		} else {
			logger.log(MyLogger.INFO, "No hay paginas a reindexar");
		}
	}*/
	
	public static void crawlerMethod() {
		MyLogger logger = new MyLogger("Crawler");
		WebSite websitesList = new WebSite();
		
		websitesList.updateServices();
		List<WebSiteBean> listByUser = websitesList.getWebsitesByUser();
		
		if (listByUser != null && listByUser.size() > 0) {
			crawl(listByUser);
		} else {
			logger.log(MyLogger.INFO, "No hay paginas a reindexar");
		}
	}

	public static void crawl(List<WebSiteBean> listByUser) {
		try {
			MyLogger logger = new MyLogger("Crawler");
			logger.log(MyLogger.INFO, "Iniciando");
			Dao<WebSiteBean, WebSiteBean> dao = DaoFactory.getDao("WebSite", "ar.edu.ubp.das");
			
			
			for (WebSiteBean website : listByUser) { 
				
				System.out.print(website.getFilters());
				
				List<CrawlController> controllers = new ArrayList<CrawlController>();
				
				WebSiteBean websiteToUpdate = new WebSiteBean();
				websiteToUpdate.setIdWebSite(website.getIdWebSite());
				websiteToUpdate.setUrl(website.getUrl());
				websiteToUpdate.setIdUser(website.getIdUser());
				websiteToUpdate.setTags(website.getTags());
				websiteToUpdate.setFilters(website.getFilters());
				
				
				if (Utils.pingURL(website.getUrl(), 5000)) {
					websiteToUpdate.setUp(true);
					
					controllers.add(Utils.startCrawler(websiteToUpdate));
				
				} else {
					logger.log(MyLogger.ERROR, "Caido" + website.getUrl());
					websiteToUpdate.setUp(false);
					
					dao.update(websiteToUpdate);
				}
				
				for (CrawlController controller : controllers) {
					controller.waitUntilFinish();
				}
				
				File dir = new File("/tmp/crawler4j");
				FileUtils.cleanDirectory(dir);
				
				if (websiteToUpdate.getUp()) {
					WebSiteBean websiteIndexed = new WebSiteBean();
					websiteIndexed.setIdWebSite(websiteToUpdate.getIdWebSite());
					websiteIndexed.setIndexed(true);
					websiteIndexed.setReindex(false);
					dao.update(websiteIndexed);
				}
				
			}
			
			logger.log(MyLogger.INFO, "Terminado");
			//System.exit(0);
		} catch (Exception e) {
			MyLogger logger = new MyLogger("Crawler");
			logger.log(MyLogger.ERROR, "Error" + e.getMessage());
		}
	}
}
