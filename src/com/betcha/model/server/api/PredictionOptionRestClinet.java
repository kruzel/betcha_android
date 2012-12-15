package com.betcha.model.server.api;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

public class PredictionOptionRestClinet extends RestClient {
	protected static String url;
	
	public static void setUrl(String url) {
		PredictionOptionRestClinet.url = url;
	}
	
	public JSONObject show_updates_for_user(String category_id, String topic_id, DateTime lastUpdate) {
		setLastRestErrorCode(HttpStatus.OK);
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_for_topic.json?"+ GetURLTokenParam() + "&updated_at=" + lastUpdate.toString() , String.class, category_id, topic_id);
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    	return null;
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return null;
		}
		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		
		return json;
	}
}
