package rahulstech.android.budgetapp.ui.theme

import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ColorProgressSafe = Color(0xFF399918)

val ColorProgressWarning = Color(0xFFE1AA36)

val ColorProgressDanger = Color(0xFFFF4646)

@Composable
fun tileColors(): CardColors =
    CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.background
    )