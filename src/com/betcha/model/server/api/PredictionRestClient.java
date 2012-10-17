package com.betcha.model.server.api;

import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.betcha.model.Prediction;


public class PredictionRestClient extends RestClient {
	
	protected static String url;
	
	public static void setUrl(String url) {
		PredictionRestClient.url = url;
	}
	
	private String bet_id;
	
	public String getServerBet_id() {
		return bet_id;
	}

	public void setServerBet_id(String bet_id) {
		this.bet_id = bet_id;
	}

	public PredictionRestClient(String bet_id) {
		super();
		this.bet_id = bet_id;
	}

	public JSONObject show(String id) {
		setLastRestErrorCode(HttpStatus.OK);
		
		//nested url = bets/:bet_id/predictions
		String res = null;
		try {
			res = restTemplate.getForObject(url + "/" + id + ".json?"+ GetURLTokenParam() , String.class, getServerBet_id());
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
	
	public JSONArray showForBet(int id) {
		setLastRestErrorCode(HttpStatus.OK);
		
		//nested url = bets/:bet_id/predictions
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_for_bet.json?"+ GetURLTokenParam() , String.class, id);
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    	return null;
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return null;
		}
		
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONArray showUpdatesForBet(int bet_id, DateTime lastUpdate) {
		setLastRestErrorCode(HttpStatus.OK);
		
		//nested url = bets/:bet_id/predictions
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_updates_for_bet.json?"+ GetURLTokenParam() + "&updated_at=" + lastUpdate , String.class, bet_id);
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    	return null;
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return null;
		}
		
		JSONArray json = null;
		try {
			json = new JSONArray(res);
		} catch (JSONException e) {
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		
		return json;
	}

	public JSONObject create(Prediction prediction) {
		setLastRestErrorCode(HttpStatus.OK);
		
		JSONObject json = prediction.toJson();
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		String res;
		try {
			res = restTemplate.postForObject(url + ".json" , request, String.class, getServerBet_id());
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    	return null;
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return null;
		}
		
		JSONObject jsonRes = null;
		try {
			jsonRes = new JSONObject(res);
		} catch (JSONException e) {
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject createAndInvite(Prediction prediction) {
		setLastRestErrorCode(HttpStatus.OK);
		
		JSONObject json = prediction.toJson();
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		String res;
		try {
			res = restTemplate.postForObject(url + "/create_and_invite" + ".json" , request, String.class, getServerBet_id());
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    	return null;
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return null;
		}
		
		JSONObject jsonRes = null;
		try {
			jsonRes = new JSONObject(res);
		} catch (JSONException e) {
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		
		return jsonRes;
	}

	public void update(Prediction prediction, String id) {
		setLastRestErrorCode(HttpStatus.OK);
		
		JSONObject json = prediction.toJson();
		
		//nested url = bets/:bet_id/predictions
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		try {
			restTemplate.put(url + "/" + id + ".json", request, getServerBet_id());
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public void update(List<Prediction> predictions) {
		setLastRestErrorCode(HttpStatus.OK);
		
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
		
		try {
			restTemplate.put(url + "/update_list.json?"+ GetURLTokenParam(), arg, getServerBet_id());
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void delete(String id) {
		setLastRestErrorCode(HttpStatus.OK);
		
		try {
			restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam(), getServerBet_id(), id);
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
