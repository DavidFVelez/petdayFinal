package com.davidvelez.petday

import android.app.Application
import com.google.firebase.auth.FirebaseUser

class ServicesApplication : Application() {

    companion object {
        const val PATH_SERVICES = "services"
        const val PROPERTY_LIKE_LIST = "likeList"

        lateinit var currentUser: FirebaseUser
    }
}