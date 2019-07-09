package com.example.wiremockexample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class HelloActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_BASE_URL = "extra_base_url"
        fun getLaunchIntent(context: Context, serverBaseUrl: String): Intent =
            Intent(context, HelloActivity::class.java)
                .putExtra(EXTRA_BASE_URL, serverBaseUrl)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)
        val textView = findViewById<TextView>(R.id.some_text)
        val helloWorldService =
            Retrofit.Builder()
                .baseUrl(intent.getStringExtra(EXTRA_BASE_URL))
                .client((application as MyApplication).createRetrofitHttpClient())
                .build()
                .create(HelloWorldService::class.java)
        helloWorldService.getHelloWorld().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                textView.text = if (response.isSuccessful) {
                    response.body()?.string()
                } else {
                    response.message()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                textView.text = t.message
            }
        })
    }
}
