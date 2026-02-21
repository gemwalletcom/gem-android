package com.gemwallet.android.features.send.solana

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gemwallet.android.R
import java.math.BigDecimal
import java.text.DecimalFormat

data class SolanaTransactionDetails(
    val tokenAmount: BigDecimal,
    val tokenSymbol: String,
    val recipientAddress: String,
    val estimatedFeeInLamports: Long,
    val decimals: Int,
)

@Composable
fun SolanaTransactionConfirmationScreen(
    transactionDetails: SolanaTransactionDetails,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val feeInSol = formatLamportsToSol(transactionDetails.estimatedFeeInLamports)
    val decimalFormat = DecimalFormat("0.########").apply {
        isGroupingUsed = false
    }
    val formattedAmount = decimalFormat.format(transactionDetails.tokenAmount)
    val shortRecipient = transactionDetails.recipientAddress.take(8) + "..." +
        transactionDetails.recipientAddress.takeLast(8)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.send_solana_confirm_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ConfirmationRow(
                label = stringResource(R.string.send_solana_amount),
                value = "$formattedAmount ${transactionDetails.tokenSymbol}",
                isHighlight = true,
            )
            ConfirmationRow(
                label = stringResource(R.string.send_solana_recipient),
                value = shortRecipient,
                valueColor = MaterialTheme.colorScheme.primary,
            )
            ConfirmationRow(
                label = stringResource(R.string.send_solana_estimated_fee),
                value = "$feeInSol SOL",
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(12.dp),
        ) {
            Text(
                text = stringResource(R.string.send_solana_warning),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text(stringResource(R.string.send_solana_cancel))
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(stringResource(R.string.send_solana_confirm))
            }
        }
    }
}

@Composable
private fun ConfirmationRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isHighlight) FontWeight.SemiBold else FontWeight.Normal,
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

private fun formatLamportsToSol(lamports: Long): String {
    val solAmount = lamports.toBigDecimal() / BigDecimal(1_000_000_000)
    val decimalFormat = DecimalFormat("0.#########").apply {
        isGroupingUsed = false
    }
    return decimalFormat.format(solAmount)
}
