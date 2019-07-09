package com.example.wiremockexample

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET

interface HelloWorldService {
    @GET("/hello/world")
    fun getHelloWorld(): Call<ResponseBody>
}