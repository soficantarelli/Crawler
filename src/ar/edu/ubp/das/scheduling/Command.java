package ar.edu.ubp.das.scheduling;

import java.util.Date;

import ar.edu.ubp.das.crawler.Crawler;

public class Command  implements Runnable{

	public Command () {}
	@Override
	public void run() {
		Crawler.crawlerMethod();
		
	}
}
