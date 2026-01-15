package rahulstech.android.budgetapp.ui.theme

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

val ColorProgressSafe = progressSafeLight

val ColorProgressWarning = progressWarningLight

val ColorProgressDanger = progressDangerLight

@Composable
fun tileColors(): CardColors =
    CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background
    )

@Composable
fun primaryTopAppBarColors(): TopAppBarColors =
    TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        scrolledContainerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
    )