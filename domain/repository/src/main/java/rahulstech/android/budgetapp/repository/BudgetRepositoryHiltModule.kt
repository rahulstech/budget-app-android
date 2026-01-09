package rahulstech.android.budgetapp.repository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import rahulstech.android.budgetapp.budgetdb.IBudgetDB
import rahulstech.android.budgetapp.repository.impl.BudgetRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BudgetRepositoryHiltModule {

    @Provides
    @Singleton
    fun budgetRepository(db: IBudgetDB): BudgetRepository = BudgetRepositoryImpl(db)
}