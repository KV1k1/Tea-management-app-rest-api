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

    //login register
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // ADMIN ENDPOINTS
    @GET("teas/admin")
    Call<List<Tea>> getAllTeasAdmin(@Header("Authorization") String authHeader);

    @GET("teas/admin/{id}")
    Call<Tea> getTeaAdmin(@Header("Authorization") String authHeader, @Path("id") long id);

    @POST("teas/admin")
    Call<Tea> createTeaAdmin(@Header("Authorization") String authHeader, @Body Tea tea);

    @PUT("teas/admin/{id}")
    Call<Tea> updateTeaAdmin(@Header("Authorization") String authHeader, @Path("id") long id, @Body Tea tea);

    @DELETE("teas/admin/{id}")
    Call<Void> deleteTeaAdmin(@Header("Authorization") String authHeader, @Path("id") long id);

}
