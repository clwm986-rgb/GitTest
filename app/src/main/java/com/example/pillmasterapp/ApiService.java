package com.example.pillmasterapp.network;

import com.example.pillmasterapp.data.PillDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("pill/list")
    Call<List<PillDto>> getPillList();
}
