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

enum class Screen(val route: String) {
    BudgetList("budgets"),

    CreateBudget("create_budget"),

    ViewBudget("budgets/{budgetId}"),

    ViewBudgetCategory("budgetCategories/{categoryId}"),

    AddExpense("budgets/{budgetId}/budgetCategories/{categoryId}/add_expense"),

    EditExpense("edit_expense/{expenseId}"),

    ;

    fun create(arguments: Bundle? = null): String  {
        val args = arguments ?: bundleOf()
        return when(this) {
            ViewBudget -> "budgets/${args.getString("budgetId")}"
            else -> route
        }
    }
}

typealias NavigateToCallback = (Screen, Bundle?)-> Unit

fun handleNavigateTo(navController: NavController,
                     screen: Screen,
                     args: Bundle? = null)
{
    val route = screen.create(args)
    navController.navigate(route)
}

typealias ExitScreenCallback = (Bundle?,Screen?)-> Unit

fun handleExitScreen(navController: NavController,
                     popUpTo: Screen? = null,
                     results: Bundle? = null,
                     )
{
    // TODO: implement handleExitScreen
    navController.popBackStack()
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
            modifier = Modifier.fillMaxSize().padding(innerPadding),
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
                    navigateTo = { screen, args ->
                        handleNavigateTo(navController,screen,args)
                    }
                )
            }

            // create_budget
            composable(
                route = Screen.CreateBudget.route
            ) {
                CreateBudgetRoute(
                    snackBarCallback = snackBarCallback,
                    exitScreen = { results, popUpTo ->
                        handleExitScreen(navController = navController, popUpTo = popUpTo, results = results)
                    }
                )
            }

            composable(
                route = Screen.ViewBudget.route,
                arguments = listOf(
                    navArgument("budgetId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val budgetId = backStackEntry.arguments?.getString("budgetId") ?: return@composable
                ViewBudgetRoute(
                    budgetId = budgetId,
                    snackBarCallback = snackBarCallback,
                    navigateToCallback = { screen, args ->
                        handleNavigateTo(navController, screen, args)
                    },
                    exitScreenCallback = { results, popUpTo ->
                        handleExitScreen(navController = navController, popUpTo = popUpTo, results = results)
                    }
                )
            }
        }
    }
}