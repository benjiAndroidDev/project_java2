package com.example.upermarket

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onDismiss: () -> Unit = {},
    favoritesViewModel: FavoritesViewModel? = null,
    cartViewModel: CartViewModel? = null
) {
    val haptic = LocalHapticFeedback.current
    var isFavorite by remember { mutableStateOf(favoritesViewModel?.isFavorite(product) ?: false) }
    var showIngredients by remember { mutableStateOf(false) }
    
    // Génération de produits similaires (simulation)
    val similarProducts = remember { generateSimilarProducts(product) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight(0.95f)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header avec image grande
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFF8F9FA))
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Overlay gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(0.3f)),
                                    startY = 200f
                                )
                            )
                    )
                    
                    // Badge source
                    val isLocal = LocalProductDatabase.getProduct(product.code ?: "") != null
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        color = if (isLocal) Color(0xFF4CAF50) else Color(0xFF2196F3),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            if (isLocal) "🏠 LOCAL" else "🌐 API",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    
                    // Bouton favoris flottant
                    FloatingActionButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isFavorite = favoritesViewModel?.toggleFavorite(product) ?: false
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(56.dp),
                        containerColor = if (isFavorite) Color.Red else Color.White,
                        contentColor = if (isFavorite) Color.White else Color.Gray
                    ) {
                        Icon(
                            if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favoris",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            
            // Info principale
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        product.name ?: "Produit",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    if (!product.brands.isNullOrBlank()) {
                        Text(
                            product.brands,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    if (!product.quantity.isNullOrBlank()) {
                        Text(
                            product.quantity,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // Scores nutritionnels
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "🏆 Scores Nutritionnels",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            product.nutriscore?.let { score ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = nutriScoreColor(score),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                score.uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 24.sp
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Nutri-Score",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        getNutriScoreDescription(score),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            product.ecoscore?.let { score ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = ecoScoreColor(score),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                score.uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 24.sp
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Eco-Score",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        getEcoScoreDescription(score),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            
                            product.novaGroup?.let { nova ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        color = novaColor(nova),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.size(60.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                nova.toString(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 24.sp
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Nova",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        getNovaDescription(nova),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Catégorie
            if (!product.categories.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.Category,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Catégorie",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    product.categories,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Ingrédients
            if (!product.ingredients.isNullOrBlank()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showIngredients = !showIngredients }
                            ) {
                                Icon(
                                    Icons.Rounded.List,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Ingrédients",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    if (showIngredients) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null
                                )
                            }
                            
                            AnimatedVisibility(
                                visible = showIngredients,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Text(
                                    product.ingredients,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Produits similaires
            if (similarProducts.isNotEmpty()) {
                item {
                    Text(
                        "💡 Produits similaires",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(similarProducts) { similarProduct ->
                            Card(
                                modifier = Modifier.width(140.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = similarProduct.imageUrl,
                                        contentDescription = similarProduct.name,
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF5F5F5)),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        similarProduct.name ?: "Produit",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2
                                    )
                                    similarProduct.brands?.let { brand ->
                                        Text(
                                            brand,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Bouton d'action principal
            item {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        cartViewModel?.addToCart(product)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Rounded.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Ajouter au panier",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Espacement final
            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

// Fonctions utilitaires
fun getNutriScoreDescription(score: String): String = when (score.lowercase()) {
    "a" -> "Très bonne qualité nutritionnelle"
    "b" -> "Bonne qualité nutritionnelle"
    "c" -> "Qualité nutritionnelle moyenne"
    "d" -> "Qualité nutritionnelle faible"
    "e" -> "Qualité nutritionnelle très faible"
    else -> "Non évalué"
}

fun getEcoScoreDescription(score: String): String = when (score.lowercase()) {
    "a" -> "Très faible impact environnemental"
    "b" -> "Faible impact environnemental"
    "c" -> "Impact environnemental modéré"
    "d" -> "Impact environnemental élevé"
    "e" -> "Impact environnemental très élevé"
    else -> "Non évalué"
}

fun getNovaDescription(nova: Int): String = when (nova) {
    1 -> "Aliments non transformés"
    2 -> "Aliments peu transformés"
    3 -> "Aliments transformés"
    4 -> "Aliments ultra-transformés"
    else -> "Non classé"
}

fun generateSimilarProducts(product: Product): List<Product> {
    val similarProducts = mutableListOf<Product>()
    
    // Simulation de produits similaires basés sur la catégorie
    when {
        product.categories?.contains("Chocolats", ignoreCase = true) == true -> {
            similarProducts.addAll(listOf(
                Product(code = "sim1", name = "Chocolat au lait", brands = "Milka", categories = "Chocolats"),
                Product(code = "sim2", name = "Chocolat noir 70%", brands = "Lindt", categories = "Chocolats"),
                Product(code = "sim3", name = "Chocolat blanc", brands = "Nestlé", categories = "Chocolats")
            ))
        }
        product.categories?.contains("Fromages", ignoreCase = true) == true -> {
            similarProducts.addAll(listOf(
                Product(code = "sim4", name = "Camembert", brands = "Président", categories = "Fromages"),
                Product(code = "sim5", name = "Brie", brands = "Lactalis", categories = "Fromages"),
                Product(code = "sim6", name = "Roquefort", brands = "Société", categories = "Fromages")
            ))
        }
        else -> {
            similarProducts.addAll(listOf(
                Product(code = "sim7", name = "Produit recommandé", brands = "Marque", categories = "Divers"),
                Product(code = "sim8", name = "Alternative", brands = "Bio", categories = "Divers")
            ))
        }
    }
    
    return similarProducts.take(3)
}