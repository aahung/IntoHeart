package ee3316.intoheart.HTTP;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    public void search(String email, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        request("users/search", parameters, callback);
    }

    public void sendRequest(String email, String password, String tEmail, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("t_email", tEmail);
        request("users/request", parameters, callback);
    }

    public void responseRequest(String email, String password, String tEmail, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("t_email", tEmail);
        request("users/response", parameters, callback);
    }

    public void rank(String email, String password, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        api.makePostRequest("users/rank", parameters, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement jsonElement, Response response) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                boolean success = jsonObject.get("success").getAsInt() == 1;
                Object object = "Network error";
                try {
                    object = jsonObject.get("friends").getAsJsonArray();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                callback.call(new Outcome(success, object));
            }

            @Override
            public void failure(RetrofitError error) {
                callback.call(new Outcome(false, null));
            }
        });
    }

    public void friendRequests(String email, String password, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        api.makePostRequest("users/get_request", parameters, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement jsonElement, Response response) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                boolean success = jsonObject.get("success").getAsInt() == 1;
                Object object = "Network error";
                try {
                    object = jsonObject.get("from").getAsJsonArray();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                callback.call(new Outcome(success, object));
            }

            @Override
            public void failure(RetrofitError error) {
                callback.call(new Outcome(false, null));
            }
        });
    }

    public void getUserInfo(String email, String password, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        api.makePostRequest("users/info", parameters, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement jsonElement, Response response) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                boolean success = jsonObject.get("success").getAsInt() == 1;
                Object object = "Network error";
                try {
                    object = jsonObject.get("info").getAsJsonObject();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                callback.call(new Outcome(success, object));
            }

            @Override
            public void failure(RetrofitError error) {
                callback.call(new Outcome(false, null));
            }
        });
    }

    public void updateUserInfo(String email, String password, String data, final JCallback<Outcome> callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("data", data);
        request("users/update", parameters, callback);
    }

    public void request(String url, Map<String, String> parameters, final JCallback<Outcome> callback) {
        api.makePostRequest(url, parameters, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement jsonElement, Response response) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                boolean success = jsonObject.get("success").getAsInt() == 1;
                String object = "Network error";
                try {
                    if (jsonObject.get("message") != null)
                        object = jsonObject.get("message").getAsString();
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
                callback.call(new Outcome(success, object));
            }

            @Override
            public void failure(RetrofitError error) {
                callback.call(new Outcome(false, null));
            }
        });
    }
}
