package com.betcha.api;

import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public abstract class RESTClient {
	protected RestTemplate restTemplate;
	protected String url;

	public RESTClient(String url) {
		this.url = url;
		restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
	}

}
