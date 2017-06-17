package com.example.artivatic.rapido;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by artivatic on 12/6/17.
 */



    public class RestClient {
        public static final
        String url = "https://maps.googleapis.com/maps/";


        public static final String version = "v1";
        private ApiService apiService;


        public RestClient() {



            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(new ItemTypeAdapterFactory()) // This is the important line ;)
                    .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                    .create();
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
//

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
//        Log.e("LOGGER","HEADERS !!!! +++ "+client.)
            apiService =
                    retrofit.create(ApiService.class);





        }

        public ApiService getApiService() {
            return apiService;
        }


}
