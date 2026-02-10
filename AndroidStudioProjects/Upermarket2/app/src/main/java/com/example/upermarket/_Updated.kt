package com.example.upermarket

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.content.SharedPreferences
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.content.ClipboardManager
import android.content.ClipData
import com.google.gson.Gson
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Store
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.upermarket.ui.theme.UpermarketTheme
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import kotlin.math.absoluteValue
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.media.RingtoneManager
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.reflect.TypeToken
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.rounded.ChecklistRtl
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.AccountBalance

// ==================== AUTH SYSTEM (REAL FIREBASE) ====================

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val isVip: Boolean = false
)

class AuthManager(context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var authState by mutableStateOf<AuthState>(AuthState.Idle)
        private set

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) fetchUserData(firebaseUser.uid)
            else authState = AuthState.NotAuthenticated
        }
    }

    private fun fetchUserData(uid: String) {
        authState = AuthState.Loading
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val user = doc.toObject(User::class.java) ?: User(uid = uid, name = "Utilisateur", email = auth.currentUser?.email ?: "")
                authState = AuthState.Authenticated(user)
            }
            .addOnFailureListener {
                authState = AuthState.Error("Erreur de récupération des données")
            }
    }

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) return
        authState = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnFailureListener { authState = AuthState.Error(it.localizedMessage ?: "Échec de connexion") }
    }

    fun signUp(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) return
        authState = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val user = User(uid = result.user!!.uid, name = name, email = email)
                db.collection("users").document(user.uid).set(user)
            }
            .addOnFailureListener { authState = AuthState.Error(it.localizedMessage ?: "Échec d'inscription") }
    }

    fun signOut() {
        auth.signOut()
        authState = AuthState.NotAuthenticated
    }

    fun getCurrentUser(): User? = (authState as? AuthState.Authenticated)?.user

    fun updateVipStatus(isVip: Boolean, expiryDate: Long?) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("isVip", isVip)
            .addOnSuccessListener { fetchUserData(uid) }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpermarketTheme {
                MainApp()
            }
        }
    }
}

// ViewModel pour gérer le panier
class CartViewModel : ViewModel() {
    private val _cartItems = mutableStateOf<List<CartItem>>(emptyList())
    val cartItems: List<CartItem> get() = _cartItems.value

    val itemCount: Int get() = _cartItems.value.sumOf { it.quantity }
    
    // Prix total estimé (prix fictif pour la démo, car OpenFoodFacts ne fournit pas de prix)
    val totalPrice: Float get() = _cartItems.value.sumOf { 
        // Prix estimé : 3.99$ par produit pour la démo
        (it.quantity * 3.99).toDouble() 
    }.toFloat()

    fun addToCart(product: Product) {
        val productId = "${product.name}_${product.brands}"
        val existingItem = _cartItems.value.find {
            "${it.product.name}_${it.product.brands}" == productId
        }
        if (existingItem != null) {
            _cartItems.value = _cartItems.value.map {
                if ("${it.product.name}_${it.product.brands}" == productId) {
                    it.copy(quantity = it.quantity + 1)
                } else {
                    it
                }
            }
        } else {
            _cartItems.value = _cartItems.value + CartItem(product, 1)
        }
    }

    fun removeFromCart(product: Product) {
        val productId = "${product.name}_${product.brands}"
        _cartItems.value = _cartItems.value.filter {
            "${it.product.name}_${it.product.brands}" != productId
        }
    }

    fun updateQuantity(product: Product, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(product)
        } else {
            val productId = "${product.name}_${product.brands}"
            _cartItems.value = _cartItems.value.map {
                if ("${it.product.name}_${it.product.brands}" == productId) {
                    it.copy(quantity = quantity)
                } else {
                    it
                }
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }
}

// ViewModel pour gérer les favoris
class FavoritesViewModel(private val favoritesManager: FavoritesManager) : ViewModel() {
    private val _favorites = mutableStateOf<List<Product>>(emptyList())
    val favorites: List<Product> get() = _favorites.value

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        _favorites.value = favoritesManager.getFavorites()
    }

    fun toggleFavorite(product: Product): Boolean {
        val isNowFavorite = favoritesManager.toggleFavorite(product)
        loadFavorites()
        return isNowFavorite
    }

    fun isFavorite(product: Product): Boolean {
        return favoritesManager.isFavorite(product)
    }

    fun removeFavorite(product: Product) {
        favoritesManager.removeFavorite(product)
        loadFavorites()
    }

    fun clearFavorites() {
        favoritesManager.clearFavorites()
        loadFavorites()
    }

    val favoriteCount: Int get() = _favorites.value.size
}

// ==================== FAVORIS MANAGER ====================

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_FAVORITES = "favorite_products"
    }

    fun getFavorites(): List<Product> {
        val json = prefs.getString(KEY_FAVORITES, null) ?: return emptyList()
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<Product>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addFavorite(product: Product): Boolean {
        val favorites = getFavorites().toMutableList()

        // Vérifier si déjà dans les favoris
        val productId = "${product.name}_${product.brands}"
        if (favorites.any { "${it.name}_${it.brands}" == productId }) {
            return false
        }

        favorites.add(product)
        saveFavorites(favorites)
        return true
    }

    fun removeFavorite(product: Product): Boolean {
        val favorites = getFavorites().toMutableList()
        val productId = "${product.name}_${product.brands}"
        val removed = favorites.removeAll { "${it.name}_${it.brands}" == productId }

        if (removed) {
            saveFavorites(favorites)
        }

        return removed
    }

    fun isFavorite(product: Product): Boolean {
        val productId = "${product.name}_${product.brands}"
        return getFavorites().any { "${it.name}_${it.brands}" == productId }
    }

    fun toggleFavorite(product: Product): Boolean {
        return if (isFavorite(product)) {
            removeFavorite(product)
            false
        } else {
            addFavorite(product)
            true
        }
    }

    private fun saveFavorites(favorites: List<Product>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    fun getFavoriteCount(): Int = getFavorites().size

    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }
}

// ==================== AUTH MANAGER (SIMPLIFIÉ) ====================

