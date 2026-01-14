package rahulstech.android.budgetapp.ui.screen.createbudet

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
import androidx.compose.material3.Text
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
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.components.CategoryDialog
import rahulstech.android.budgetapp.ui.screen.IconValue
import rahulstech.android.budgetapp.ui.screen.NavigationCallback
import rahulstech.android.budgetapp.ui.screen.NavigationEvent
import rahulstech.android.budgetapp.ui.screen.ScaffoldState
import rahulstech.android.budgetapp.ui.screen.ScaffoldStateCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarCallback
import rahulstech.android.budgetapp.ui.screen.SnackBarEvent
import rahulstech.android.budgetapp.ui.screen.TopBarAction
import rahulstech.android.budgetapp.ui.screen.UISideEffect
import rahulstech.android.budgetapp.ui.screen.UIText
import rahulstech.android.budgetapp.ui.theme.BudgetAppTheme

private const val TAG = "CreateBudget"

@Composable
fun CreateBudgetRoute(scaffoldStateCallback: ScaffoldStateCallback,
                      snackBarCallback: SnackBarCallback,
                      navigateCallback: NavigationCallback,
                      viewModel: CreateBudgetViewModel = hiltViewModel())
{
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when(effect) {
                is UISideEffect.ShowSnackBar -> {
                    val message = when(effect.message) {
                        is UIText.StringResource -> effect.message.resolveString(context)
                        is UIText.PlainString -> effect.message.value
                    }
                    snackBarCallback(SnackBarEvent(message = message))
                }
                is UISideEffect.NavigateTo -> { navigateCallback(effect.event) }
                is UISideEffect.ExitScreen -> {
                    navigateCallback(NavigationEvent.Exit())
                }
            }
        }
    }

    val uiState by viewModel.state.collectAsStateWithLifecycle()

    scaffoldStateCallback(ScaffoldState(
        showNavUp = true,
        title = stringResource(R.string.title_new_budget),
        actions = listOf(
            TopBarAction.IconAction(
                icon = IconValue.VectorIcon(Icons.Default.Check),
                enabled = uiState.canSave,
                onClick = { viewModel.createBudget(uiState.prepareBudget()) },
            )
        )
    ))

    CreateBudgetScreen(uiState,viewModel::onUIEvent)

    if (uiState.categoryDialog.showDialog) {
        CategoryDialog(
            categoryDialogState = uiState.categoryDialog,
            onClickSave = { category ->
                viewModel.hideCategoryDialog()
                viewModel.saveCategory(category)
            },
            onDismiss = { viewModel.hideCategoryDialog() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBudgetScreen(uiState: CreateBudgetUIState,
                       onUIEvent: CreateBudgetUIEventCallback = {},
                       )
{
    val budget = uiState.budget
    LazyVerticalGrid (
        columns = GridCells.Adaptive(minSize = 320.dp),
        modifier = Modifier.fillMaxSize().padding(16.dp),
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
            CategorySectionHeader { onUIEvent(CreateBudgetUIEvent.ShowCategoryDialogEvent()) }
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
                    onClickEdit = { onUIEvent(CreateBudgetUIEvent.ShowCategoryDialogEvent(category = it)) }
                )
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
fun CategorySectionHeader(onAddCategory: ()-> Unit) {
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
        FilledTonalIconButton (onClick = onAddCategory) {
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier.width(280.dp),
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