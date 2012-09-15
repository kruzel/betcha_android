package com.betcha.model.server.api;

import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import com.betcha.model.ChatMessage;
import com.betcha.model.Prediction;
import com.betcha.model.User;


public class ChatMessageRestClient extends RestClient {
	
	protected static String url;
	
	public static void setUrl(String url) {
		ChatMessageRestClient.url = url;
	}
	
	private int server_bet_id;
	
	public int getServerBet_id() {
		return server_bet_id;
	}

	public void setServerBet_id(int bet_id) {
		this.server_bet_id = bet_id;
	}

	public ChatMessageRestClient(int server_bet_id) {
		super();
		this.server_bet_id = server_bet_id;
	}

	public JSONArray list() {
		// TODO Auto-generated method stub
		return null;
	}

	public JSONObject show(int id) {
		//nested url = bets/:bet_id/predictions
		String res = null;
		try {
			res = restTemplate.getForObject(url + "/" + id + ".json?"+ GetURLTokenParam() , String.class, getServerBet_id());
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
	
	public JSONArray showForBet(int id) {
		//nested url = bets/:bet_id/chat_messages
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_for_bet.json?"+ GetURLTokenParam() , String.class, id);
		} catch (RestClientException e1) {
			e1.printStackTrace();
			return null;
		}
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONArray showUpdatesForBet(int bet_id, DateTime lastUpdate) {
		//nested url = bets/:bet_id/chat_messages
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_updates_for_bet.json?"+ GetURLTokenParam() + "&updated_at=" + lastUpdate , String.class, bet_id);
		} catch (RestClientException e1) {
			e1.printStackTrace();
			return null;
		}
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject create(ChatMessage chatMessage) {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
//			jsonContent.put("type",chatMessage...ChatMessageRestClient..());
//			jsonContent.put("notification_sent",chatMessage.....());
			
			jsonContent.put("message",chatMessage.getMessage());
			jsonContent.put("bet_id",Integer.toString(chatMessage.getBet().getServer_id()));
			jsonContent.put("user_id",Integer.toString(chatMessage.getUser().getServer_id()));
		
			try {
				jsonParent.put("chat_message", jsonContent);
			} catch (RestClientException e) {
				e.printStackTrace();
				return null;
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		String res;
		try {
			res = restTemplate.postForObject(url + ".json" , request, String.class, getServerBet_id());
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
	
	public void update(ChatMessage chatMessage, int id) {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("message",chatMessage.getMessage());
			jsonContent.put("bet_id",Integer.toString(chatMessage.getBet().getServer_id()));
			jsonContent.put("user_id",Integer.toString(chatMessage.getUser().getServer_id()));
			jsonParent.put("chat_message", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		try {
			restTemplate.put(url + "/" + id + ".json", request, getServerBet_id());
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}

	public void delete(int id) {
		try {
			restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam(), getServerBet_id(), id);
		} catch (RestClientException e) {
			e.printStackTrace();
		}
	}
}
