package com.Upermarket.upermarket

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ShoppingItem(val id: Int, val name: String, val isChecked: Boolean = false)

@Composable
fun ShoppingListSheet(onDismiss: () -> Unit) {
    var items by remember { mutableStateOf(listOf(
        ShoppingItem(1, "Lait demi-écrémé"),
        ShoppingItem(2, "Pain de mie complet"),
        ShoppingItem(3, "Pommes Gala"),
        ShoppingItem(4, "Pâtes Barilla n°5")
    )) }
    var newItemName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ma Liste", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            
            if (items.isNotEmpty()) {
                IconButton(onClick = { items = emptyList() }) {
                    Icon(Icons.Rounded.DeleteSweep, "Vider la liste", tint = Color.Red)
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))

        // Input Area
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newItemName,
                onValueChange = { newItemName = it },
                placeholder = { Text("Ajouter un article...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            Spacer(Modifier.width(12.dp))
            FloatingActionButton(
                onClick = { 
                    if (newItemName.isNotBlank()) {
                        val nextId = if (items.isEmpty()) 1 else items.maxOf { it.id } + 1
                        items = items + ShoppingItem(nextId, newItemName)
                        newItemName = ""
                    }
                },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Add, null)
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                ShoppingRow(
                    item = item,
                    onCheckedChange = { checked ->
                        items = items.map { if (it.id == item.id) it.copy(isChecked = checked) else it }
                    },
                    onDelete = {
                        items = items.filter { it.id != item.id }
                    }
                )
            }
        }
    }
}

@Composable
fun ShoppingRow(item: ShoppingItem, onCheckedChange: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.isChecked) Color(0xFFE8F5E9) else Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onCheckedChange(!item.isChecked) }) {
                Icon(
                    if (item.isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    null,
                    tint = if (item.isChecked) Color(0xFF4CAF50) else Color.LightGray
                )
            }
            Text(
                text = item.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium
                ),
                color = if (item.isChecked) Color.Gray else Color.Black
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, "Supprimer", tint = Color.LightGray.copy(alpha = 0.6f))
            }
        }
    }
}
