package com.betcha.model.server.api;

import java.io.ByteArrayOutputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.betcha.model.User;

public class UserRestClient extends RestClient {

	protected static String url;
	
	public static void setUrl(String url) {
		UserRestClient.url = url;
	}

	public JSONObject show(String id) {
		setLastRestErrorCode(HttpStatus.OK);
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/" + id + ".json?"+ GetURLTokenParam() , String.class);
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
	
	public JSONObject showViaEmail(String email) {
		setLastRestErrorCode(HttpStatus.OK);
		
		if(email.isEmpty()) {
			setLastRestErrorCode(HttpStatus.BAD_REQUEST);
			return null;
		}
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_by_email.json?"+ GetURLTokenParam() + "&email=" + email , String.class);
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    	return null;
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return null;
		}
		
		if(res==null)
			return null;
		
		JSONObject json = null;
		try {
			json = new JSONObject(res);
		} catch (JSONException e) {
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		
		return json;
	}
	
	public JSONObject showViaUid(String uid) {
		setLastRestErrorCode(HttpStatus.OK);
		
		String res;
		try {
			res = restTemplate.getForObject(url + "/show_by_uid.json?"+ GetURLTokenParam() + "&uid=" + uid , String.class);
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
	
	public JSONObject create(String id, String full_name, String email, String password, Bitmap bm)   {		
		setLastRestErrorCode(HttpStatus.CREATED);
		
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("id", id);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		try {
			jsonContent.put("is_app_installed", true);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
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
		
		//TODO paperclip - profile picture support
//		if(bm!=null) {
//			ByteArrayOutputStream output = new ByteArrayOutputStream();  
//			bm.compress(Bitmap.CompressFormat.JPEG, 100, output); //bm is the bitmap object   
//			byte[] bytes = output.toByteArray();
//			String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
//			try {
//				jsonContent.put("avatar", base64Image);
//			} catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
		
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
        } catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    	return null;
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
			return null;
		}
		
		JSONObject json = null;
		if(res!=null) {
			try {
				json = new JSONObject(res);
			} catch (JSONException e) {
				setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			}
		}
		
		return json;
	}
	
	public JSONObject createOAuth(String id, String provider, String uid, String access_token )   {
		setLastRestErrorCode(HttpStatus.CREATED);
		
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("id", id);
			jsonContent.put("is_app_installed", true);
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

	public void update(User user)   {
		setLastRestErrorCode(HttpStatus.OK);
		
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();
		
		try {
			jsonContent.put("is_app_installed", true);
		} catch (JSONException e1) {
		}
		
		try {
			jsonContent.put("provider", user.getProvider());
		} catch (JSONException e1) {
		}
		
		try {
			jsonContent.put("email", user.getEmail());
		} catch (JSONException e1) {
		}
		
		try {
			jsonContent.put("full_name", user.getName());
		} catch (JSONException e1) {
		}
		
		try {
			jsonContent.put("password", user.getPassword());
		} catch (JSONException e1) {
		}
		
		try {
			jsonContent.put("push_notifications_device_id", user.getPush_notifications_device_id());
		} catch (JSONException e1) {
		}
		
		try {
			jsonContent.put("device_type", "android");
		} catch (JSONException e1) {
		}
		
//		if(user.getProvider().equals("email") && user.getProfile_pic_url()!=null) {
//			Bitmap bm = BitmapFactory.decodeFile(user.getProfile_pic_url());
//			if(bm==null) {
//				Log.e("UserRestClient.update()", "profile picture not found in path: " + user.getProfile_pic_url());
//			} else {
//				ByteArrayOutputStream output = new ByteArrayOutputStream();  
//				bm.compress(Bitmap.CompressFormat.JPEG, 100, output); //bm is the bitmap object   
//				byte[] bytes = output.toByteArray();
//				String base64Image = Base64.encodeToString(bytes, Base64.DEFAULT);
//				try {
//					jsonContent.put("avatar", base64Image);
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//				}
//			}
//		}
		
		try {
			jsonParent.put("user", jsonContent);
		} catch (JSONException e1) {
			e1.printStackTrace();
			return;
		}
		
		HttpHeaders headers = new HttpHeaders();
        headers.setContentType( MediaType.APPLICATION_JSON );
        headers.set("X-AUTH-TOKEN", GetToken());
        HttpEntity request= new HttpEntity( jsonParent.toString(), headers);
		
        String res = null;
        try {
        	Log.i("UserRestClient.update()", "updating server through rest api");
        	restTemplate.put(url + "/" + user.getId() + ".json", request);
        } catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		return;
	}

	public void delete(String id)   {
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
	
	public void resetPassword(String email) {   	
		setLastRestErrorCode(HttpStatus.OK);
		
		try {
			restTemplate.put(url + "/reset_password.json?email=" + email,null);
		} catch (HttpClientErrorException e) {
	    	setLastRestErrorCode(e.getStatusCode());
	    } catch (RestClientException e) {
			e.printStackTrace();
			setLastRestErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
