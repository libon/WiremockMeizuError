package com.example.wiremockexample

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import kotlin.reflect.jvm.jvmName

class MyTestRunner: AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, AndroidTestMyApplication::class.jvmName, context)
    }
}