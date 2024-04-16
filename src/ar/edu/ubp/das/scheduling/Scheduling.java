package ar.edu.ubp.das.scheduling;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ar.edu.ubp.das.beans.PreferenceBean;
import ar.edu.ubp.das.resources.WebSite;

public class Scheduling {

	public static void main(String[] args) {
		
		WebSite websitesList = new WebSite();
		List<PreferenceBean> preference = websitesList.getPreferenceByAdmin();
		
		Integer time = preference.get(0).getTimeMetadata();
		
		Runnable task1 = new Command();
		
		ScheduledThreadPoolExecutor tp2 = new ScheduledThreadPoolExecutor(10);
		tp2.scheduleAtFixedRate(task1, 1, time, TimeUnit.SECONDS);
		//tp2.schedule(task1, 1, TimeUnit.MINUTES);
		
	}

}
