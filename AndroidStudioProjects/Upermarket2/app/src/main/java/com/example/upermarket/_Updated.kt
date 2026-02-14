package com.example.upermarket

import android.Manifest
import android.annotation.SuppressLint
import android.os.*
import android.util.Log
import android.view.ViewGroup
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.Bitmap
import android.media.RingtoneManager
import com.google.gson.Gson
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.upermarket.ui.theme.UpermarketTheme
import com.google.mlkit.vision.barcode.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

// ==================== AUTH SYSTEM ====================

sealed class AuthState {
    object Idle : AuthState(); object Loading : AuthState(); object NotAuthenticated : AuthState()
    data class Authenticated(val user: User) : AuthState(); data class Error(val message: String) : AuthState()
}
data class User(val uid: String = "", val name: String = "", val email: String = "", val isVip: Boolean = false)

class AuthManager(context: Context) {
    private val auth = FirebaseAuth.getInstance(); private val db = FirebaseFirestore.getInstance()
    var authState by mutableStateOf<AuthState>(AuthState.Idle); private set
    init { auth.addAuthStateListener { if (it.currentUser != null) fetchUserData(it.currentUser!!.uid) else authState = AuthState.NotAuthenticated } }
    private fun fetchUserData(uid: String) { 
        authState = AuthState.Loading
        db.collection("users").document(uid).get().addOnSuccessListener { 
            authState = AuthState.Authenticated(it.toObject(User::class.java) ?: User(uid=uid)) 
        }.addOnFailureListener {
            authState = AuthState.Authenticated(User(uid=uid))
        }
    }
    fun signIn(email: String, pass: String) { 
        authState = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass).addOnFailureListener { authState = AuthState.Error(it.localizedMessage ?: "Erreur") } 
    }
    fun signUp(email: String, pass: String, name: String) { 
        authState = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass).addOnSuccessListener { res ->
            val u = User(res.user!!.uid, name, email)
            db.collection("users").document(u.uid).set(u).addOnSuccessListener { fetchUserData(u.uid) }
        }.addOnFailureListener { authState = AuthState.Error(it.localizedMessage ?: "Erreur") }
    }
    fun signOut() { auth.signOut(); authState = AuthState.NotAuthenticated }
    fun getCurrentUser(): User? = (authState as? AuthState.Authenticated)?.user
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { UpermarketTheme { MainApp() } }
    }
}

