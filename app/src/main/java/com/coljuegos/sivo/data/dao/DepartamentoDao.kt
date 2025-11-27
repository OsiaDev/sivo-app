package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coljuegos.sivo.data.entity.DepartamentoEntity

@Dao
interface DepartamentoDao {

    @Insert
    suspend fun insert(departamento: DepartamentoEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(departamentos: Collection<DepartamentoEntity>)

    @Query("SELECT * FROM departamentos ORDER BY nombreDepartamento ASC")
    suspend fun getAllDepartamentos(): List<DepartamentoEntity>

}