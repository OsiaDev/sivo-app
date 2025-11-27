package com.coljuegos.sivo.utils

import android.content.Context
import com.coljuegos.sivo.data.dao.DepartamentoDao
import com.coljuegos.sivo.data.dao.MunicipioDao
import com.coljuegos.sivo.data.entity.DepartamentoEntity
import com.coljuegos.sivo.data.entity.MunicipioEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataLoaderService @Inject constructor(
    private val departamentoDao: DepartamentoDao,
    private val municipioDao: MunicipioDao,
    @ApplicationContext private val context: Context
) {

    suspend fun loadLocationData() {
        val json = readJsonFromAssets("lugares.json")
        val lugares = JSONArray(json)

        // Obtener datos actuales
        val existingDepartamentos = departamentoDao.getAllDepartamentos()
        val existingDepartamentosMap = existingDepartamentos.associateBy { it.nombreDepartamento }

        // Procesar datos del JSON
        val departamentosFromJson = mutableSetOf<String>()
        val municipiosFromJson = mutableSetOf<Pair<String, String>>()

        (0 until lugares.length()).forEach { i ->
            val item = lugares.getJSONObject(i)
            val nombreDep = item.getString("departamento")
            val nombreMun = item.getString("municipio")

            departamentosFromJson.add(nombreDep)
            municipiosFromJson.add(nombreMun to nombreDep)
        }

        // 1. Insertar departamentos nuevos (OnConflictStrategy.IGNORE ya maneja duplicados)
        val newDepartamentos = departamentosFromJson
            .filter { !existingDepartamentosMap.containsKey(it) }
            .map { DepartamentoEntity(nombreDepartamento = it) }

        if (newDepartamentos.isNotEmpty()) {
            departamentoDao.insertAll(newDepartamentos)
            println("DEBUG: Insertados ${newDepartamentos.size} departamentos nuevos")
        }

        // 2. Obtener mapa actualizado de departamentos
        val allDepartamentos = departamentoDao.getAllDepartamentos()
        val departamentosMap = allDepartamentos.associateBy { it.nombreDepartamento }

        // 3. Insertar municipios - la UNIQUE constraint evitará duplicados automáticamente
        val municipiosToInsert = municipiosFromJson.mapNotNull { (municipio, departamento) ->
            val depEntity = departamentosMap[departamento]
            depEntity?.let {
                MunicipioEntity(
                    nombreMunicipio = municipio,
                    uuidDepartamento = it.uuidDepartamento
                )
            }
        }

        if (municipiosToInsert.isNotEmpty()) {
            try {
                // OnConflictStrategy.IGNORE en el DAO evitará errores por duplicados
                municipioDao.insertAll(municipiosToInsert)
                println("DEBUG: Procesados ${municipiosToInsert.size} municipios (duplicados ignorados automáticamente)")
            } catch (e: Exception) {
                println("DEBUG: Error insertando municipios: ${e.message}")
            }
        }

        val finalCount = municipioDao.getAllMunicipios().size
        println("DEBUG: Total municipios en base de datos: $finalCount")
    }

    fun readJsonFromAssets(filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }

}