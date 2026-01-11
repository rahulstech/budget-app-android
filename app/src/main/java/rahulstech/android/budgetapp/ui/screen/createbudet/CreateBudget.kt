package rahulstech.android.budgetapp.ui.screen.createbudet

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.components.BudgetCategoryDialogState
import rahulstech.android.budgetapp.ui.components.CategoryDialog
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.Screen
import rahulstech.android.budgetapp.ui.screen.ScreenArgs
import rahulstech.android.budgetapp.ui.screen.SnackBarAction
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarEvent
import rahulstech.android.budgetapp.ui.screen.UIState
import rahulstech.android.budgetapp.ui.theme.BudgetAppTheme
import rahulstech.android.budgetapp.ui.theme.primaryTopAppBarColors

private const val TAG = "CreateBudget"

@Composable
fun CreateBudgetRoute(snackBarCallback: SnackBarCallback,
                      navigateTo: NavigationCallback,
                      viewModel: CreateBudgetViewModel = hiltViewModel())
{
    val context = LocalContext.current
    val categoryDialogState by viewModel.categoryDialogState.collectAsStateWithLifecycle()
    val createBudgetUiState by viewModel.createBudgetUIState.collectAsStateWithLifecycle()

    val budgetSaveState by viewModel.budgetSaveState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(budgetSaveState) {
        when(budgetSaveState) {
            is UIState.Loading -> {
                // TODO: implement loading state
            }
            is UIState.Success<Budget> -> {
                val budget = (budgetSaveState as UIState.Success<Budget>).data
                snackBarCallback(
                    SnackBarEvent(
                        message = context.getString(R.string.message_budget_save_success),
                        duration = SnackbarDuration.Long,
                        action = SnackBarAction(label = context.getString(R.string.label_ok))
                    )
                )
                navigateTo(NavigationEvent.ForwardTo(
                    screen = Screen.ViewBudget,
                    args = ScreenArgs(budgetId = budget.id),
                    popCurrent = true)
                )
            }
            is UIState.Error -> {
                Log.e(TAG, "create budget error", (budgetSaveState as UIState.Error).cause)
                snackBarCallback(
                    SnackBarEvent(
                        message = context.getString(R.string.message_budget_save_error),
                        duration = SnackbarDuration.Long,
                        action = SnackBarAction(label = context.getString(R.string.label_ok))
                    )
                )
            }
            else -> {}
        }
    }

    CreateBudgetScreen(
        navigateTo = navigateTo,
        onUIEvent = { event ->
            when (event) {
                is CreateBudgetUIEvent.AddBudgetEvent -> { viewModel.createBudget(event.budget) }
                is CreateBudgetUIEvent.ShowCategoryDialogEvent -> {
                    viewModel.updateCategoryDialogState(BudgetCategoryDialogState(showDialog = true, category = event.category))
                }
                is CreateBudgetUIEvent.RemoveCategoryEvent -> {
                    viewModel.removeCategory(event.category)
                }
                is CreateBudgetUIEvent.SaveCategoryEvent -> {
                    viewModel.saveCategory(event.category)
                }
                is CreateBudgetUIEvent.BudgetUpdateEvent -> {
                    viewModel.updateBudget(event.budget)
                }
            }
        },
        uiState = createBudgetUiState,
    )

    if (categoryDialogState.showDialog) {
        CategoryDialog(
            categoryDialogState = categoryDialogState,
            onClickSave = { category ->
                viewModel.saveCategory(category)
                viewModel.updateCategoryDialogState(BudgetCategoryDialogState())
            },
            onDismiss = { viewModel.updateCategoryDialogState(BudgetCategoryDialogState()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBudgetScreen(uiState: CreateBudgetUIState,
                       navigateTo: NavigationCallback = {},
                       onUIEvent: CreateBudgetUIEventCallback = {},
                       )
{
    val budget = uiState.budget
    Scaffold(
        topBar = {
            TopAppBar(
                colors = primaryTopAppBarColors(),
                title = {
                    Text(
                        text = stringResource(R.string.title_new_budget),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                actions = {
                    IconButton(
                        enabled = !uiState.isSaving && uiState.canSave,
                        onClick = {
                            onUIEvent(
                                CreateBudgetUIEvent.AddBudgetEvent(
                                    budget.copy(categories = uiState.categories)
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.Check, stringResource(R.string.label_save))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigateTo(NavigationEvent.Exit()) }) {
                        Icon( Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.message_navigate_up))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid (
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                BudgetSection(
                    name = budget.name,
                    onNameChanged = { onUIEvent(CreateBudgetUIEvent.BudgetUpdateEvent(budget.copy(name = it)) )},
                    details = uiState.budget.details,
                    onDetailsChanged = { onUIEvent(CreateBudgetUIEvent.BudgetUpdateEvent(budget.copy(details = it)) ) },
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                CategorySectionHeader(onUIEvent = onUIEvent)
            }

            if (uiState.categories.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    NoCategoriesComponent()
                }
            }
            else {
                items(items = uiState.categories, key = { it.id }) { category ->
                    CategoryItem(
                        category = category,
                        onClickRemove = { onUIEvent(CreateBudgetUIEvent.RemoveCategoryEvent(it)) },
                        onClickEdit = { onUIEvent(CreateBudgetUIEvent.ShowCategoryDialogEvent(category = it, isEditMode = true)) }
                    )
                }
            }
        }
    }
}

@Composable
fun BudgetSection(name: String,
                  onNameChanged: (String)-> Unit,
                  details: String,
                  onDetailsChanged: (String)-> Unit)
{
    Column {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = onNameChanged,
            label = { Text(stringResource(R.string.label_budget_name)) },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            value = details,
            onValueChange = onDetailsChanged,
            label = { Text(stringResource(R.string.label_budget_details)) },
            maxLines = 10,
            trailingIcon = {
                if (details.isNotEmpty()) {
                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.message_clear_text)
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun CategorySectionHeader(onUIEvent: CreateBudgetUIEventCallback) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth()
                .weight(1f),
            text = stringResource(R.string.label_budget_categories),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.width(12.dp))

        // add category
        FilledTonalIconButton (onClick = { onUIEvent(CreateBudgetUIEvent.ShowCategoryDialogEvent()) }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
        }
    }
}


@Composable
fun NoCategoriesComponent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(R.string.label_no_category),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = "add at least one category",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
@Composable
fun CategoryItem(category: BudgetCategory,
                 onClickRemove: (BudgetCategory)-> Unit,
                 onClickEdit: (BudgetCategory)-> Unit)
{
    Card(
        modifier = Modifier.width(280.dp).height(240.dp),
        shape = RoundedCornerShape(size = 20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.note,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = category.allocation.toInt().toString(),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.label_allocation),
                style = MaterialTheme.typography.labelMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                // remove
                IconButton(onClick = { onClickRemove(category)}) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.message_edit_category, category.name),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // edit
                IconButton(onClick = { onClickEdit(category) } ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.message_edit_category, category.name),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}



@Preview(
    showBackground = true,
)
@Composable
fun CreateBudgetScreenPreview() {
    BudgetAppTheme {
        CreateBudgetScreen(
            uiState = CreateBudgetUIState()
        )
    }
}