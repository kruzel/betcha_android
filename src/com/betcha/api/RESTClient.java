package com.betcha.api;

import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public abstract class RESTClient {
	protected RestTemplate restTemplate;
	protected String url;
	static protected String token;

	public RESTClient(String url) {
		this.url = url;
		restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
	}
	
	static public void SetToken(String newToken) {
		token = newToken;
	}
	
	static public String GetURLTokenParam() {
		return "auth_token=" + token;
	}
	
	static public String GetToken() {
		return token;
	}

}
