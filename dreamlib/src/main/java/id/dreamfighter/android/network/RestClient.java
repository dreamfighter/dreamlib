package id.dreamfighter.android.network;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by ecq on 18/04/2016.
 */
public class RestClient {

    //public static final String BASE_URL = "http://192.168.100.120:9005";
//    public static final String BASE_URL = "http://192.168.0.12:9091";
//    public static final String BASE_URL = "http://52.8.150.124:9090";
    //private  Class<? extends APIService> service;
    //private static OkHttpClient client;
    //private Context context;

    public RestClient(){

    }

    public static <T> T withAuth(Class<T> clazz,final String token) {
        //Gson gson = new GsonBuilder()
        //        .setLenient()
        //        .create();

        okhttp3.Interceptor interceptor = new okhttp3.Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();

                Request request = builder
                        .header("User-Agent", "Retrofit app")
                        .header("Authorization", String.format("Bearer %s", token))
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        };
        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(interceptor);
        builder.interceptors().add(new RestInterceptor());
        builder.connectTimeout(30, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();

        //dinamic IP
        APIService obj;
        String mIpAddress = null;
        if(APIService.class.equals(clazz.getSuperclass())){
            try {
                obj = ((APIService) clazz.newInstance());
                mIpAddress = obj.domain;
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        Retrofit.Builder restBuilder = new Retrofit.Builder();

        if(mIpAddress!=null){
            restBuilder.baseUrl(mIpAddress);
        }

        Retrofit retrofit = restBuilder

                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

                .client(client)
                .build();

        return retrofit.create(clazz);
    }

    public <T> T extService(Class<T> clazz) {
        //Gson gson = new GsonBuilder()
        //        .setLenient()
        //        .create();

        okhttp3.Interceptor interceptor = new okhttp3.Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder();



                Request request = builder
                        .header("User-Agent", "Retrofit app")
                        .header("Referer", "Retrofit app")
                        .header("Content-Type", "application/json")
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        };
        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(interceptor);
        builder.interceptors().add(new RestInterceptor());
        builder.connectTimeout(30, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();

        //dinamic IP

        APIService obj;
        String mIpAddress = null;
        if(APIService.class.equals(clazz.getSuperclass())){
            try {
                obj = ((APIService) clazz.newInstance());
                mIpAddress = obj.domain;
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        Retrofit.Builder restBuilder = new Retrofit.Builder();

        if(mIpAddress!=null){
            restBuilder.baseUrl(mIpAddress);
        }

        Retrofit retrofit = restBuilder
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        return retrofit.create(clazz);
    }

    public <T> T request(Class<T> clazz) {
        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder b = original.newBuilder();

            Request request = b
                    .header("User-Agent", "Retrofit app")
                    .header("Content-Type", "application/json")
                    .method(original.method(), original.body())
                    .build();

            return chain.proceed(request);
        });
        //builder.interceptors().add(new RestInterceptor());
        builder.connectTimeout(30, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();

        //dinamic IP

        APIService obj;
        String mIpAddress = null;
        if(APIService.class.equals(clazz.getSuperclass())){
            try {
                obj = ((APIService) clazz.newInstance());
                mIpAddress = obj.domain;
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        Retrofit.Builder restBuilder = new Retrofit.Builder();

        if(mIpAddress!=null){
            restBuilder.baseUrl(mIpAddress);
        }

        Retrofit retrofit = restBuilder
                //.addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        return retrofit.create(clazz);
    }

    public <T> T requestProgress(Class<T> clazz,final ProgressListener listener) {


        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder b = original.newBuilder();

            Request request = b
                    .header("User-Agent", "Retrofit app")
                    .header("Content-Type", "application/json")
                    .method(original.method(), original.body())

                    .build();

            String requestLog = String.format("Sending request %s: %s on %s%n%s",request.method(), request.url(), chain.connection(), request.header("Authorization"));

            Log.d("Retrofit",requestLog);

            Response originalResponse = chain.proceed(request);

            return originalResponse.newBuilder()
                    .body(new ProgressResponseBody(originalResponse.body(), listener))
                    .build();
        });
        //builder.interceptors().add(new RestInterceptor());
        builder.connectTimeout(60, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();

        //dinamic IP

        APIService obj;
        String mIpAddress = null;
        if(APIService.class.equals(clazz.getSuperclass())){
            try {
                obj = ((APIService) clazz.newInstance());
                mIpAddress = obj.domain;
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        Retrofit.Builder restBuilder = new Retrofit.Builder();

        if(mIpAddress!=null){
            restBuilder.baseUrl(mIpAddress);
        }

        Retrofit retrofit = restBuilder
                //.addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

                .client(client)
                .build();
        return retrofit.create(clazz);
    }

    public static <T> T json(Class<T> clazz) {
        return json(clazz);
    }

    public static <T> T json(Class<T> clazz,final ProgressListener listener) {


        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder b = original.newBuilder();

            Request request = b
                    .header("User-Agent", "Retrofit app")
                    .header("Content-Type", "application/json")
                    .method(original.method(), original.body())

                    .build();

            String requestLog = String.format("Sending request %s: %s on %s%n%s",request.method(), request.url(), chain.connection(), request.header("Authorization"));

            Log.d("Retrofit",requestLog);

            Response originalResponse = chain.proceed(request);

            if(listener!=null) {
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), listener))
                        .build();
            }else{
                return originalResponse;
            }
        });
        //builder.interceptors().add(new RestInterceptor());
        builder.connectTimeout(60, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();

        //dinamic IP

        APIService obj;
        String mIpAddress = null;
        if(APIService.class.equals(clazz.getSuperclass())){
            try {
                obj = ((APIService) clazz.newInstance());
                mIpAddress = obj.domain;
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        Retrofit.Builder restBuilder = new Retrofit.Builder();

        if(mIpAddress!=null){
            restBuilder.baseUrl(mIpAddress);
        }

        Retrofit retrofit = restBuilder
                .addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
        return retrofit.create(clazz);
    }

    public static <T> T raw(Class<T> clazz) {
        return raw(clazz,null);
    }

    public static <T> T raw(Class<T> clazz,final ProgressListener listener) {

        // Add the interceptor to OkHttpClient
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder b = original.newBuilder();

            Request request = b
                    .header("User-Agent", "Retrofit app")
                    .method(original.method(), original.body())
                    .build();

            String requestLog = String.format("Sending request %s: %s on %s%n%s",request.method(), request.url(), chain.connection(), request.header("Authorization"));

            Log.d("Retrofit",requestLog);

            Response originalResponse = chain.proceed(request);

            if(listener!=null) {
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), listener))
                        .build();
            }else{
                return originalResponse;
            }
        });
        //builder.interceptors().add(new RestInterceptor());
        builder.connectTimeout(60, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();

        //dinamic IP

        APIService obj;
        String mIpAddress = null;
        if(APIService.class.equals(clazz.getSuperclass())){
            try {
                obj = ((APIService) clazz.newInstance());
                mIpAddress = obj.domain;
            }catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        Retrofit.Builder restBuilder = new Retrofit.Builder();

        if(mIpAddress!=null){
            restBuilder.baseUrl(mIpAddress);
        }

        Retrofit retrofit = restBuilder
                //.addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

                .client(client)
                .build();
        return retrofit.create(clazz);
    }

    public interface ProgressListener {
        void update(long bytesRead, long contentLength, double percentage, boolean done);
    }

    public static class ProgressResponseBody extends ResponseBody {

        private ResponseBody responseBody;
        private ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody,
                                            ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;

                    if (null != progressListener) {
                        progressListener.update(totalBytesRead, responseBody.contentLength(),
                                100 * (1.0 * totalBytesRead / responseBody.contentLength()), bytesRead == -1);
                    }
                    return bytesRead;
                }
            };

        }
    }
}