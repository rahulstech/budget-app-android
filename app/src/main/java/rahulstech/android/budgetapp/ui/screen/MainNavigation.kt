package rahulstech.android.budgetapp.ui.screen

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
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.ui.screen.createbudet.CreateBudgetRoute
import rahulstech.android.budgetapp.ui.screen.viewbudget.ViewBudgetRoute

enum class Screen(val route: String) {
    BudgetList("budgets"),

    CreateBudget("create_budget"),

    ViewBudget("budgets/{budgetId}"),

    ViewBudgetCategory("budgetCategories/{categoryId}"),

    ;

    fun create(args: ScreenArgs): String  {
        return when(this) {
            ViewBudget -> "budgets/${args.budgetId}"
            ViewBudgetCategory -> "budgetCategories/${args.categoryId}"
            else -> route
        }
    }
}

data class ScreenArgs(
    val budgetId: String = "",
    val categoryId: String = ""
)

data class NavigationEvent(
    val screen: Screen,
    val args: ScreenArgs = ScreenArgs(),
)

typealias NavigationCallback = (NavigationEvent)-> Unit

fun handleNavigateTo(navController: NavController,
                     event: NavigationEvent)
{
    val route = event.screen.create(event.args)
    navController.navigate(route)
}

typealias ExitCallback = ()-> Unit

fun handleExitScreen(navController: NavController)
{
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
                    navigateTo = { handleNavigateTo(navController,it) }
                )
            }

            // create_budget
            composable(
                route = Screen.CreateBudget.route
            ) {
                CreateBudgetRoute(
                    snackBarCallback = snackBarCallback,
                    exitScreen = { handleExitScreen(navController) },
                    navigateTo = { handleNavigateTo(navController, it) }
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
                    navigateToCallback = { handleNavigateTo(navController, it) },
                    exitScreenCallback = { handleExitScreen(navController) }
                )
            }
        }
    }
}