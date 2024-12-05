package net.megogo.api;

import net.megogo.api.utils.RequestUtilInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MegogoRestClient {

    private static final String BASE_URL = "https://epg.megogo.net/";

    private OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new RequestUtilInterceptor())
            .build();

    private Retrofit retrofit = new Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    public final MegogoScheduleService megogoScheduleService = retrofit.create(MegogoScheduleService.class);
}
