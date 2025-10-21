package com.example.tea_quarkus;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface TeaApi {
    @GET("teas")
    Call<List<Tea>> getAllTeas();

    @GET("teas/{id}")
    Call<Tea> getTea(@Path("id") long id);

    @POST("teas")
    Call<Tea> createTea(@Body Tea tea);

    @PUT("teas/{id}")
    Call<Tea> updateTea(@Path("id") long id, @Body Tea tea);

    @DELETE("teas/{id}")
    Call<Void> deleteTea(@Path("id") long id);
}
