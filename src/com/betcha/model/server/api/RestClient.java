package com.betcha.model.server.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class RestClient {
	protected RestTemplate restTemplate = new RestTemplate(true);
	protected static  String token;
	private static Context context;
	
	protected HttpStatus lastRestErrorCode = HttpStatus.OK;
	
	public HttpStatus getLastRestErrorCode() {
		return lastRestErrorCode;
	}

	protected void setLastRestErrorCode(HttpStatus lastRestErrorCode) {
		this.lastRestErrorCode = lastRestErrorCode;
	}

	public RestClient() {
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());
		( (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setConnectTimeout(1 * 3000 );
		//( (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory()).setReadTimeout( 3 * 1000 );
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
		
	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		RestClient.context = context;
	}

	public static boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnected()) {
	        return true;
	    }
	    return false;
	}

}
