package com.betcha.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestClientException;

import android.content.Context;

import com.betcha.R;
import com.betcha.api.model.RESTToken;

public class RESTClientToken extends RESTClient {

	public RESTClientToken(Context context) {
		super(context.getString(R.string.betcha_api) + "/tokens");
	}
	
	//sign_up, sign_in
	public RESTToken create(String email, String password, String name) {
		Map<String,String> arg = new HashMap<String,String>();
		arg.put("name", name);
		arg.put("email", email);
		arg.put("password", password);
		
		RESTToken res = restTemplate.postForObject(url + ".json" , arg, RESTToken.class);	
		token = res.getToken();
		return res;
	}

	//sign_out
	public void delete() throws RestClientException {
		restTemplate.delete(url  + "/" + token + ".json");
	}
}
