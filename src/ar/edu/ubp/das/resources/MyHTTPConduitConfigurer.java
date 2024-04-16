package ar.edu.ubp.das.resources;

import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;

public class MyHTTPConduitConfigurer implements HTTPConduitConfigurer {
	 
	 private final String _token;
	 
	 public MyHTTPConduitConfigurer(String token) {
		 	_token = token;
	    }
	 
	@Override
	public void configure(String name, String address, HTTPConduit c) {
		AuthorizationPolicy ap = new AuthorizationPolicy();
        ap.setAuthorization(_token);
        c.setAuthorization(ap);
        ap.setAuthorizationType("Bearer");
		
	}

}
