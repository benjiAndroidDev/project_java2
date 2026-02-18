package com.Upermarket.upermarket

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.util.Locale

@Composable
fun CartSheet(cartViewModel: CartViewModel, favoritesViewModel: FavoritesViewModel) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Mon Panier", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("${cartViewModel.itemCount} articles", color = Color.Gray)
        }

        if (cartViewModel.cartItems.isEmpty()) {
            EmptyCartView()
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartViewModel.cartItems) { item ->
                    CartProductCard(
                        item = item, 
                        cartViewModel = cartViewModel, 
                        favoritesViewModel = favoritesViewModel,
                        onProductClick = { selectedProduct = item.product }
                    )
                }
            }

            // Bottom Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${String.format(Locale.FRANCE, "%.2f", cartViewModel.totalPrice)} €", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { /* Checkout */ },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Commander", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Affichage de la fiche produit au clic
    if (selectedProduct != null) {
        ProductDetailSheet(
            product = selectedProduct!!,
            isFavorite = favoritesViewModel.isFavorite(selectedProduct!!),
            onToggleFavorite = { favoritesViewModel.toggleFavorite(selectedProduct!!) },
            onAddToCart = { price -> 
                cartViewModel.addToCart(selectedProduct!!, price)
                selectedProduct = null
            },
            onDismiss = { selectedProduct = null }
        )
    }
}

@Composable
fun CartProductCard(
    item: CartItem, 
    cartViewModel: CartViewModel, 
    favoritesViewModel: FavoritesViewModel,
    onProductClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(item.product.imageUrl),
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
                    .clickable(onClick = onProductClick),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).clickable(onClick = onProductClick)) {
                Text(item.product.name ?: "Produit", fontWeight = FontWeight.Bold, maxLines = 1)
                Text(item.product.brands ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("${String.format(Locale.FRANCE, "%.2f", item.price)} €", fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
            }
            
            // Quantity Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { cartViewModel.updateQuantity(item.product, item.quantity - 1) }) {
                    Icon(Icons.Rounded.RemoveCircleOutline, null, tint = Color.LightGray)
                }
                Text("${item.quantity}", fontWeight = FontWeight.Bold)
                IconButton(onClick = { cartViewModel.updateQuantity(item.product, item.quantity + 1) }) {
                    Icon(Icons.Rounded.AddCircle, null, tint = Color.Black)
                }
            }
        }
    }
}

@Composable
fun EmptyCartView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.ShoppingCart, null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text("Votre panier est vide", color = Color.Gray)
        }
    }
}
