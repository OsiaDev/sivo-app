package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coljuegos.sivo.data.entity.NovedadRegistradaEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface NovedadRegistradaDao {

    @Query("SELECT * FROM novedades_registradas WHERE uuidActa = :actaUuid")
    fun getNovedadesRegistradasByActa(actaUuid: UUID): Flow<List<NovedadRegistradaEntity>>

    @Query("SELECT * FROM novedades_registradas WHERE uuidActa = :actaUuid")
    suspend fun getNovedadesRegistradasByActaList(actaUuid: UUID): List<NovedadRegistradaEntity>

    @Query("SELECT * FROM novedades_registradas WHERE uuidNovedadRegistrada = :uuidNovedadRegistrada")
    suspend fun getNovedadRegistradaById(uuidNovedadRegistrada: UUID): NovedadRegistradaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(novedadRegistrada: NovedadRegistradaEntity): Long

    @Update
    suspend fun update(novedadRegistrada: NovedadRegistradaEntity)

    @Delete
    suspend fun delete(novedadRegistrada: NovedadRegistradaEntity)

    @Query("DELETE FROM novedades_registradas WHERE uuidNovedadRegistrada = :uuidNovedadRegistrada")
    suspend fun deleteById(uuidNovedadRegistrada: UUID)

    @Query("DELETE FROM novedades_registradas WHERE uuidActa = :actaUuid")
    suspend fun deleteByActa(actaUuid: UUID)

    @Query("SELECT COUNT(*) FROM novedades_registradas WHERE uuidActa = :actaUuid")
    fun getCountByActa(actaUuid: UUID): Flow<Int>

}