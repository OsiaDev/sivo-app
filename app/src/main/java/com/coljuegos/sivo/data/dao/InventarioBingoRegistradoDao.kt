package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coljuegos.sivo.data.entity.InventarioBingoRegistradoEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface InventarioBingoRegistradoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: InventarioBingoRegistradoEntity): Long

    @Query("SELECT * FROM inventarios_bingo_registrados WHERE uuidActa = :actaUuid")
    fun getByActa(actaUuid: UUID): Flow<List<InventarioBingoRegistradoEntity>>

    @Query("SELECT * FROM inventarios_bingo_registrados WHERE uuidActa = :actaUuid")
    suspend fun getByActaList(actaUuid: UUID): List<InventarioBingoRegistradoEntity>

    @Query("SELECT * FROM inventarios_bingo_registrados WHERE uuidInventarioBingoRegistrado = :uuid")
    suspend fun getById(uuid: UUID): InventarioBingoRegistradoEntity?

    @Query("SELECT * FROM inventarios_bingo_registrados WHERE uuidInventario = :uuidInventario")
    suspend fun getByInventarioId(uuidInventario: UUID): InventarioBingoRegistradoEntity?

    @Delete
    suspend fun delete(entity: InventarioBingoRegistradoEntity)

    @Query("DELETE FROM inventarios_bingo_registrados WHERE uuidInventarioBingoRegistrado = :uuid")
    suspend fun deleteById(uuid: UUID)

    @Query("SELECT COUNT(*) FROM inventarios_bingo_registrados WHERE uuidActa = :actaUuid")
    fun getCountByActa(actaUuid: UUID): Flow<Int>

}