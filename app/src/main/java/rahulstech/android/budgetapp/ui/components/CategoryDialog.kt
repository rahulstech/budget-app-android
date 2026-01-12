package rahulstech.android.budgetapp.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import rahulstech.android.budgetapp.R
import rahulstech.android.budgetapp.repository.BudgetRepository
import rahulstech.android.budgetapp.repository.model.Budget
import rahulstech.android.budgetapp.repository.model.BudgetCategory
import rahulstech.android.budgetapp.ui.screen.BUDGET_CATEGORY_PLACEHOLDER
import rahulstech.android.budgetapp.ui.screen.BUDGET_PLACEHOLDER
import rahulstech.android.budgetapp.ui.screen.UIState
import javax.inject.Inject

private const val TAG = "CategoryDialog"

data class BudgetCategoryDialogState(
    val showDialog: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val category: BudgetCategory = BUDGET_CATEGORY_PLACEHOLDER,
    val budget: Budget? = null
)

class CategoryDialogStateManager(initialState: BudgetCategoryDialogState = BudgetCategoryDialogState()) {

    private val _categoryDialogStateFlow = MutableStateFlow(initialState)
    val categoryDialogStateFlow = _categoryDialogStateFlow.asStateFlow()

    val categoryDialogState get() = _categoryDialogStateFlow.value

    val showDialog: Boolean get() = _categoryDialogStateFlow.value.showDialog

    fun updateSaving(isSaving: Boolean, category: BudgetCategory? = null) {
        _categoryDialogStateFlow.value = if (null == category) {
            _categoryDialogStateFlow.value.copy(isSaving = isSaving)
        }
        else {
            _categoryDialogStateFlow.value.copy(isSaving = isSaving, category = category)
        }
    }

    fun showDialog(budget: Budget = BUDGET_PLACEHOLDER, category: BudgetCategory? = null) {
        _categoryDialogStateFlow.value = if (null == category) {
            BudgetCategoryDialogState(showDialog = true, budget = budget)
        }
        else {
            BudgetCategoryDialogState(showDialog = true, budget = budget, category = category)
        }
    }

    fun hideDialog() {
        _categoryDialogStateFlow.value = BudgetCategoryDialogState()
    }
}

@Composable
fun CategoryDialog(categoryDialogState: BudgetCategoryDialogState,
                   onDismiss: ()-> Unit,
                   onClickSave: (BudgetCategory)-> Unit,
                   )
{
    val enabled = !categoryDialogState.isSaving
    val initialCategory = categoryDialogState.category
    var name by rememberSaveable { mutableStateOf(initialCategory.name ) }
    var allocation by rememberSaveable { mutableStateOf(initialCategory.allocation.toString()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet (
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            // top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            ) {
                // title
                Text(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    text = stringResource(R.string.title_new_budget_category),
                    style = MaterialTheme.typography.titleLarge,
                )

                // cancel button
                TextButton(
                    enabled = enabled,
                    onClick = { onDismiss() }
                ) {
                    Text(text = stringResource(R.string.label_cancel))
                }

                // save button
                TextButton(
                    enabled = enabled && name.isNotBlank(),
                    onClick = {
                        val alloc = allocation.toDoubleOrNull() ?: 0.0
                        val category = BudgetCategory(
                            id = initialCategory.id,
                            budgetId = categoryDialogState.budget?.id ?: initialCategory.budgetId,
                            name = name,
                            allocation = alloc
                        )
                        onClickSave(category)
                    }) {
                    Text(text = stringResource(R.string.label_save))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // content
            Column(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(R.string.label_budget_category_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        keyboardType = KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                // category allocation
                OutlinedTextField(
                    value = allocation,
                    onValueChange = { allocation = it },
                    // TODO: add currency symbol prefix
                    prefix = {
                        Icon(
                            painterResource(R.drawable.baseline_currency_rupee_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    },
                    label = { Text(text = stringResource(R.string.label_allocation)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CategoryChooserDialog(budgetId: Long,
                          onDismiss: () -> Unit,
                          onClickChoose: (List<BudgetCategory>)-> Unit,
                          viewModel: CategoryChooserViewModel = hiltViewModel()
                          )
{
    LaunchedEffect(budgetId) { viewModel.observeCategoriesOfBudget(budgetId) }

    val categoriesState by viewModel.categoriesState.collectAsStateWithLifecycle()
    val selectionState by viewModel.selectedCategoriesState.collectAsStateWithLifecycle()

    BasicAlertDialog(onDismissRequest = onDismiss)
    {
        Surface(
            modifier = Modifier.heightIn(max = 560.dp),
            shape = MaterialTheme.shapes.small,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.title_choose_categories),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp).weight(1f),
                ) {
                    when(categoriesState) {
                        is UIState.Loading -> {
                            items(count = 10) {
                                CategoryChooserDialogItemShimmer()
                            }
                        }
                        is UIState.Success -> {
                            val categories = (categoriesState as UIState.Success<List<BudgetCategory>>).data
                            Log.i(TAG, "number of categories ${categories.size} of budget with id $budgetId")
                            items(items = categories, key = { it.id }) { category ->
                                CategoryChooserDialogItem(
                                    category = category,
                                    onToggleSelection = { viewModel.toggleCategorySelection(it) },
                                    isSelected = selectionState.contains(category)
                                )
                            }
                        }
                        else -> {}
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.End)
                ) {
                    // cancel
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.label_cancel)) }

                    TextButton(onClick = { onClickChoose(viewModel.selectedCategories) }) { Text(stringResource(R.string.label_choose)) }
                }
            }
        }
    }
}

@Composable
fun CategoryChooserDialogItemShimmer()
{
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(56.dp)
                .shimmer(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        )

        Spacer(modifier = Modifier.height(12.dp))
    }

}

@Composable
fun CategoryChooserDialogItem(category: BudgetCategory,
                              onToggleSelection: (BudgetCategory)-> Unit,
                              isSelected: Boolean = false,
                              )
{
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
            .clickable(
                onClick = { onToggleSelection(category) }
            ),
    ) {
        Text(
            category.name, 
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider()
    }
}

@HiltViewModel
class CategoryChooserViewModel @Inject constructor(val repo: BudgetRepository): ViewModel()
{
    private var lastBudgetId: Long = 0

    private val _categoriesState = MutableStateFlow<UIState<List<BudgetCategory>>>(UIState.Idle())

    val categoriesState = _categoriesState.asStateFlow()

    fun observeCategoriesOfBudget(budgetId: Long) {
        if (lastBudgetId == budgetId) {
            return
        }
        Log.d(TAG, "loading categories of budget $budgetId")
        viewModelScope.launch {
            repo.observeBudgetCategoriesForBudget(budgetId)
                .onStart { _categoriesState.value = UIState.Loading() }
                .catch { _categoriesState.value = UIState.Error(it) }
                .collectLatest { categories ->
                    lastBudgetId = budgetId
                    _categoriesState.value = when {
                        categories.isEmpty() -> UIState.NotFound()
                        else -> UIState.Success(categories)
                    }
                }
        }
    }

    private val _selectedCategoriesState = MutableStateFlow(setOf<BudgetCategory>())

    val selectedCategoriesState = _selectedCategoriesState.asStateFlow()

    val selectedCategories: List<BudgetCategory> get() = _selectedCategoriesState.value.toList()

    fun toggleCategorySelection(category: BudgetCategory) {
        val current = _selectedCategoriesState.value
        if (current.contains(category)) {
            _selectedCategoriesState.value = current - category
        }
        else {
            _selectedCategoriesState.value = current + category
        }
    }
}