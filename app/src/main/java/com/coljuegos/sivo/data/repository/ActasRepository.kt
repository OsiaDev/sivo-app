package com.coljuegos.sivo.data.repository

import com.coljuegos.sivo.data.dao.ActaDao
import com.coljuegos.sivo.data.dao.FuncionarioDao
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaStateEnum
import com.coljuegos.sivo.data.entity.FuncionarioEntity
import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.data.remote.api.ApiService
import com.coljuegos.sivo.data.remote.model.ActaResponseDTO
import com.coljuegos.sivo.utils.NetworkResult
import com.coljuegos.sivo.utils.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActasRepository @Inject constructor(
    private val apiService: ApiService,
    private val actaDao: ActaDao,
    private val funcionarioDao: FuncionarioDao,
    private val inventarioDao: InventarioDao,
    private val sessionManager: SessionManager
) {

    fun getActasByCurrentUser(): Flow<NetworkResult<List<ActaEntity>>> = flow {
        try {
            emit(NetworkResult.Loading())

            // Obtener sesión actual
            val currentSession = sessionManager.getCurrentSession()
            if (currentSession == null) {
                emit(NetworkResult.Error("No hay sesión activa"))
                return@flow
            }

            // Primero obtener datos locales activos
            val localActas = actaDao.getActiveActasBySession(currentSession.uuidSession)
            val shouldFetchFromServer = localActas.isEmpty() ||
                    localActas.any { it.lastUpdatedActa.isBefore(LocalDateTime.now().minusHours(24)) }

            if (localActas.isNotEmpty() && !shouldFetchFromServer) {
                emit(NetworkResult.Success(localActas))
                return@flow
            }

            // Obtener datos del servidor
            val authHeader = sessionManager.getAuthorizationHeader()
            if (authHeader != null) {
                val response = apiService.getActasByUserId(authHeader)

                if (response.isSuccessful) {
                    response.body()?.let { actaResponse ->
                        // Procesar y actualizar datos
                        updateActasWithStateManagement(actaResponse, currentSession.uuidSession)

                        // Emitir los datos actualizados
                        val updatedActas = actaDao.getActiveActasBySession(currentSession.uuidSession)
                        emit(NetworkResult.Success(updatedActas))
                    } ?: run {
                        emit(NetworkResult.Error("Respuesta vacía del servidor"))
                    }
                } else {
                    // Si hay datos locales, los mantenemos aunque falle la actualización
                    if (localActas.isNotEmpty()) {
                        emit(NetworkResult.Success(localActas))
                    } else {
                        emit(NetworkResult.Error("Error al obtener datos: ${response.code()}"))
                    }
                }
            } else {
                emit(NetworkResult.Error("Error de autenticación"))
            }

        } catch (e: Exception) {
            // En caso de error, intentar devolver datos locales
            val currentSession = sessionManager.getCurrentSession()
            if (currentSession != null) {
                val localActas = actaDao.getActiveActasBySession(currentSession.uuidSession)
                if (localActas.isNotEmpty()) {
                    emit(NetworkResult.Success(localActas))
                } else {
                    emit(NetworkResult.Error("Error de conexión: ${e.message}"))
                }
            } else {
                emit(NetworkResult.Error("Error de conexión: ${e.message}"))
            }
        }
    }

    /**
     * Método para refrescar actas desde el backend (pull-to-refresh)
     * Siempre consulta al backend y actualiza la base de datos local
     */
    fun refreshActasFromBackend(): Flow<NetworkResult<List<ActaEntity>>> = flow {
        try {
            emit(NetworkResult.Loading())

            println("DEBUG: Iniciando refresh desde backend")

            // Obtener sesión actual
            val currentSession = sessionManager.getCurrentSession()
            if (currentSession == null) {
                println("DEBUG: No hay sesión activa")
                emit(NetworkResult.Error("No hay sesión activa"))
                return@flow
            }

            // Obtener token de autorización
            val authHeader = sessionManager.getAuthorizationHeader()
            if (authHeader == null) {
                println("DEBUG: No hay token de autorización")
                emit(NetworkResult.Error("No hay token de autorización"))
                return@flow
            }

            println("DEBUG: Haciendo llamada al backend...")

            // Obtener datos del servidor
            val response = apiService.getActasByUserId(authHeader)

            println("DEBUG: Respuesta recibida - código: ${response.code()}, exitosa: ${response.isSuccessful}")

            if (response.isSuccessful) {
                response.body()?.let { actaResponse ->
                    println("DEBUG: Procesando ${actaResponse.actas.size} actas")
                    // Procesar y actualizar datos
                    updateActasWithStateManagement(actaResponse, currentSession.uuidSession)

                    // Emitir los datos actualizados
                    val updatedActas = actaDao.getActiveActasBySession(currentSession.uuidSession)
                    println("DEBUG: Emitiendo ${updatedActas.size} actas actualizadas")
                    emit(NetworkResult.Success(updatedActas))
                } ?: run {
                    println("DEBUG: Response body es null")
                    emit(NetworkResult.Error("Respuesta vacía del servidor"))
                }
            } else {
                println("DEBUG: Error en respuesta: ${response.code()} - ${response.message()}")
                emit(NetworkResult.Error("Error ${response.code()}: ${response.message()}"))
            }

        } catch (e: Exception) {
            println("DEBUG: Excepción capturada: ${e.message}")
            e.printStackTrace()
            emit(NetworkResult.Error("Error de conexión: ${e.message}"))
        }
    }

    /**
     * Actualiza actas con gestión de estado, preservando relaciones de foreign key
     * IMPORTANTE: Actualiza en lugar de reemplazar para mantener integridad referencial
     */
    private suspend fun updateActasWithStateManagement(
        actaResponse: ActaResponseDTO,
        sessionId: UUID
    ) {
        val currentTime = LocalDateTime.now()

        println("DEBUG: Actualizando actas con gestión de estado")

        // Obtener números de acta que vienen del servidor
        val serverNumActas = actaResponse.actas.mapNotNull { it.numActa }.toSet()
        println("DEBUG: Actas del servidor: $serverNumActas")

        // Obtener números de acta que ya existen en la base de datos para esta sesión
        val existingNumActas = actaDao.getNumActasBySession(sessionId).toSet()
        println("DEBUG: Actas existentes localmente: $existingNumActas")

        // Marcar como inactivas las actas que no vienen en el servidor (pero NO las eliminar)
        val actasToDeactivate = existingNumActas - serverNumActas
        if (actasToDeactivate.isNotEmpty()) {
            println("DEBUG: Marcando como inactivas: $actasToDeactivate")
            actaDao.updateActasState(
                actasToDeactivate.toList(),
                ActaStateEnum.INACTIVE,
                currentTime
            )

            // IMPORTANTE: NO eliminar funcionarios e inventarios de actas inactivas
            // Solo actualizar su estado para mantener integridad referencial
        }

        // Procesar actas del servidor
        val funcionariosToInsert = mutableListOf<FuncionarioEntity>()
        val inventariosToInsert = mutableListOf<InventarioEntity>()

        actaResponse.actas.forEach { actaDTO ->
            try {
                val numActa = actaDTO.numActa ?: 0
                println("DEBUG: Procesando acta #$numActa")

                // Verificar si el acta ya existe
                val existingActa = actaDao.getActaByNumActa(numActa)

                if (existingActa != null) {
                    println("DEBUG: Acta #$numActa ya existe, verificando si debe actualizarse...")

                    // SOLO actualizar actas en estado ACTIVE
                    if (existingActa.stateActa == ActaStateEnum.ACTIVE) {
                        println("DEBUG: Acta #$numActa está en estado ACTIVE, actualizando información del backend...")

                        val actaActualizada = existingActa.copy(
                            numAucActa = actaDTO.numAuc ?: existingActa.numAucActa,
                            fechaVisitaAucActa = try {
                                LocalDate.parse(actaDTO.fechaVisitaAuc)
                            } catch (_: Exception) {
                                existingActa.fechaVisitaAucActa
                            },
                            numContratoActa = actaDTO.numContrato ?: existingActa.numContratoActa,
                            nitActa = actaDTO.nit ?: existingActa.nitActa,
                            estCodigoActa = actaDTO.estCodigo?.toInt() ?: existingActa.estCodigoActa,
                            conCodigoActa = actaDTO.conCodigo?.toInt() ?: existingActa.conCodigoActa,
                            nombreOperadorActa = actaDTO.nombreOperador ?: existingActa.nombreOperadorActa,
                            fechaFinContratoActa = try {
                                LocalDate.parse(actaDTO.fechaFinContrato)
                            } catch (_: Exception) {
                                existingActa.fechaFinContratoActa
                            },
                            emailActa = actaDTO.email ?: existingActa.emailActa,
                            tipoVisitaActa = actaDTO.tipoVisita ?: existingActa.tipoVisitaActa,
                            fechaCorteInventarioActa = try {
                                LocalDateTime.parse(actaDTO.fechaCorteInventario)
                            } catch (_: Exception) {
                                existingActa.fechaCorteInventarioActa
                            },
                            direccionActa = actaDTO.direccion?.direccion ?: existingActa.direccionActa,
                            establecimientoActa = actaDTO.direccion?.establecimiento ?: existingActa.establecimientoActa,
                            estCodigoInternoActa = actaDTO.direccion?.estCodigo ?: existingActa.estCodigoInternoActa,
                            ciudadActa = actaDTO.direccion?.ciudad ?: existingActa.ciudadActa,
                            departamentoActa = actaDTO.direccion?.departamento ?: existingActa.departamentoActa,
                            latitudActa = actaDTO.direccion?.latitud ?: existingActa.latitudActa,
                            longitudActa = actaDTO.direccion?.longitud ?: existingActa.longitudActa,
                            stateActa = ActaStateEnum.ACTIVE,
                            lastUpdatedActa = currentTime
                        )

                        actaDao.update(actaActualizada)

                        funcionarioDao.deleteFuncionariosByActa(existingActa.uuidActa)
                        inventarioDao.deleteInventariosByActa(existingActa.uuidActa)

                        actaDTO.funcionarios?.forEach { funcionarioDTO ->
                            funcionariosToInsert.add(
                                mapFuncionarioToEntity(funcionarioDTO, existingActa.uuidActa)
                            )
                        }

                        actaDTO.inventarios?.forEach { inventarioDTO ->
                            inventariosToInsert.add(
                                mapInventarioToEntity(inventarioDTO, existingActa.uuidActa)
                            )
                        }
                    } else {
                        println("DEBUG: Acta #$numActa está en estado ${existingActa.stateActa}, NO se actualiza desde el backend")
                        // Preservar completamente el acta
                    }
                } else {
                    println("DEBUG: Acta #$numActa es nueva, insertando...")

                    // Crear nueva acta
                    val actaEntity = mapActaToEntity(actaDTO, sessionId, currentTime)
                    actaDao.insert(actaEntity)

                    // Mapear funcionarios
                    actaDTO.funcionarios?.forEach { funcionarioDTO ->
                        funcionariosToInsert.add(
                            mapFuncionarioToEntity(funcionarioDTO, actaEntity.uuidActa)
                        )
                    }

                    // Mapear inventarios
                    actaDTO.inventarios?.forEach { inventarioDTO ->
                        inventariosToInsert.add(
                            mapInventarioToEntity(inventarioDTO, actaEntity.uuidActa)
                        )
                    }
                }

            } catch (e: Exception) {
                println("ERROR: Error mapeando acta ${actaDTO.numActa}: ${e.message}")
                e.printStackTrace()
            }
        }

        // Insertar funcionarios e inventarios
        if (funcionariosToInsert.isNotEmpty()) {
            println("DEBUG: Insertando ${funcionariosToInsert.size} funcionarios")
            funcionarioDao.insertAll(funcionariosToInsert)
        }
        if (inventariosToInsert.isNotEmpty()) {
            println("DEBUG: Insertando ${inventariosToInsert.size} inventarios")
            inventarioDao.insertAll(inventariosToInsert)
        }

        println("DEBUG: Sincronización de actas completada")
    }

    private fun mapActaToEntity(
        actaDTO: com.coljuegos.sivo.data.remote.model.ActaDTO,
        sessionId: UUID,
        currentTime: LocalDateTime
    ): ActaEntity {
        return ActaEntity(
            uuidSession = sessionId,
            numAucActa = actaDTO.numAuc ?: 0,
            fechaVisitaAucActa = try { LocalDate.parse(actaDTO.fechaVisitaAuc) } catch (_: Exception) { LocalDate.now() },
            numActa = actaDTO.numActa ?: 0,
            numContratoActa = actaDTO.numContrato ?: "",
            nitActa = actaDTO.nit ?: "",
            estCodigoActa = actaDTO.estCodigo?.toInt() ?: 0,
            conCodigoActa = actaDTO.conCodigo?.toInt() ?: 0,
            nombreOperadorActa = actaDTO.nombreOperador ?: "",
            fechaFinContratoActa = try { LocalDate.parse(actaDTO.fechaFinContrato) } catch (_: Exception) { LocalDate.now() },
            emailActa = actaDTO.email ?: "",
            tipoVisitaActa = actaDTO.tipoVisita ?: "Establecimiento",
            fechaCorteInventarioActa = try { LocalDateTime.parse(actaDTO.fechaCorteInventario) } catch (_: Exception) { LocalDateTime.now() },
            direccionActa = actaDTO.direccion?.direccion ?: "",
            establecimientoActa = actaDTO.direccion?.establecimiento ?: "",
            estCodigoInternoActa = actaDTO.direccion?.estCodigo ?: "",
            ciudadActa = actaDTO.direccion?.ciudad ?: "",
            departamentoActa = actaDTO.direccion?.departamento ?: "",
            latitudActa = actaDTO.direccion?.latitud ?: 0.0,
            longitudActa = actaDTO.direccion?.longitud ?: 0.0,
            stateActa = ActaStateEnum.ACTIVE,
            lastUpdatedActa = currentTime
        )
    }

    private fun mapFuncionarioToEntity(
        funcionarioDTO: com.coljuegos.sivo.data.remote.model.FuncionarioDTO,
        actaUuid: UUID
    ): FuncionarioEntity {
        return FuncionarioEntity(
            uuidActa = actaUuid,
            idUsuarioFuncionario = funcionarioDTO.idUsuario ?: "",
            nombreFuncionario = funcionarioDTO.nombre ?: "",
            cargoFuncionario = funcionarioDTO.cargo ?: "",
            emailFuncionario = funcionarioDTO.email ?: "",
            identificacionFuncionario = funcionarioDTO.identificacion ?: ""
        )
    }

    private fun mapInventarioToEntity(
        inventarioDTO: com.coljuegos.sivo.data.remote.model.InventarioDTO,
        actaUuid: UUID
    ): InventarioEntity {
        return InventarioEntity(
            uuidActa = actaUuid,
            nombreMarcaInventario = inventarioDTO.nombreMarca ?: "",
            metSerialInventario = inventarioDTO.metSerial ?: "",
            insCodigoInventario = inventarioDTO.insCodigo ?: "",
            invSillasInventario = inventarioDTO.invSillas ?: 0,
            tipoApuestaNombreInventario = inventarioDTO.tipoApuestaNombre ?: "",
            metOnlineInventario = inventarioDTO.metOnline ?: false,
            codigoTipoApuestaInventario = inventarioDTO.codigoTipoApuesta ?: "",
            nucInventario = inventarioDTO.nuc ?: "",
            conCodigoInventario = inventarioDTO.conCodigo?.toInt() ?: 0,
            aucNumeroInventario = inventarioDTO.aucNumero ?: 0,
            estCodigoInventario = inventarioDTO.estCodigo?.toInt() ?: 0
        )
    }

    // Métodos públicos utilizados por ViewModels

    suspend fun getActaByUuid(actaUuid: UUID): ActaEntity? {
        return try {
            actaDao.getActaByUuid(actaUuid)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getFuncionariosByActa(actaUuid: UUID): List<FuncionarioEntity> {
        return try {
            funcionarioDao.getFuncionariosByActa(actaUuid)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getInventariosByActa(actaUuid: UUID): List<InventarioEntity> {
        return try {
            inventarioDao.getInventariosByActa(actaUuid)
        } catch (_: Exception) {
            emptyList()
        }
    }

}