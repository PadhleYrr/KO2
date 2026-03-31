package com.gkk.mppsc

import android.app.Application
import com.google.firebase.FirebaseApp

class GKKApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
