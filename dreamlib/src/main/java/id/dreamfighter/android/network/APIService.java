package id.dreamfighter.android.network;


import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface APIService {
    String domain = null;

    @GET
    Observable<Response<ResponseBody>> get(@Url String url);

    @POST
    Observable<Response<ResponseBody>> post(@Url String url);
}