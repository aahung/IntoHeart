package ee3316.intoheart.HTTP;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by aahung on 3/30/15.
 */
public class Connector {

    private static final String API_URL = "http://128.199.255.194:3000/";
    private API api;

    public Connector() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_URL) //call your base url
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
                    public void log(String msg) {
                        Log.i("retrofit", msg);
                    }
                })
                .build();
        api = restAdapter.create(API.class);
    }

    // email & password -> user name
    public void login(String email, String password, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        request("users/login", parameters, callback);
    }

    public void register(String name, String email, String password, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        request("users/signup", parameters, callback);
    }

    public void request(String url, Map<String, String> parameters, final JCallback<Outcome> callback) {
        api.makePostRequest(url, parameters, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement jsonElement, Response response) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                boolean success = jsonObject.get("success").getAsInt() == 1;
                callback.call(new Outcome(success, jsonObject.get("message").getAsString()));
            }

            @Override
            public void failure(RetrofitError error) {
                callback.call(new Outcome(false, null));
            }
        });
    }
}
