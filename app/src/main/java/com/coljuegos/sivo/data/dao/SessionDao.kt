package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.coljuegos.sivo.data.entity.SessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions WHERE isAuthSession = 1 ORDER BY expirationSession DESC LIMIT 1")
    suspend fun getCurrentSession(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE isAuthSession = 1 ORDER BY expirationSession DESC LIMIT 1")
    fun getCurrentSessionFlow(): Flow<SessionEntity?>

    @Query("UPDATE sessions SET isAuthSession = 0")
    suspend fun deactivateAllSessions()

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("UPDATE sessions SET isAuthSession = 0 WHERE uuidSession = :sessionId")
    suspend fun deactivateSession(sessionId: String)

    @Query("SELECT COUNT(*) > 0 FROM sessions WHERE isAuthSession = 1 AND expirationSession > :currentTime")
    suspend fun hasValidSession(currentTime: LocalDateTime): Boolean

}