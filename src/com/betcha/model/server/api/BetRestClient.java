package com.betcha.model.server.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import com.betcha.model.Bet;

public class BetRestClient extends RestClient {

	protected static String url;
	
	public static void setUrl(String url) {
		BetRestClient.url = url;
	}
	
	public JSONArray list() throws RestClientException {
		// TODO Auto-generated method stub
		return null;
	}


	public JSONObject show(int id) throws RestClientException {
		String res = restTemplate.getForObject(url + "/" + id  + ".json?"+ GetURLTokenParam() , String.class);
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONArray show_for_user() throws RestClientException {
		String res = restTemplate.getForObject(url + "/show_for_user.json?"+ GetURLTokenParam() , String.class);
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

	public JSONObject create(Bet bet) throws RestClientException {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			
			jsonContent.put("user_id", Integer.toString(bet.getOwner().getServer_id()));
			jsonContent.put("subject", bet.getSubject());
			jsonContent.put("reward", bet.getReward());
			if(bet.getDueDate()!=null)
				jsonContent.put("due_date", bet.getDueDate().toString());
			jsonContent.put("state", bet.getState());
			jsonParent.put("bet", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		String res = restTemplate.postForObject(url + ".json" , request, String.class);		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

	public void update(Bet bet, int id) throws RestClientException {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("user_id", Integer.toString(bet.getOwner().getServer_id()));
			jsonContent.put("subject", bet.getSubject());
			jsonContent.put("reward", bet.getReward());
			jsonContent.put("due_date", bet.getDueDate().toString());
			jsonContent.put("state", bet.getState());
			jsonParent.put("bet", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		restTemplate.put(url + "/" + id + ".json", request);
	}

	public void delete(int id) throws RestClientException {
		restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam());
	}
}
