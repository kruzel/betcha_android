package com.betcha.model;

import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.betcha.model.cache.ModelCache;
import com.betcha.model.server.api.PredictionRestClient;
import com.betcha.model.tasks.UpdatePredictionsTask;
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
	
	// TODO change to static to save memory
	private PredictionRestClient predictionRestClient;
	private UpdatePredictionsTask updatePredictionsTask;
	
	public Prediction() {
		super();
	}

	public Prediction(Bet bet) {
		super();
		setBet(bet);
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
	public static Dao<Prediction,Integer> getModelDao() throws SQLException {
		return getDbHelper().getDao(Prediction.class);
	}
	
	@Override
	public void initDao() {
		try {
			setDao((Dao<Prediction, Integer>) getDbHelper().getDao(Prediction.class));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected static Class<?> getModelClass() {
		return Prediction.class;
	}
	
	/** inherited ModelCache methods */
	
	public int onRestCreate() {		
		int res = 0;
		JSONObject json = null;
		json = getPredictionRestClient().create(this);
		
		setServer_id(json.optInt("id", -1));
		try {
			res = updateLocal();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		JSONObject json = getPredictionRestClient().show(getServer_id());
		try {
			user = User.getModelDao().queryForId(json.getInt("user_id"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		try {
			bet = Bet.getModelDao().queryForId(json.getInt("bet_id"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		try {
			prediction = json.getString("prediction");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			date= DateTime.parse(json.getString("created_at"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			result = json.getBoolean("result");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			user_ack = json.getString("user_ack");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int res = 0;
		try {
			res = updateLocal();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}

	public void update(List<Prediction> predictions) {
		updatePredictionsTask = new UpdatePredictionsTask(bet.getServer_id());
		updatePredictionsTask.setValues(predictions);
		updatePredictionsTask.run();
	}
}
