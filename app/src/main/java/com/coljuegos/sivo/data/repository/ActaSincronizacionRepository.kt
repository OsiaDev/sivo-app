package com.coljuegos.sivo.data.repository

import android.graphics.BitmapFactory
import android.util.Base64
import com.coljuegos.sivo.data.dao.*
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaStateEnum
import com.coljuegos.sivo.data.entity.ActaVisitaEntity
import com.coljuegos.sivo.data.entity.FirmaActaEntity
import com.coljuegos.sivo.data.entity.ImagenEntity
import com.coljuegos.sivo.data.entity.InventarioRegistradoEntity
import com.coljuegos.sivo.data.entity.NovedadRegistradaEntity
import com.coljuegos.sivo.data.entity.VerificacionContractualEntity
import com.coljuegos.sivo.data.entity.VerificacionSiplaftEntity
import com.coljuegos.sivo.data.remote.api.ApiService
import com.coljuegos.sivo.data.remote.model.*
import com.coljuegos.sivo.utils.ImageCompressionUtils
import com.coljuegos.sivo.utils.NetworkResult
import com.coljuegos.sivo.utils.SessionManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class ActaSincronizacionRepository @Inject constructor(
    private val actaDao: ActaDao,
    private val actaVisitaDao: ActaVisitaDao,
    private val verificacionContractualDao: VerificacionContractualDao,
    private val verificacionSiplaftDao: VerificacionSiplaftDao,
    private val inventarioRegistradoDao: InventarioRegistradoDao,
    private val inventarioDao: InventarioDao,
    private val novedadRegistradaDao: NovedadRegistradaDao,
    private val firmaActaDao: FirmaActaDao,
    private val imagenDao: ImagenDao,
    private val municipioDao: MunicipioDao,
    private val actaApiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun marcarActaComoCompleta(actaUuid: UUID, latitud: Double, longitud: Double): NetworkResult<ActaEntity> {
        return try {
            val acta = actaDao.getActaByUuid(actaUuid)
                ?: return NetworkResult.Error("Acta no encontrada")

            val actaActualizada = acta.copy(
                stateActa = ActaStateEnum.COMPLETE,
                latitudActa = latitud,
                longitudActa = longitud,
                lastUpdatedActa = LocalDateTime.now()
            )

            actaDao.update(actaActualizada)
            NetworkResult.Success(actaActualizada)
        } catch (e: Exception) {
            NetworkResult.Error("Error al marcar acta como completa: ${e.message}")
        }
    }

    suspend fun sincronizarActaConBackend(actaUuid: UUID): Flow<NetworkResult<String>> = flow {
        try {
            emit(NetworkResult.Loading())

            val acta = actaDao.getActaByUuid(actaUuid)
                ?: return@flow emit(NetworkResult.Error("Acta no encontrada"))

            if (acta.stateActa != ActaStateEnum.COMPLETE) {
                return@flow emit(NetworkResult.Error("El acta debe estar en estado COMPLETE"))
            }

            // Construir DTO completo
            val actaCompleteDTO = buildActaCompleteDTO(acta)

            // Obtener token de autorización
            val authHeader = sessionManager.getAuthorizationHeader()
                ?: return@flow emit(NetworkResult.Error("No hay sesión activa"))

            // Enviar al backend
            val response = actaApiService.uploadActa(authHeader, actaCompleteDTO)

            if (response.isSuccessful && response.body()?.success == true) {
                // Marcar como sincronizado
                val actaSincronizada = acta.copy(
                    stateActa = ActaStateEnum.SINCRONIZADO,
                    lastUpdatedActa = LocalDateTime.now()
                )
                actaDao.update(actaSincronizada)

                emit(NetworkResult.Success("Acta sincronizada correctamente"))
            } else {
                val errorMsg = response.body()?.message ?: "Error desconocido del servidor"
                emit(NetworkResult.Error(errorMsg))
            }

        } catch (e: CancellationException) {
            throw e  // Re-lanzar excepciones de cancelación
        } catch (e: Exception) {
            emit(NetworkResult.Error("Error de conexión: ${e.message}"))
        }
    }

    private suspend fun buildActaCompleteDTO(acta: ActaEntity): ActaCompleteDTO {
        // Obtener todas las entidades relacionadas
        val actaVisita = actaVisitaDao.getActaVisitaByActaId(acta.uuidActa)
        val verificacionContractual = verificacionContractualDao.getVerificacionContractualByActaId(acta.uuidActa)
        val verificacionSiplaft = verificacionSiplaftDao.getVerificacionSiplaftByActaId(acta.uuidActa)
        val inventariosRegistrados = inventarioRegistradoDao.getInventariosRegistradosByActaList(acta.uuidActa)
        val novedadesRegistradas = novedadRegistradaDao.getNovedadesRegistradasByActaList(acta.uuidActa)
        val firmaActa = firmaActaDao.getFirmaActaByActaUuidSuspend(acta.uuidActa)
        val imagenes = imagenDao.getImagenesByActa(acta.uuidActa)

        return ActaCompleteDTO(
            numActa = acta.numActa,
            latitud = acta.latitudActa,
            longitud = acta.longitudActa,
            actaVisita = actaVisita?.let { mapActaVisitaToDTO(it) },
            verificacionContractual = verificacionContractual?.let { mapVerificacionContractualToDTO(it) },
            verificacionSiplaft = verificacionSiplaft?.let { mapVerificacionSiplaftToDTO(it) },
            inventariosRegistrados = inventariosRegistrados.mapNotNull  { mapInventarioRegistradoToDTO(it) },
            novedadesRegistradas = novedadesRegistradas.map { mapNovedadRegistradaToDTO(it) },
            firmaActa = firmaActa?.let { mapFirmaActaToDTO(it) },
            imagenes = imagenes.mapNotNull { mapImagenToDTO(it) }
        )
    }

    private suspend fun mapActaVisitaToDTO(entity: ActaVisitaEntity): ActaVisitaDTO {
        // Obtener nombre del municipio si existe
        val municipioNombre = entity.uuidMunicipio?.let { uuid ->
            municipioDao.getMunicipioByUuid(uuid)?.nombreMunicipio
        }

        return ActaVisitaDTO(
            nombrePresente = entity.nombrePresente,
            identificacionPresente = entity.identificacionPresente,
            municipio = municipioNombre,
            cargoPresente = entity.cargoPresente,
            emailPresente = entity.emailPresente,
            correosContacto = entity.correosContacto
        )
    }

    private fun mapVerificacionContractualToDTO(entity: VerificacionContractualEntity): VerificacionContractualDTO {
        return VerificacionContractualDTO(
            avisoAutorizacion = entity.avisoAutorizacion,
            direccionCorresponde = entity.direccionCorresponde,
            otraDireccion = entity.otraDireccion,
            nombreEstablecimientoCorresponde = entity.nombreEstablecimientoCorresponde,
            otroNombre = entity.otroNombre,
            desarrollaActividadesDiferentes = entity.desarrollaActividadesDiferentes,
            tipoActividad = entity.tipoActividad,
            especificacionOtros = entity.especificacionOtros,
            cuentaRegistrosMantenimiento = entity.cuentaRegistrosMantenimiento
        )
    }

    private fun mapVerificacionSiplaftToDTO(entity: VerificacionSiplaftEntity): VerificacionSiplaftDTO {
        return VerificacionSiplaftDTO(
            cuentaFormatoIdentificacion = entity.cuentaFormatoIdentificacion,
            montoIdentificacion = entity.montoIdentificacion,
            cuentaFormatoReporteInterno = entity.cuentaFormatoReporteInterno,
            senalesAlerta = entity.senalesAlerta,
            conoceCodigoConducta = entity.conoceCodigoConducta
        )
    }

    private suspend fun mapInventarioRegistradoToDTO(entity: InventarioRegistradoEntity): InventarioRegistradoDTO? {

        val inventario = inventarioDao.getInventarioByUuid(entity.uuidInventario) ?: return null

        return InventarioRegistradoDTO(
            serial = inventario.metSerialInventario,
            marca = inventario.nombreMarcaInventario,
            codigoApuesta = inventario.codigoTipoApuestaInventario,
            estado = entity.estado.name,
            codigoApuestaDiferente = entity.codigoApuestaDiferente,
            codigoApuestaDiferenteValor = entity.codigoApuestaDiferenteValor,
            serialVerificado = entity.serialVerificado,
            serialDiferente = entity.serialDiferente,
            descripcionJuego = entity.descripcionJuego,
            planPremios = entity.planPremios,
            valorPremios = entity.valorPremios,
            valorCredito = entity.valorCredito,
            contadoresVerificado = entity.contadoresVerificado,
            coinInMet = entity.coinInMet,
            coinOutMet = entity.coinOutMet,
            jackpotMet = entity.jackpotMet,
            coinInSclm = entity.coinInSclm,
            coinOutSclm = entity.coinOutSclm,
            jackpotSclm = entity.jackpotSclm,
            observaciones = entity.observaciones
        )
    }

    private fun mapNovedadRegistradaToDTO(entity: NovedadRegistradaEntity): NovedadRegistradaDTO {
        return NovedadRegistradaDTO(
            serial = entity.serial,
            marca = entity.marca,
            codigoApuesta = entity.codigoApuesta,
            tienePlaca = entity.tienePlaca,
            operando = entity.operando,
            valorCredito = entity.valorCredito,
            coinInMet = entity.coinInMet,
            coinOutMet = entity.coinOutMet,
            jackpotMet = entity.jackpotMet,
            coinInSclm = entity.coinInSclm,
            coinOutSclm = entity.coinOutSclm,
            jackpotSclm = entity.jackpotSclm,
            observaciones = entity.observaciones
        )
    }

    private fun mapFirmaActaToDTO(entity: FirmaActaEntity): FirmaActaDTO {
        return FirmaActaDTO(
            nombreFiscalizadorPrincipal = entity.nombreFiscalizadorPrincipal,
            ccFiscalizadorPrincipal = entity.ccFiscalizadorPrincipal,
            cargoFiscalizadorPrincipal = entity.cargoFiscalizadorPrincipal,
            firmaFiscalizadorPrincipal = entity.firmaFiscalizadorPrincipal?.let { compressFirmaBase64(it) },
            nombreFiscalizadorSecundario = entity.nombreFiscalizadorSecundario,
            ccFiscalizadorSecundario = entity.ccFiscalizadorSecundario,
            cargoFiscalizadorSecundario = entity.cargoFiscalizadorSecundario,
            firmaFiscalizadorSecundario = entity.firmaFiscalizadorSecundario?.let { compressFirmaBase64(it) },
            nombreOperador = entity.nombreOperador,
            ccOperador = entity.ccOperador,
            cargoOperador = entity.cargoOperador,
            firmaOperador = entity.firmaOperador?.let { compressFirmaBase64(it) }
        )
    }

    /**
     * Comprime una firma almacenada como Base64 simple a Base64 con ZLIB
     * Las firmas en BD están en Base64 PNG sin comprimir
     * Esta función las convierte al formato esperado por el backend
     */
    private fun compressFirmaBase64(base64Simple: String): String {
        return try {
            // Decodificar Base64 a bytes
            val imageBytes = Base64.decode(base64Simple, Base64.DEFAULT)

            // Convertir bytes a Bitmap
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                ?: return base64Simple // Si falla, devolver original

            // Comprimir con ZLIB usando el nuevo método
            ImageCompressionUtils.compressBitmapToBase64Zlib(bitmap)

        } catch (e: Exception) {
            // Si hay error, devolver el Base64 original
            base64Simple
        }
    }

    private fun mapImagenToDTO(entity: ImagenEntity): ImagenDTO? {
        return try {
            val file = File(entity.rutaImagen)
            if (!file.exists()) return null

            // Comprimir con ZLIB + PNG sin pérdida para el backend
            val imagenBase64 = ImageCompressionUtils.compressImageFileToBase64Zlib(file)
                ?: return null

            ImagenDTO(
                nombreImagen = entity.nombreImagen,
                imagenBase64 = imagenBase64,
                descripcion = entity.descripcion,
                fragmentOrigen = entity.fragmentOrigen
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getActaByUuid(actaUuid: UUID): ActaEntity? {
        return try {
            actaDao.getActaByUuid(actaUuid)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getActasPendientesSincronizacion(): List<ActaEntity> {
        return try {
            actaDao.getActasByState(ActaStateEnum.COMPLETE)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getActasCompletasCount(): Int {
        return try {
            actaDao.getActasCountByState(ActaStateEnum.COMPLETE)
        } catch (_: Exception) {
            0
        }
    }

}