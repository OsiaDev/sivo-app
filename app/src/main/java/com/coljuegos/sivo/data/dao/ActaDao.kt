package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaStateEnum
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.UUID

@Dao
interface ActaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(actas: List<ActaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(acta: ActaEntity)

    @Update
    suspend fun update(acta: ActaEntity)

    @Query("SELECT * FROM actas WHERE uuidSession = :sessionId AND stateActa = 'ACTIVE' ORDER BY fechaVisitaAucActa DESC")
    suspend fun getActiveActasBySession(sessionId: UUID): List<ActaEntity>

    @Query("SELECT * FROM actas WHERE uuidSession = :sessionId AND stateActa = 'ACTIVE' ORDER BY fechaVisitaAucActa DESC")
    fun getActiveActasBySessionFlow(sessionId: UUID): Flow<List<ActaEntity>>

    @Query("SELECT * FROM actas WHERE uuidSession = :sessionId ORDER BY fechaVisitaAucActa DESC")
    suspend fun getActasBySession(sessionId: UUID): List<ActaEntity>

    @Query("SELECT * FROM actas WHERE numActa = :numActa LIMIT 1")
    suspend fun getActaByNumActa(numActa: Int): ActaEntity?

    @Query("SELECT numActa FROM actas WHERE uuidSession = :sessionId")
    suspend fun getNumActasBySession(sessionId: UUID): List<Int>

    @Query("UPDATE actas SET stateActa = :newState, lastUpdatedActa = :updatedTime WHERE numActa IN (:numActas)")
    suspend fun updateActasState(numActas: List<Int>, newState: ActaStateEnum, updatedTime: LocalDateTime)

    @Query("DELETE FROM actas WHERE uuidSession = :sessionId")
    suspend fun deleteActasBySession(sessionId: UUID)

    @Query("DELETE FROM actas WHERE uuidSession = :sessionId")
    suspend fun deleteAllActasBySession(sessionId: UUID)

    @Query("DELETE FROM actas WHERE stateActa = 'INACTIVE' AND lastUpdatedActa < :cutoffDate")
    suspend fun deleteOldInactiveActas(cutoffDate: LocalDateTime)

    @Query("SELECT * FROM actas WHERE uuidActa = :actaUuid LIMIT 1")
    suspend fun getActaByUuid(actaUuid: UUID): ActaEntity?

    @Query("SELECT * FROM actas WHERE stateActa = :state ORDER BY lastUpdatedActa DESC")
    suspend fun getActasByState(state: ActaStateEnum): List<ActaEntity>

    @Query("SELECT COUNT(*) FROM actas WHERE stateActa = :state")
    suspend fun getActasCountByState(state: ActaStateEnum): Int

    @Query("SELECT * FROM actas WHERE stateActa = :state ORDER BY lastUpdatedActa DESC")
    fun getActasByStateFlow(state: ActaStateEnum): Flow<List<ActaEntity>>

}