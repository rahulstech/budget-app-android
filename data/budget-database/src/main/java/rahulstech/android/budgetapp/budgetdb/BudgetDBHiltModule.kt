package rahulstech.android.budgetapp.budgetdb

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BudgetDBHiltModule {

    @Provides
    @Singleton
    fun budgetDB(@ApplicationContext context: Context): IBudgetDB = BudgetDB.getNewInstance(context)
}