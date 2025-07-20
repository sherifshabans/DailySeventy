package com.elsharif.dailyseventy.di.binds

import com.elsharif.dailyseventy.domain.data.datasource.AzkarDatasourceImp
import com.elsharif.dailyseventy.domain.data.datasource.NamesOfAllahDatasourceImp
import com.elsharif.dailyseventy.domain.data.datasource.PrayerTimesDataSourceImp
import com.elsharif.dailyseventy.domain.data.datasource.QuranDatasourceDatabaseImp
import com.elsharif.dailyseventy.domain.data.datasource.TasbeehDatasourceLocalImp
import com.example.core.data.datasource.AzkarDatasource
import com.example.core.data.datasource.NamesOfAllahDatasource
import com.example.core.data.datasource.PrayerTimesDatasource
import com.example.core.data.datasource.QuranDatasource
import com.example.core.data.datasource.TasbeehDatasource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatasourceModule {

    @Binds
    @Singleton
    abstract fun bindQuranDatasource(
        impl: QuranDatasourceDatabaseImp
    ): QuranDatasource

    @Binds
    @Singleton
    abstract fun bindPrayerTimesDatasource(
        impl: PrayerTimesDataSourceImp
    ): PrayerTimesDatasource

    @Binds
    @Singleton
    abstract fun bindTasbeehDatasource(
        impl: TasbeehDatasourceLocalImp
    ): TasbeehDatasource

    @Binds
    @Singleton
    abstract fun bindAzkarDatasource(
        impl: AzkarDatasourceImp
    ): AzkarDatasource

    @Binds
    @Singleton
    abstract fun bindNamesOfAllahDatasource(
        impl: NamesOfAllahDatasourceImp
    ): NamesOfAllahDatasource

}
