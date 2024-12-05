package net.megogo.api;

import net.megogo.api.utils.RequestUtilInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

public class MegogoRestClient {

    public static final String BASE_URL = "https://epg.megogo.net/";

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new RequestUtilInterceptor())
            .build();

    Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    public MegogoScheduleService megogoScheduleService = retrofit.create(MegogoScheduleService.class);
}
