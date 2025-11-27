package com.coljuegos.sivo.data.dao

import androidx.room.*
import com.coljuegos.sivo.data.entity.FirmaActaEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface FirmaActaDao {

    @Query("SELECT * FROM firma_acta WHERE uuidActa = :actaUuid")
    fun getFirmaActaByActaUuid(actaUuid: UUID): Flow<FirmaActaEntity?>

    @Query("SELECT * FROM firma_acta WHERE uuidActa = :actaUuid")
    suspend fun getFirmaActaByActaUuidSuspend(actaUuid: UUID): FirmaActaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFirmaActa(firmaActa: FirmaActaEntity)

    @Update
    suspend fun updateFirmaActa(firmaActa: FirmaActaEntity)

    @Query("DELETE FROM firma_acta WHERE uuidActa = :actaUuid")
    suspend fun deleteFirmaActaByActaUuid(actaUuid: UUID)

}