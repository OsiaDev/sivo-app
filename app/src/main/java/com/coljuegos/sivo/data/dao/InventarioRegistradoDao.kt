package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coljuegos.sivo.data.entity.InventarioRegistradoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface InventarioRegistradoDao {

    @Query("SELECT * FROM inventarios_registrados WHERE uuidActa = :actaUuid")
    fun getInventariosRegistradosByActa(actaUuid: UUID): Flow<List<InventarioRegistradoEntity>>

    @Query("SELECT * FROM inventarios_registrados WHERE uuidInventarioRegistrado = :uuidInventarioRegistrado")
    suspend fun getInventarioRegistradoById(uuidInventarioRegistrado: UUID): InventarioRegistradoEntity?

    @Query("SELECT * FROM inventarios_registrados WHERE uuidInventario = :uuidInventario")
    suspend fun getInventarioRegistradoByInventarioId(uuidInventario: UUID): InventarioRegistradoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inventarioRegistrado: InventarioRegistradoEntity): Long

    @Update
    suspend fun update(inventarioRegistrado: InventarioRegistradoEntity)

    @Delete
    suspend fun delete(inventarioRegistrado: InventarioRegistradoEntity)

    @Query("DELETE FROM inventarios_registrados WHERE uuidInventarioRegistrado = :uuidInventarioRegistrado")
    suspend fun deleteById(uuidInventarioRegistrado: UUID)

    @Query("DELETE FROM inventarios_registrados WHERE uuidActa = :actaUuid")
    suspend fun deleteByActa(actaUuid: UUID)

    @Query("SELECT COUNT(*) FROM inventarios_registrados WHERE uuidActa = :actaUuid")
    fun getCountByActa(actaUuid: UUID): Flow<Int>

    @Query("SELECT * FROM inventarios_registrados WHERE uuidActa = :actaUuid")
    suspend fun getInventariosRegistradosByActaList(actaUuid: UUID): List<InventarioRegistradoEntity>

}