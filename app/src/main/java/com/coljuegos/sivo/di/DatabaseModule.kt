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
import com.coljuegos.sivo.data.dao.SessionDao
import com.coljuegos.sivo.data.dao.TipoApuestaDao
import com.coljuegos.sivo.data.dao.VerificacionContractualDao
import com.coljuegos.sivo.data.dao.VerificacionSiplaftDao
import com.coljuegos.sivo.data.database.SivoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): SivoDatabase {
        return Room.databaseBuilder(
            context, SivoDatabase::class.java, "sivo_database"
        ).fallbackToDestructiveMigration(false)
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
    fun provideInventarioRegistradoDao(database: SivoDatabase): InventarioRegistradoDao = database.inventarioRegistradoDao()

    @Provides
    fun provideTipoApuestaDao(database: SivoDatabase): TipoApuestaDao = database.tipoApuestaDao()

    @Provides
    fun provideNovedadRegistradaDao(database: SivoDatabase): NovedadRegistradaDao = database.novedadRegistradaDao()

    @Provides
    fun provideFirmaActaDao(database: SivoDatabase): FirmaActaDao = database.firmaActaDao()

}