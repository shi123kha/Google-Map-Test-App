package com.example.artivatic.rapido;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by artivatic on 12/6/17.
 */

interface ApiService {

    @GET("api/place/nearbysearch/json?sensor=true&key=AIzaSyDN7RJFmImYAca96elyZlE5s_fhX-MMuhk")
    Call<Example> getNearbyPlaces(@Query("type") int type, @Query("location") String location, @Query("radius") int radius);





  @GET("api/place/details/json?key=AIzaSyBCQCxI6tR2vDEyLE2oZUJ_GOIbzCkgfEM")
  Call<ResponseData>getPlaceDetail(@Query("placeid") String placeid);




}
