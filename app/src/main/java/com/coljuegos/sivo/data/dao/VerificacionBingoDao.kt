package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coljuegos.sivo.data.entity.VerificacionBingoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface VerificacionBingoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VerificacionBingoEntity)

    @Query("SELECT * FROM verificacion_bingo WHERE uuidActa = :actaUuid LIMIT 1")
    suspend fun getVerificacionBingoByActaId(actaUuid: UUID): VerificacionBingoEntity?

    @Query("SELECT * FROM verificacion_bingo WHERE uuidActa = :actaUuid LIMIT 1")
    fun getVerificacionBingoByActaIdFlow(actaUuid: UUID): Flow<VerificacionBingoEntity?>

    @Query("DELETE FROM verificacion_bingo WHERE uuidActa = :actaUuid")
    suspend fun deleteByActaId(actaUuid: UUID)

}