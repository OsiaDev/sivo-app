package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coljuegos.sivo.data.entity.ActaVisitaEntity
import java.util.UUID

@Dao
interface ActaVisitaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(actaVisita: ActaVisitaEntity)

    @Update
    suspend fun update(actaVisita: ActaVisitaEntity)

    @Query("SELECT * FROM actasvisitas WHERE uuidActa = :actaUuid LIMIT 1")
    suspend fun getActaVisitaByActaId(actaUuid: UUID): ActaVisitaEntity?

}