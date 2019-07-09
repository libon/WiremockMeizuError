package com.example.wiremockexample

import okhttp3.OkHttpClient

class AndroidTestMyApplication : MyApplication() {
    override fun createRetrofitHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .sslSocketFactory(TrustingSSL.trustingSSLSocketFactory, TrustingSSL.trustingTrustManager)
            .hostnameVerifier(TrustingSSL.trustingHostenameVerifier)
            .build()

}