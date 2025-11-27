package com.coljuegos.sivo.utils

import androidx.room.TypeConverter
import com.coljuegos.sivo.data.entity.ActaStateEnum

class ActaStateConverter {

    @TypeConverter
    fun fromActaState(state: ActaStateEnum?): String? {
        return state?.name
    }

    @TypeConverter
    fun toActaState(stateName: String?): ActaStateEnum? {
        return stateName?.let { ActaStateEnum.valueOf(it) }
    }

}