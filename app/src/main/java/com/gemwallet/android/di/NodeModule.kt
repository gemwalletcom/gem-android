package com.gemwallet.android.di

import android.content.Context
import com.gemwallet.android.cases.nodes.AddNodeCase
import com.gemwallet.android.cases.nodes.GetBlockExplorersCase
import com.gemwallet.android.cases.nodes.GetCurrentBlockExplorerCase
import com.gemwallet.android.cases.nodes.GetCurrentNodeCase
import com.gemwallet.android.cases.nodes.GetNodesCase
import com.gemwallet.android.cases.nodes.SetBlockExplorerCase
import com.gemwallet.android.cases.nodes.SetCurrentNodeCase
import com.gemwallet.android.data.database.NodesDao
import com.gemwallet.android.data.repositories.config.ConfigStore
import com.gemwallet.android.data.repositories.nodes.NodesRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NodeModule {

    @Provides
    @Singleton
    fun provideNodesRepository(
        gson: Gson,
        @ApplicationContext context: Context,
        nodesDao: NodesDao,
    ): NodesRepository {
        return NodesRepository(
            gson = gson,
            nodesDao = nodesDao,
            configStore = ConfigStore(context.getSharedPreferences("node-config", Context.MODE_PRIVATE)),
        )
    }

    @Provides
    fun provideSetCurrentNodeCase(repository: NodesRepository): SetCurrentNodeCase = repository

    @Provides
    fun provideGetCurrentNodeCase(repository: NodesRepository): GetCurrentNodeCase = repository

    @Provides
    fun provideSetBlockExplorerCase(repository: NodesRepository): SetBlockExplorerCase = repository

    @Provides
    fun provideGetBlockExplorersCase(repository: NodesRepository): GetBlockExplorersCase = repository

    @Provides
    fun provideGetCurrentBlockExplorerCase(repository: NodesRepository): GetCurrentBlockExplorerCase = repository

    @Provides
    fun provideGetNodesCase(repository: NodesRepository): GetNodesCase = repository

    @Provides
    fun provideAddNodeCase(repository: NodesRepository): AddNodeCase = repository
}