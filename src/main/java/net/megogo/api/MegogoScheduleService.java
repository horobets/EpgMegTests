package net.megogo.api;

import net.megogo.api.models.ApiResponseResult;
import net.megogo.api.models.ChannelData;
import net.megogo.api.models.TimestampData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface MegogoScheduleService {

    @GET("time")
    Call<ApiResponseResult<TimestampData>> getTime();

    @GET("channel")
    Call<ApiResponseResult<List<ChannelData>>> getChannel(@Query("video_ids") String videoIds);
}
