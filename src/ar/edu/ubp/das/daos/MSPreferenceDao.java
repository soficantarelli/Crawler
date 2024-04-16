package ar.edu.ubp.das.daos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import ar.edu.ubp.das.beans.PreferenceBean;
import ar.edu.ubp.das.db.Dao;

public class MSPreferenceDao extends Dao<PreferenceBean, PreferenceBean>{


	@Override
	public PreferenceBean make(ResultSet result) throws SQLException {
		PreferenceBean preference = new PreferenceBean();
		preference.setIdUser(result.getInt("idUser"));
		preference.setColor(result.getString("color"));
		preference.setIconUrl(result.getString("iconUrl"));
		preference.setSearch(result.getString("search"));
		preference.setBorderRadius(result.getDouble("borderRadius"));
		preference.setBorderWith(result.getDouble("borderWith"));
		preference.setTimeMetadata(result.getInt("timeMetadata"));
		preference.setIconSize(result.getInt("iconSize"));
		return preference;
	}

	@Override
	public List<PreferenceBean> select(PreferenceBean preference) throws SQLException {
		try {
			this.connect();
			this.setProcedure("dbo.get_preference_admin");
			return this.executeQuery();
		} finally {
			this.close();
		}
	}

	@Override
	public PreferenceBean update(PreferenceBean preference) throws SQLException {
		return preference;
	}

	@Override
	public PreferenceBean delete(PreferenceBean preference) throws SQLException {
		return preference;
	}

	@Override
	public PreferenceBean insert(PreferenceBean pre) throws SQLException {
		return pre;	
	}

	@Override
	public boolean valid(PreferenceBean arg0) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
	
}