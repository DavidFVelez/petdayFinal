package com.davidvelez.petday

import android.app.Application
import com.google.firebase.auth.FirebaseUser

class MomentsApplication : Application() {
    companion object {
        const val PATH_SNAPSHOTS = "snapshots"
        const val PROPERTY_LIKE_LIST = "likeList"
        lateinit var currentUser: FirebaseUser
    }

}