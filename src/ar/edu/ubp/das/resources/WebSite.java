package ar.edu.ubp.das.resources;

import java.lang.reflect.Type;
import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

import org.apache.cxf.Bus;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.apache.cxf.transport.http.auth.HttpAuthHeader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ar.edu.ubp.das.beans.PreferenceBean;
import ar.edu.ubp.das.beans.ServicesBean;
import ar.edu.ubp.das.beans.UrlBean;
import ar.edu.ubp.das.beans.WebSiteBean;
import ar.edu.ubp.das.db.Dao;
import ar.edu.ubp.das.db.DaoFactory;
import ar.edu.ubp.das.elastic.ElasticSearch;
import ar.edu.ubp.das.logger.MyLogger;
import ar.edu.ubp.das.utils.Utils;

public class WebSite {
	
	@Context
	ContainerRequestContext request;
	
	private MyLogger logger;
	private ElasticSearch elasticSearch;
	
	public WebSite() {
		this.logger = new MyLogger(this.getClass().getSimpleName());
	}
	
	public List<PreferenceBean> getPreferenceByAdmin() {
		try {
			Dao<PreferenceBean, PreferenceBean> dao = DaoFactory.getDao("Preference", "ar.edu.ubp.das");
			
			List<PreferenceBean> preference = dao.select(null);
			
			return preference;
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Error al obtener la preferencia: " + e.getMessage());
			List<PreferenceBean> p = (List<PreferenceBean>) new PreferenceBean();
			((PreferenceBean) p).setTimeMetadata(20);
			return p;
		}
		
	}
	
	public List<WebSiteBean> getWebsitesByUser() {
		try {
			Dao<WebSiteBean, WebSiteBean> dao = DaoFactory.getDao("WebSite", "ar.edu.ubp.das");
			
			List<WebSiteBean> websites = dao.select(null);
			
			if (websites.size() > 0) {
				this.logger.log(MyLogger.INFO, "Se encontraron paginas para indexar");
			}
			return websites;
		} catch (SQLException e) {
			this.logger.log(MyLogger.ERROR, "Error al obtener el listado de paginas: " + e.getMessage());
		}
		return null;
	}
	
