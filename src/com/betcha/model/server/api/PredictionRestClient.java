package com.betcha.model.server.api;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import com.betcha.model.Prediction;
import com.betcha.model.User;


public class PredictionRestClient extends RestClient {
	
	protected static String url;
	
	public static void setUrl(String url) {
		PredictionRestClient.url = url;
	}
	
	private int server_bet_id;
	
	public int getServerBet_id() {
		return server_bet_id;
	}

	public void setServerBet_id(int bet_id) {
		this.server_bet_id = bet_id;
	}

	public PredictionRestClient(int server_bet_id) {
		super();
		this.server_bet_id = server_bet_id;
	}

	public JSONArray list() throws RestClientException {
		// TODO Auto-generated method stub
		return null;
	}

	public JSONObject show(int id) throws RestClientException {
		//nested url = bets/:bet_id/predictions
		String res = restTemplate.getForObject(url + "/" + id + ".json?"+ GetURLTokenParam() , String.class, getServerBet_id());
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONArray showPredictionsForBet(int id) throws RestClientException {
		//nested url = bets/:bet_id/predictions
		String res = restTemplate.getForObject(url + "/show_bet_id.json?"+ GetURLTokenParam() , String.class, id);
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

	public JSONObject create(Prediction prediction) throws RestClientException {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("user_ack",prediction.getMyAck());
			jsonContent.put("prediction",prediction.getPrediction());
			jsonContent.put("bet_id",Integer.toString(prediction.getBet().getServer_id()));
			jsonContent.put("user_id",Integer.toString(prediction.getUser().getServer_id()));
			//jsonContent.put("result",Boolean.toString(prediction.getResult()));
			jsonParent.put("prediction", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		String res = restTemplate.postForObject(url + ".json" , request, String.class, getServerBet_id());		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject createAndInvite(Prediction prediction) throws RestClientException {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			//jsonContent.put("user_ack",prediction.getMyAck());
			jsonContent.put("prediction","");
			jsonContent.put("bet_id",Integer.toString(prediction.getBet().getServer_id()));
			jsonContent.put("user_id",Integer.toString(prediction.getUser().getServer_id()));
			//jsonContent.put("result",Boolean.toString(prediction.getResult()));
			jsonParent.put("prediction", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		String res = restTemplate.postForObject(url + "/create_and_invite" + ".json" , request, String.class, getServerBet_id());		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}

	public void update(Prediction prediction, int id) throws RestClientException {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("user_ack",prediction.getMyAck());
			jsonContent.put("prediction",prediction.getPrediction());
			jsonContent.put("bet_id",Integer.toString(prediction.getBet().getServer_id()));
			jsonContent.put("user_id",Integer.toString(prediction.getUser().getServer_id()));
			jsonContent.put("result",Boolean.toString(prediction.getResult()));
			jsonParent.put("prediction", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		restTemplate.put(url + "/" + id + ".json", request, getServerBet_id());
	}
	
	public void update(List<Prediction> predictions) throws RestClientException {
		JSONArray arg = new JSONArray();
		
		for (Prediction prediction : predictions) {
			JSONObject json = new JSONObject();
			try {
				json.put("id", prediction.getId());
				json.put("prediction", prediction.getPrediction());
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
			arg.put(json);
		}
		
		restTemplate.put(url + "/update_list.json?"+ GetURLTokenParam(), arg, getServerBet_id());
	}

	public void delete(int id) throws RestClientException {
		restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam(), getServerBet_id(), id);
	}

	public JSONObject sendInvites(List<User> users) throws RestClientException {
		
		JSONArray jsonUsers = new JSONArray();
		
		for (User user : users) {
			JSONObject jsonUser = new JSONObject();
			JSONObject jsonUserParent = new JSONObject();
			try {
				if(user.getServer_id()!=-1) {
					jsonUser.put("id", user.getServer_id());
				}
//				jsonUser.put("provider", user.getProvider());
//				if(user.getProvider()=="email") {
//					jsonUser.put("email", user.getEmail());
//					jsonUser.put("full_name", user.getName());
//				} else {
//					jsonUser.put("uid", user.getUid());
//				}
				jsonUserParent.put("user", jsonUser);
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
			
			jsonUsers.put(jsonUserParent);
			
		}
		
		JSONObject jsonArrayParent = new JSONObject();
		
		try {
			jsonArrayParent.put("users", jsonUsers);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonArrayParent.toString(), headers);
		String res = restTemplate.postForObject(url + "/send_invites" + ".json" , request, String.class, getServerBet_id());	
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
			
	}

}