@Composable
fun MainApp() {
    val context = LocalContext.current.applicationContext
    val authManager = remember { AuthManager(context) }
    
    when (val state = authManager.authState) {
        is AuthState.Idle, is AuthState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is AuthState.NotAuthenticated -> AuthScreen(authManager)
        is AuthState.Authenticated -> {
            val cartViewModel = remember(state.user.uid) { CartViewModel(context, state.user.uid) }
            MainAppContent(authManager, cartViewModel)
        }
        is AuthState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { 
            Column(horizontalAlignment = Alignment.CenterHorizontally) { 
                Text(state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
                Button(onClick = { authManager.signOut() }) { Text("Retour") } 
            } 
        }
    }
}

// ==================== VIEWMODELS & MANAGERS ====================

data class CartItem(val product: Product, val quantity: Int, val price: Float)

class CartViewModel(context: Context, private val uid: String) : ViewModel() {
    private val prefs = context.getSharedPreferences("cart_$uid", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _cartItems = mutableStateOf<List<CartItem>>(loadCart())
    val cartItems: List<CartItem> get() = _cartItems.value
    val itemCount: Int get() = _cartItems.value.sumOf { it.quantity }
    val totalPrice: Float get() = _cartItems.value.sumOf { (it.quantity * it.price).toDouble() }.toFloat()
    
    var userMaxBudget by mutableStateOf(prefs.getFloat("max_budget", 100f))
    var selectedDrive by mutableStateOf(prefs.getString("selected_drive", "Sélectionner un Drive")!!)
    var selectedDriveLogo by mutableIntStateOf(prefs.getInt("selected_drive_logo", 0))

    private fun saveCart() { 
        prefs.edit().putString("items", gson.toJson(_cartItems.value))
            .putFloat("max_budget", userMaxBudget)
            .putString("selected_drive", selectedDrive)
            .putInt("selected_drive_logo", selectedDriveLogo)
            .apply() 
    }
    
    private fun loadCart(): List<CartItem> {
        val json = prefs.getString("items", null) ?: return emptyList()
        return try { gson.fromJson(json, object : com.google.gson.reflect.TypeToken<List<CartItem>>() {}.type) } catch (e: Exception) { emptyList() }
    }

    fun addToCart(product: Product, initialPrice: Float = 0f) {
        val id = product.code ?: "${product.name}_${product.brands}"
        val existing = _cartItems.value.find { (it.product.code ?: "${it.product.name}_${it.product.brands}") == id }
        if (existing != null) {
            _cartItems.value = _cartItems.value.map { if ((it.product.code ?: "${it.product.name}_${it.product.brands}") == id) it.copy(quantity = it.quantity + 1) else it }
        } else {
            _cartItems.value = _cartItems.value + CartItem(product, 1, initialPrice)
        }
        saveCart()
    }

    fun updatePrice(product: Product, newPrice: Float) {
        val id = product.code ?: "${product.name}_${product.brands}"
        _cartItems.value = _cartItems.value.map { if ((it.product.code ?: "${it.product.name}_${it.product.brands}") == id) it.copy(price = newPrice) else it }
        saveCart()
    }

    fun removeFromCart(product: Product) { 
        val id = product.code ?: "${product.name}_${product.brands}"
        _cartItems.value = _cartItems.value.filter { (it.product.code ?: "${it.product.name}_${it.product.brands}") != id } 
        saveCart()
    }
    
    fun updateQuantity(product: Product, quantity: Int) { 
        if (quantity <= 0) removeFromCart(product) 
        else { 
            val id = product.code ?: "${product.name}_${product.brands}"
            _cartItems.value = _cartItems.value.map { if ((it.product.code ?: "${it.product.name}_${it.product.brands}") == id) it.copy(quantity = quantity) else it } 
        } 
        saveCart()
    }
    fun clearCart() { _cartItems.value = emptyList(); saveCart() }
    fun updateBudget(b: Float) { userMaxBudget = b; saveCart() }
    fun updateDrive(name: String, logo: Int) { selectedDrive = name; selectedDriveLogo = logo; saveCart() }
}

class FavoritesViewModel(private val mgr: FavoritesManager) : ViewModel() {
    private val _favorites = mutableStateOf<List<Product>>(emptyList())
    val favorites: List<Product> get() = _favorites.value
    init { loadFavorites() }
    fun loadFavorites() { _favorites.value = mgr.getFavorites() }
    fun toggleFavorite(p: Product): Boolean { val res = mgr.toggleFavorite(p); loadFavorites(); return res }
    fun isFavorite(p: Product): Boolean = mgr.isFavorite(p)
    fun removeFavorite(p: Product) { mgr.removeFavorite(p); loadFavorites() }
    val favoriteCount: Int get() = _favorites.value.size
}

class FavoritesManager(context: Context, private val uid: String) {
    private val prefs = context.getSharedPreferences("favorites_$uid", Context.MODE_PRIVATE); private val gson = Gson()
    fun getFavorites(): List<Product> { val json = prefs.getString("favorite_products", null) ?: return emptyList(); return try { gson.fromJson(json, object : com.google.gson.reflect.TypeToken<List<Product>>() {}.type) } catch (e: Exception) { emptyList() } }
    fun toggleFavorite(p: Product): Boolean {
        val f = getFavorites().toMutableList(); val id = p.code ?: "${p.name}_${p.brands}"
        val exists = f.any { (it.code ?: "${it.name}_${it.brands}") == id }
        if (exists) f.removeAll { (it.code ?: "${it.name}_${it.brands}") == id } else f.add(p)
        prefs.edit().putString("favorite_products", gson.toJson(f)).apply(); return !exists
    }
    fun isFavorite(p: Product): Boolean { val id = p.code ?: "${p.name}_${p.brands}"; return getFavorites().any { (it.code ?: "${it.name}_${it.brands}") == id } }
    fun removeFavorite(p: Product) {
        val f = getFavorites().toMutableList()
        val id = p.code ?: "${p.name}_${p.brands}"
        f.removeAll { (it.code ?: "${it.name}_${p.brands}") == id }
        prefs.edit().putString("favorite_products", gson.toJson(f)).apply()
    }
}

class ScanHistoryManager(context: Context, private val uid: String) {
    private val prefs = context.getSharedPreferences("history_$uid", Context.MODE_PRIVATE); private val gson = Gson()
    fun getHistory(): List<Product> { val json = prefs.getString("history", null) ?: return emptyList(); return try { gson.fromJson(json, object : com.google.gson.reflect.TypeToken<List<Product>>() {}.type) } catch (e: Exception) { emptyList() } }
    fun addToHistory(p: Product) {
        val list = getHistory().toMutableList(); val id = p.code ?: "${p.name}_${p.brands}"
        list.removeAll { (it.code ?: "${it.name}_${it.brands}") == id }
        list.add(0, p)
        if (list.size > 50) list.removeAt(list.size - 1)
        prefs.edit().putString("history", gson.toJson(list)).apply()
    }
    fun clearHistory() { prefs.edit().remove("history").apply() }
}

// ==================== MAIN UI CONTENT ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(authManager: AuthManager, cartViewModel: CartViewModel) {
    val context = LocalContext.current
    val user = authManager.getCurrentUser()
    val favoritesManager = remember(user?.uid) { FavoritesManager(context, user?.uid ?: "default") }
    val favoritesViewModel = remember(user?.uid) { FavoritesViewModel(favoritesManager) }
    val scanHistoryManager = remember(user?.uid) { ScanHistoryManager(context, user?.uid ?: "default") }
    val navController = rememberNavController()
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    
    var showCartSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var showFavoritesSheet by remember { mutableStateOf(false) }
    var showShoppingListSheet by remember { mutableStateOf(false) }
    var showBudgetManagerSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { },
                navigationIcon = {
                    Row {
                        IconButton(onClick = { showBudgetManagerSheet = true }) { Icon(Icons.Rounded.Tune, null, tint = Color(0xFF4CAF50)) }
                        IconButton(onClick = { showShoppingListSheet = true }) { Icon(Icons.Rounded.ChecklistRtl, null, tint = Color(0xFF1976D2)) }
                    }
                },
                actions = {
                    IconButton(onClick = { showHistorySheet = true }) { Icon(Icons.Rounded.History, null) }
                    IconButton(onClick = { showFavoritesSheet = true }) {
                        BadgedBox(badge = { if (favoritesViewModel.favoriteCount > 0) Badge { Text(favoritesViewModel.favoriteCount.toString()) } }) {
                            Icon(if (favoritesViewModel.favoriteCount > 0) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (favoritesViewModel.favoriteCount > 0) Color.Red else Color.Black)
                        }
                    }
                    IconButton(onClick = { showCartSheet = true }) {
                        BadgedBox(badge = { if (cartViewModel.itemCount > 0) Badge { Text(cartViewModel.itemCount.toString()) } }) {
                            Icon(Icons.Rounded.ShoppingCart, null)
                        }
                    }
                    IconButton(onClick = { showProfileSheet = true }) { Icon(Icons.Rounded.Person, null, modifier = Modifier.size(32.dp)) }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                Destination.entries.forEachIndexed { index, dest ->
                    val isSelected = selectedItem == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedItem = index; navController.navigate(dest.route) },
                        icon = { Icon(if (isSelected) dest.selectedIcon else dest.unselectedIcon, null) },
                        label = null
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController, Destination.HOME.route, Modifier.padding(padding)) {
            composable(Destination.HOME.route) { HomeScreen(favoritesViewModel, cartViewModel) }
            composable(Destination.SEARCH.route) { SearchScreen(favoritesViewModel, cartViewModel) }
            composable(Destination.SCAN.route) { ScanScreen(cartViewModel, favoritesViewModel, scanHistoryManager) }
            composable(Destination.VIP.route) { VipScreen(authManager) }
            composable(Destination.SETTINGS.route) { SettingsScreen() }
        }

        if (showCartSheet) ModalBottomSheet(onDismissRequest = { showCartSheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) { CartSheet(cartViewModel, favoritesViewModel) }
        if (showFavoritesSheet) ModalBottomSheet(onDismissRequest = { showFavoritesSheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) { FavoritesSheet(favoritesViewModel, cartViewModel) }
        if (showProfileSheet) ModalBottomSheet(onDismissRequest = { showProfileSheet = false }) { ProfileScreen(authManager, onVip = { showProfileSheet=false; selectedItem=3; navController.navigate(Destination.VIP.route) }, onDismiss = { showProfileSheet = false }) }
        if (showShoppingListSheet) ModalBottomSheet(onDismissRequest = { showShoppingListSheet = false }) { ShoppingListSheet { showShoppingListSheet = false } }
        if (showBudgetManagerSheet) ModalBottomSheet(onDismissRequest = { showBudgetManagerSheet = false }) { BudgetManagerSheet({showBudgetManagerSheet=false}, cartViewModel, context) }
        if (showHistorySheet) ModalBottomSheet(onDismissRequest = { showHistorySheet = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) { ScanHistorySheet(scanHistoryManager, favoritesViewModel, cartViewModel) }
    }
}

// ==================== HOME SCREEN ====================

@Composable
fun HomeScreen(favoritesViewModel: FavoritesViewModel, cartViewModel: CartViewModel) {
    val api = remember { OpenFoodFactsApi.create() }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var detailProduct by remember { mutableStateOf<Product?>(null) }
    LaunchedEffect(Unit) { try { products = api.searchProducts("").products.take(20) } catch (_: Exception) {} }

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
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
        Box(Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 12.dp)) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val cat = categories[page]
                Card(modifier = Modifier.fillMaxWidth().height(160.dp), shape = RoundedCornerShape(16.dp)) {
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(model = cat.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)))))
                        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                            Text(cat.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(cat.subtitle, color = Color.White.copy(0.9f))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(listOf(Triple("CARREFOUR", R.drawable.carrefour_0, Color.White), Triple("E.LECLERC", R.drawable.e_leclerc_logo_svg, Color.White), Triple("AUCHAN", R.drawable.logo_auchan_, Color.White), Triple("INTERMARCHÉ", R.drawable.nouveau_logo_intermarche, Color.White), Triple("LIDL", R.drawable.images__4_, Color.White))) { (name, logo, text) ->
                Card(Modifier.size(110.dp, 65.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Image(painter = painterResource(logo), contentDescription = name, modifier = Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
            items(products) { product ->
                var isFav by remember { mutableStateOf(favoritesViewModel.isFavorite(product)) }
                Card(Modifier.width(140.dp).clickable { detailProduct = product }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White)) {
                    Column {
                        AsyncImage(product.imageUrl, null, Modifier.fillMaxWidth().height(140.dp), contentScale = ContentScale.Crop)
                        Text(product.name ?: "Produit", Modifier.padding(8.dp), fontWeight = FontWeight.Bold, maxLines = 2, fontSize = 14.sp)
                        IconButton(onClick = { isFav = favoritesViewModel.toggleFavorite(product) }) { Icon(if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder, null, tint = if (isFav) Color.Red else Color.Black) }
                    }
                }
            }
        }
    }
    detailProduct?.let { ProductDetailScreen(product = it, onDismiss = { detailProduct = null }, favoritesViewModel = favoritesViewModel, cartViewModel = cartViewModel) }
}

data class CategoryData(val title: String, val subtitle: String, val imageUrl: String)

// ==================== SEARCH SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable 
fun SearchScreen(favoritesViewModel: FavoritesViewModel, cartViewModel: CartViewModel) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var detailProduct by remember { mutableStateOf<Product?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Rechercher un produit...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(16.dp), singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { scope.launch { isSearching = true; results = OpenFoodFactsApi.create().searchProducts(query).products; isSearching = false } })
        )
        Spacer(Modifier.height(16.dp))
        if (isSearching) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(results) { product ->
                Card(Modifier.fillMaxWidth().clickable { detailProduct = product }) {
                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(product.imageUrl, null, Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)))
                        Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(product.name ?: "Produit", fontWeight = FontWeight.Bold); Text(product.brands ?: "", style = MaterialTheme.typography.bodySmall) }
                        IconButton(onClick = { cartViewModel.addToCart(product) }) { Icon(Icons.Rounded.AddShoppingCart, null) }
                    }
                }
            }
        }
    }
    detailProduct?.let { ProductDetailScreen(product = it, onDismiss = { detailProduct = null }, favoritesViewModel = favoritesViewModel, cartViewModel = cartViewModel) }
}

