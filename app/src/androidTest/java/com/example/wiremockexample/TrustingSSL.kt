package com.example.wiremockexample

import java.net.Socket
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

object TrustingSSL {
    val trustingSSLSocketFactory: SSLSocketFactory
        get() {
            val trustingTrustManagers = arrayOf<TrustManager>(trustingTrustManager)
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustingTrustManagers, SecureRandom())
            return sc.socketFactory
        }

    val trustingTrustManager: X509ExtendedTrustManager
        get() = object : X509ExtendedTrustManager() {
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

            override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String, socket: Socket) {
            }

            override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String, socket: Socket) {
            }

            override fun checkClientTrusted(x509Certificates: Array<X509Certificate>, s: String, sslEngine: SSLEngine) {
            }

            override fun checkServerTrusted(x509Certificates: Array<X509Certificate>, s: String, sslEngine: SSLEngine) {
            }

            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}

            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
        }

    val trustingHostenameVerifier: HostnameVerifier = object : HostnameVerifier {
        override fun verify(hostname: String?, session: SSLSession?) = true
    }

}
