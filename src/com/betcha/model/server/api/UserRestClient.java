package com.betcha.model.server.api;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

public class UserRestClient extends RestClient {

	protected static String url;
	
	public static void setUrl(String url) {
		UserRestClient.url = url;
	}
	
	public JSONArray list() {
		// TODO Auto-generated method stub
		return null;
	}

	public JSONObject show(int id) {
		String res;
		try {
			res = restTemplate.getForObject(url + "/" + id + ".json?"+ GetURLTokenParam() , String.class);
		} catch (RestClientException e1) {
			e1.printStackTrace();
			return null;
		}
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject create(String full_name, String email, String password)   {		
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("provider", "email");
			jsonContent.put("email", email);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		try {
			jsonContent.put("full_name", full_name);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			jsonContent.put("password", password);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			jsonParent.put("user", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		
        String res = null;
        try {
        	res = restTemplate.postForObject(url + ".json" , request, String.class);
        } catch (RestClientException e) {
        	e.printStackTrace();
    		return null;
    	}
		
		JSONObject json = null;
		if(res!=null) {
			try {
				json = new JSONObject(res);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return json;
	}
	
	public JSONObject createOAuth(String provider, String uid, String access_token )   {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("provider", provider);
			jsonContent.put("uid", uid);
			jsonContent.put("access_token", access_token);
			jsonParent.put("user", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		String res;
		try {
			res = restTemplate.postForObject(url + ".json" , request, String.class);
		} catch (RestClientException e1) {
			e1.printStackTrace();
			return null;
		}	
		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

	public void update(Map<String,String> arg, int id)   {
		arg.put("auth_token", GetToken());
		
		restTemplate.put(url + "/" + id + ".json", arg);
	}

	public void delete(int id)   {
		try {
			restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam());
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
}
