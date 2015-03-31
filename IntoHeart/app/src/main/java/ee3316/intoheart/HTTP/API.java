package ee3316.intoheart.HTTP;

import com.google.gson.JsonElement;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.EncodedPath;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;

/**
 * Created by aahung on 3/30/15.
 */
public interface API {
    @GET("/{url}")
    public void makeGetRequest(@EncodedPath("url") String url,
                               Callback<JsonElement> callback);

    @FormUrlEncoded
    @POST("/{url}")
    public void makePostRequest(@EncodedPath("url") String url,
                                @FieldMap Map<String, String> parameters,
                                Callback<JsonElement> callback);
}
