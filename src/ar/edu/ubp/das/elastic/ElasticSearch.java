package ar.edu.ubp.das.elastic;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.common.xcontent.XContentType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ar.edu.ubp.das.beans.MetadataBean;
import ar.edu.ubp.das.logger.MyLogger;

public class ElasticSearch {
	private MyLogger logger;
	RestHighLevelClient client;
	Gson gson;
	

    public ElasticSearch() {
    	this.logger = new MyLogger(this.getClass().getSimpleName());
    	this.client = new RestHighLevelClient(
							RestClient.builder(
				    		    new HttpHost("localhost", 9200, "http"),
				    		    new HttpHost("localhost", 9201, "http")));   
    	this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

	public void indexPage(MetadataBean metadata) {
		try {
			IndexRequest request = new IndexRequest("metadata");
			request.id(UUID.randomUUID().toString());
			request.source(this.gson.toJson(metadata), XContentType.JSON);
			client.index(request, RequestOptions.DEFAULT);
			this.logger.log(MyLogger.INFO, "Metadato insertado" + request);
		} catch (IOException e) {
			this.logger.log(MyLogger.ERROR, "Error al insertar metadato en elasticsearch: " + e.getMessage());
		}
	}
	
	public void deleteWebsiteId(Integer id) throws ElasticsearchException, Exception {
		DeleteByQueryRequest request = new DeleteByQueryRequest("metadata");
		request.setQuery(new TermQueryBuilder("websiteId", id));
		request.setRefresh(true);
		
		try {
			BulkByScrollResponse bulkResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
			this.logger.log(MyLogger.INFO, "ELASTIC: Delete Metadatos. Respuesta: " + bulkResponse.getStatus().toString());
		} catch (ElasticsearchException e) {
		    if (e.status() == RestStatus.NOT_FOUND) {
		    	this.logger.log(MyLogger.ERROR, "ELASTIC: Delete Not Found. Respuesta: " + e.status());
		    }
		    
		    if (e.status() == RestStatus.CONFLICT) {
		    	this.logger.log(MyLogger.ERROR, "ELASTIC: Delete Conflict. Respuesta: " + e.status());
		    }
		}
	}
    
}
