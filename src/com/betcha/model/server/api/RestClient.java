package com.betcha.model.server.api;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public abstract class RestClient {
	protected RestTemplate restTemplate = new RestTemplate(true);
	static protected String token;

	public RestClient() {
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
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
