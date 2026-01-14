package rahulstech.android.budgetapp.ui.screen

import android.os.Bundle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.ui.components.OptionItem
import rahulstech.android.budgetapp.ui.components.OptionsDialog
import rahulstech.android.budgetapp.ui.screen.budgetlist.BudgetListRoute
import rahulstech.android.budgetapp.ui.screen.createbudet.CreateBudgetRoute
import rahulstech.android.budgetapp.ui.screen.viewbudget.ViewBudgetRoute
import rahulstech.android.budgetapp.ui.screen.viewexpenses.ViewExpensesRoute
import rahulstech.android.budgetapp.ui.theme.primaryTopAppBarColors

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



sealed interface IconValue {
    data class VectorIcon(val value: ImageVector): IconValue

    data class PainterIcon(val value: Painter): IconValue
}

sealed interface TopBarAction {

    val enabled: Boolean
    val onClick: ()-> Unit

    data class IconAction(
        val icon: IconValue,
        override val onClick: () -> Unit,
        override val enabled: Boolean = true,
        val contentDescription: String? = null,
    ): TopBarAction

    data class TextAction(
        val text: String,
        override val onClick: ()-> Unit,
        override val enabled: Boolean = true,
    ): TopBarAction
}

sealed interface FAB {
    val position: FabPosition

    data class IconAndText(
        val icon: IconValue,
        val label: String,
        val onClick: () -> Unit,
        override val position: FabPosition = FabPosition.Center,
    ): FAB
}

data class ScaffoldState(
    val title: String = "",
    val actions: List<TopBarAction> = emptyList(),
    val showNavUp: Boolean = false,
    val actionNavUp: ()-> NavigationEvent = { NavigationEvent.Exit() },
    val floatingActionButton: FAB? = null,
)

typealias ScaffoldStateCallback = (ScaffoldState)-> Unit

@Composable
fun TopAppBarTitle(title: String)
{
    Text(title,
        style = MaterialTheme.typography.titleLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TopAppBarIconAction(action: TopBarAction.IconAction)
{
    IconButton(enabled = action.enabled, onClick = action.onClick) {
        when(action.icon) {
            is IconValue.VectorIcon -> {
                Icon(action.icon.value,action.contentDescription)
            }
            is IconValue.PainterIcon -> {
                Icon(action.icon.value,action.contentDescription)
            }
        }
    }
}

@Composable
fun TopBarTextActionsDialog(onDismiss: ()-> Unit, actions: List<TopBarAction.TextAction>)
{
    OptionsDialog(
        onDismiss = onDismiss,
        options = actions.map { action ->
            OptionItem(action.text,
                onClick = { action.onClick() },
                enabled = action.enabled
            )
        }.toTypedArray()
    )
}

@Composable
fun FloatActionButton(fab: FAB) {
    when(fab) {
        is FAB.IconAndText -> {
            ExtendedFloatingActionButton(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.primary,
                onClick = fab.onClick
            ) {
                when(fab.icon) {
                    is IconValue.VectorIcon -> {
                        Icon(fab.icon.value, null)
                    }
                    is IconValue.PainterIcon -> {
                        Icon(fab.icon.value, null)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(fab.label)
            }
        }
    }
}

@Composable
fun AppScaffold(navUpCallback: (NavigationEvent)-> Unit,
                content: @Composable (ScaffoldStateCallback, SnackBarCallback)-> Unit,
                )
{
    var state by remember { mutableStateOf(ScaffoldState()) }
    val iconActions: List<TopBarAction.IconAction> = remember(state) {
        state.actions.
        filter { it is TopBarAction.IconAction }
            .map { it as TopBarAction.IconAction }
    }
    val textActions: List<TopBarAction.TextAction> = remember(state) {
        state.actions
            .filter { it is TopBarAction.TextAction }
            .map { it as TopBarAction.TextAction }
    }
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var showMoreActions by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        snackbarHost = {
            SnackbarHost(snackBarHostState) { data ->
                val snackBarEvent = (data.visuals as SnackBarEvent)
                Snackbar(
                    shape = RoundedCornerShape(0.dp),
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
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        topBar = {
            TopAppBar(
                colors = primaryTopAppBarColors(),
                title = { TopAppBarTitle(state.title) },
                actions = {
                    for (action in iconActions) {
                        TopAppBarIconAction(action)
                    }

                    if (textActions.isNotEmpty()) {
                        TopAppBarIconAction(TopBarAction.IconAction(
                            icon = IconValue.VectorIcon(Icons.Default.MoreVert),
                            onClick = { showMoreActions = true },
                            contentDescription = stringResource(R.string.message_show_more_options)
                        ))
                    }
                },
                navigationIcon = {
                    if (state.showNavUp) {
                        IconButton(onClick = { navUpCallback(state.actionNavUp()) }) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.message_navigate_up))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            state.floatingActionButton?.let { FloatActionButton(it) }
        },
        floatingActionButtonPosition = state.floatingActionButton?.position ?: FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier.widthIn(max = 800.dp),
                contentAlignment = Alignment.Center,
            ) {
                content(
                    { state = it },
                    { snackBarEvent -> coroutineScope.launch { snackBarHostState.showSnackbar(snackBarEvent) }}
                )
            }
        }
    }

    if (showMoreActions) {
        TopBarTextActionsDialog(onDismiss = { showMoreActions = false }, textActions)
    }
}

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    AppScaffold(navUpCallback = { handleNavigateTo(navController, it) }) { scaffoldStateCallback, snackBarCallback ->
        NavHost(
            navController = navController,
            startDestination = Screen.BudgetList.route
        ) {
            // budget_list
            composable(
                route = Screen.BudgetList.route
            ) {
                BudgetListRoute(scaffoldStateCallback, { handleNavigateTo(navController, it) })
            }

            // create_budget
            composable(
                route = Screen.CreateBudget.route
            ) {
                CreateBudgetRoute(scaffoldStateCallback, snackBarCallback,
                    { handleNavigateTo(navController, it) }
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
                ViewBudgetRoute(budgetId,scaffoldStateCallback,snackBarCallback,
                    { handleNavigateTo(navController, it) },
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
                val categoryId = backStackEntry.arguments?.getLong(ARG_CATEGORY_ID).takeUnless { it == 0L }
                ViewExpensesRoute(budgetId,categoryId,scaffoldStateCallback,snackBarCallback,
                    { handleNavigateTo(navController, it) }
                )
            }
        }
    }
}