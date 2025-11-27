package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.coljuegos.sivo.data.entity.MunicipioDisplayItem
import com.coljuegos.sivo.data.entity.MunicipioEntity
import java.util.UUID

@Dao
interface MunicipioDao {

    @Insert
    suspend fun insert(municipio: MunicipioEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(municipios: Collection<MunicipioEntity>)

    @Query("""
            SELECT
                m.uuidMunicipio as municipioId,
                m.nombreMunicipio as municipioNombre, 
                d.nombreDepartamento as departamentoNombre 
            FROM municipios m 
            INNER JOIN departamentos d ON m.uuidDepartamento = d.uuidDepartamento 
            ORDER BY m.nombreMunicipio ASC""")
    suspend fun getAllMunicipiosWithDepartamento(): List<MunicipioDisplayItem>

    @Query("select m.* from municipios m order by m.nombreMunicipio asc")
    suspend fun getAllMunicipios(): List<MunicipioEntity>

    @Query("SELECT * FROM municipios WHERE uuidMunicipio = :uuidMunicipio LIMIT 1")
    suspend fun getMunicipioByUuid(uuidMunicipio: UUID): MunicipioEntity?

}