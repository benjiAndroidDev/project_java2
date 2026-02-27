package com.Upermarket.upermarket

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    categoryName: String,
    categoryTag: String,
    favoritesViewModel: FavoritesViewModel,
    cartViewModel: CartViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val searchManager = remember { MegaProductSearchManager(context) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    // Filtres et Tri
    var selectedSubFilter by remember { mutableStateOf("Tous") }
    var nutriFilter by remember { mutableStateOf<String?>(null) }
    var sortByNutriScore by remember { mutableStateOf(false) }

    val subFilters = remember(categoryName) {
        when (categoryName) {
            "Viandes" -> listOf("Tous", "Poulet", "Boeuf", "Dinde", "Steak", "Saucisse", "Veau")
            "Boissons" -> listOf("Tous", "Eau", "Jus", "Soda", "Lait", "Café", "Thé", "Energy Drink")
            "Laiterie" -> listOf("Tous", "Lait", "Yaourt", "Fromage", "Beurre", "Crème", "Lait Végétal")
            "Épicerie" -> listOf("Tous", "Pâtes", "Riz", "Sauce", "Conserve", "Gâteaux")
            "Surgelés" -> listOf("Tous", "Pizza", "Légumes", "Poisson", "Glace", "Plat Prêt", "Frites")
            "Légumes" -> listOf("Tous", "Carotte", "Salade", "Tomate", "Pomme de terre", "Oignon", "Poivron", "Bio")
            "Fruits" -> listOf("Tous", "Pomme", "Banane", "Orange", "Fraise", "Bio", "Exotique")
            "Boulang." -> listOf("Tous", "Pain", "Pain de mie", "Brioche", "Pain burger", "Baguette", "Viennoiserie")
            "Pâtiss." -> listOf("Tous", "Gâteau", "Tarte", "Éclair", "Biscuit", "Chocolat", "Dessert")
            else -> listOf("Tous", "Bio", "Nutri-Score A")
        }
    }

    // Logique de filtrage et tri
    val filteredProducts = remember(products, selectedSubFilter, nutriFilter, sortByNutriScore) {
        var list = if (selectedSubFilter == "Tous") products
        else {
            products.filter { 
                it.name?.contains(selectedSubFilter, ignoreCase = true) == true ||
                it.categories?.contains(selectedSubFilter, ignoreCase = true) == true
            }
        }

        if (nutriFilter != null) {
            list = list.filter { it.nutriscore?.lowercase() == nutriFilter?.lowercase() }
        }

        if (sortByNutriScore) {
            list = list.sortedBy { it.nutriscore ?: "z" }
        }
        list
    }

    LaunchedEffect(categoryTag) {
        isLoading = true
        products = searchManager.searchProducts(query = categoryName, categoryTag = categoryTag)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(categoryName, fontWeight = FontWeight.Black, fontSize = 22.sp)
                        Text("${filteredProducts.size} produits disponibles", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { sortByNutriScore = !sortByNutriScore }) {
                        Icon(Icons.Rounded.Sort, null, tint = if (sortByNutriScore) Color(0xFF00C853) else Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA))) {
            
            // 1. Barre de sous-catégories
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subFilters) { filter ->
                    FilterChip(
                        selected = selectedSubFilter == filter,
                        onClick = { selectedSubFilter = filter },
                        label = { Text(filter, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00C853),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // 2. Barre Nutri-Score (Mini Chips)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Nutri-Score:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                listOf("A", "B", "C").forEach { score ->
                    val isSelected = nutriFilter == score
                    Surface(
                        onClick = { nutriFilter = if (isSelected) null else score },
                        shape = CircleShape,
                        color = if (isSelected) getNutriColor(score) else Color.White,
                        border = if (!isSelected) BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(score, fontSize = 12.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else Color.Black)
                        }
                    }
                }
            }

            // 3. Liste des produits
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00C853))
                } else {
                    AnimatedContent(targetState = filteredProducts.isEmpty()) { isEmpty ->
                        if (isEmpty) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Aucun produit correspondant", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredProducts, key = { it.code ?: it.name ?: "" }) { product ->
                                    SearchProductRow(
                                        product = product,
                                        isFavorite = favoritesViewModel.isFavorite(product),
                                        onToggleFavorite = { favoritesViewModel.toggleFavorite(product) },
                                        onClick = { selectedProduct = product }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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
