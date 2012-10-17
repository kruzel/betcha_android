package com.betcha.model.server.api;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import com.betcha.model.Bet;

public class BetRestClient extends RestClient {

	protected static String url;
	
	public static void setUrl(String url) {
		BetRestClient.url = url;
	}

	public JSONObject show(String id) {
		setLastRestErrorCode(HttpStatus.OK);
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/" + id  + ".json?"+ GetURLTokenParam() , String.class);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject show_for_user() {
		setLastRestErrorCode(HttpStatus.OK);
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_for_user.json?"+ GetURLTokenParam() , String.class);
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
	
	public JSONObject show_updates_for_user(DateTime lastUpdate) {
		setLastRestErrorCode(HttpStatus.OK);
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_updates_for_user.json?"+ GetURLTokenParam() + "&updated_at=" + lastUpdate.toString() , String.class);
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

	public JSONObject create(Bet bet) {
		setLastRestErrorCode(HttpStatus.OK);
		
		JSONObject json = bet.toJson();
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		String res;
		try {
			res = restTemplate.postForObject(url + ".json" , request, String.class);
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

	public void update(Bet bet) {
		setLastRestErrorCode(HttpStatus.OK);
		
		JSONObject json = bet.toJson();
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		try {
			restTemplate.put(url + "/" + bet.getId() + ".json", request);
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public void updateOrCreate(Bet bet) {
		setLastRestErrorCode(HttpStatus.OK);
		
		JSONObject json = bet.toJson();
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( json.toString(), headers);
		try {
			restTemplate.put(url + "/" + bet.getId() + "/update_or_create.json", request);
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public void delete(String id)  {
		setLastRestErrorCode(HttpStatus.OK);
		
		try {
			restTemplate.delete(url  + "/" + id + ".json?"+ GetURLTokenParam());
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
