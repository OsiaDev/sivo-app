package com.coljuegos.sivo.data.remote.api

import com.coljuegos.sivo.data.remote.model.ActaCompleteDTO
import com.coljuegos.sivo.data.remote.model.ActaResponseDTO
import com.coljuegos.sivo.data.remote.model.ActaSincronizacionResponse
import com.coljuegos.sivo.data.remote.model.LoginRequestDTO
import com.coljuegos.sivo.data.remote.model.LoginResponseDTO
import com.coljuegos.sivo.data.remote.model.MaestrosResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequestDTO): Response<LoginResponseDTO>

    @GET("acta/obtenerActas")
    suspend fun getActasByUserId(
        @Header("Authorization") authorization: String
    ): Response<ActaResponseDTO>

    @GET("maestros/obtenerMaestros")
    suspend fun getMaestros(): Response<MaestrosResponseDTO>

    @POST("acta/upload")
    suspend fun uploadActa(
        @Header("Authorization") authorization: String,
        @Body actaCompleteDTO: ActaCompleteDTO
    ): Response<ActaSincronizacionResponse>

}