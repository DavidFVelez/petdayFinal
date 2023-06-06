package com.davidvelez.petday

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
data class Moment(
    @get:Exclude var id: String = "",
    var ownerUid: String = "",
    var title: String = "",
    var photoUrl: String ="",
    var likeList: Map<String, Boolean> = mutableMapOf()
)
