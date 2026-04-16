package com.coljuegos.sivo.utils

import android.content.Context
import android.content.SharedPreferences
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

    companion object {
        // ⬆️ Incrementar este número cada vez que se actualice lugares.json
        private const val LUGARES_JSON_VERSION = 2
        private const val PREFS_NAME = "sivo_data_prefs"
        private const val KEY_LUGARES_VERSION = "lugares_json_version"
    }

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun loadLocationData() {
        val savedVersion = prefs.getInt(KEY_LUGARES_VERSION, 0)

        if (savedVersion >= LUGARES_JSON_VERSION) {
            println("DEBUG: lugares.json v$LUGARES_JSON_VERSION ya cargado, omitiendo")
            return
        }

        println("DEBUG: Cargando lugares.json v$LUGARES_JSON_VERSION (versión anterior: $savedVersion)")

        val json = readJsonFromAssets("lugares.json")
        val lugares = JSONArray(json)

        val departamentosFromJson = mutableSetOf<String>()
        val municipiosFromJson = mutableSetOf<Pair<String, String>>()

        (0 until lugares.length()).forEach { i ->
            val item = lugares.getJSONObject(i)
            departamentosFromJson.add(item.getString("departamento"))
            municipiosFromJson.add(item.getString("municipio") to item.getString("departamento"))
        }

        // 1. Upsert departamentos preservando UUID de existentes
        val existingDepartamentos = departamentoDao.getAllDepartamentos()
            .associateBy { it.nombreDepartamento }

        val departamentosToUpsert = departamentosFromJson.map { nombre ->
            existingDepartamentos[nombre] ?: DepartamentoEntity(nombreDepartamento = nombre)
        }
        departamentoDao.upsertAll(departamentosToUpsert)
        println("DEBUG: Upsert ${departamentosToUpsert.size} departamentos")

        // 2. Upsert municipios preservando UUID de existentes
        val departamentosMap = departamentoDao.getAllDepartamentos()
            .associateBy { it.nombreDepartamento }

        val existingMunicipios = municipioDao.getAllMunicipios()
            .associateBy { it.nombreMunicipio to it.uuidDepartamento }

        val municipiosToUpsert = municipiosFromJson.mapNotNull { (municipio, departamento) ->
            val depEntity = departamentosMap[departamento] ?: return@mapNotNull null
            val key = municipio to depEntity.uuidDepartamento
            existingMunicipios[key] ?: MunicipioEntity(
                nombreMunicipio = municipio,
                uuidDepartamento = depEntity.uuidDepartamento
            )
        }
        municipioDao.upsertAll(municipiosToUpsert)
        println("DEBUG: Upsert ${municipiosToUpsert.size} municipios")

        // 3. Guardar versión procesada
        prefs.edit().putInt(KEY_LUGARES_VERSION, LUGARES_JSON_VERSION).apply()
        println("DEBUG: Versión $LUGARES_JSON_VERSION guardada correctamente")
    }

    fun readJsonFromAssets(filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }

}