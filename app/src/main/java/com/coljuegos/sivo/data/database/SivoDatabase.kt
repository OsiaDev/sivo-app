package com.coljuegos.sivo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.coljuegos.sivo.data.dao.ActaDao
import com.coljuegos.sivo.data.dao.ActaVisitaDao
import com.coljuegos.sivo.data.dao.DepartamentoDao
import com.coljuegos.sivo.data.dao.FirmaActaDao
import com.coljuegos.sivo.data.dao.FuncionarioDao
import com.coljuegos.sivo.data.dao.ImagenDao
import com.coljuegos.sivo.data.dao.InventarioDao
import com.coljuegos.sivo.data.dao.InventarioRegistradoDao
import com.coljuegos.sivo.data.dao.MunicipioDao
import com.coljuegos.sivo.data.dao.NovedadRegistradaDao
import com.coljuegos.sivo.data.dao.SessionDao
import com.coljuegos.sivo.data.dao.TipoApuestaDao
import com.coljuegos.sivo.data.dao.VerificacionContractualDao
import com.coljuegos.sivo.data.dao.VerificacionSiplaftDao
import com.coljuegos.sivo.data.entity.ActaEntity
import com.coljuegos.sivo.data.entity.ActaVisitaEntity
import com.coljuegos.sivo.data.entity.DepartamentoEntity
import com.coljuegos.sivo.data.entity.FirmaActaEntity
import com.coljuegos.sivo.data.entity.FuncionarioEntity
import com.coljuegos.sivo.data.entity.ImagenEntity
import com.coljuegos.sivo.data.entity.InventarioEntity
import com.coljuegos.sivo.data.entity.InventarioRegistradoEntity
import com.coljuegos.sivo.data.entity.MunicipioEntity
import com.coljuegos.sivo.data.entity.NovedadRegistradaEntity
import com.coljuegos.sivo.data.entity.SessionEntity
import com.coljuegos.sivo.data.entity.TipoApuestaEntity
import com.coljuegos.sivo.data.entity.VerificacionContractualEntity
import com.coljuegos.sivo.data.entity.VerificacionSiplaftEntity
import com.coljuegos.sivo.utils.ActaStateConverter
import com.coljuegos.sivo.utils.UUIDConverter
import com.coljuegos.sivo.utils.BigDecimalConverter
import com.coljuegos.sivo.utils.EstadoInventarioConverter
import com.coljuegos.sivo.utils.LocalDateConverter
import com.coljuegos.sivo.utils.LocalDateTimeConverter

@Database(
    entities = [
        DepartamentoEntity::class,
        MunicipioEntity::class,
        SessionEntity::class,
        ActaEntity::class,
        FuncionarioEntity::class,
        InventarioEntity::class,
        InventarioRegistradoEntity::class,
        ImagenEntity::class,
        ActaVisitaEntity::class,
        VerificacionContractualEntity::class,
        VerificacionSiplaftEntity::class,
        TipoApuestaEntity::class,
        NovedadRegistradaEntity::class,
        FirmaActaEntity::class
    ], version = 4, exportSchema = false
)
@TypeConverters(
    BigDecimalConverter::class,
    LocalDateConverter::class,
    LocalDateTimeConverter::class,
    UUIDConverter::class,
    ActaStateConverter::class,
    EstadoInventarioConverter::class,
)
abstract class SivoDatabase : RoomDatabase() {

    abstract fun departamentoDao(): DepartamentoDao

    abstract fun municipioDao(): MunicipioDao

    abstract fun sessionDao(): SessionDao

    abstract fun actaDao(): ActaDao

    abstract fun funcionarioDao(): FuncionarioDao

    abstract fun inventarioDao(): InventarioDao

    abstract fun inventarioRegistradoDao(): InventarioRegistradoDao

    abstract fun imagenDao(): ImagenDao

    abstract fun actaVisitaDao(): ActaVisitaDao

    abstract fun verificacionContractualDao(): VerificacionContractualDao

    abstract fun verificacionSiplaftDao(): VerificacionSiplaftDao

    abstract fun tipoApuestaDao(): TipoApuestaDao

    abstract fun novedadRegistradaDao(): NovedadRegistradaDao

    abstract fun firmaActaDao(): FirmaActaDao

}