// ==================== SCANNER ULTRA-STABLE ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(cart: CartViewModel, favs: FavoritesViewModel, historyMgr: ScanHistoryManager) {
    val context = LocalContext.current
    var hasCam by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCam = it }
    LaunchedEffect(Unit) { if (!hasCam) launcher.launch(Manifest.permission.CAMERA) }
    if (!hasCam) { Box(Modifier.fillMaxSize().background(Color.Black), Alignment.Center) { Text("Permission caméra requise", color = Color.White) }; return }

    val api = remember { OpenFoodFactsApi.create() }
    val offline = remember { OfflineProductManager(context, api) }
    val vm: BarcodeViewModel = remember { BarcodeViewModel(offline, historyMgr, context) }
    val state = vm.barScanState
    var scannedProduct by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(state) { if (state is BarScanState.ProductDetected) scannedProduct = state.product }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(vm)
        ViewfinderOverlayYuka()
        Box(Modifier.align(Alignment.Center).size(260.dp).border(2.dp, if(state is BarScanState.Loading) Color.Yellow else Color.White.copy(0.3f), RoundedCornerShape(24.dp)))
        Text(text = when(state) { is BarScanState.Loading -> "Recherche..."; is BarScanState.ScanError -> "❌ ${state.message}"; else -> "Visez un code-barres" }, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp).background(Color.Black.copy(0.7f), RoundedCornerShape(20.dp)).padding(16.dp, 8.dp), color = Color.White, fontWeight = FontWeight.Bold)
        if (state is BarScanState.Loading) { CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color.White) }
    }
    scannedProduct?.let { product -> ProductDetailScreen(product = product, onDismiss = { scannedProduct = null; vm.resetState() }, favoritesViewModel = favs, cartViewModel = cart) }
}

