package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.ServicesBean;
import ar.edu.ubp.das.db.Dao;

public class MSServiceDao extends Dao<ServicesBean, ServicesBean>{

	@Override
	public ServicesBean delete(ServicesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServicesBean insert(ServicesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServicesBean make(ResultSet result) throws SQLException {
		ServicesBean service = new ServicesBean();
		service.setIdService(result.getInt("idService"));
		service.setIdUser(result.getInt("idUser"));
		service.setUrl(result.getString("url"));
		service.setProtocol(result.getString("protocol"));
		service.setIndexed(result.getBoolean("indexed"));
		service.setReindex(result.getBoolean("reindex"));
		service.setUp(result.getBoolean("up"));
		service.setToken(result.getString("token"));
		return service;
	}
	
	@Override
	public List<ServicesBean> select(ServicesBean arg0) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_for_crawler");
			List<ServicesBean> serviceFind = this.executeQuery();
	        return serviceFind;
		} finally {
			this.close();
		}
	}

	@Override
	public ServicesBean update(ServicesBean service) throws SQLException {
		try {
			this.connect();
			if (service.getReindex() != null) {
				this.setProcedure("dbo.reindex_service(?,?)");
				this.setParameter(1, service.getIdService());
				this.setParameter(2, service.getReindex()); //false
				this.executeUpdate();
			} else {
				this.connect();
				this.setProcedure("dbo.up_service(?,?)");
				this.setParameter(1, service.getIdService());
				this.setParameter(2, service.getUp());
				this.executeUpdate();
			}
			
		} finally {
			this.close();
		}
		return service;
	}

	@Override
	public boolean valid(ServicesBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

}
