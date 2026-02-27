package com.Upermarket.upermarket

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.util.Locale

// Import du R correct
import com.benjamin.upermarket.R

data class DriveEnseigne(val name: String, val logoRes: Int, val url: String)

@Composable
fun CartSheet(cartViewModel: CartViewModel, favoritesViewModel: FavoritesViewModel) {
    val context = LocalContext.current
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showDrivePicker by remember { mutableStateOf(false) }
    var showOrderDialog by remember { mutableStateOf(false) }

    val drives = remember { listOf(
        DriveEnseigne("E.Leclerc", R.drawable.e_leclerc_logo_svg, "https://www.leclercdrive.fr"),
        DriveEnseigne("Lidl", R.drawable.lidl_logo_svg, "https://www.lidl.fr"),
        DriveEnseigne("Carrefour", R.drawable.carrefour_logo_1982, "https://www.carrefour.fr/courses-en-ligne"),
        DriveEnseigne("Auchan", R.drawable.logo_auchan__2015__svg, "https://www.auchan.fr/drive"),
        DriveEnseigne("Intermarché", R.drawable.nouveau_logo_intermarche, "https://www.intermarche.com"),
        DriveEnseigne("Monoprix", R.drawable.monoprix, "https://www.monoprix.fr/courses-en-ligne"),
        DriveEnseigne("Franprix", R.drawable.franprix, "https://www.franprix.fr"),
        DriveEnseigne("Netto", R.drawable.french_netto_logo_2019_svg, "https://www.netto.fr"),
        DriveEnseigne("Casino", R.drawable.casino_supermarket_logo, "https://www.casinosupermarches.fr")
    ) }

    Column(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Mon Panier", 
                style = MaterialTheme.typography.headlineMedium, 
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text("${cartViewModel.itemCount} articles", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
        }

        // Section Sélection du Drive
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var typeExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clickable { typeExpanded = true },
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8F9FA),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (cartViewModel.selectedType == "Drive") Icons.Rounded.DirectionsCar else Icons.Rounded.Storefront, null, tint = Color(0xFF00C853), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(cartViewModel.selectedType, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Icon(Icons.Rounded.ArrowDropDown, null, tint = Color.Gray)
                    }
                }
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }, modifier = Modifier.background(Color.White)) {
                    DropdownMenuItem(text = { Text("Drive", fontSize = 14.sp) }, onClick = { cartViewModel.selectedType = "Drive"; cartViewModel.saveCart(); typeExpanded = false }, leadingIcon = { Icon(Icons.Rounded.DirectionsCar, null) })
                    DropdownMenuItem(text = { Text("Magasin", fontSize = 14.sp) }, onClick = { cartViewModel.selectedType = "Magasin"; cartViewModel.saveCart(); typeExpanded = false }, leadingIcon = { Icon(Icons.Rounded.Storefront, null) })
                }
            }

            Surface(
                modifier = Modifier.weight(1.2f).clickable { showDrivePicker = true },
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8F9FA),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (cartViewModel.selectedDriveLogo != 0) {
                        Image(painter = painterResource(id = cartViewModel.selectedDriveLogo), contentDescription = null, modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)))
                    } else {
                        Icon(Icons.Rounded.Store, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(cartViewModel.selectedDrive.replace(" Drive", ""), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.ExpandMore, null, tint = Color.Gray)
                }
            }
        }

        if (cartViewModel.cartItems.isEmpty()) {
            EmptyCartView()
        } else {
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Meilleurs prix pour votre panier", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(cartViewModel.getPriceComparisons()) { comparison ->
                                ComparisonCard(comparison) {
                                    val drive = drives.find { it.name == comparison.storeName }
                                    if (drive != null) {
                                        cartViewModel.selectedDrive = drive.name
                                        cartViewModel.selectedDriveLogo = drive.logoRes
                                        cartViewModel.saveCart()
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(drive.url))
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        }
                    }
                }
                
                items(cartViewModel.cartItems) { item ->
                    CartProductCard(item = item, cartViewModel = cartViewModel, favoritesViewModel = favoritesViewModel, onProductClick = { selectedProduct = item.product })
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("${String.format(Locale.FRANCE, "%.2f", cartViewModel.totalPrice)} €", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00C853))
                    }
                    Spacer(Modifier.height(20.dp))
                    
                    Button(
                        onClick = { showOrderDialog = true },
                        modifier = Modifier.fillMaxWidth().height(64.dp).shadow(12.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853), contentColor = Color.White),
                        enabled = cartViewModel.selectedDrive != "Drive"
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text("VALIDER MES COURSES", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    if (showOrderDialog) {
        AlertDialog(
            onDismissRequest = { showOrderDialog = false },
            title = { Text("🛒 Confirmer ma commande", fontWeight = FontWeight.Black, color = Color.Black) },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (cartViewModel.selectedDriveLogo != 0) {
                        Image(
                            painter = painterResource(id = cartViewModel.selectedDriveLogo),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).padding(bottom = 12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(
                        "Vous allez finaliser vos courses chez ${cartViewModel.selectedDrive}.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(12.dp))
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                        Text(text = "Total estimé : ${String.format(Locale.FRANCE, "%.2f", cartViewModel.totalPrice)} €", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = Color(0xFF00C853), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        val currentDrive = drives.find { it.name == cartViewModel.selectedDrive.replace(" Drive", "") }
                        if (currentDrive != null) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentDrive.url))
                            context.startActivity(intent)
                        }
                        showOrderDialog = false
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Text("CONFIRMER ET PAYER", fontWeight = FontWeight.Black, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOrderDialog = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("RETOUR AU PANIER", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White, shape = RoundedCornerShape(28.dp)
        )
    }

    if (showDrivePicker) {
        AlertDialog(
            onDismissRequest = { showDrivePicker = false },
            title = { Text("Quelle enseigne ?", fontWeight = FontWeight.Black) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(drives) { enseigne ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                                cartViewModel.selectedDrive = enseigne.name
                                cartViewModel.selectedDriveLogo = enseigne.logoRes
                                cartViewModel.saveCart()
                                showDrivePicker = false
                            },
                            shape = RoundedCornerShape(12.dp), color = Color(0xFFF8F9FA)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Image(painter = painterResource(id = enseigne.logoRes), contentDescription = null, modifier = Modifier.size(40.dp).padding(4.dp), contentScale = ContentScale.Fit)
                                Spacer(Modifier.width(16.dp))
                                Text(enseigne.name, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDrivePicker = false }) { Text("Fermer") } },
            containerColor = Color.White, shape = RoundedCornerShape(28.dp)
        )
    }

    if (selectedProduct != null) {
        ProductDetailSheet(
            product = selectedProduct!!,
            isFavorite = favoritesViewModel.isFavorite(selectedProduct!!),
            onToggleFavorite = { favoritesViewModel.toggleFavorite(selectedProduct!!) },
            onAddToCart = { price -> cartViewModel.addToCart(selectedProduct!!, price); selectedProduct = null },
            onDismiss = { selectedProduct = null }
        )
    }
}

@Composable
fun ComparisonCard(comparison: PriceComparison, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(135.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8F9FA),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = comparison.logoRes), contentDescription = null, modifier = Modifier.size(45.dp), contentScale = ContentScale.Fit)
            Spacer(Modifier.height(8.dp))
            Text(comparison.storeName, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text("${String.format(Locale.FRANCE, "%.2f", comparison.totalPrice)} €", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF00C853))
            Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                Text("CHOISIR", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF00C853))
            }
        }
    }
}

@Composable
fun CartProductCard(item: CartItem, cartViewModel: CartViewModel, favoritesViewModel: FavoritesViewModel, onProductClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painter = rememberAsyncImagePainter(item.product.imageUrl), contentDescription = null, modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).clickable(onClick = onProductClick), contentScale = ContentScale.Fit)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f).clickable(onClick = onProductClick)) {
                Text(item.product.name ?: "Produit", fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                Text(item.product.brands ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${String.format(Locale.FRANCE, "%.2f", item.price)} €", fontWeight = FontWeight.ExtraBold, color = Color(0xFF00C853))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { cartViewModel.updateQuantity(item.product, item.quantity - 1) }) { Icon(Icons.Rounded.RemoveCircleOutline, null, tint = MaterialTheme.colorScheme.primary) }
                Text("${item.quantity}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = { cartViewModel.updateQuantity(item.product, item.quantity + 1) }) { Icon(Icons.Rounded.AddCircle, null, tint = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}

@Composable
fun EmptyCartView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Rounded.ShoppingCart, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text("Votre panier est vide", color = MaterialTheme.colorScheme.outline)
        }
    }
}
