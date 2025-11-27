package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coljuegos.sivo.data.entity.InventarioEntity
import java.util.UUID

@Dao
interface InventarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(inventarios: List<InventarioEntity>)

    @Query("SELECT * FROM inventarios WHERE uuidActa = :actaId")
    suspend fun getInventariosByActa(actaId: UUID): List<InventarioEntity>

    @Query("DELETE FROM inventarios WHERE uuidActa IN (SELECT uuidActa FROM actas WHERE numActa IN (:numActas))")
    suspend fun deleteInventariosByNumActas(numActas: List<Int>)

    @Query("DELETE FROM inventarios WHERE uuidActa = :actaId")
    suspend fun deleteInventariosByActa(actaId: UUID)

    @Query("SELECT * FROM inventarios WHERE uuidInventario = :uuidInventario LIMIT 1")
    suspend fun getInventarioByUuid(uuidInventario: UUID): InventarioEntity?

}