package com.example.wiremockexample

import android.app.Application
import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Before
import java.io.File
import java.net.HttpURLConnection

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private var wireMockServer: WireMockServer? = null

    @Before
    fun setUp() {
        wireMockServer = configureWiremockServer()
        WireMock.resetToDefault()
    }

    @After
    fun tearDown() {
        wireMockServer?.stop()
    }

    @Test
    fun wiremockTest() {
        // Create a response body with a bunch of X characters
        // If the response is 256 characters or longer, this results in an exception on Meizu devices
        val responseBodyCharacterCount = 256
        val expectedServerResponse = (1..responseBodyCharacterCount).map { "X" }.fold("") { acc, elem -> acc + elem}
        mockServerResponse(expectedServerResponse)
        val appContext = ApplicationProvider.getApplicationContext<Application>()
        val launchIntent = HelloActivity.getLaunchIntent(appContext, wireMockServer!!.baseUrl())
        launch<HelloActivity>(launchIntent)
        SystemClock.sleep(500) // yeah yeah set up idling resources
        onView(withId(R.id.some_text)).check(matches(withText(expectedServerResponse)))
    }

    private fun mockServerResponse(expectedServerResponse: String) {
        stubFor(
            get(urlMatching("/hello/world"))
                .willReturn(
                    aResponse()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withHeader("Content-Type", "text/plain")
                        .withBody(expectedServerResponse)
                )
        )
    }

    private fun configureWiremockServer(): WireMockServer {

        val keystorePath = copyKeystore()
        val wireMockServer = WireMockServer(
            wireMockConfig()
                .notifier(ConsoleNotifier(true))
                .dynamicPort()
                .dynamicHttpsPort()
                .keystoreType("BKS")
                .keystorePath(keystorePath)
        )
        wireMockServer.start()
        WireMock.configureFor("localhost", wireMockServer.port())
        WireMock.resetAllRequests()
        return wireMockServer
    }

    private fun copyKeystore(): String {
        val application = ApplicationProvider.getApplicationContext<Application>()
        // This test keystore was created following these instructions:
        // https://medium.com/walmartlabs/android-testing-with-mocking-fcec5b9f71c2
        // https://groups.google.com/forum/#!topic/wiremock-user/oDExHctRPCc
        val keystoreFile = File(application.filesDir, "keystore_bks")
        if (!keystoreFile.exists()) {
            application.assets.open("tests/keystore_bks").use { input ->
                application.openFileOutput("keystore_bks", Context.MODE_PRIVATE).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return keystoreFile.absolutePath
    }
}
