package rahulstech.android.budgetapp.ui.screen

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.ui.components.CategoryDialog
import rahulstech.android.budgetapp.ui.theme.BudgetAppTheme
import javax.inject.Inject

private const val TAG = "CreateBudget"

@HiltViewModel
class CreateBudgetViewModel @Inject constructor(val repo: BudgetRepository): ViewModel() {

    private val _budgetSaveState = MutableSharedFlow<UIState<Budget>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val budgetSaveState = _budgetSaveState.asSharedFlow()

    fun createBudget(budget: Budget) {
        viewModelScope.launch {
            // set saveState loading
            _budgetSaveState.tryEmit(UIState.Loading())

            try {
                val newBudget = repo.createBudget(budget)
                _budgetSaveState.tryEmit(UIState.Success(newBudget))
            }
            catch (cause: Throwable) {
                _budgetSaveState.tryEmit(UIState.Error(cause))
            }
        }
    }
}

@Composable
fun CreateBudgetRoute(snackBarCallback: SnackBarCallback,
                      exitScreen: ExitScreenCallback,
                      viewModel: CreateBudgetViewModel = hiltViewModel())
{
    val context = LocalContext.current
    val budgetSaveState by viewModel.budgetSaveState.collectAsStateWithLifecycle(UIState.Idle())
    LaunchedEffect(budgetSaveState) {
        when(budgetSaveState) {
            is UIState.Loading -> {
                // TODO: implement loading state
            }
            is UIState.Success<Budget> -> {
                snackBarCallback(SnackBarEvent(
                    message = context.getString(R.string.message_budget_save_success),
                    duration = SnackbarDuration.Long,
                    action = SnackBarAction(label = context.getString(R.string.label_ok))
                ))
                exitScreen(null,null)
            }
            is UIState.Error -> {
                Log.e(TAG, "create budget error", (budgetSaveState as UIState.Error).cause)
                snackBarCallback(SnackBarEvent(
                    message = context.getString(R.string.message_budget_save_error),
                    duration = SnackbarDuration.Long,
                    action = SnackBarAction(label = context.getString(R.string.label_ok))
                ))
            }
            else -> {}
        }
    }

    CreateBudgetScreen(
        onClickSaveBudget = { viewModel.createBudget(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBudgetScreen(onClickSaveBudget: (Budget)-> Unit) {

    var name by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf("") }
    var categories by rememberSaveable { mutableStateOf<List<BudgetCategoryParcelable>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.title_new_budget),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
                actions = {
                    TextButton(
                        enabled = name.isNotBlank() && categories.isNotEmpty(), // TODO: remove category non-empty check
                        onClick = {
                            // TODO: don't save if not category added. show a message in SnackBar
                            val budget = Budget(
                                name = name,
                                details = details,
                                categories = categories.map {
                                    it.toBudgetCategory()
                                }
                            )
                        onClickSaveBudget(budget)
                    }) {
                        Text(text = stringResource(R.string.label_save))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.label_budget_name)) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                modifier = Modifier.height(150.dp),
                value = details,
                onValueChange = { details = it },
                label = { Text(stringResource(R.string.label_budget_details)) },
                maxLines = 10,
                trailingIcon = {
                    if (details.isNotEmpty()) {
                        IconButton(
                            modifier = Modifier.focusable(false),
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

            CategoriesSection(
                categories = categories,
                onSaveCategory = { category, exists ->
                    if (exists) {
                        categories = categories.map {
                            if (it.key == category.key) category else it
                        }
                    }
                    else {
                        categories += category
                    }
                },
                onRemoveCategory = { categories -= it }
            )
        }
    }
}

@Composable
fun CategoriesSection(categories: List<BudgetCategoryParcelable>,
                      onSaveCategory: (BudgetCategoryParcelable, Boolean)-> Unit,
                      onRemoveCategory: (BudgetCategoryParcelable)-> Unit)
{
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editCategory by remember { mutableStateOf<BudgetCategoryParcelable?>(null) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth()
                .weight(1f),
            text = stringResource(R.string.label_budget_categories),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.width(12.dp))

        TextButton(
            onClick = {
                editCategory = null
                showCategoryDialog = true
            }
        ) {
            Text(text = stringResource(R.string.label_add))
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow (
        modifier = Modifier.fillMaxWidth()
            .height(260.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = categories, key = { it.key }) { category ->
            CategoryItem(
                category = category,
                onClickRemove = { onRemoveCategory(it) },
                onClickEdit = {
                    editCategory = it
                    showCategoryDialog = true
                }
            )
        }
    }

    if (showCategoryDialog) {
        CategoryDialog(
            initialCategory = editCategory?.toBudgetCategory(),
            onClickSave = {
                if (null == editCategory) {
                    onSaveCategory(it.toBudgetCategoryParcel(), false)
                }
                else {
                    onSaveCategory(it.toBudgetCategoryParcel(key = editCategory!!.key), true)
                }
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
}

@Composable
fun CategoryItem(category: BudgetCategoryParcelable,
                 onClickRemove: (BudgetCategoryParcelable)-> Unit,
                 onClickEdit: (BudgetCategoryParcelable)-> Unit)
{
    Card(
        modifier = Modifier.width(280.dp).height(240.dp),
        shape = RoundedCornerShape(size = 20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                // edit
                IconButton(onClick = { onClickEdit(category) } ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.message_edit_category, category.name),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // remove
                IconButton(onClick = { onClickRemove(category)}) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.message_edit_category, category.name),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.note,
                style = MaterialTheme.typography.bodyMedium,
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
            onClickSaveBudget = {}
        )
    }
}