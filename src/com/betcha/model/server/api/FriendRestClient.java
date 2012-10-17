/**
 * 
 */
package com.betcha.model.server.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

/**
 * @author ofer
 *
 */
public class FriendRestClient extends RestClient {
	protected static String url;
	
	private String user_id;

	public FriendRestClient(String user_id) {
		super();
		this.user_id = user_id;
	}

	public static void setUrl(String url) {
		FriendRestClient.url = url;
	}
		
	public JSONObject show_for_user() {
		setLastRestErrorCode(HttpStatus.OK);
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_for_user.json?"+ GetURLTokenParam() , String.class, user_id);
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
