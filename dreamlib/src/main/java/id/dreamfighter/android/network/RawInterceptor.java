package id.dreamfighter.android.network;

import android.util.Log;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Created by fitra on 19/04/2016.
 */
class RawInterceptor implements Interceptor {
    private static final String TAG = "Retrofit";

    @Override
    public Response intercept(Chain chain) throws IOException {
        //Log.d(TAG," --- START REST ---");
        Request request = chain.request();
        long t1 = System.nanoTime();
        StringBuilder sb = new StringBuilder("[HEADERS]\n");
        for(String name:request.headers().names()){
            sb.append(String.format("%s:%s\n",name,request.header(name)));
        }
        String requestLog = String.format(Locale.getDefault(),"Sending request %s: %s on %s%n%s",request.method(), request.url(), chain.connection(),sb.toString());
        if(request.method().compareToIgnoreCase("post")==0){
            requestLog ="\n"+requestLog+"\n"+bodyToString(request);
        }

//        Log.d(TAG,requestLog);
        //Log.d(TAG," --- start request --- " + t1);

        //Log.d(TAG," --- end request ---");
        Response response = chain.proceed(request);
        long t2 = System.nanoTime();

        String responseLog = String.format(Locale.getDefault(),"Received response in %.1fms%n%s",
                (t2 - t1) / 1e6d, response.headers());

       // String bodyString = response.body();


        //Log.d(TAG," --- start response--- " + t1);
        Log.d(TAG,"request : "+requestLog);
        Log.d(TAG,"response: "+responseLog);
        //Log.d(TAG,"Content : "+bodyString);

        //Log.d(TAG," --- end response---");

//        Log.d("TAG","response"+"\n"+responseLog+"\n"+bodyString);
        //Log.d(TAG," --- END REST ---");
        //ResponseBody responseBody = ResponseBody.create(response.body(), bodyString);

        Response.Builder finalResponse = response.newBuilder()
                .code(200)
                .body(response.body());


        return finalResponse.build();

    }


    public static String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }
}