data class CartItem(
    val product: Product,
    val quantity: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val context = LocalContext.current
    val authManager: AuthManager = remember { AuthManager(context) }
    val cartViewModel = remember { CartViewModel() }

    when (val authState = authManager.authState) {
        is AuthState.Idle, is AuthState.Loading -> {
            // Écran de chargement
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Upermarket",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        is AuthState.NotAuthenticated -> {
            // Écran de connexion/inscription
            AuthScreen(authManager = authManager, onAuthSuccess = {
                // L'état changera automatiquement et affichera l'app
            })
        }

        is AuthState.Authenticated -> {
            // App principale
            MainAppContent(authManager = authManager, cartViewModel = cartViewModel)
        }

        is AuthState.Error -> {
            // Gérer les erreurs d'authentification
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Erreur d'authentification",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        authState.message,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { authManager.signOut() }) {
                        Text("Réessayer")
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(favoritesViewModel: FavoritesViewModel) {
    val api = remember { OpenFoodFactsApi.create() }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    LaunchedEffect(Unit) { try { products = api.searchProducts("").products.take(20) } catch (_: Exception) {} }

    // CAROUSEL AUTO avec Pager
    val pagerState = rememberPagerState(pageCount = { 6 })
    val categories = remember {
        listOf(
            CategoryData("🍓 Fruits & Légumes", "Frais du jour", "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=800"),
            CategoryData("🧀 Fromages", "Sélection", "https://images.unsplash.com/photo-1486297678162-eb2a19b0a32d?w=800"),
            CategoryData("🍞 Boulangerie", "Pain frais", "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800"),
            CategoryData("🥩 Viandes", "Qualité premium", "https://images.unsplash.com/photo-1588347818036-8cf47dff6f12?w=800"),
            CategoryData("🍛 Epicerie", "Produits du monde", "https://images.unsplash.com/photo-1543352632-5a4b24e4d2a6?w=800"),
            CategoryData("🍪 Bio", "Local & bio", "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800")
        )
    }
    
    // Auto-scroll carousel
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp)
    ) {
        // CAROUSEL MODERNE AVEC IMAGES
        Box(Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 12.dp)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val category = categories[page]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Image de fond
                        AsyncImage(
                            model = category.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(0.7f))
                                    )
                                )
                        )
                        // Texte
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                category.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                category.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(0.9f)
                            )
                        }
                    }
                }
            }
            
            // Indicateurs de page
            Row(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(pagerState.pageCount) { index ->
                    Box(
                        Modifier
                            .size(if (pagerState.currentPage == index) 24.dp else 8.dp, 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (pagerState.currentPage == index) Color.White 
                                else Color.White.copy(0.5f)
                            )
                    )
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // CAROUSEL DES ENSEIGNES FRANÇAISES
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(listOf(
                Triple("CARREFOUR", Color(0xFF0055A4), Color.White),
                Triple("E.LECLERC", Color(0xFF009A44), Color.White),
                Triple("AUCHAN", Color(0xFFE30613), Color.White),
                Triple("INTERMARCHÉ", Color(0xFFE30613), Color.White),
                Triple("LIDL", Color(0xFF0050AA), Color(0xFFFDD006)),
                Triple("MONOPRIX", Color(0xFF8B1E5D), Color.White),
                Triple("FRANPRIX", Color(0xFFE30613), Color.White),
                Triple("CASINO", Color(0xFFE30613), Color.White)
            )) { (name, bgColor, textColor) ->
                Card(
                    modifier = Modifier
                        .size(110.dp, 65.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(bgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            name,
                            color = textColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // PRODUITS (sans titre inutile)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(products) { product ->
                var isFav by remember { mutableStateOf(favoritesViewModel.isFavorite(product)) }
                Card(
                    Modifier.width(140.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Box {
                        Column {
                            AsyncImage(
                                product.imageUrl, null,
                                Modifier.fillMaxWidth().height(140.dp),
                                contentScale = ContentScale.Crop
                            )
                            Column(Modifier.padding(8.dp)) {
                                Text(
                                    product.name ?: "Produit", 
                                    fontWeight = FontWeight.Bold, 
                                    maxLines = 2,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        IconButton(
                            onClick = { isFav = favoritesViewModel.toggleFavorite(product) },
                            Modifier
                                .align(Alignment.TopEnd)
                                .size(36.dp)
                                .background(Color.White.copy(0.9f), CircleShape)
                        ) {
                            Icon(
                                if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                null,
                                tint = if (isFav) Color(0xFFFF1744) else Color(0xFF757575),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

data class CategoryData(val title: String, val subtitle: String, val imageUrl: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(authManager: AuthManager, onAuthSuccess: () -> Unit = {}) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val authState = authManager.authState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Upermarket" else "Bienvenue",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isLogin) "Connectez-vous pour continuer" else "Créez votre compte",
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                if (!isLogin) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Nom complet") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                }
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Mot de passe") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp)
                )
                if (authState is AuthState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(authState.message, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (isLogin) authManager.signIn(email, password)
                        else authManager.signUp(email, password, name)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(if (isLogin) "Se connecter" else "Créer mon compte", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { isLogin = !isLogin },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(if (isLogin) "Pas encore membre ? S'inscrire" else "Déjà membre ? Se connecter")
        }
    }
}

@Composable
fun ProductDetailModal(
    product: Product,
    onDismiss: () -> Unit,
    favoritesViewModel: FavoritesViewModel? = null,
    cartViewModel: CartViewModel? = null
) {
    ProductDetailScreen(
        product = product,
        onDismiss = onDismiss,
        favoritesViewModel = favoritesViewModel,
        cartViewModel = cartViewModel
    )
}

@Composable
fun FavoritesSheet(
    favoritesViewModel: FavoritesViewModel,
    onProductClick: (Product) -> Unit = {}
) {
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    val haptic = LocalHapticFeedback.current
    
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Mes Favoris", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        if (favoritesViewModel.favorites.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.FavoriteBorder, null, Modifier.size(80.dp), tint = Color(0xFF64B5F6).copy(0.5f))
                Spacer(Modifier.height(16.dp))
                Text("Aucun favori enregistré", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(favoritesViewModel.favorites) { product ->
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Image cliquable avec effet feedback
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE3F2FD))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedProduct = product
                                    }
                            ) {
                                AsyncImage(
                                    product.imageUrl, null,
                                    Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f).clickable { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedProduct = product 
                            }) {
                                Text(product.name ?: "Produit", fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(product.brands ?: "", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64B5F6))
                            }
                            IconButton(onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                favoritesViewModel.removeFavorite(product) 
                            }) {
                                Icon(Icons.Rounded.Favorite, null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Modal détail produit
    selectedProduct?.let { product ->
        ProductDetailModal(
            product = product,
            onDismiss = { selectedProduct = null }
        )
    }
}


@Composable
fun CheckInSuccessScreen(scannedUserId: String?) {
    val context = LocalContext.current
    var visitStats by remember { mutableStateOf<Pair<Int, Int>?>(null) } // (Total, Streak)
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(scannedUserId) {
        if (scannedUserId != null) {
            // 1. Son de succès
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(context, notification).play()

            // 2. Update Firestore (Incrémentation atomique)
            val userRef = db.collection("users").document(scannedUserId)
            userRef.update(
                "visitCount", FieldValue.increment(1),
                "lastVisit", FieldValue.serverTimestamp()
            )

            // Simuler la récupération de la streak pour l'exemple
            visitStats = Pair(12, 5) // Mock: 12 visites total, 5 jours de streak
        }
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = visitStats != null,
            enter = fadeIn() + expandVertically()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(100.dp))
                Text("Bienvenue chez Upermarket !", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)

                Spacer(Modifier.height(16.dp))

                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔥 Streak : ${visitStats?.second} jours", fontWeight = FontWeight.Bold, color = Color.Red)
                        Text("Visite n°${visitStats?.first} cette semaine")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Alerte Promo Ciblée (Mock)
                Surface(color = Color(0xFFFFEB3B), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Warning, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Promo : Votre steak préféré est à -30% au rayon Boucherie !", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// Utilitaire de génération QR
fun generateQRCode(content: String): Bitmap {
    val size = 512
    val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bits[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@Composable
fun MyQrCheckInScreen(userId: String?) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ma Carte Upermarket", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        if (userId != null) {
            val qrBitmap = remember(userId) { generateQRCode("upermarket://checkin/$userId") }
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "QR Code Check-in",
                modifier = Modifier.size(280.dp).clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Présentez ce code à la caisse ou sur une borne pour cumuler vos points.",
                textAlign = TextAlign.Center, color = Color.Gray
            )
        } else {
            Button(onClick = { /* Nav vers Login */ }) {
                Text("Connectez-vous pour voir votre QR")
            }
        }
    }
}





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    authManager: AuthManager,
    cartViewModel: CartViewModel
) {
    val context = LocalContext.current
    val favoritesManager = remember { FavoritesManager(context) }
    val favoritesViewModel = remember { FavoritesViewModel(favoritesManager) }

    val navController = rememberNavController()
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val destinations = Destination.entries
    var showCartSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showFavoritesSheet by remember { mutableStateOf(false) }
    var showShoppingListSheet by remember { mutableStateOf(false) }
    var showBudgetManagerSheet by remember { mutableStateOf(false) }
    val user = authManager.getCurrentUser()

    Scaffold(
        topBar = @Composable {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    Row {
                        // Icône Budget & Préférences (NOUVEAU)
                        IconButton(onClick = { showBudgetManagerSheet = true }) {
                            Icon(
                                Icons.Rounded.Tune,
                                contentDescription = "Budget & Préférences",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        // Icône liste de courses
                        IconButton(onClick = { showShoppingListSheet = true }) {
                            Icon(
                                Icons.Rounded.ChecklistRtl,
                                contentDescription = "Liste de courses",
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Favoris avec badge ROUGE VIF
                    IconButton(onClick = { showFavoritesSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (favoritesViewModel.favoriteCount > 0) {
                                    Badge(containerColor = Color(0xFFFF1744)) {
                                        Text(favoritesViewModel.favoriteCount.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                if (favoritesViewModel.favoriteCount > 0) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favoris",
                                tint = if (favoritesViewModel.favoriteCount > 0) Color(0xFFFF1744) else Color(0xFF212121),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    // Panier NOIR VIF
                    IconButton(onClick = { showCartSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (cartViewModel.itemCount > 0) {
                                    Badge(containerColor = Color(0xFF1976D2)) {
                                        Text(cartViewModel.itemCount.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Rounded.ShoppingCart, 
                                contentDescription = "Panier", 
                                tint = Color(0xFF212121),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    // Avatar utilisateur VISIBLE
                    IconButton(onClick = { showProfileSheet = true }) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(
                                    if (user?.isVip == true)
                                        Brush.linearGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
                                    else
                                        Brush.linearGradient(colors = listOf(Color(0xFF1976D2), Color(0xFF2196F3)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user?.name?.take(1)?.uppercase() ?: "👤",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 17.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = @Composable {
            NavigationBar(
                containerColor = Color.White
            ) {
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
                                contentDescription = destination.contentDescription,
                                tint = if (isSelected) Color(0xFF1976D2) else Color(0xFF424242),
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        alwaysShowLabel = false
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
                HomeScreen(favoritesViewModel = favoritesViewModel)
            }
            composable(Destination.SEARCH.route) {
                SearchScreen(favoritesViewModel = favoritesViewModel, cartViewModel = cartViewModel)
            }
            composable(Destination.SCAN.route) {
                ScanScreen(cartViewModel = cartViewModel, favoritesViewModel = favoritesViewModel)
            }
            composable(Destination.VIP.route) {
            VipScreen(
                    auth = authManager,
                    onVipStatusChanged = {
                        // Forcer la mise à jour de l'UI
                    }
                )
            }
            composable(Destination.SETTINGS.route) {
                SettingsScreen(authManager = authManager)
            }
        }

        // Modal pour le panier
        if (showCartSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                CartSheet(cartViewModel = cartViewModel)
            }
        }

        // Modal pour les favoris
        if (showFavoritesSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFavoritesSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                FavoritesSheet(
                    favoritesViewModel = favoritesViewModel,
                    onProductClick = { product ->
                        // Optionnel : naviguer vers détail produit
                    }
                )
            }
        }

        // Modal pour le profil
        if (showProfileSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProfileSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ProfileScreen(
                    auth = authManager,
                    onVip = {
                        showProfileSheet = false
                        selectedItem = 3 // Index de l'onglet VIP
                        navController.navigate(Destination.VIP.route)
                    },
                    onDismiss = { showProfileSheet = false }
                )
            }
        }
        
        // Modal pour la liste de courses
        if (showShoppingListSheet) {
            ModalBottomSheet(
                onDismissRequest = { showShoppingListSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                ShoppingListSheet(onDismiss = { showShoppingListSheet = false })
            }
        }
        
        // Modal pour Budget & Préférences (NOUVEAU)
        if (showBudgetManagerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBudgetManagerSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                BudgetManagerSheet(
                    onDismiss = { showBudgetManagerSheet = false },
                    cartViewModel = cartViewModel,
                    context = context
                )
            }
        }
    }
}

// ==================== BUDGET MANAGER ====================

data class BudgetPreferences(
    val monthlyBudget: Float = 0f,
    val weeklyBudget: Float = 0f,
    val priceRange: ClosedFloatingPointRange<Float> = 0f..100f,
    val preferredBrands: List<String> = emptyList(),
    val avoidBrands: List<String> = emptyList(),
    val dietaryPreferences: List<String> = emptyList(),
    val sortBy: String = "price_asc" // price_asc, price_desc, nutriscore, name
)

@Composable
fun BudgetManagerSheet(
    onDismiss: () -> Unit,
    cartViewModel: CartViewModel,
    context: Context
) {
    val prefs = context.getSharedPreferences("budget_prefs", Context.MODE_PRIVATE)
    var monthlyBudget by remember { mutableStateOf(prefs.getFloat("monthly_budget", 0f)) }
    var weeklyBudget by remember { mutableStateOf(prefs.getFloat("weekly_budget", 0f)) }
    var sortOption by remember { mutableStateOf(prefs.getString("sort_by", "price_asc") ?: "price_asc") }
    var showBudgetInput by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    
    val currentSpent = cartViewModel.totalPrice
    val budgetProgress = if (weeklyBudget > 0) (currentSpent / weeklyBudget).coerceIn(0f, 1f) else 0f
    val budgetColor = when {
        budgetProgress >= 0.9f -> Color(0xFFFF1744)
        budgetProgress >= 0.7f -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Budget & Préférences",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Organisez vos courses",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, "Fermer", tint = Color.Gray)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Budget actuel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = budgetColor.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Dépenses actuelles",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                        Text(
                            "$${String.format("%.2f", currentSpent)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = budgetColor
                        )
                    }
                    if (weeklyBudget > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "Budget hebdo",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                "$${String.format("%.2f", weeklyBudget)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                if (weeklyBudget > 0) {
                    Spacer(Modifier.height(12.dp))
                    
                    // Barre de progression
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Gray.copy(0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(budgetProgress)
                                .fillMaxHeight()
                                .background(budgetColor)
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    val remaining = weeklyBudget - currentSpent
                    Text(
                        if (remaining >= 0) 
                            "Reste : $${String.format("%.2f", remaining)}"
                        else 
                            "Dépassement : $${String.format("%.2f", -remaining)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (remaining >= 0) Color(0xFF4CAF50) else Color(0xFFFF1744)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Configuration du budget
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "💰 Définir un budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                
                // Budget hebdomadaire
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Budget hebdomadaire", fontWeight = FontWeight.Medium)
                        Text(
                            if (weeklyBudget > 0) "$${String.format("%.2f", weeklyBudget)}" else "Non défini",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = { showBudgetInput = !showBudgetInput },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text(if (weeklyBudget > 0) "Modifier" else "Définir")
                    }
                }
                
                if (showBudgetInput) {
                    Spacer(Modifier.height(12.dp))
                    var budgetText by remember { mutableStateOf(weeklyBudget.toString()) }
                    
                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Montant ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            weeklyBudget = budgetText.toFloatOrNull() ?: 0f
                            prefs.edit().putFloat("weekly_budget", weeklyBudget).apply()
                            showBudgetInput = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enregistrer")
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Options de tri
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "🔄 Trier les produits",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            getSortOptionLabel(sortOption),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    TextButton(onClick = { showSortOptions = !showSortOptions }) {
                        Text("Changer")
                    }
                }
                
                if (showSortOptions) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    
                    val sortOptions = listOf(
                        "price_asc" to "⬆️ Prix croissant",
                        "price_desc" to "⬇️ Prix décroissant",
                        "nutriscore" to "⭐ Nutri-Score",
                        "name" to "🔤 Nom (A-Z)"
                    )
                    
                    sortOptions.forEach { (value, label) ->
                        Surface(
                            onClick = {
                                sortOption = value
                                prefs.edit().putString("sort_by", value).apply()
                                showSortOptions = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (sortOption == value) Color(0xFF2196F3).copy(0.1f) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label)
                                if (sortOption == value) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        null,
                                        tint = Color(0xFF2196F3),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Préférences alimentaires
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "🌱 Préférences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                
                val preferences = listOf(
                    "Bio" to "🌱",
                    "Végétarien" to "🥗",
                    "Vegan" to "🌿",
                    "Sans gluten" to "🌾",
                    "Sans lactose" to "🥛"
                )
                
                preferences.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (label, emoji) ->
                            Surface(
                                onClick = { /* TODO: Implémenter */ },
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF4CAF50).copy(0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(label, fontSize = 12.sp)
                                }
                            }
                        }
                        if (row.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

fun getSortOptionLabel(option: String): String = when (option) {
    "price_asc" -> "Prix croissant"
    "price_desc" -> "Prix décroissant"
    "nutriscore" -> "Nutri-Score"
    "name" -> "Nom (A-Z)"
    else -> "Non défini"
}

@Composable
fun ShoppingListSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("shopping_list", Context.MODE_PRIVATE)
    var items by remember { mutableStateOf(getShoppingList(context)) }
    var newItemText by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Ma liste de courses",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            if (items.isNotEmpty()) {
                TextButton(onClick = {
                    items = emptyList()
                    saveShoppingList(context, items)
                }) {
                    Text("Tout effacer", color = Color(0xFFFF1744))
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Champ d'ajout
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ajouter un produit...") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1976D2),
                    unfocusedBorderColor = Color(0xFFBDBDBD)
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newItemText.isNotBlank()) {
                            items = items + ShoppingListItem(newItemText, false)
                            saveShoppingList(context, items)
                            newItemText = ""
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    }
                )
            )
            
            IconButton(
                onClick = {
                    if (newItemText.isNotBlank()) {
                        items = items + ShoppingListItem(newItemText, false)
                        saveShoppingList(context, items)
                        newItemText = ""
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF1976D2), CircleShape)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Ajouter",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Liste des items
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.ChecklistRtl,
                        null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBDBDBD)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Aucun article",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF757575)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, false)
            ) {
                items(items.size) { index ->
                    val item = items[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.checked) Color(0xFFF5F5F5) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = item.checked,
                                onCheckedChange = { checked ->
                                    items = items.toMutableList().apply {
                                        this[index] = item.copy(checked = checked)
                                    }
                                    saveShoppingList(context, items)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                colors = androidx.compose.material3.CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF4CAF50)
                                )
                            )
                            Text(
                                item.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                textDecoration = if (item.checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                color = if (item.checked) Color(0xFF9E9E9E) else Color(0xFF212121)
                            )
                            IconButton(
                                onClick = {
                                    items = items.filterIndexed { i, _ -> i != index }
                                    saveShoppingList(context, items)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Supprimer",
                                    tint = Color(0xFF757575),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ShoppingListItem(val name: String, val checked: Boolean)

fun getShoppingList(context: Context): List<ShoppingListItem> {
    val prefs = context.getSharedPreferences("shopping_list", Context.MODE_PRIVATE)
    val json = prefs.getString("items", "[]")
    return try {
        Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<List<ShoppingListItem>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }
}

fun saveShoppingList(context: Context, items: List<ShoppingListItem>) {
    val prefs = context.getSharedPreferences("shopping_list", Context.MODE_PRIVATE)
    val json = Gson().toJson(items)
    prefs.edit().putString("items", json).apply()
}

@Composable
fun CartSheet(cartViewModel: CartViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var isPhysicalStore by rememberSaveable { mutableStateOf(true) }
    var showRecap by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Mon Panier",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (cartViewModel.cartItems.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "${cartViewModel.itemCount} article${if (cartViewModel.itemCount > 1) "s" else ""}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Mode toggle: Magasin / Drive ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(Modifier.fillMaxWidth().padding(4.dp)) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isPhysicalStore = true
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPhysicalStore) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (isPhysicalStore) 1.dp else 0.dp
                ) {
                    Row(
                        Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Store, null,
                            Modifier.size(18.dp),
                            tint = if (isPhysicalStore) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Magasin",
                            fontWeight = if (isPhysicalStore) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (isPhysicalStore) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isPhysicalStore = false
                        },
                    shape = RoundedCornerShape(12.dp),
                    color = if (!isPhysicalStore) MaterialTheme.colorScheme.surface else Color.Transparent,
                    shadowElevation = if (!isPhysicalStore) 1.dp else 0.dp
                ) {
                    Row(
                        Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.LocalShipping, null,
                            Modifier.size(18.dp),
                            tint = if (!isPhysicalStore) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Drive",
                            fontWeight = if (!isPhysicalStore) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (!isPhysicalStore) MaterialTheme.colorScheme.onSurface else Color.Gray
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (cartViewModel.cartItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Rounded.ShoppingCart, null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.Gray.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Votre panier est vide",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Scannez des produits pour les ajouter",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray.copy(0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cartViewModel.cartItems) { cartItem ->
                    CartItemCard(
                        cartItem = cartItem,
                        onQuantityChange = { newQuantity ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            cartViewModel.updateQuantity(cartItem.product, newQuantity)
                        },
                        onRemove = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            cartViewModel.removeFromCart(cartItem.product)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color.Gray.copy(0.12f))
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "${cartViewModel.itemCount} article${if (cartViewModel.itemCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${cartViewModel.cartItems.size} produit${if (cartViewModel.cartItems.size > 1) "s" else ""} différent${if (cartViewModel.cartItems.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                if (isPhysicalStore) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showRecap = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("J'ai fini mes courses", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // TODO: Remplacer par le lien affilié
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.example.com/drive"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Rounded.LocalShipping, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Commander en drive", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ── Recap dialog ──
    if (showRecap) {
        CartRecapDialog(
            itemCount = cartViewModel.itemCount,
            productCount = cartViewModel.cartItems.size,
            onDismiss = { showRecap = false },
            onConfirm = {
                cartViewModel.clearCart()
                showRecap = false
            }
        )
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    var showProductDetail by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image cliquable avec feedback
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showProductDetail = true
                    }
            ) {
                AsyncImage(
                    model = cartItem.product.imageUrl,
                    contentDescription = cartItem.product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.ic_launcher_foreground)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.product.name ?: "Produit",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!cartItem.product.brands.isNullOrBlank()) {
                    Text(
                        text = cartItem.product.brands,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64B5F6)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onQuantityChange(cartItem.quantity - 1) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Remove, "Diminuer", Modifier.size(18.dp))
                        }
                    }

                    Text(
                        text = cartItem.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onQuantityChange(cartItem.quantity + 1) },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Add, "Augmenter", Modifier.size(18.dp), tint = Color.White)
                        }
                    }
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "Supprimer",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Modal détail produit
    if (showProductDetail) {
        ProductDetailModal(
            product = cartItem.product,
            onDismiss = { showProductDetail = false }
        )
    }
}

@Composable
fun CartRecapDialog(
    itemCount: Int,
    productCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        @Suppress("DEPRECATION")
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 80, 60, 120), -1)
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF2E7D32).copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle, null,
                        modifier = Modifier.size(52.dp),
                        tint = Color(0xFF2E7D32)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    "Courses terminées !",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "$productCount produit${if (productCount > 1) "s" else ""} · $itemCount article${if (itemCount > 1) "s" else ""}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(20.dp))

                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("\uD83C\uDF3F", fontSize = 22.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Économies du jour",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF2E7D32).copy(0.7f)
                            )
                            Text(
                                "-${String.format("%.2f", itemCount * 0.47)} €",
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Sauvegarder & Fermer", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuer les courses", color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel? = null
) {
    val context = LocalContext.current
    val offlineManager: OfflineProductManager = remember {
        OfflineProductManager(context, OpenFoodFactsApi.create())
    }
    val viewModel: BarcodeViewModel = remember {
        BarcodeViewModel(offlineManager, context)
    }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val state = viewModel.barScanState

    // Gestion des permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(state) {
        if (state is BarScanState.QrCodeDetected) {
            showBottomSheet = true
        }
    }

    // Demander la permission au chargement
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var showTestScreen by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreview(vm = viewModel, isTorchOn = isTorchOn)
            ViewfinderOverlay()

            // Test button (top left)
            IconButton(
                onClick = { showTestScreen = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(Color(0xFF2196F3).copy(alpha = 0.8f), CircleShape)
            ) {
                Text(
                    "🧪",
                    fontSize = 20.sp,
                    color = Color.White
                )
            }
            
            // Torch toggle
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isTorchOn = !isTorchOn
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        if (isTorchOn) Color.Yellow.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
            ) {
                Icon(
                    if (isTorchOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = if (isTorchOn) "Désactiver le flash" else "Activer le flash",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Scan hint
            Text(
                "Placez le QR code dans le cadre",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color.Black.copy(0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Permission de caméra requise",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pour scanner des QR codes, veuillez autoriser l'accès à la caméra",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { launcher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Autoriser la caméra")
                }
            }
        }

        if (showBottomSheet && state is BarScanState.QrCodeDetected) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    viewModel.resetState()
                },
                sheetState = sheetState
            ) {
                QrCodeResultDisplay(
                    qrContent = state.content,
                    onDismiss = {
                        showBottomSheet = false
                        viewModel.resetState()
                    }
                )
            }
        }

        // Loading overlay
        if (state is BarScanState.Loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Scan en cours…",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // Error overlay
        if (state is BarScanState.ScanError) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(
                        Color.Red.copy(alpha = 0.9f), 
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.Warning, 
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        state.message,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // Test Barcode Simulator Modal
        if (showTestScreen) {
            ModalBottomSheet(
                onDismissRequest = { showTestScreen = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                TestBarcodeScreen(
                    onBarcodeSelected = { barcode ->
                        showTestScreen = false
                        viewModel.onBarCodeDetected(barcode)
                    },
                    onDismiss = { showTestScreen = false }
                )
            }
        }
    }
}


@Composable
fun ViewfinderOverlay() {
    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        // Cadre carré pour QR code
        val squareSize = w * 0.7f
        val l = (w - squareSize) / 2
        val t = (h - squareSize) / 2

        // Background dimming
        drawRect(Color.Black.copy(0.6f))

        // Zone transparente carrée
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(l, t),
            size = Size(squareSize, squareSize),
            cornerRadius = CornerRadius(16.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Bordure du cadre
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val cornerColor = Color(0xFF4CAF50)

        // Coins arrondis avec style moderne
        // Top-Left
        drawLine(cornerColor, Offset(l, t + cornerLength), Offset(l, t), strokeWidth)
        drawLine(cornerColor, Offset(l, t), Offset(l + cornerLength, t), strokeWidth)
        
        // Top-Right
        drawLine(cornerColor, Offset(l + squareSize - cornerLength, t), Offset(l + squareSize, t), strokeWidth)
        drawLine(cornerColor, Offset(l + squareSize, t), Offset(l + squareSize, t + cornerLength), strokeWidth)
        
        // Bottom-Left
        drawLine(cornerColor, Offset(l, t + squareSize - cornerLength), Offset(l, t + squareSize), strokeWidth)
        drawLine(cornerColor, Offset(l, t + squareSize), Offset(l + cornerLength, t + squareSize), strokeWidth)
        
        // Bottom-Right
        drawLine(cornerColor, Offset(l + squareSize, t + squareSize - cornerLength), Offset(l + squareSize, t + squareSize), strokeWidth)
        drawLine(cornerColor, Offset(l + squareSize - cornerLength, t + squareSize), Offset(l + squareSize, t + squareSize), strokeWidth)
    }
}

// ==================== VIP & PROFILE ====================

@Composable
fun VipScreen(auth: AuthManager, onVipStatusChanged: (() -> Unit)? = null) {
    val user = auth.getCurrentUser(); val isVip = user?.isVip ?: false
    Column(Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
        Text("Espace VIP", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Card(Modifier.fillMaxWidth().padding(vertical = 16.dp), colors = CardDefaults.cardColors(Color(0xFF2196F3))) {
            Column(Modifier.padding(24.dp)) {
                Text(if (isVip) "Vous êtes SUPER VIP !" else "Devenez SUPER VIP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Avantages exclusifs et -10% immédiat.", color = Color.White.copy(0.8f))
                Button(onClick = { auth.updateVipStatus(!isVip, null) }, Modifier.padding(top = 16.dp), colors = ButtonDefaults.buttonColors(Color.White)) {
                    Text(if (isVip) "Gérer l'abonnement" else "S'abonner - 4,99€", color = Color(0xFF2196F3))
                }
            }
        }
        Text("Rayons VIP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        listOf("Caviar & Luxe", "Grands Crus", "Chocolats de Maître").forEach {
            Card(Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(Color.Yellow.copy(0.2f)), Alignment.Center) { Icon(Icons.Rounded.Star, null, tint = Color(0xFFFFA500)) }
                    Text(it, Modifier.padding(start = 16.dp), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    if (!isVip) Icon(Icons.Rounded.Lock, null, tint = Color.Gray)
                }
            }
        }
    }
}

@Composable fun ProfileScreen(auth: AuthManager, onVip: () -> Unit, onDismiss: () -> Unit) {
    val user = auth.getCurrentUser()
    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(100.dp).clip(CircleShape).background(if (user?.isVip == true) Brush.linearGradient(listOf(Color.Yellow, Color.Red)) else Brush.linearGradient(listOf(Color.Blue, Color.Cyan))), Alignment.Center) {
            Text(user?.name?.take(1) ?: "?", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        }
        Text(user?.name ?: "Utilisateur", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 16.dp))
        Button(onVip, Modifier.fillMaxWidth().padding(top = 24.dp)) { Text("VIP") }
        Button(onDismiss, Modifier.fillMaxWidth()) { Text("Fermer") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable 
fun SearchScreen(favoritesViewModel: FavoritesViewModel? = null, cartViewModel: CartViewModel? = null) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchHistory by remember { mutableStateOf(getSearchHistory(context)) }
    var showHistory by remember { mutableStateOf(false) }
    var resultCount by remember { mutableStateOf(0) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    // Suggestions populaires étendues
    val popularSearches = listOf(
        "Coca Cola", "Nutella", "Danette", "Evian", "Kinder", 
        "Président", "Harry's", "Ferrero", "Danone", "Badoit",
        "Fromage", "Chocolat", "Pain", "Pasta", "Quinoa", "Salmon", "Chicken", "Yogurt"
    )
    
    val haptic = LocalHapticFeedback.current
    
    fun performSearch(query: String) {
        Log.d("SearchScreen", "=== PERFORM SEARCH CALLED ===")
        Log.d("SearchScreen", "Query: '$query'")
        Log.d("SearchScreen", "Query length: ${query.length}")
        Log.d("SearchScreen", "Query isBlank: ${query.isBlank()}")
        
        if (query.isBlank()) {
            Log.w("SearchScreen", "Query is blank, aborting")
            return
        }
        
        isSearching = true
        searchHistory = addToSearchHistory(context, query, searchHistory)
        
        Log.d("SearchScreen", "Starting coroutine...")
        
        // Recherche directe OpenFoodFacts avec TIMEOUT ÉNORMÉ
        coroutineScope.launch {
            try {
                Log.d("SearchScreen", "Creating API instance...")
                val offApi = OpenFoodFactsApi.create()
                Log.d("SearchScreen", "API created successfully")
                Log.d("SearchScreen", "Calling searchProducts with query='$query', pageSize=100")
                
                val response = offApi.searchProducts(
                    searchTerms = query,
                    pageSize = 100
                )
                
                Log.d("SearchScreen", "API response received!")
                Log.d("SearchScreen", "Products count: ${response.products.size}")
                
                if (response.products.isEmpty()) {
                    Log.w("SearchScreen", "NO PRODUCTS FOUND for query '$query'")
                } else {
                    Log.d("SearchScreen", "First product: ${response.products.first().name}")
                }
                
                searchResults = response.products
                resultCount = response.products.size
                isSearching = false
                
                Log.d("SearchScreen", "Search complete! Showing ${searchResults.size} results")
            } catch (e: Exception) {
                Log.e("SearchScreen", "=== SEARCH FAILED ===")
                Log.e("SearchScreen", "Error type: ${e.javaClass.simpleName}")
                Log.e("SearchScreen", "Error message: ${e.message}")
                Log.e("SearchScreen", "Stack trace:", e)
                
                searchResults = emptyList()
                resultCount = 0
                isSearching = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header avec SearchBar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = if (showHistory) 0.dp else 2.dp
        ) {
            Column(Modifier.padding(16.dp)) {
                // Titre
                Text(
                    "Rechercher",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF212121),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // CHAMP DE RECHERCHE SIMPLE ET FONCTIONNEL
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Rechercher un produit", color = Color(0xFFBDBDBD)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, null, tint = Color(0xFF2196F3))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                searchResults = emptyList()
                            }) {
                                Icon(Icons.Rounded.Close, null, tint = Color(0xFF2196F3))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2196F3),
                        unfocusedBorderColor = Color(0xFF90CAF9),
                        focusedContainerColor = Color(0xFFE3F2FD),
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            performSearch(searchQuery)
                        }
                    )
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Bouton de recherche VISIBLE
                Button(
                    onClick = { performSearch(searchQuery) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = searchQuery.isNotBlank()
                ) {
                    Icon(Icons.Filled.Search, null, Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "CHERCHER",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Résultats de recherche
        if (isSearching) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Recherche en cours...")
                }
            }
        } else if (searchResults.isNotEmpty()) {
            // Résultats trouvés
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF2196F3).copy(0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF2196F3), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "${searchResults.size} résultats",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                        }
                    }
                }
                
                items(searchResults) { product ->
                    var isFavorite by remember { mutableStateOf(favoritesViewModel?.isFavorite(product) ?: false) }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedProduct = product },
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Image produit - CLIQUABLE avec fallback moderne
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE3F2FD))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedProduct = product
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!product.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    // Fallback moderne si pas d'image
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.ShoppingCart,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = Color.Gray.copy(0.3f)
                                        )
                                        Text(
                                            product.name?.take(10) ?: "?",
                                            fontSize = 8.sp,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                            
                            Spacer(Modifier.width(16.dp))
                            
                            // Infos produit
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    product.name ?: "Produit",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                
                                if (!product.brands.isNullOrBlank()) {
                                    Text(
                                        product.brands,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF64B5F6),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                
                                if (!product.quantity.isNullOrBlank()) {
                                    Text(
                                        product.quantity,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF90CAF9)
                                    )
                                }
                                
                                // Badges nutritionnels
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    
                                    product.nutriscore?.let { score ->
                                        Surface(
                                            color = nutriScoreColor(score),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    score.uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                    
                                    product.ecoscore?.let { score ->
                                        Surface(
                                            color = ecoScoreColor(score),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    score.uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            // Bouton favoris
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    isFavorite = favoritesViewModel?.toggleFavorite(product) ?: false
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if (isFavorite) Color.Red.copy(0.1f) else Color.Gray.copy(0.1f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                                    tint = if (isFavorite) Color.Red else Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else if (searchQuery.isNotEmpty() && !isSearching) {
            // Aucun résultat
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray.copy(0.3f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Aucun produit trouvé",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Essayez avec d'autres mots-clés",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray.copy(0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // État initial - suggestions
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(0.1f))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Explore,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "🔍 Recherche Libre",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2196F3)
                                    )
                                    Text(
                                        "Trouvez n'importe quel produit alimentaire",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF2196F3).copy(0.7f)
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = Color(0xFF2196F3).copy(0.2f))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "TAPEZ CE QUE VOUS VOULEZ :",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2196F3)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "• 'tomate' - pour trouver des tomates\n• 'fromage chevre' - pour du fromage de chèvre\n• 'chocolat milka' - pour du chocolat Milka\n• 'lait bio' - pour du lait biologique\n• N'IMPORTE QUEL aliment que vous voulez !",
                                color = Color(0xFF2196F3).copy(0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B).copy(0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🔥", fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "La recherche utilise OpenFoodFacts avec MILLIONS de produits réels !",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
                
                item {
                    Text(
                        "Recherches populaires",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Chips des recherches populaires
                items(popularSearches.chunked(2)) { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { search ->
                            Surface(
                                onClick = {
                                    searchQuery = search
                                    performSearch(search)
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(0.3f),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    search,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        // Padding pour les lignes incomplètes
                        if (rowItems.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        
        // Modal de détail produit
        selectedProduct?.let { product ->
            ProductDetailScreen(
                product = product,
                onDismiss = { selectedProduct = null },
                favoritesViewModel = favoritesViewModel,
                cartViewModel = cartViewModel
            )
        }
    }
}

// Gestion de l'historique de recherche
fun getSearchHistory(context: Context): List<String> {
    val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    val historyJson = prefs.getString("history", "[]")
    return try {
        Gson().fromJson(historyJson, object : com.google.gson.reflect.TypeToken<List<String>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }
}

fun addToSearchHistory(context: Context, query: String, currentHistory: List<String>): List<String> {
    val newHistory = (listOf(query) + currentHistory).distinct().take(10)
    val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    prefs.edit().putString("history", Gson().toJson(newHistory)).apply()
    return newHistory
}
@Composable fun SettingsScreen(authManager: AuthManager? = null) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Paramètres") } }

// ==================== CAMERA UTILS ====================

@Composable
fun CameraPreview(vm: BarcodeViewModel, isTorchOn: Boolean = false) {
    val ctx = LocalContext.current
    val life = LocalLifecycleOwner.current
    val exec = remember { Executors.newSingleThreadExecutor() }
    var cameraInstance by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    LaunchedEffect(isTorchOn) {
        cameraInstance?.cameraControl?.enableTorch(isTorchOn)
    }

    AndroidView(
        factory = { context ->
            val previewView = PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            ProcessCameraProvider.getInstance(context).addListener({
                val prov = ProcessCameraProvider.getInstance(context).get()
                val pre = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                @Suppress("DEPRECATION")
                val ana = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(android.util.Size(1280, 720))
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                    .also {
                        it.setAnalyzer(exec, BarcodeAnalyzer { barcode ->
                            vm.onBarCodeDetected(barcode)
                        })
                    }
                try {
                    prov.unbindAll()
                    val cam = prov.bindToLifecycle(
                        life, CameraSelector.DEFAULT_BACK_CAMERA, pre, ana
                    )
                    cameraInstance = cam
                    // Autofocus + exposition rapide, puis retour en continu
                    val afPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
                        .createPoint(0.5f, 0.5f)
                    cam.cameraControl.startFocusAndMetering(
                        FocusMeteringAction.Builder(
                            afPoint,
                            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                        ).setAutoCancelDuration(1, java.util.concurrent.TimeUnit.SECONDS)
                         .build()
                    )
                } catch (e: Exception) {
                    Log.e("Camera", "Binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

sealed interface BarScanState {
    object Idle : BarScanState
    data class QrCodeDetected(val content: String) : BarScanState
    object Loading : BarScanState
    data class ScanError(val message: String) : BarScanState
}

class BarcodeViewModel(private val mgr: OfflineProductManager?, private val context: Context) : ViewModel() {
    var barScanState by mutableStateOf<BarScanState>(BarScanState.Idle); private set
    private var lastScannedQrCode: String? = null
    private var processingQrCode: String? = null
    private var isProcessing = false

    fun onBarCodeDetected(qrContent: String) {
        if (qrContent.isBlank()) return
        
        // STOP : Un scan est déjà en cours
        if (isProcessing || processingQrCode == qrContent) {
            return
        }
        
        // STOP : Ce code a déjà été scanné avec succès (ne pas rescanner jusqu'au reset)
        if (qrContent == lastScannedQrCode && barScanState is BarScanState.QrCodeDetected) {
            return
        }

        Log.d("BarcodeVM", "🔍 QR CODE DÉTECTÉ: $qrContent")
        processingQrCode = qrContent
        isProcessing = true

        viewModelScope.launch {
            barScanState = BarScanState.Loading
            try {
                vibrate()
                lastScannedQrCode = qrContent
                barScanState = BarScanState.QrCodeDetected(qrContent)
                Log.d("BarcodeVM", "✅ QR CODE: $qrContent")
                // Garde le succès jusqu'au reset (pas de retour auto à Idle)
            } catch (e: Exception) {
                barScanState = BarScanState.ScanError("Erreur: ${e.message}")
                Log.e("BarcodeVM", "⚠️ ERREUR", e)
                
                // Retour automatique après erreur
                launch {
                    delay(2000)
                    if (barScanState is BarScanState.ScanError) {
                        isProcessing = false
                        processingQrCode = null
                        barScanState = BarScanState.Idle
                    }
                }
            }
        }
    }
    fun resetState() { 
        lastScannedQrCode = null
        processingQrCode = null
        isProcessing = false
        barScanState = BarScanState.Idle 
    }
    private fun vibrate() {
        val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) v.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        else v.vibrate(150)
    }
}

typealias BarcodeListener = (barcode: String) -> Unit

class BarcodeAnalyzer(private val barcodeListener: BarcodeListener) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )
    private var lastAnalysisTime = 0L
    private var lastDetectedCode: String? = null
    private var lastDetectedTime = 0L
    private val analysisIntervalMs = 300L // Analyse stable (300ms)
    private val debounceMs = 1000L // Attendre 1s avant de re-détecter le même code

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        
        // Throttle de l'analyse pour éviter le clignotement
        if (currentTime - lastAnalysisTime < analysisIntervalMs) {
            imageProxy.close()
            return
        }
        
        lastAnalysisTime = currentTime
        
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val firstBarcode = barcodes.firstOrNull()
                    firstBarcode?.rawValue?.let { code ->
                        if (code.isNotBlank()) {
                            // Debounce : ne pas envoyer le même code plusieurs fois
                            if (code != lastDetectedCode || currentTime - lastDetectedTime > debounceMs) {
                                Log.d("BarcodeAnalyzer", "✓ DÉTECTÉ: $code")
                                lastDetectedCode = code
                                lastDetectedTime = currentTime
                                barcodeListener(code)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("BarcodeAnalyzer", "✗ Erreur ML Kit: ${e.message}")
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

// ==================== QR CODE RESULT DISPLAY ====================

@Composable
fun QrCodeResultDisplay(
    qrContent: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Header avec icône QR code
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFF4CAF50).copy(0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📦", fontSize = 32.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "QR Code Scanné",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Contenu détecté",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, "Fermer", tint = Color.Gray)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Contenu du QR code
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Contenu :",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    qrContent,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(Modifier.height(20.dp))
        
        // Informations supplémentaires
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3).copy(0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Scan réussi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "${qrContent.length} caractères détectés",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Boutons d'action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Copier
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("QR Code", qrContent)
                    clipboard.setPrimaryClip(clip)
                },
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("📋 Copier", fontWeight = FontWeight.Bold)
            }
            
            // Fermer
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Fermer", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==================== PRODUCT SCAN DETAIL ====================

@Composable
fun ProductScanDetail(
    product: Product,
    isFromCache: Boolean = false,
    favoritesViewModel: FavoritesViewModel? = null,
    onAddToCart: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(favoritesViewModel?.isFavorite(product) ?: false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        // Product header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE3F2FD)),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    product.name ?: "Produit inconnu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!product.brands.isNullOrBlank()) {
                    Text(
                        product.brands,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                if (!product.quantity.isNullOrBlank()) {
                    Text(
                        product.quantity,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Score badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            product.nutriscore?.let { score ->
                ScoreBadge(
                    label = "Nutri-Score",
                    value = score.uppercase(),
                    color = nutriScoreColor(score)
                )
            }
            product.ecoscore?.let { score ->
                ScoreBadge(
                    label = "Eco-Score",
                    value = score.uppercase(),
                    color = ecoScoreColor(score)
                )
            }
            product.novaGroup?.let { nova ->
                ScoreBadge(
                    label = "Nova",
                    value = nova.toString(),
                    color = novaColor(nova)
                )
            }
        }

        // Ingredients
        if (!product.ingredients.isNullOrBlank()) {
            Spacer(Modifier.height(20.dp))
            Text(
                "Ingrédients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                product.ingredients,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(24.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Add to cart
            Button(
                onClick = onAddToCart,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.ShoppingCart, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ajouter au panier", fontWeight = FontWeight.Bold)
            }

            // Favorite toggle
            if (favoritesViewModel != null) {
                IconButton(
                    onClick = {
                        isFavorite = favoritesViewModel.toggleFavorite(product)
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            if (isFavorite) Color.Red.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favoris",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = color,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    value,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

fun nutriScoreColor(score: String): Color = when (score.lowercase()) {
    "a" -> Color(0xFF038141)
    "b" -> Color(0xFF85BB2F)
    "c" -> Color(0xFFFECB02)
    "d" -> Color(0xFFEE8100)
    "e" -> Color(0xFFE63E11)
    else -> Color.Gray
}

fun ecoScoreColor(score: String): Color = when (score.lowercase()) {
    "a" -> Color(0xFF1E8F4E)
    "b" -> Color(0xFF2ECC71)
    "c" -> Color(0xFFF1C40F)
    "d" -> Color(0xFFE67E22)
    "e" -> Color(0xFFE74C3C)
    else -> Color.Gray
}

fun novaColor(nova: Int): Color = when (nova) {
    1 -> Color(0xFF038141)
    2 -> Color(0xFF85BB2F)
    3 -> Color(0xFFEE8100)
    4 -> Color(0xFFE63E11)
    else -> Color.Gray
}

@Composable fun NutriScoreBadgeLarge(score: String) {
    val color = when (score.lowercase()) {
        "a" -> Color(0xFF038141)
        "b" -> Color(0xFF85BB2F)
        "c" -> Color(0xFFFECB02)
        "d" -> Color(0xFFEE8100)
        "e" -> Color(0xFFE63E11)
        else -> Color.Gray
    }
    Surface(color = color, shape = RoundedCornerShape(6.dp)) {
        Text(
            score.uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
        )
    }
}
