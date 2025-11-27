package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coljuegos.sivo.data.entity.TipoApuestaEntity

@Dao
interface TipoApuestaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tiposApuesta: List<TipoApuestaEntity>)

    @Query("SELECT * FROM tipos_apuesta ORDER BY nombreTipoApuesta ASC")
    suspend fun getAllTiposApuesta(): List<TipoApuestaEntity>

    @Query("SELECT * FROM tipos_apuesta WHERE codigoTipoApuesta = :codigo LIMIT 1")
    suspend fun getTipoApuestaByCodigo(codigo: Long): TipoApuestaEntity?

    @Query("SELECT COUNT(*) FROM tipos_apuesta")
    suspend fun getCount(): Int

    @Query("DELETE FROM tipos_apuesta")
    suspend fun deleteAll()

}