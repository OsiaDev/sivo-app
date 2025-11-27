package com.coljuegos.sivo.data.repository

import com.coljuegos.sivo.data.dao.TipoApuestaDao
import com.coljuegos.sivo.data.entity.TipoApuestaEntity
import com.coljuegos.sivo.data.remote.api.ApiService
import com.coljuegos.sivo.data.remote.model.TipoApuestaDTO
import com.coljuegos.sivo.utils.NetworkResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaestrosRepository @Inject constructor(
    private val apiService: ApiService,
    private val tipoApuestaDao: TipoApuestaDao
) {

    /**
     * Sincroniza los tipos de apuesta desde el servidor
     * Se ejecuta al iniciar sesión
     */
    suspend fun sincronizarMaestros(): Flow<NetworkResult<Boolean>> = flow {
        try {
            emit(NetworkResult.Loading())

            val response = apiService.getMaestros()

            if (response.isSuccessful) {
                response.body()?.let { maestros ->
                    // Guardar tipos de apuesta
                    val tiposApuesta = maestros.tiposApuesta.map { mapToTipoApuestaEntity(it) }
                    tipoApuestaDao.insertAll(tiposApuesta)

                    emit(NetworkResult.Success(true))
                } ?: emit(NetworkResult.Error("Respuesta vacía del servidor"))
            } else {
                emit(NetworkResult.Error("Error al obtener maestros: ${response.code()}"))
            }

        } catch (e: Exception) {
            emit(NetworkResult.Error("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Obtiene los tipos de apuesta locales
     */
    suspend fun getTiposApuestaLocales(): List<TipoApuestaEntity> {
        return tipoApuestaDao.getAllTiposApuesta()
    }

    /**
     * Obtiene un tipo de apuesta por código
     */
    suspend fun getTipoApuestaByCodigo(codigo: Long): TipoApuestaEntity? {
        return tipoApuestaDao.getTipoApuestaByCodigo(codigo)
    }

    /**
     * Mapea el DTO a la entidad
     * Los nombres coinciden exactamente, solo se genera el UUID
     */
    private fun mapToTipoApuestaEntity(dto: TipoApuestaDTO): TipoApuestaEntity {
        return TipoApuestaEntity(
            codigoTipoApuesta = dto.codigoTipoApuesta,
            nombreTipoApuesta = dto.nombreTipoApuesta,
            descripcionTipoApuesta = dto.descripcionTipoApuesta
        )
    }

    /**
     * Verifica si necesita sincronizar maestros
     * @return true si necesita sincronizar (primera vez o desactualizado)
     */
    suspend fun necesitaSincronizacion(): Boolean {
        val tiposApuestaLocales = tipoApuestaDao.getAllTiposApuesta()
        return tiposApuestaLocales.isEmpty()
    }

    /**
     * Obtiene el número de tipos de apuesta almacenados
     */
    suspend fun getCantidadTiposApuesta(): Int {
        return tipoApuestaDao.getCount()
    }

    /**
     * Fuerza la actualización de maestros desde el servidor
     * Elimina los datos locales y descarga nuevamente
     */
    suspend fun forzarActualizacion(): Flow<NetworkResult<Boolean>> = flow {
        try {
            emit(NetworkResult.Loading())

            // Eliminar datos locales
            tipoApuestaDao.deleteAll()

            // Descargar nuevos datos
            val response = apiService.getMaestros()

            if (response.isSuccessful) {
                response.body()?.let { maestros ->
                    val tiposApuesta = maestros.tiposApuesta.map { mapToTipoApuestaEntity(it) }
                    tipoApuestaDao.insertAll(tiposApuesta)
                    emit(NetworkResult.Success(true))
                } ?: emit(NetworkResult.Error("Respuesta vacía del servidor"))
            } else {
                emit(NetworkResult.Error("Error al obtener maestros: ${response.code()}"))
            }

        } catch (e: Exception) {
            emit(NetworkResult.Error("Error de conexión: ${e.message}"))
        }
    }

}