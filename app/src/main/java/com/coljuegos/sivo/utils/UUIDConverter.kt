package com.coljuegos.sivo.utils

import androidx.room.TypeConverter
import java.util.UUID
import kotlin.let

class UUIDConverter {

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return uuid?.let { UUID.fromString(it) }
    }

}