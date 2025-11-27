package com.coljuegos.sivo.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.coljuegos.sivo.data.entity.ImagenEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface ImagenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImagen(imagen: ImagenEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(imagenes: List<ImagenEntity>)

    @Query("SELECT * FROM imagenes WHERE uuidActa = :uuidActa ORDER BY fechaCaptura DESC")
    suspend fun getImagenesByActa(uuidActa: UUID): List<ImagenEntity>

    @Query("SELECT * FROM imagenes WHERE uuidActa = :uuidActa ORDER BY fechaCaptura DESC")
    fun getImagenesByActaFlow(uuidActa: UUID): Flow<List<ImagenEntity>>

    @Query("SELECT * FROM imagenes WHERE uuidActa = :uuidActa AND fragmentOrigen = :fragmentOrigen ORDER BY fechaCaptura DESC")
    suspend fun getImagenesByActaAndFragment(uuidActa: UUID, fragmentOrigen: String): List<ImagenEntity>

    @Query("SELECT * FROM imagenes WHERE uuidActa = :uuidActa AND fragmentOrigen = :fragmentOrigen ORDER BY fechaCaptura DESC")
    fun getImagenesByActaAndFragmentFlow(uuidActa: UUID, fragmentOrigen: String): Flow<List<ImagenEntity>>

    @Query("SELECT * FROM imagenes WHERE uuidImagen = :uuidImagen")
    suspend fun getImagenById(uuidImagen: UUID): ImagenEntity?

    @Delete
    suspend fun deleteImagen(imagen: ImagenEntity)

    @Query("DELETE FROM imagenes WHERE uuidActa = :uuidActa")
    suspend fun deleteImagenesByActa(uuidActa: UUID)

    @Query("SELECT COUNT(*) FROM imagenes WHERE uuidActa = :uuidActa")
    suspend fun getImagenesCountByActa(uuidActa: UUID): Int

}