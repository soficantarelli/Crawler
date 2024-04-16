package ar.edu.ubp.das.beans;

public class WebSiteBean {
	private Integer idWebSite;
	private Integer idUser;
	private String url;
	private Integer idService;
	private Boolean indexed;
	private Boolean reindex;
	private Boolean up;
	private String tags;
	private String filters;
	
	public Integer getIdWebSite() {
		return idWebSite;
	}
	public void setIdWebSite(Integer idWebSite) {
		this.idWebSite = idWebSite;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Integer getIdService() {
		return idService;
	}
	public void setIdService(Integer idService) {
		this.idService = idService;
	}
	public Boolean getIndexed() {
		return indexed;
	}
	public void setIndexed(Boolean indexed) {
		this.indexed = indexed;
	}
	public Boolean getReindex() {
		return reindex;
	}
	public void setReindex(Boolean reindex) {
		this.reindex = reindex;
	}
	public Boolean getUp() {
		return up;
	}
	public void setUp(Boolean up) {
		this.up = up;
	}
	public Integer getIdUser() {
		return idUser;
	}
	public void setIdUser(Integer idUser) {
		this.idUser = idUser;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getFilters() {
		return filters;
	}
	public void setFilters(String filters) {
		this.filters = filters;
	}
}