@Composable
fun CameraPreview(vm: BarcodeViewModel) {
    val life = LocalLifecycleOwner.current; val exec = remember { Executors.newSingleThreadExecutor() }
    AndroidView(factory = { context ->
        val view = PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
        val provFuture = ProcessCameraProvider.getInstance(context)
        provFuture.addListener({
            try {
                val prov = provFuture.get()
                val pre = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                val ana = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also { it.setAnalyzer(exec, BarcodeAnalyzer { code -> vm.onBarCodeDetected(code) }) }
                prov.unbindAll(); prov.bindToLifecycle(life, CameraSelector.DEFAULT_BACK_CAMERA, pre, ana)
            } catch (e: Exception) { Log.e("Scanner", "Camera fail", e) }
        }, ContextCompat.getMainExecutor(context))
        view
    }, modifier = Modifier.fillMaxSize())
}

@Composable fun ViewfinderOverlayYuka() { Canvas(Modifier.fillMaxSize().graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)) { val w = size.width; val h = size.height; val side = w * 0.75f; val l = (w - side) / 2; val t = (h - side) / 2; drawRect(Color.Black.copy(0.5f)); drawRoundRect(color = Color.Transparent, topLeft = Offset(l, t), size = Size(side, side), cornerRadius = CornerRadius(24.dp.toPx()), blendMode = BlendMode.Clear) } }

