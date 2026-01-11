package rahulstech.android.budgetapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import rahulstech.android.budgetapp.ui.theme.BudgetAppTheme

@Composable
fun Modifier.shimmer(color: Color = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)): Modifier =
    background(
        color = color
    )

@Preview(
    showBackground = true
)
@Composable
fun PreviewShimmer() {
    BudgetAppTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)
                .size(width = 300.dp, height = 300.dp).shimmer())
        }

    }
}