package com.Upermarket.upermarket

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    favoritesViewModel: FavoritesViewModel, 
    cartViewModel: CartViewModel,
    initialQuery: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val searchManager = remember { MegaProductSearchManager(context) }
    
    var query by remember { mutableStateOf(initialQuery ?: "") }
    var results by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    // Filtres
    var selectedCategory by remember { mutableStateOf("Tous") }
    var nutriFilter by remember { mutableStateOf<String?>(null) }
    var selectedSubFilter by remember { mutableStateOf("Tous") }

    val categories = mapOf(
        "Tous" to null,
        "Viandes" to "en:meats",
        "Boissons" to "en:beverages",
        "Laiterie" to "en:dairies",
        "Surgelés" to "en:frozen-foods",
        "Épicerie" to "en:groceries",
        "Fruits" to "en:fruits",
        "Légumes" to "en:vegetables",
        "Boulang." to "en:breads"
    )

    val subFilters = remember(selectedCategory) {
        when (selectedCategory) {
            "Viandes" -> listOf("Tous", "Bacon", "Poulet", "Boeuf", "Steak", "Saucisse", "Jambon", "Dinde", "Lardons", "Veau", "Boucherie")
            "Boissons" -> listOf("Tous", "Monster", "Redbull", "Coca", "Eau", "Jus", "Soda", "Pepsi", "Oasis", "Lait", "Café", "Thé", "Energy Drink")
            "Laiterie" -> listOf("Tous", "Lait", "Yaourt", "Fromage", "Beurre", "Alpro", "Crème", "Bio", "Lait Végétal")
            "Surgelés" -> listOf("Tous", "Pizza", "Glace", "Poisson Pané", "Saumon", "Cabillaud", "Crevettes", "Fruits de mer", "Frites", "Plat Prêt")
            "Épicerie" -> listOf("Tous", "Pâtes", "Riz", "Biscuits", "Chocolat", "Sauce", "Conserve", "Chips", "Huile", "Sucre")
            "Légumes" -> listOf("Tous", "Pomme de terre", "Oignon", "Carotte", "Tomate", "Salade", "Poivron", "Bio", "Champignon")
            "Fruits" -> listOf("Tous", "Pomme", "Banane", "Orange", "Fraise", "Exotique", "Raisin", "Poire", "Citron")
            "Boulang." -> listOf("Tous", "Pain burger", "Baguette", "Croissant", "Pain de mie", "Brioche", "Pain")
            else -> emptyList()
        }
    }

    // Fonction de recherche puissante
    fun performSearch(q: String, catTag: String?) {
        if (q.isBlank() && catTag == null) return
        isLoading = true
        scope.launch {
            results = searchManager.searchProducts(query = q, categoryTag = catTag)
            isLoading = false
        }
    }

    // Filtrage local (Nutri-Score et Sous-filtre)
    val finalResults = remember(results, nutriFilter, selectedSubFilter) {
        var list = results
        
        // 1. Filtre Nutri-Score
        if (nutriFilter != null) {
            list = list.filter { it.nutriscore?.lowercase() == nutriFilter?.lowercase() }
        }
        
        // 2. Filtre par aliment précis avec logique d'exclusion pour le Bacon
        if (selectedSubFilter != "Tous") {
            list = list.filter { product ->
                val name = product.name?.lowercase() ?: ""
                val brands = product.brands?.lowercase() ?: ""
                val cats = product.categories?.lowercase() ?: ""
                val sub = selectedSubFilter.lowercase()
                
                if (sub == "bacon") {
                    // On accepte Bacon et Poitrine, mais on dégage strictement les lardons
                    (name.contains("bacon") || name.contains("poitrine")) && !name.contains("lardon")
                } else {
                    name.contains(sub) || brands.contains(sub) || cats.contains(sub)
                }
            }
        }

        // Tri par pertinence : Image d'abord, puis correspondance exacte du nom, puis Nutri-Score
        list.sortedWith(
            compareByDescending<Product> { it.imageUrl != null }
            .thenByDescending { 
                if (selectedSubFilter != "Tous") it.name?.contains(selectedSubFilter, ignoreCase = true) == true else false 
            }
            .thenByDescending { it.nutriscore == "a" || it.nutriscore == "b" }
        )
    }

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            query = initialQuery
            performSearch(query, null)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 1. Barre de recherche
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                if (it.isBlank()) results = emptyList()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = { Text("Rechercher Monster, Steak, Pizza...") },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Color(0xFF00C853)) },
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { performSearch(query, categories[selectedCategory]) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00C853),
                focusedContainerColor = Color(0xFFF8F9FA),
                unfocusedContainerColor = Color(0xFFF8F9FA)
            )
        )

        // 2. Catégories
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories.keys.toList()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { 
                        selectedCategory = cat
                        selectedSubFilter = "Tous"
                        performSearch(query, categories[cat])
                    },
                    label = { Text(cat) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF00C853), selectedLabelColor = Color.White)
                )
            }
        }

        // 3. Sous-filtres dynamiques (Avec Bacon trié)
        if (subFilters.isNotEmpty()) {
            LazyRow(modifier = Modifier.padding(top = 8.dp), contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(subFilters) { sub ->
                    val isSelected = selectedSubFilter == sub
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubFilter = if (isSelected) "Tous" else sub },
                        label = { Text(sub, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (sub == "Bacon") Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                            selectedLabelColor = if (sub == "Bacon") Color(0xFFD32F2F) else Color(0xFF00C853)
                        )
                    )
                }
            }
        }

        // 4. Nutri-Score
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Qualité", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            listOf("A", "B", "C", "D", "E").forEach { score ->
                val isSelected = nutriFilter == score
                Surface(
                    onClick = { nutriFilter = if (isSelected) null else score }, 
                    shape = CircleShape, 
                    color = if (isSelected) getNutriColor(score) else Color(0xFFF1F3F4), 
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) { Text(score, fontSize = 13.sp, fontWeight = FontWeight.Black, color = if (isSelected) Color.White else Color.Black) }
                }
            }
        }

        // 5. Résultats
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF00C853))
            } else if (finalResults.isEmpty() && query.isNotEmpty()) {
                Text("Aucun résultat pour \"$query\"", modifier = Modifier.align(Alignment.Center), color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(finalResults, key = { it.code ?: it.name ?: "" }) { product ->
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
fun SearchProductRow(product: Product, isFavorite: Boolean, onToggleFavorite: () -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = product.imageUrl, contentDescription = null, modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Color.White), contentScale = androidx.compose.ui.layout.ContentScale.Fit)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name ?: "Produit", fontWeight = FontWeight.Bold, maxLines = 1)
                Text(product.brands ?: "Marque inconnue", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Surface(color = getNutriColor(product.nutriscore), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Text(product.nutriscore?.uppercase() ?: "?", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (isFavorite) Color.Red else Color.Gray)
            }
        }
    }
}
