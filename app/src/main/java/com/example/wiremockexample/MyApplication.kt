package com.example.wiremockexample

import android.app.Application
import okhttp3.OkHttpClient

open class MyApplication : Application() {

    open fun createRetrofitHttpClient() : OkHttpClient = OkHttpClient()
}