	public void updateServices() {
		try {
			Dao<ServicesBean, ServicesBean> dao = DaoFactory.getDao("Service", "ar.edu.ubp.das");
			
			List<ServicesBean> services = dao.select(null);
			
			if (services == null || services.size() <= 0) {
				this.logger.log(MyLogger.INFO, "No se encontraron servicios para actualizar");
			}
			
			for (ServicesBean service : services) {
				if (service.getProtocol().equals("REST")) {
					serviceRest(service);
				} else {
					serviceSoap(service);
				}
			}
			
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, "Error al actualizar los servicios: " + e.getMessage());
		}
	}

	private void serviceSoap(ServicesBean service) throws SQLException {
		this.logger.log(MyLogger.INFO, "SOAP: Servicio #" + service.getIdService());
		Dao<ServicesBean, ServicesBean> dao = DaoFactory.getDao("Service", "ar.edu.ubp.das");
		try {
			Bus bus = CXFBusFactory.getThreadDefaultBus();
	        MyHTTPConduitConfigurer conf = new MyHTTPConduitConfigurer(service.getToken());
	        bus.setExtension(conf, HTTPConduitConfigurer.class);
	        
			JaxWsDynamicClientFactory jdcf = JaxWsDynamicClientFactory.newInstance(bus);
			Client client = jdcf.createClient(service.getUrl());

			client.invoke("ping");
			
			
			Object res = client.invoke("getList");
			System.out.println(res);
			
			//JSONObject jsonObj = new JSONObject(res[0]);
			
			//System.out.println(jsonObj);
			
			/*ArrayList<String> urls = (ArrayList<String>)res[0];
			
			for (String url : urls) {
				try {
					System.out.println(url);
					//this.insertWebsite(url, service);
				} catch (Exception e) {
					this.logger.log(MyLogger.ERROR, "Error al insertar " + url + " " + e.getMessage());
				}
			}*/
			
			/*ArrayList<String> urls = (ArrayList<String>)res[0];
			System.out.println(urls);*/
			//client.close();
			
			//service.setUp(true);
			//dao.update(service);
			
			/*ArrayList<String> urls = (ArrayList<String>)res[0];
			System.out.println(urls);
			for (String url : urls) {
				try {
					this.insertWebsite(url, service);
				} catch (Exception e) {
					this.logger.log(MyLogger.ERROR, "Error al insertar " + url + " " + e.getMessage());
				}
			}
			
			/*ServicesBean ser = new ServicesBean();
			ser.setIdService(service.getIdService());
			ser.setReindex(false);
			dao.update(ser);*/
		
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, e.getMessage());
			service.setUp(false);
			dao.update(service);
		}
	}

	private void serviceRest(ServicesBean service) throws SQLException {
		this.logger.log(MyLogger.INFO, "REST: Servicio #" + service.getIdService());
		Dao<ServicesBean, ServicesBean> dao = DaoFactory.getDao("Service", "ar.edu.ubp.das");
		try {
			//String urls[] = this.makeRestCall(service);
			
			URI uri = URI.create(service.getUrl() + "list");
			HttpGet req = new HttpGet();
			req.setURI(uri);
			req.setHeader("Authorization", service.getToken());
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpResponse resp = client.execute(req);
			
			HttpEntity responseEntity = resp.getEntity();
			StatusLine responseStatus = resp.getStatusLine();
			
			
			
			
			String restResp = EntityUtils.toString(responseEntity);
			
			Gson gson = new Gson();
			Type listType = new TypeToken<List<UrlBean>>(){}.getType();
			List<UrlBean> urls = gson.fromJson(restResp, listType);
			
			
			
			for (UrlBean url : urls) {
				
				try {
					this.insertWebsite(url, service);
				} catch (Exception e) {
					this.logger.log(MyLogger.ERROR, "Error al insertar " + url + " " + e.getMessage());
				}
			}
			
			
			
			/*String restResp = EntityUtils.toString(responseEntity);
			
			/*HttpEntity responseEntity = resp.getEntity();
			StatusLine responseStatus = resp.getStatusLine();
			
			String restResp = EntityUtils.toString(responseEntity);
			
			if (responseStatus.getStatusCode() >= 400) {
				this.logger.log(MyLogger.WARNING, "Servicio #" + service.getIdService()
						+ " no respondio con el listado de paginas. Marcado como caido");
				throw new NotFoundException("Servicio Caido");
			}
			this.logger.log(MyLogger.INFO, "Servicio # " + service.getIdService() + " funcionando, obteniendo paginas...");
			
			service.setUp(true);
			dao.update(service);*/
			
			/*Gson gson = new Gson();
			Type listType = new TypeToken<List<String>>(){}.getType();
			List<String> urls = gson.fromJson(restResp, listType);
			
			System.out.print(urls);
			
			/*for (String url : urls) {
				
				try {
					this.insertWebsite(url, service);
				} catch (Exception e) {
					this.logger.log(MyLogger.ERROR, "Error al insertar " + url + " " + e.getMessage());
				}
			}*/
			
			
			ServicesBean ser = new ServicesBean();
			ser.setIdService(service.getIdService());
			ser.setReindex(false);
			dao.update(ser);
			
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, e.getMessage());
			service.setUp(false);
			dao.update(service);
		}
	}

	private void insertWebsite(UrlBean url, ServicesBean service) {
		try {
			Dao<WebSiteBean, WebSiteBean> dao = DaoFactory.getDao("WebSite", "ar.edu.ubp.das");
			
			System.out.print(url.getFilters());
			
			
			WebSiteBean website = new WebSiteBean();
			website.setIdUser(service.getIdUser());
			website.setUrl(url.getUrl());
			website.setIdService(service.getIdService());
			website.setTags(url.getTags());
			website.setFilters(url.getFilters());
			
			List<WebSiteBean> websiteFound = dao.select(website);
	
			/*if (websiteFound != null) {
				ElasticSearch elastic = new ElasticSearch();
				//elastic.deleteWebsiteId((websiteFound.get(0)).getIdWebSite());
			}*/
			
			dao.insert(website);
		} catch (Exception e) {
			this.logger.log(MyLogger.ERROR, e.getMessage());
		}
	}
}
