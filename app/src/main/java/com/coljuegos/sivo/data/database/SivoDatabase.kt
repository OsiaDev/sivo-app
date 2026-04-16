package com.coljuegos.sivo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.coljuegos.sivo.data.dao.*
import com.coljuegos.sivo.data.entity.*
import com.coljuegos.sivo.utils.*

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
        VerificacionJuegoResponsableEntity::class,
        TipoApuestaEntity::class,
        NovedadRegistradaEntity::class,
        FirmaActaEntity::class,
        ResumenInventarioEntity::class
    ], version = 12, exportSchema = false
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

    abstract fun verificacionJuegoResponsableDao(): VerificacionJuegoResponsableDao

    abstract fun tipoApuestaDao(): TipoApuestaDao

    abstract fun novedadRegistradaDao(): NovedadRegistradaDao

    abstract fun firmaActaDao(): FirmaActaDao

    abstract fun resumenInventarioDao(): ResumenInventarioDao

}