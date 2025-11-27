package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coljuegos.sivo.data.entity.FuncionarioEntity
import java.util.UUID

@Dao
interface FuncionarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(funcionarios: List<FuncionarioEntity>)

    @Query("SELECT * FROM funcionarios WHERE uuidActa = :actaId")
    suspend fun getFuncionariosByActa(actaId: UUID): List<FuncionarioEntity>

    @Query("DELETE FROM funcionarios WHERE uuidActa IN (SELECT uuidActa FROM actas WHERE numActa IN (:numActas))")
    suspend fun deleteFuncionariosByNumActas(numActas: List<Int>)

    @Query("DELETE FROM funcionarios WHERE uuidActa = :actaId")
    suspend fun deleteFuncionariosByActa(actaId: UUID)

}