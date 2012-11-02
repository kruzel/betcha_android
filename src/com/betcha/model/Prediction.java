package com.betcha.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;

import android.os.AsyncTask;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.PredictionRestClient;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "predictions")
public class Prediction extends ModelCache<Prediction, String> {
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private User user;
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	private Bet bet;
	@DatabaseField
	private String prediction;
	@DatabaseField
	private Boolean result;
	@DatabaseField
	private String user_ack; // Yes/No/Pending

	private static PredictionRestClient predictionRestClient;
	private static UpdateBatchPredictionsTask updatePredictionsTask;
	private static Dao<Prediction, String> dao;

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
		result = newPrediction.getResult();
		user_ack = newPrediction.getMyAck();
		setCreated_at(newPrediction.getCreated_at());
		setUpdated_at(newPrediction.getUpdated_at());
	}

	public PredictionRestClient getPredictionRestClient() {
		if (predictionRestClient == null)
			// nested url = bets/:bet_id/predictions
			predictionRestClient = new PredictionRestClient(bet.getId());

		return predictionRestClient;
	}
	
	public HttpStatus getLastRestErrorCode() {
		return getPredictionRestClient().getLastRestErrorCode();
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
	 * 
	 * @return Dao object
	 * @throws SQLException
	 */
	public static Dao<Prediction, String> getModelDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Prediction.class);
			dao.setObjectCache(true);
		}
		return dao;
	}

	@Override
	protected Dao<Prediction, String> getDao() throws SQLException {
		if (dao == null) {
			dao = getDbHelper().getDao(Prediction.class);
			dao.setObjectCache(true);
		}
		return dao;
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
		JSONObject json = null;

		if (getSendInvite()) {
			json = getPredictionRestClient().createAndInvite(this);
		} else {
			json = getPredictionRestClient().create(this);
		}

		if (json == null)
			return 0;

		return 1;
	}

	public int onRestUpdate() {
		getPredictionRestClient().update(this, getId());
		return 1;
	}

	public int onRestDelete() {
		getPredictionRestClient().delete(getId());
		return 1;
	}

	@Override
	public int onRestGet() {
		PredictionRestClient predictionRestClient = new PredictionRestClient(id);
		JSONObject jsonPredictions = predictionRestClient.show(id);
		if(jsonPredictions==null)
			return 0;
		
		JSONArray jsonArray = null;
		try {
			jsonArray = jsonPredictions.getJSONArray("predictions");
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
		
		if(jsonArray==null)
			return 0;

		JSONObject jsonContent = null;
		try {
			jsonContent = jsonArray.getJSONObject(0);
		} catch (JSONException e1) {
		}
		
		if(jsonContent==null)
			return 0;

		User user = null;
		try {
			List<User> users = User.getModelDao().queryForEq("id",
					jsonContent.getString("user_id"));
			if (users != null && users.size() > 0)
				user = users.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (user == null)
			return 0;

		Bet bet = null;
		try {
			List<Bet> bets = Bet.getModelDao().queryForEq("id",
					jsonContent.getString("bet_id"));
			if (bets != null && bets.size() > 0)
				bet = bets.get(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (bet == null)
			return 0;

		setJson(jsonContent);

		try {
			createOrUpdateLocal();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}

		return 1;
	}

	@Override
	public int onRestGetAllForCurUser() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Boolean setJson(JSONObject json) {
		super.setJson(json);

		setResult(json.optBoolean("result", false));
		setPrediction(json.optString("prediction", ""));
		setMyAck(json.optString("user_ack", "Pending"));

		try {
			user = User.getUserLocalOrRemoteInner(json.getString("user_id"));
		} catch (JSONException e2) {
			e2.printStackTrace();
			return false;
		}

		return true;
	}

	public JSONObject toJson() {
		JSONObject jsonContent = new JSONObject();
		JSONObject jsonParent = new JSONObject();

		try {
			jsonContent.put("id", getId());
			jsonContent.put("user_ack", getMyAck());
			jsonContent.put("prediction", getPrediction());
			jsonContent.put("bet_id", getBet().getId());
			jsonContent.put("user_id", getUser().getId());
			if(getResult()!=null)
				jsonContent.put("result", Boolean.toString(getResult()));
			try {
				jsonParent.put("prediction", jsonContent);
			} catch (RestClientException e) {
				e.printStackTrace();
				return null;
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}

		return jsonParent;
	}

	public static void update(List<Prediction> predictions, String bet_id) {
		for (Prediction prediction : predictions) {
			try {
				prediction.getDao().update(prediction);
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
		}

		updatePredictionsTask = new UpdateBatchPredictionsTask(bet_id);
		updatePredictionsTask.run(predictions);
	}

	private static class UpdateBatchPredictionsTask extends
			AsyncTask<Void, Void, Boolean> {

		private List<Prediction> predictions;
		private String bet_id;

		public UpdateBatchPredictionsTask(String bet_id) {
			super();
			this.bet_id = bet_id;
		}

		public void run(List<Prediction> predictions) {
			this.predictions = predictions;

			if (getStatus() != Status.RUNNING)
				execute();
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			List<Prediction> paramList = new ArrayList<Prediction>();

			PredictionRestClient predictionClient = new PredictionRestClient(
					bet_id);

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
