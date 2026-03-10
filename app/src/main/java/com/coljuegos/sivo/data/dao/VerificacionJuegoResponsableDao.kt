package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coljuegos.sivo.data.entity.VerificacionJuegoResponsableEntity
import java.util.UUID

@Dao
interface VerificacionJuegoResponsableDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VerificacionJuegoResponsableEntity)

    @Query("SELECT * FROM verificacion_juego_responsable WHERE uuidActa = :actaUuid LIMIT 1")
    suspend fun getVerificacionByActaId(actaUuid: UUID): VerificacionJuegoResponsableEntity?

    @Query("DELETE FROM verificacion_juego_responsable WHERE uuidActa = :actaUuid")
    suspend fun deleteByActaId(actaUuid: UUID)

}