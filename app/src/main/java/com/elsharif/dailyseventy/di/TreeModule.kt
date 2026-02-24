package com.elsharif.dailyseventy.di


import android.content.Context
import com.elsharif.dailyseventy.domain.data.tree.TreeDatabase
import com.elsharif.dailyseventy.domain.data.tree.TreeDao
import com.elsharif.dailyseventy.domain.repository.TreeRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TreeModule {

    @Provides
    @Singleton
    fun provideTreeDatabase(@ApplicationContext context: Context): TreeDatabase {
        return TreeDatabase.getInstance(context)
    }

    @Provides
    fun provideTreeDao(database: TreeDatabase): TreeDao {
        return database.treeDao()
    }

    @Provides
    @Singleton
    fun provideTreeRepository(treeDao: TreeDao): TreeRepository {
        return TreeRepository(treeDao)
    }
}