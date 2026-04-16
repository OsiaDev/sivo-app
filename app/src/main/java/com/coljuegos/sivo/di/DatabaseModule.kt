package com.coljuegos.sivo.di

import android.content.Context
import androidx.room.Room
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
import com.coljuegos.sivo.data.dao.ResumenInventarioDao
import com.coljuegos.sivo.data.dao.SessionDao
import com.coljuegos.sivo.data.dao.TipoApuestaDao
import com.coljuegos.sivo.data.dao.VerificacionContractualDao
import com.coljuegos.sivo.data.dao.VerificacionJuegoResponsableDao
import com.coljuegos.sivo.data.dao.VerificacionSiplaftDao
import com.coljuegos.sivo.data.database.SivoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `verificacion_juego_responsable` (
                    `uuidVerificacionJuegoResponsable` TEXT NOT NULL,
                    `uuidActa` TEXT NOT NULL,
                    `cuentaTestIdentificacionRiesgos` TEXT,
                    `existenPiezasPublicitarias` TEXT,
                    `cuentaProgramaJuegoResponsable` TEXT,
                    PRIMARY KEY(`uuidVerificacionJuegoResponsable`),
                    FOREIGN KEY(`uuidActa`) REFERENCES `actas`(`uuidActa`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_verificacion_juego_responsable_uuidVerificacionJuegoResponsable` ON `verificacion_juego_responsable` (`uuidVerificacionJuegoResponsable`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_verificacion_juego_responsable_uuidActa` ON `verificacion_juego_responsable` (`uuidActa`)")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `resumen_inventario` (
                    `uuidResumen` TEXT NOT NULL,
                    `uuidActa` TEXT NOT NULL,
                    `notasResumen` TEXT NOT NULL,
                    PRIMARY KEY(`uuidResumen`),
                    FOREIGN KEY(`uuidActa`) REFERENCES `actas`(`uuidActa`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
            """.trimIndent())
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_resumen_inventario_uuidActa` ON `resumen_inventario` (`uuidActa`)")
        }
    }

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE resumen_inventario ADD COLUMN observacionesOperador TEXT")
        }
    }

    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE imagenes ADD COLUMN isSincronizada INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE imagenes ADD COLUMN ultimoError TEXT")
        }
    }

    private val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE novedades_registradas ADD COLUMN descripcionJuego INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE novedades_registradas ADD COLUMN planPremios INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE novedades_registradas ADD COLUMN valorPremios INTEGER NOT NULL DEFAULT 1")
        }
    }

    private val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE novedades_registradas ADD COLUMN numeroInternoMet TEXT")
        }
    }

    private val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE novedades_registradas ADD COLUMN contadoresVerificado INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_12_13 = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE actasvisitas ADD COLUMN tipoDocumentoPresente TEXT DEFAULT 'CC'")
        }
    }

    private val MIGRATION_13_14 = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE imagenes ADD COLUMN verificacionesConfirmadas INTEGER NOT NULL DEFAULT 0")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): SivoDatabase {
        return Room.databaseBuilder(
            context, SivoDatabase::class.java, "sivo_database"
        ).addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
            MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14)
         .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideDepartamentoDao(database: SivoDatabase): DepartamentoDao = database.departamentoDao()

    @Provides
    fun provideMunicipioDao(database: SivoDatabase): MunicipioDao = database.municipioDao()

    @Provides
    fun provideSessionDao(database: SivoDatabase): SessionDao = database.sessionDao()

    @Provides
    fun provideActaDao(database: SivoDatabase): ActaDao = database.actaDao()

    @Provides
    fun provideFuncionarioDao(database: SivoDatabase): FuncionarioDao = database.funcionarioDao()

    @Provides
    fun provideInventarioDao(database: SivoDatabase): InventarioDao = database.inventarioDao()

    @Provides
    fun provideImagenDao(database: SivoDatabase): ImagenDao = database.imagenDao()

    @Provides
    fun provideActaVisitaDao(database: SivoDatabase): ActaVisitaDao = database.actaVisitaDao()

    @Provides
    fun provideVerificacionContractualDao(database: SivoDatabase): VerificacionContractualDao = database.verificacionContractualDao()

    @Provides
    fun provideVerificacionSiplaftDao(database: SivoDatabase): VerificacionSiplaftDao = database.verificacionSiplaftDao()

    @Provides
    fun provideVerificacionJuegoResponsableDao(database: SivoDatabase): VerificacionJuegoResponsableDao = database.verificacionJuegoResponsableDao()

    @Provides
    fun provideInventarioRegistradoDao(database: SivoDatabase): InventarioRegistradoDao = database.inventarioRegistradoDao()

    @Provides
    fun provideTipoApuestaDao(database: SivoDatabase): TipoApuestaDao = database.tipoApuestaDao()

    @Provides
    fun provideNovedadRegistradaDao(database: SivoDatabase): NovedadRegistradaDao = database.novedadRegistradaDao()

    @Provides
    fun provideFirmaActaDao(database: SivoDatabase): FirmaActaDao = database.firmaActaDao()

    @Provides
    fun provideResumenInventarioDao(database: SivoDatabase): ResumenInventarioDao = database.resumenInventarioDao()

}