package com.coljuegos.sivo.utils

import com.coljuegos.sivo.data.dao.SessionDao
import com.coljuegos.sivo.data.entity.SessionEntity
import com.coljuegos.sivo.data.remote.model.LoginResponseDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sessionDao: SessionDao
) {

    /**
     * Guarda una nueva sesión del usuario
     */
    suspend fun saveUserSession(loginResponse: LoginResponseDTO) {
        // Primero desactivar todas las sesiones anteriores
        this.sessionDao.deactivateAllSessions()

        // Crear nueva sesión
        val expirationTime = LocalDateTime.now().plusDays(30)

        val newSession = SessionEntity(
            tokenSession = loginResponse.token,
            tokenTypeSession = loginResponse.tokenType,
            usernameSession = loginResponse.username,
            idUserSession = loginResponse.user.idUser,
            nameUserSession = loginResponse.user.nameUser,
            emailUserSession = loginResponse.user.emailUser,
            perCodigoSession = loginResponse.user.perCodigoUser,
            fullNameUserSession = loginResponse.user.fullNameUser,
            expirationSession = expirationTime
        )

        this.sessionDao.insertSession(newSession)
    }

    /**
     * Verifica si hay una sesión válida activa
     */
    suspend fun isLoggedIn(): Boolean {
        return this.sessionDao.hasValidSession(LocalDateTime.now())
    }

    /**
     * Obtiene la sesión actual si es válida, sino la desactiva automáticamente
     */
    suspend fun getCurrentSession(): SessionEntity? {
        val session = this.sessionDao.getCurrentSession()
        return if (session != null) {
            if (session.expirationSession.isAfter(LocalDateTime.now())) {
                // Sesión válida
                session
            } else {
                // Sesión expirada - desactivar automáticamente
                this.sessionDao.deactivateSession(session.uuidSession.toString())
                null
            }
        } else {
            null
        }
    }

    /**
     * Flow para observar cambios en la sesión actual con verificación de expiración
     */
    fun getCurrentSessionFlow(): Flow<SessionEntity?> {
        return sessionDao.getCurrentSessionFlow().map { session ->
            if (session != null) {
                if (session.expirationSession.isAfter(LocalDateTime.now())) {
                    // Sesión válida
                    session
                } else {
                    // Sesión expirada - desactivar automáticamente
                    this.sessionDao.deactivateSession(session.uuidSession.toString())
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Obtiene el token de autenticación
     */
    suspend fun getToken(): String? {
        return this.getCurrentSession()?.tokenSession
    }

    /**
     * Obtiene el header de autorización completo
     */
    suspend fun getAuthorizationHeader(): String? {
        val session = this.getCurrentSession()
        return if (session != null) {
            "${session.tokenTypeSession} ${session.tokenSession}"
        } else null
    }

    /**
     * Obtiene el nombre del usuario actual
     */
    suspend fun getUserName(): String? {
        return this.getCurrentSession()?.nameUserSession
    }

    /**
     * Cierra la sesión actual desactivando todas las sesiones
     */
    suspend fun logout() {
        sessionDao.deactivateAllSessions()
    }


}