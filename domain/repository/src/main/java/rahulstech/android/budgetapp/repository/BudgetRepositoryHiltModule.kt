package rahulstech.android.budgetapp.repository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rahulstech.android.budgetapp.repository.impl.BudgetRepositoryMemoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BudgetRepositoryHiltModule {

    @Provides
    @Singleton
    fun budgetRepository(): BudgetRepository = BudgetRepositoryMemoryImpl()
}