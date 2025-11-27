package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coljuegos.sivo.data.entity.VerificacionContractualEntity
import java.util.UUID

@Dao
interface VerificacionContractualDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(verificacionContractual: VerificacionContractualEntity)

    @Update
    suspend fun update(verificacionContractual: VerificacionContractualEntity)

    @Query("SELECT * FROM verificacion_contractual WHERE uuidActa = :actaUuid LIMIT 1")
    suspend fun getVerificacionContractualByActaId(actaUuid: UUID): VerificacionContractualEntity?

    @Query("DELETE FROM verificacion_contractual WHERE uuidActa = :actaUuid")
    suspend fun deleteByActaId(actaUuid: UUID)

}