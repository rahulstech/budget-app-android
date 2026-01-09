package rahulstech.android.budgetapp.ui.screen

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.ui.screen.budgetlist.BudgetListRoute
import rahulstech.android.budgetapp.ui.screen.createbudet.CreateBudgetRoute
import rahulstech.android.budgetapp.ui.screen.viewbudget.ViewBudgetRoute
import rahulstech.android.budgetapp.ui.screen.viewexpenses.ViewExpensesRoute

private const val ARG_BUDGET_ID = "budgetId"

private const val ARG_CATEGORY_ID = "categoryId"

enum class Screen(val route: String) {
    BudgetList("budgets"),

    CreateBudget("create_budget"),

    ViewBudget("budgets/{${ARG_BUDGET_ID}}"),

    ViewBudgetCategory("budgetCategories/{${ARG_CATEGORY_ID}}"),

    ViewExpenses("budgets/{${ARG_BUDGET_ID}}/expenses?categoryId={${ARG_CATEGORY_ID}}")

    ;

    fun create(args: ScreenArgs): String  {
        return when(this) {
            ViewBudget -> "budgets/${args.budgetId ?: ""}"
            ViewBudgetCategory -> "budgetCategories/${args.categoryId ?: ""}"
            ViewExpenses -> {
                val queries = buildString {
                    args.categoryId?.let { append("${ARG_CATEGORY_ID}=${it}") }
                }
                buildString {
                    append("budgets/${args.budgetId}/expenses")
                    if (queries.isNotBlank()) {
                        append("?")
                        append(queries)
                    }
                }
            }
            else -> route
        }
    }
}

data class ScreenArgs(
    val budgetId: Long? = null,
    val categoryId: Long? = null
)

sealed class NavigationEvent {

    data class ForwardTo(
        val screen: Screen,
        val args: ScreenArgs = ScreenArgs(),
        val popCurrent: Boolean = false
    ): NavigationEvent()

    data class Exit(val result: Bundle = bundleOf()): NavigationEvent()
}

typealias NavigationCallback = (NavigationEvent)-> Unit

fun handleNavigateTo(navController: NavController,
                     event: NavigationEvent)
{
    when(event) {
        is NavigationEvent.ForwardTo -> {
            val route = event.screen.create(event.args)
            if (event.popCurrent) {
                navController.popBackStack()
            }
            navController.navigate(route)
        }
        is NavigationEvent.Exit -> {
            navController.popBackStack()
        }
    }
}

data class SnackBarAction(
    val label: String,
    val onClick: ()-> Boolean = { true }
)

data class SnackBarEvent(
    override val message: String,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val action: SnackBarAction? = null,
): SnackbarVisuals {

    override val actionLabel: String? = null

    override val withDismissAction: Boolean
        get() = duration != SnackbarDuration.Short && action == null
}

typealias SnackBarCallback = (SnackBarEvent)-> Unit

@Composable
fun RouteContent(content: @Composable (SnackBarCallback)-> Unit) {
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                val snackBarEvent = (data.visuals as SnackBarEvent)
                Snackbar(
                    action = {
                        val sbAction = snackBarEvent.action
                        if (sbAction != null) {
                            TextButton(
                                onClick = {
                                    if (sbAction.onClick()) {
                                        data.dismiss()
                                    }
                                }
                            ) {
                                Text(text = sbAction.label)
                            }
                        }
                    }
                ) {
                    Text(
                        text = snackBarEvent.message,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier.widthIn(max = 800.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                content(
                    { snackBarEvent ->
                        coroutineScope.launch {
                            snackBarHostState.showSnackbar(snackBarEvent)
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    RouteContent { snackBarCallback ->
        NavHost(
            navController = navController,
            startDestination = Screen.BudgetList.route
        ) {
            // budget_list
            composable(
                route = Screen.BudgetList.route
            ) {
                BudgetListRoute(
                    navigateTo = { handleNavigateTo(navController, it) }
                )
            }

            // create_budget
            composable(
                route = Screen.CreateBudget.route
            ) {
                CreateBudgetRoute(
                    snackBarCallback = snackBarCallback,
                    navigateTo = { handleNavigateTo(navController, it) }
                )
            }

            composable(
                route = Screen.ViewBudget.route,
                arguments = listOf(
                    navArgument(ARG_BUDGET_ID) {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val budgetId = backStackEntry.arguments?.getLong(ARG_BUDGET_ID) ?: return@composable
                ViewBudgetRoute(
                    budgetId = budgetId,
                    snackBarCallback = snackBarCallback,
                    navigateTo = { handleNavigateTo(navController, it) },
                )
            }

            // view expenses
            composable(
                route = Screen.ViewExpenses.route,
                arguments = listOf(
                    navArgument(ARG_BUDGET_ID) {
                        type = NavType.LongType
                    },
                    navArgument(ARG_CATEGORY_ID) {
                        type = NavType.LongType
                        defaultValue = 0
                    }
                )
            ) { backStackEntry ->
                val budgetId = backStackEntry.arguments?.getLong(ARG_BUDGET_ID)!!
                val categoryId = backStackEntry.arguments?.getLong(ARG_CATEGORY_ID)
                ViewExpensesRoute(
                    budgetId = budgetId,
                    categoryId = categoryId,
                    snackBarCallback = snackBarCallback,
                    navigateTo = { handleNavigateTo(navController, it) }
                )
            }
        }
    }
}