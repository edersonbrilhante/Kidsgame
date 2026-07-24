package com.example.matteogames.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matteogames.R

/**
 * Simple gate to keep a 3–4 year old out of "grown-up" actions (photo import, settings).
 * The child must tap the number seven among a few options — trivial for an adult, unlikely
 * to be passed accidentally by a toddler.
 */
@Composable
fun ParentalGate(onPass: () -> Unit, onDismiss: () -> Unit) {
    val digits = remember { listOf(7, 2, 5, 9).shuffled() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResourceCompat(R.string.parental_gate_prompt)) },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                digits.forEach { d ->
                    Button(
                        onClick = { if (d == 7) onPass() },
                        modifier = Modifier.size(64.dp),
                    ) {
                        Text(text = d.toString(), fontSize = 24.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResourceCompat(R.string.back)) }
        },
    )
}

@Composable
private fun stringResourceCompat(id: Int): String =
    androidx.compose.ui.res.stringResource(id)
