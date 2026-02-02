package com.example.upermarket

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.upermarket.ui.theme.UpermarketTheme
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VipScreen()
            UpermarketTheme {
                NavigationBarExample()
            }
        }
    }
}

@Composable
fun NavigationBarExample() {
    val options = FirebaseVisionBarcodeDetectorOptions.Builder()
        .setBarcodeFormats(
            FirebaseVisionBarcode.FORMAT_QR_CODE,
            FirebaseVisionBarcode.FORMAT_AZTEC)
        .build()
    val navController = rememberNavController()
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val destinations = Destination.entries

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinations.forEachIndexed { index, destination ->
                    val isSelected = selectedItem == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            selectedItem = index
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { 
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon, 
                                contentDescription = destination.contentDescription 
                            ) 
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.HOME.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Destination.HOME.route) {
                HomeScreen()
            }
            composable(Destination.SEARCH.route) {
                SearchScreen()
            }
            composable(Destination.SETTINGS.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Paramètres")
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VipScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Espace VIP", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        // Badge VIP Actif fond bleu clair
                        Surface(color = Color(0xFFE3F2FD), shape = CircleShape) {
                        }
                    }
                }
            )
        }
    ) {}
}



@Composable
fun HomeScreen() {
    val api = remember { OpenFoodFactsApi.create() }
    var popularProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                popularProducts = api.searchProducts(searchTerms = "").products
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        HomeHeader()
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("", onSeeAll = {})
        CarouselExample(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier
            .background(Color.Gray)
            .height(24.dp))
        SectionTitle("Rayons", onSeeAll = {})
        FoodCategoriesRow()
        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle("Populaire en ce moment", onSeeAll = {})
        PopularProductsRow(products = popularProducts)
    }
}



@Composable
fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .padding(top = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Bonjour 👋",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                text = "Upermarket",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row {
            IconButton(
                onClick = { },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Rounded.Notifications, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            ) {
                Icon(Icons.Rounded.ShoppingCart, contentDescription = null)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tout voir",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable { onSeeAll() }
        )
    }
}






    @Composable
    fun FoodCategoriesRow() {
        val categories = listOf(
            "Fruits" to R.drawable.fruits,
            "Viandes" to R.drawable.viandes,
            "Laitier" to R.drawable.lait,
            "Frais" to R.drawable.produit_frais,
            "Boissons" to R.drawable.coca,
            "Surgelés" to R.drawable.surgele,
            "Épicerie" to R.drawable.charcuterie
        )

        val listState = rememberLazyListState()

        LaunchedEffect(Unit) {
            while (true) {
                delay(3000)
                val nextIndex = (listState.firstVisibleItemIndex + 1) % categories.size
                listState.animateScrollToItem(nextIndex)
            }
        }

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(vertical = 8.dp)
        ) {
            items(categories) { (name, icon) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(60.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = icon),
                            contentDescription = name,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(70.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    @Composable
    fun PopularProductsRow(products: List<Product>) {
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(products) { product ->
                    ProductCard(product = product)
                }
            }
        }
    }

    @Composable
    fun ProductCard(product: Product) {
        Card(
            modifier = Modifier.width(160.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground)
                )

                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = product.name ?: "Produit",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = product.brands ?: "Marque",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "1.99 €",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(
                            onClick = {},
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                Icons.Rounded.ShoppingCart,
                                contentDescription = "Add to cart",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SearchScreen() {
        val api = remember { OpenFoodFactsApi.create() }
        var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
        var isLoading by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val textFieldState = remember { TextFieldState() }

        Column(Modifier
            .fillMaxSize()
            .padding(top = 20.dp)) {
            SimpleSearchBar(
                textFieldState = textFieldState,
                onSearch = { query ->
                    scope.launch {
                        if (query.isBlank()) {
                            searchResults = emptyList()
                            return@launch
                        }
                        isLoading = true
                        try {
                            val response = api.searchProducts(searchTerms = query)
                            searchResults = response.products
                        } catch (e: Exception) {
                            e.printStackTrace()
                            searchResults = emptyList()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                searchResults = searchResults,
                isLoading = isLoading
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SimpleSearchBar(
        textFieldState: TextFieldState,
        onSearch: (String) -> Unit,
        searchResults: List<Product>,
        isLoading: Boolean,
    ) {
        var expanded by rememberSaveable { mutableStateOf(false) }

        Box(Modifier.fillMaxWidth()) {
            SearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = if (expanded) 0.dp else 16.dp),
                inputField = {
                    SearchBarDefaults.InputField(
                        modifier = Modifier.fillMaxWidth(),
                        query = textFieldState.text.toString(),
                        onQueryChange = {
                            textFieldState.edit { replace(0, length, it) }
                            if (it.length >= 3) {
                                onSearch(it.toString())
                            }
                        },
                        onSearch = {
                            onSearch(textFieldState.text.toString())
                            expanded = true
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text("Rechercher un produit ") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                if (isLoading && searchResults.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(searchResults) { product ->
                            ProductItem(product = product)
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProductItem(product: Product) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
                placeholder = painterResource(R.drawable.ic_launcher_foreground)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name ?: "Produit inconnu",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
                Text(
                    text = product.brands ?: "Marque inconnue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                if (!product.quantity.isNullOrBlank()) {
                    Text(
                        text = product.quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            product.nutriscore?.let { score ->
                NutriScoreBadge(score = score)
            }
        }
    }

    @Composable
    fun NutriScoreBadge(score: String) {
        val color = when (score.lowercase()) {
            "a" -> Color(0xFF038141)
            "b" -> Color(0xFF85BB2F)
            "c" -> Color(0xFFFECB02)
            "d" -> Color(0xFFEE8100)
            "e" -> Color(0xFFE63E11)
            else -> Color.Gray
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = score.uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CarouselExample(modifier: Modifier = Modifier) {
        val carouselItems = remember {
            listOf(
                R.drawable.viande,
                R.drawable.produits_laitiers,
                R.drawable.charcuterie,
                R.drawable.legumes,
                R.drawable.surgele,
                R.drawable.boissons,

                )
        }

        HorizontalUncontainedCarousel(
            state = rememberCarouselState { carouselItems.count() },
            modifier = modifier
                .height(130.dp)
                .fillMaxWidth(),
            itemWidth = 150.dp,
            itemSpacing = 12.dp,
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) { i ->
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                painter = painterResource(id = carouselItems[i]),
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }
    }
