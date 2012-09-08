package com.betcha.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.client.RestClientException;

import android.os.AsyncTask;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.PredictionRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "predictions")
public class Prediction extends ModelCache<Prediction,Integer> {
	 
	// id is generated by the database and set on the object automagically
	@DatabaseField(generatedId = true)
	private int id;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Bet bet;
	@DatabaseField
	private String prediction;
	@DatabaseField
	private DateTime date;
	@DatabaseField
	private Boolean result;
	@DatabaseField
	private String user_ack; // Yes/No/Pending
	
	private static PredictionRestClient predictionRestClient;
	private static UpdateBatchPredictionsTask updatePredictionsTask;
	private static Dao<Prediction,Integer> dao;
	
	private Boolean sendInvite = false;

	public Prediction() {
		super();
	}

	public Prediction(Bet bet) {
		super();
		setBet(bet);
	}
	
	public void setPrediction(Prediction newPrediction) {
		id = newPrediction.getId();
		user = newPrediction.getUser();
		bet = newPrediction.getBet();
		prediction = newPrediction.getPrediction();
		date = newPrediction.getDate();
		result = newPrediction.getResult();
		user_ack = newPrediction.getMyAck();
	}
	
	public PredictionRestClient getPredictionRestClient() {
		if(predictionRestClient==null)
			//nested url = bets/:bet_id/predictions
			predictionRestClient = new PredictionRestClient(bet.getServer_id());
			
		return predictionRestClient;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getPrediction() {
		return prediction;
	}

	public void setPrediction(String prediction) {
		this.prediction = prediction;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Bet getBet() {
		return bet;
	}

	public void setBet(Bet bet) {
		this.bet = bet;
	}

	public DateTime getDate() {
		return date;
	}

	public void setDate(DateTime date) {
		this.date = date;
	}

	public String getMyAck() {
		return user_ack;
	}

	public void setMyAck(String myAck) {
		this.user_ack = myAck;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	/**
	 * static methods that must be implemented by derived class
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Prediction,Integer> getModelDao() throws SQLException  {
		if(dao==null){
			dao = getDbHelper().getDao(Prediction.class);
		}
		return dao;
	}
	
	@Override
	public void initDao() {
		try {
			setDao(getModelDao());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected static Class<?> getModelClass() {
		return Prediction.class;
	}
	
	public Boolean getSendInvite() {
		return sendInvite;
	}

	public void setSendInvite(Boolean sendInvite) {
		this.sendInvite = sendInvite;
	}
	
	/** inherited ModelCache methods */
	
	public int onRestCreate() {		
		int res = 0;
		JSONObject json = null;
		
		if (getSendInvite()) {
			json = getPredictionRestClient().createAndInvite(this);
		} else {
			json = getPredictionRestClient().create(this);
		}
		
		if(json!=null) {
			setServer_id(json.optInt("id", -1));
			try {
				res = updateLocal();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		
		return res;
	}

	public int onRestUpdate() {
		getPredictionRestClient().update(this, getServer_id());
		return 1;
	}

	public int onRestDelete() {
		getPredictionRestClient().delete(getServer_id());
		return 1;
	}

	public int onRestSync() {
		//TODO save client side changes to server
		
		if(Prediction.getAndCreatePrediction(getServer_id())==null)
			return 0;
		else
			return 1;
	}
	
	@Override
	public int onRestGet() {
		Prediction pred = Prediction.getAndCreatePrediction(getServer_id());
		if(pred==null)
			return 0;
		else {
			setPrediction(pred);
			return 1;
		}
	}

	@Override
	public int onRestGetWithDependents() {
		// fetch also prediction's user
		Prediction pred = Prediction.getAndCreatePrediction(getServer_id());
		if(pred==null)
			return 0;
		else {
			setPrediction(pred);
			return 1;
		}
	}
	
	@Override
	public int onRestGetAllForCurUser() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static Prediction getAndCreatePrediction(int server_id) {
				
		PredictionRestClient predictionRestClient = new PredictionRestClient(server_id);
		JSONObject json = predictionRestClient.show(server_id);
		Prediction prediction = null;
		try {
			prediction = Prediction.getModelDao().queryForId(json.getInt("server_id"));
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} 	
		
		if(prediction==null)
			return null;
		
		User user = null;
		try {
			user = User.getModelDao().queryForId(json.getInt("user_id"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		
		if(user==null)
			return null;
		
		Bet bet = null;
		try {
			bet = Bet.getModelDao().queryForId(json.getInt("bet_id"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		
		if(bet==null)
			return null;
		
		try {
			prediction.setPrediction(json.getString("prediction"));
		} catch (JSONException e1) {
			
		}
		try {
			prediction.setDate(DateTime.parse(json.getString("created_at")));
		} catch (JSONException e) {
			
		}
		try {
			prediction.setResult(json.getBoolean("result"));
		} catch (JSONException e1) {
		
		}
		try {
			prediction.setMyAck(json.getString("user_ack"));
		} catch (JSONException e1) {
			
		}
		
		
		try {
			prediction.createOrUpdateLocal();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		return prediction;
	}
	
	public static List<Prediction> getAndCreatePredictions(Bet inBet) {
		
		List<Prediction> predictions = new ArrayList<Prediction>();
		
		DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
		
		PredictionRestClient userBetClient = new PredictionRestClient(inBet.getServer_id());
		JSONArray jsonPredictions = null;
		try {
			jsonPredictions = userBetClient.showPredictionsForBet(inBet.getServer_id());
		} catch (RestClientException e) {
			e.printStackTrace();
			return null;
		}
		if(jsonPredictions==null)
			return null;
		
		User user = null;
		JSONObject jsonPrediction = null;
		for (int i = 0; i < jsonPredictions.length(); i++) {
			try {
				jsonPrediction = jsonPredictions.getJSONObject(i);
			} catch (JSONException e1) {
				e1.printStackTrace();
				continue;
			}
					
			try {
				user = User.getAndCreateUser(jsonPrediction.getInt("user_id"));
			} catch (JSONException e2) {
				e2.printStackTrace();
				continue;
			}
			
			if(user==null)
				continue;
						
			try {		
				Prediction prediction = null;
				List<Prediction> tmpPredictions = null;
				try {
					tmpPredictions = Prediction.getModelDao().queryForEq("server_id", jsonPrediction.getInt("id"));
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if(tmpPredictions==null || tmpPredictions.size()==0) {
					prediction = new Prediction(inBet);
				} else {
					prediction = tmpPredictions.get(0);
				}
				
				prediction.setDate(formatter.parseDateTime(jsonPrediction.optString("created_at")));
				prediction.setMyAck(jsonPrediction.optString("user_ack"));
				prediction.setPrediction(jsonPrediction.optString("prediction"));
				prediction.setResult(jsonPrediction.optBoolean("result"));
				prediction.setServer_id(jsonPrediction.optInt("id",-1));
				prediction.setUser(user);
				prediction.createOrUpdateLocal();				
				predictions.add(prediction);
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
       	
		}
		return predictions;
	}

	public static void update(List<Prediction> predictions, int bet_id) {
		for (Prediction prediction : predictions) {
			try {
				prediction.update();
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}
		
		updatePredictionsTask = new UpdateBatchPredictionsTask(bet_id);
		updatePredictionsTask.run(predictions);
	}

	public Boolean setJson(JSONObject json) {
		return true;
	}
	
	private static class UpdateBatchPredictionsTask extends AsyncTask<Void, Void, Boolean> {
		
		private List<Prediction> predictions;
		private int bet_server_id;

		public UpdateBatchPredictionsTask(int bet_server_id) {
			super();
			this.bet_server_id = bet_server_id;
		}
			
		public void run(List<Prediction> predictions) {		
			this.predictions = predictions;
			
			if(getStatus()!=Status.RUNNING)
				execute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
	    	
			List<Prediction> paramList = new ArrayList<Prediction>();
			
			PredictionRestClient predictionClient = new PredictionRestClient(bet_server_id);
			
			for (Prediction prediction : predictions) {
				paramList.add(prediction);
			}
			
			try {
				predictionClient.update(paramList);
			} catch (RestClientException e) {
				e.printStackTrace();
				return false;
			}
		       					
			return true;
		}
		
	}
}
