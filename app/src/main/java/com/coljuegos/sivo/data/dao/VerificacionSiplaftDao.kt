package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coljuegos.sivo.data.entity.VerificacionSiplaftEntity
import java.util.UUID

@Dao
interface VerificacionSiplaftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(verificacionSiplaft: VerificacionSiplaftEntity)

    @Update
    suspend fun update(verificacionSiplaft: VerificacionSiplaftEntity)

    @Query("SELECT * FROM verificacion_siplaft WHERE uuidActa = :actaUuid LIMIT 1")
    suspend fun getVerificacionSiplaftByActaId(actaUuid: UUID): VerificacionSiplaftEntity?

    @Query("DELETE FROM verificacion_siplaft WHERE uuidActa = :actaUuid")
    suspend fun deleteByActaId(actaUuid: UUID)

}