class BarcodeViewModel(private val mgr: OfflineProductManager, private val historyMgr: ScanHistoryManager, private val ctx: Context) : ViewModel() {
    var barScanState by mutableStateOf<BarScanState>(BarScanState.Idle); private set
    private var lastCode: String? = null; private var lastTime = 0L
    fun onBarCodeDetected(code: String) {
        val now = System.currentTimeMillis()
        if (barScanState !is BarScanState.Idle || (code == lastCode && now - lastTime < 3000)) return
        lastCode = code; lastTime = now
        viewModelScope.launch {
            barScanState = BarScanState.Loading
            try {
                val res = withContext(Dispatchers.IO) { mgr.getProductByBarcode(code) }
                if (res?.product != null) { historyMgr.addToHistory(res.product); barScanState = BarScanState.ProductDetected(res.product) }
                else { barScanState = BarScanState.ScanError("Non trouvé"); delay(2000); resetState() }
            } catch (e: Exception) { barScanState = BarScanState.ScanError("Erreur"); delay(2000); resetState() }
        }
    }
    fun resetState() { barScanState = BarScanState.Idle }
}

class BarcodeAnalyzer(private val onDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build())
    private var lastRun = 0L
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(proxy: ImageProxy) { val now = System.currentTimeMillis(); if (now - lastRun < 500) { proxy.close(); return }; lastRun = now; val media = proxy.image; if (media != null) { scanner.process(InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)).addOnSuccessListener { barcodes -> barcodes.firstOrNull()?.rawValue?.let { onDetected(it) } }.addOnCompleteListener { proxy.close() } } else proxy.close() }
}

sealed interface BarScanState { object Idle : BarScanState; object Loading : BarScanState; data class ProductDetected(val product: Product) : BarScanState; data class ScanError(val message: String) : BarScanState }

// ==================== AUTH SCREEN ====================

