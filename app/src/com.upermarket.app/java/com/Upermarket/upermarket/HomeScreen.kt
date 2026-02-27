package com.Upermarket.upermarket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

// Import du R basé sur le namespace défini dans build.gradle.kts
import com.benjamin.upermarket.R

data class Category(val name: String, val imageRes: Int, val color: Color, val apiTag: String)
data class Brand(val name: String, val logoRes: Int, val storeUrl: String)

@Composable
fun HomeScreen(
    favoritesViewModel: FavoritesViewModel,
    cartViewModel: CartViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var brandQuery by remember { mutableStateOf("") }
    var isLocating by remember { mutableStateOf(false) }

    val allBrands = remember { listOf(
        Brand("Lidl", R.drawable.lidl_logo_svg, "https://www.lidl.fr"),
        Brand("Carrefour", R.drawable.carrefour_logo_1982, "https://www.carrefour.fr"),
        Brand("E.Leclerc", R.drawable.e_leclerc_logo_svg, "https://www.e.leclerc"),
        Brand("Auchan", R.drawable.logo_auchan__2015__svg, "https://www.auchan.fr"),
        Brand("Intermarché", R.drawable.nouveau_logo_intermarche, "https://www.intermarche.com"),
        Brand("Casino", R.drawable.casino_supermarket_logo, "https://www.casinosupermarches.fr"),
        Brand("Netto", R.drawable.french_netto_logo_2019_svg, "https://www.netto.fr"),
        Brand("Monoprix", R.drawable.monoprix, "https://www.monoprix.fr"),
        Brand("Franprix", R.drawable.franprix, "https://www.franprix.fr")
    ) }

    var filteredBrands by remember { mutableStateOf(allBrands) }

    val locationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            isLocating = true
            findNearbyBrands(context, scope, allBrands) { results ->
                filteredBrands = results ?: allBrands
                isLocating = false
            }
        }
    }

    LaunchedEffect(brandQuery) {
        if (brandQuery.isNotEmpty()) {
            filteredBrands = allBrands.filter { it.name.contains(brandQuery, ignoreCase = true) }
        } else if (!isLocating) {
            filteredBrands = allBrands
        }
    }

    val categories = remember { listOf(
        Category("Fruits", R.drawable.fruits, Color(0xFFFFE0E0), "en:fruits"),
        Category("Légumes", R.drawable.legumes, Color(0xFFE0FFE0), "en:vegetables"),
        Category("Viandes", R.drawable.viandes, Color(0xFFFFE0B2), "en:meats"),
        Category("Boulang.", R.drawable.ambiance_gourmande_images_pastry_2021_1, Color(0xFFFFF3E0), "en:breads"),
        Category("Pâtiss.", R.drawable.cofetarie_patiserie, Color(0xFFFCE4EC), "en:pastries"),
        Category("Boissons", R.drawable.boissons, Color(0xFFE1F5FE), "en:beverages"),
        Category("Épicerie", R.drawable.epicerie, Color(0xFFF3E5F5), "en:groceries"),
        Category("Surgelés", R.drawable.surgele, Color(0xFFE0F7FA), "en:frozen-foods"),
        Category("Laiterie", R.drawable.produits_laitiers, Color(0xFFF5F5F5), "en:dairies")
    ) }

    LazyColumn(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
                Text("Trouvez votre magasin", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = brandQuery,
                    onValueChange = { brandQuery = it },
                    modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(20.dp)),
                    placeholder = { Text("Rechercher une enseigne...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Color(0xFF00C853)) },
                    trailingIcon = {
                        IconButton(onClick = { 
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                isLocating = true
                                findNearbyBrands(context, scope, allBrands) { results ->
                                    filteredBrands = results ?: allBrands
                                    isLocating = false
                                }
                            } else {
                                locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }) {
                            if (isLocating) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF00C853))
                            else Icon(Icons.Rounded.MyLocation, "Proximité", tint = Color(0xFF00C853))
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF00C853), unfocusedBorderColor = Color.Transparent, focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            }
        }

        item {
            SectionHeader("Vos enseignes préférer")
            if (filteredBrands.isEmpty()) {
                Text("Aucune enseigne trouvée", modifier = Modifier.padding(24.dp).fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray)
            } else {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredBrands) { brand ->
                        BrandCard(brand) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(brand.storeUrl))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Choisisiez vos aliments préferer")
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                categories.chunked(3).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { cat ->
                            ModernCategoryItem(category = cat, modifier = Modifier.weight(1f), onClick = { navController.navigate("category_products/${cat.name}/${cat.apiTag}") })
                        }
                        if (rowItems.size < 3) { repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) } }
                    }
                }
            }
        }
    }
}

private fun findNearbyBrands(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    allBrands: List<Brand>,
    onFinish: (List<Brand>?) -> Unit
) {
    val client = LocationServices.getFusedLocationProviderClient(context)
    try {
        client.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                scope.launch(Dispatchers.IO) {
                    try {
                        val url = "https://nominatim.openstreetmap.org/search?format=json&q=supermarket&lat=${loc.latitude}&lon=${loc.longitude}&bounded=1&viewbox=${loc.longitude-0.1},${loc.latitude+0.1},${loc.longitude+0.1},${loc.latitude-0.1}"
                        val response = URL(url).readText()
                        val json = JSONArray(response)
                        val names = mutableSetOf<String>()
                        for (i in 0 until json.length()) { names.add(json.getJSONObject(i).getString("display_name").lowercase()) }
                        val results = allBrands.filter { b -> names.any { it.contains(b.name.lowercase()) } }
                        withContext(Dispatchers.Main) { onFinish(if (results.isEmpty()) allBrands else results) }
                    } catch (e: Exception) { withContext(Dispatchers.Main) { onFinish(allBrands) } }
                }
            } else { onFinish(allBrands) }
        }
    } catch (e: SecurityException) { onFinish(allBrands) }
}

@Composable
fun BrandCard(brand: Brand, onClick: () -> Unit) {
    Surface(modifier = Modifier.size(100.dp).padding(4.dp).shadow(8.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))) {
        Box(modifier = Modifier.fillMaxSize().clickable(onClick = onClick).padding(16.dp), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = brand.logoRes), contentDescription = brand.name, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        }
    }
}

@Composable
fun ModernCategoryItem(category: Category, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.padding(4.dp).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(75.dp).background(category.color, CircleShape).padding(15.dp), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(id = category.imageRes), contentDescription = category.name, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = category.name, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 0.5.sp), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.onBackground)
}
