package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.WebSiteBean;
import ar.edu.ubp.das.db.Dao;

public class MSWebSiteDao extends Dao<WebSiteBean, WebSiteBean>{

	@Override
	public WebSiteBean delete(WebSiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public WebSiteBean insert(WebSiteBean website) throws SQLException {
		try {
			
			System.out.print(website);
			this.connect();
			this.setProcedure("dbo.new_website_service(?,?,?,?,?)");
			this.setParameter(1, website.getUrl());
			this.setParameter(2, website.getIdService());
			this.setParameter(3, website.getIdUser());
			this.setParameter(4, website.getTags());
			this.setParameter(5, website.getFilters());
			this.executeUpdate();
		} catch(SQLException e) {
			throw e;
		} finally {
			this.close();
		}
		return website;
	}

	@Override
	public WebSiteBean make(ResultSet result) throws SQLException {
		WebSiteBean website = new WebSiteBean();
		website.setIdUser(result.getInt("idUser"));
		website.setUrl(result.getString("url"));
		website.setIdWebSite(result.getInt("idWebSite"));
		website.setFilters(result.getString("filters"));
		website.setTags(result.getString("tags"));;
		//website.setIndexed(result.getBoolean("indexed"));
		//website.setReindex(result.getBoolean("reindex"));
		//website.setUp(result.getBoolean("up"));
		//website.setIdService(result.getInt("idService"));
		return website;
	}

	@Override
	public List<WebSiteBean> select(WebSiteBean website) throws SQLException {
		try {
			this.connect();
			if (website == null) {
				this.setProcedure("dbo.crawler_get_website");
				List<WebSiteBean> user = this.executeQuery();
				return user;
			} else {			
				this.setProcedure("dbo.get_website_indexed_service(?,?)");
				this.setParameter(1, website.getUrl());
				this.setParameter(2, website.getIdService());
				List<WebSiteBean> user = this.executeQuery();
				return user;
			}
		} finally {
			this.close();
		}
	}

	@Override
	public WebSiteBean update(WebSiteBean website) throws SQLException {
		try {
			this.connect();
			if (website.getUp() != null) {
				this.setProcedure("dbo.up_website(?,?)");
				this.setParameter(1, website.getIdWebSite());
				this.setParameter(2, website.getUp());
				this.executeQuery();
			} else if (website.getIndexed() != null) {
				this.setProcedure("dbo.update_website_indexed(?)");
				this.setParameter(1, website.getIdWebSite());
				this.executeQuery();
			} else {
				this.setProcedure("dbo.reindex_website(?)");
				this.setParameter(1, website.getIdWebSite());
				this.executeQuery();
			}
		} finally {
			this.close();
		}
		return website;
	}

	@Override
	public boolean valid(WebSiteBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