@Composable
fun AuthScreen(auth: AuthManager) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val state = auth.authState

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.ShoppingBag,
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).padding(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = if (isLogin) "Bon retour !" else "Créer un compte",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = if (isLogin) "Connectez-vous pour continuer" else "Inscrivez-vous pour commencer",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(32.dp))

        if (!isLogin) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nom complet") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Rounded.Person, null) },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Rounded.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password, onValueChange = { password = it },
            label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Rounded.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        if (state is AuthState.Error) {
            Text(state.message, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { if (isLogin) auth.signIn(email, password) else auth.signUp(email, password, name) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = state !is AuthState.Loading
        ) {
            if (state is AuthState.Loading) { CircularProgressIndicator(Modifier.size(24.dp), color = Color.White) }
            else { Text(if (isLogin) "Se connecter" else "S'inscrire", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = { isLogin = !isLogin }) {
            Text(if (isLogin) "Pas encore de compte ? S'inscrire" else "Déjà un compte ? Se connecter")
        }
    }
}

// ==================== SHEETS ====================

@Composable
fun CartSheet(cartViewModel: CartViewModel, favoritesViewModel: FavoritesViewModel) {
    var detailProduct by remember { mutableStateOf<Product?>(null) }
    var showDriveSelection by remember { mutableStateOf(false) }
    val drives = listOf(
        Triple("Carrefour Drive", R.drawable.images__4_, Color(0xFF0055A4)),
        Triple("E.Leclerc Drive", R.drawable.logo_e_leclerc_sans_le_texte_svg, Color(0xFF0066B2)),
        Triple("Auchan Drive", R.drawable.logo_auchan_, Color(0xFFE30613)),
        Triple("Intermarché Drive", R.drawable.nouveau_logo_intermarche, Color(0xFFE30613)),
        Triple("Lidl", R.drawable.lidl_logo_svg, Color(0xFF0050AA)),
        Triple("Système U", R.drawable.images, Color(0xFFE30613)),
        Triple("Aldi", R.drawable.images__3_, Color(0xFF0050AA)),
        Triple("Cora", R.drawable.adhesif_logo_grande_distribution_gms_cora_bleu_et_rouge_fond_blanc, Color(0xFFE30613)),
        Triple("Casino", R.drawable.casino_supermarket_logo, Color(0xFF0066B2)),
        Triple("Monoprix", R.drawable.monoprix, Color(0xFFE30613)),
        Triple("Franprix", R.drawable.franprix, Color(0xFFE30613)),
        Triple("Netto", R.drawable.french_netto_logo_2019_svg, Color(0xFFE30613)),
        Triple("Grand Frais", R.drawable.grand_frais_logo, Color(0xFF0066B2))
    )

    Column(Modifier.padding(24.dp).fillMaxWidth().fillMaxHeight(0.9f)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Mon Panier (${cartViewModel.itemCount})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = { cartViewModel.clearCart() }) { Icon(Icons.Rounded.DeleteSweep, "Vider", tint = MaterialTheme.colorScheme.error) }
        }
        Spacer(Modifier.height(16.dp))
        
        OutlinedCard(onClick = { showDriveSelection = !showDriveSelection }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (cartViewModel.selectedDriveLogo != 0) {
                    Image(painter = painterResource(cartViewModel.selectedDriveLogo), null, Modifier.size(32.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Fit)
                } else {
                    Icon(Icons.Rounded.Store, null, tint = MaterialTheme.colorScheme.primary)
                }
                Text(cartViewModel.selectedDrive, modifier = Modifier.weight(1f).padding(start = 12.dp), fontWeight = FontWeight.Bold)
                Icon(if(showDriveSelection) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null)
            }
        }
        
        AnimatedVisibility(visible = showDriveSelection) {
            LazyColumn(modifier = Modifier.padding(top = 12.dp).heightIn(max = 300.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(drives) { drive ->
                    Card(
                        onClick = { cartViewModel.updateDrive(drive.first, drive.second); showDriveSelection = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if(cartViewModel.selectedDrive == drive.first) drive.third.copy(0.1f) else Color.White),
                        border = if(cartViewModel.selectedDrive == drive.first) BorderStroke(2.dp, drive.third) else null
                    ) {
                        Box(Modifier.padding(12.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Image(painter = painterResource(drive.second), null, Modifier.size(60.dp), contentScale = ContentScale.Fit)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (cartViewModel.cartItems.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("Votre panier est vide", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(cartViewModel.cartItems) { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { detailProduct = item.product }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(item.product.imageUrl, null, Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)))
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(item.product.name ?: "Produit", fontWeight = FontWeight.Bold, maxLines = 1)
                                var priceInput by remember { mutableStateOf(if(item.price == 0f) "" else item.price.toString()) }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    BasicTextField(value = priceInput, onValueChange = { priceInput = it; it.replace(",", ".").toFloatOrNull()?.let { newPrice -> cartViewModel.updatePrice(item.product, newPrice) } }, textStyle = TextStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.width(60.dp), decorationBox = { innerTextField -> if (priceInput.isEmpty()) Text("0.00", color = Color.LightGray, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) ; innerTextField() })
                                    Text(" €", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)) { IconButton(onClick = { cartViewModel.updateQuantity(item.product, item.quantity - 1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.Remove, null, Modifier.size(16.dp)) }; Text("${item.quantity}", Modifier.padding(horizontal = 4.dp), fontWeight = FontWeight.Bold); IconButton(onClick = { cartViewModel.updateQuantity(item.product, item.quantity + 1) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Rounded.Add, null, Modifier.size(16.dp)) } }
                        }
                    }
                }
            }
        }
        Divider(Modifier.padding(vertical = 16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("Total :", style = MaterialTheme.typography.titleLarge); Text("${String.format("%.2f", cartViewModel.totalPrice)} €", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color(0xFF4CAF50)) }
        Button(onClick = { /* Checkout */ }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(56.dp), shape = RoundedCornerShape(16.dp), enabled = cartViewModel.cartItems.isNotEmpty()) { Text("Commander", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
    }
    detailProduct?.let { ProductDetailScreen(product = it, onDismiss = { detailProduct = null }, favoritesViewModel = favoritesViewModel, cartViewModel = cartViewModel) }
}

@Composable
fun FavoritesSheet(favoritesViewModel: FavoritesViewModel, cartViewModel: CartViewModel) {
    var detailProduct by remember { mutableStateOf<Product?>(null) }
    Column(Modifier.padding(24.dp).fillMaxWidth().fillMaxHeight(0.9f)) {
        Text("Mes Favoris", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        if (favoritesViewModel.favorites.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) { Text("Aucun favori pour le moment", color = Color.Gray) }
        } else {
            LazyColumn(Modifier.weight(1f)) {
                items(favoritesViewModel.favorites) { p ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { detailProduct = p }, shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                             AsyncImage(p.imageUrl, null, Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)))
                            Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(p.name ?: "Produit", fontWeight = FontWeight.Bold, maxLines = 1); Text(p.brands ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                            IconButton(onClick = { favoritesViewModel.removeFavorite(p) }) { Icon(Icons.Rounded.Favorite, null, tint = Color.Red) }
                        }
                    }
                }
            }
        }
    }
    detailProduct?.let { ProductDetailScreen(product = it, onDismiss = { detailProduct = null }, favoritesViewModel = favoritesViewModel, cartViewModel = cartViewModel) }
}

@Composable
fun ScanHistorySheet(manager: ScanHistoryManager, favs: FavoritesViewModel, cart: CartViewModel) {
    var history by remember { mutableStateOf(manager.getHistory()) }
    var detailProduct by remember { mutableStateOf<Product?>(null) }
    Column(Modifier.padding(24.dp).fillMaxWidth().fillMaxHeight(0.9f)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Historique des Scans", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (history.isNotEmpty()) { TextButton(onClick = { manager.clearHistory(); history = emptyList() }) { Text("Vider", color = Color.Red) } }
        }
        Spacer(Modifier.height(16.dp))
        if (history.isEmpty()) { Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) { Text("Aucun produit scanné", color = Color.Gray) } }
        else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(history) { p ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { detailProduct = p }, shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(p.imageUrl, null, Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)))
                            Column(Modifier.padding(start = 12.dp).weight(1f)) { Text(p.name ?: "Produit", fontWeight = FontWeight.Bold, maxLines = 1); Text(p.brands ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                            IconButton(onClick = { cart.addToCart(p) }) { Icon(Icons.Rounded.AddShoppingCart, null) }
                        }
                    }
                }
            }
        }
    }
    detailProduct?.let { ProductDetailScreen(product = it, onDismiss = { detailProduct = null }, favoritesViewModel = favs, cartViewModel = cart) }
}

