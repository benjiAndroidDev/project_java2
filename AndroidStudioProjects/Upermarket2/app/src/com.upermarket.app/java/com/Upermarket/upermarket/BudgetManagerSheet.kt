package com.Upermarket.upermarket

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BudgetManagerSheet(
    onDismiss: () -> Unit,
    cartViewModel: CartViewModel,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Gestion du Budget", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Budget actuel : ${cartViewModel.userMaxBudget} €")
        // Ajoutez ici les contrôles pour modifier le budget
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("Fermer")
        }
    }
}
