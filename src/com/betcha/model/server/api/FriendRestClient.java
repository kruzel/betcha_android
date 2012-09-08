/**
 * 
 */
package com.betcha.model.server.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.web.client.RestClientException;

/**
 * @author ofer
 *
 */
public class FriendRestClient extends RestClient {
	protected static String url;
	
	private int user_id;

	public FriendRestClient(int user_id) {
		super();
		this.user_id = user_id;
	}

	public static void setUrl(String url) {
		FriendRestClient.url = url;
	}
		
	public JSONArray show_for_user() {
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_for_user.json?"+ GetURLTokenParam() , String.class, user_id);
		} catch (RestClientException e1) {
			e1.printStackTrace();
			return null;
		}
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
}