@Composable fun ShoppingListSheet(onDismiss: () -> Unit) { val context = LocalContext.current; var items by remember { mutableStateOf(getShoppingList(context)) }; var newItemName by remember { mutableStateOf("") }; Column(modifier = Modifier.fillMaxHeight(0.8f).padding(24.dp)) { Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) { Text("Ma Liste de Courses", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f)); IconButton(onClick = { items = emptyList(); saveShoppingList(context, items) }) { Icon(Icons.Rounded.DeleteSweep, "Clear all", tint = MaterialTheme.colorScheme.error) } }; Spacer(Modifier.height(16.dp)); OutlinedTextField(value = newItemName, onValueChange = { newItemName = it }, placeholder = { Text("Ajouter un article...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), singleLine = true, trailingIcon = { IconButton(onClick = { if (newItemName.isNotBlank()) { val newList = items + ShoppingListItem(newItemName.trim(), false); items = newList; saveShoppingList(context, newList); newItemName = "" } }, enabled = newItemName.isNotBlank()) { Icon(Icons.Rounded.Add, null) } }, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { if (newItemName.isNotBlank()) { val newList = items + ShoppingListItem(newItemName.trim(), false); items = newList; saveShoppingList(context, newList); newItemName = "" } })); Spacer(Modifier.height(24.dp)); if (items.isEmpty()) { Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Rounded.ListAlt, null, modifier = Modifier.size(64.dp), tint = Color.LightGray); Text("Votre liste est vide", color = Color.Gray) } } } else { LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { items(items) { item -> Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (item.checked) Color.LightGray.copy(0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f))) { Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = item.checked, onCheckedChange = { isChecked -> val newList = items.map { if (it.name == item.name) it.copy(checked = isChecked) else it }; items = newList; saveShoppingList(context, newList) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))); Text(text = item.name, modifier = Modifier.weight(1f).padding(horizontal = 8.dp), style = MaterialTheme.typography.bodyLarge.copy(textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None, color = if (item.checked) Color.Gray else Color.Unspecified)); IconButton(onClick = { val newList = items.filter { it.name != item.name }; items = newList; saveShoppingList(context, newList) }) { Icon(Icons.Rounded.Close, null, modifier = Modifier.size(20.dp), tint = Color.Gray) } } } } } } } }
@Composable fun BudgetManagerSheet(onD: () -> Unit, cart: CartViewModel, context: Context) { Column(Modifier.padding(24.dp)) { Text("Mon Budget Max", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Spacer(Modifier.height(16.dp)); var budgetInput by remember { mutableStateOf(cart.userMaxBudget.toString()) }; OutlinedTextField(value = budgetInput, onValueChange = { budgetInput = it; it.replace(",", ".").toFloatOrNull()?.let { newBudget -> cart.updateBudget(newBudget) } }, label = { Text("Fixer mon budget (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)); Spacer(Modifier.height(24.dp)); Text("Progression", fontWeight = FontWeight.Bold); Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress = { (cart.totalPrice / cart.userMaxBudget).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(12.dp).clip(CircleShape), color = if (cart.totalPrice > cart.userMaxBudget) Color.Red else Color(0xFF4CAF50)); Spacer(Modifier.height(8.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${String.format("%.2f", cart.totalPrice)}€", fontWeight = FontWeight.Black); Text("${cart.userMaxBudget}€", color = Color.Gray) }; if (cart.totalPrice > cart.userMaxBudget) { Text("⚠️ Budget dépassé !", color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) } } }
@Composable fun VipScreen(auth: AuthManager) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Espace Membre") } }
@Composable fun SettingsScreen() { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Paramètres") } }
@Composable fun ProfileScreen(auth: AuthManager, onVip: () -> Unit, onDismiss: () -> Unit) { Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray), Alignment.Center) { Icon(Icons.Rounded.Person, null, modifier = Modifier.size(48.dp)) } ; Text(auth.getCurrentUser()?.name ?: "Utilisateur", Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Button(onClick = onVip, Modifier.fillMaxWidth().padding(top = 24.dp)) { Text("Devenir membre") } ; TextButton(onClick = { auth.signOut() }) { Text("Se déconnecter", color = Color.Red) } } }

data class ShoppingListItem(val name: String, val checked: Boolean)
fun getShoppingList(c: Context): List<ShoppingListItem> { val prefs = c.getSharedPreferences("shopping_list", Context.MODE_PRIVATE); val json = prefs.getString("items", null) ?: return emptyList(); return try { Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<List<ShoppingListItem>>() {}.type) } catch (e: Exception) { emptyList() } }
fun saveShoppingList(c: Context, i: List<ShoppingListItem>) { val prefs = c.getSharedPreferences("shopping_list", Context.MODE_PRIVATE); prefs.edit().putString("items", Gson().toJson(i)).apply() }
