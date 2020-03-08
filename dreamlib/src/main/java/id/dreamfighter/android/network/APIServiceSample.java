package id.dreamfighter.android.network;


import io.reactivex.Observable;
import retrofit2.http.GET;

public interface APIServiceSample extends APIService{
    String domain = "http://google.com";

    @GET
    Observable<String> getString();
}