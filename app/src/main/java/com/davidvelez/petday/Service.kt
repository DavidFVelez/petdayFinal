package com.davidvelez.petday

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Service(
    @get:Exclude var id: String = "", // Se excluye el ID en Firebase con una anotación
    var ownerUid: String = "",
    var title: String = "",
    var photoUrl: String ="",
    var likeList: Map<String, Boolean> = mutableMapOf()
)
