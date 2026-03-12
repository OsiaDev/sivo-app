package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coljuegos.sivo.data.entity.ResumenInventarioEntity
import java.util.UUID

@Dao
interface ResumenInventarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resumen: ResumenInventarioEntity)

    @Update
    suspend fun update(resumen: ResumenInventarioEntity)

    @Query("SELECT * FROM resumen_inventario WHERE uuidActa = :actaUuid LIMIT 1")
    suspend fun getResumenByActaId(actaUuid: UUID): ResumenInventarioEntity?

}