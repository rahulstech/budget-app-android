package rahulstech.android.budgetapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rahulstech.android.budgetapp.ui.theme.ColorProgressDanger
import rahulstech.android.budgetapp.ui.theme.ColorProgressSafe
import rahulstech.android.budgetapp.ui.theme.ColorProgressWarning
import kotlin.math.roundToLong

fun defaultExpenseProgressColorProvider(progress: Float): Color = when{
    progress < .5f -> ColorProgressSafe
    progress < .9f -> ColorProgressWarning
    else -> ColorProgressDanger
}

@Composable
fun defaultExpenseProgressExpenseColorProvider(progress: Float,
                                               progressColor: (Float) -> Color = { defaultExpenseProgressColorProvider(it)}
                                               ): Color =
    when {
        progress == 0f -> LocalTextStyle.current.color
        else -> progressColor(progress)
    }


@Composable
fun ExpenseLinearProgress(expense: Double,
                          allocation: Double,
                          progressColorCallback: (Float)-> Color = { defaultExpenseProgressColorProvider(it) },
                          barThickness: Dp = 12.dp,
                          textSize: TextUnit = 16.sp,
                          labelExpense: String = "",
                          labelAllocation: String = "",
                          )
{
    val progress = (expense / allocation).coerceIn(0.0, 1.0).toFloat()
    val progressColor = progressColorCallback(progress)
    val expenseTextColor = when(progress) {
        0f -> LocalTextStyle.current.color
        else -> progressColor
    }
    // TODO: apply number format and currency symbol
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (labelExpense.isNotBlank()) {
            Text(
                modifier = Modifier.align(Alignment.Start),
                text = labelExpense,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
            )

            Spacer(modifier = Modifier.height(4.dp))
        }

        Text(
            modifier = Modifier.align(Alignment.Start),
            text = expense.roundToLong().toString(),
            color = expenseTextColor,
            fontSize = textSize,
            fontWeight = FontWeight.W700,
        )

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(barThickness),
            progress = { progress },
            color = progressColor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            modifier = Modifier.align(Alignment.End),
            text = allocation.roundToLong().toString(),
            fontSize = textSize,
        )

        if (labelAllocation.isNotBlank()) {

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                modifier = Modifier.align(Alignment.End),
                text = labelAllocation,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Normal,
            )
        }
    }

}