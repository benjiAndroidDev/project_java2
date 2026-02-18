package com.Upermarket.upermarket

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upermarket.R

data class Category(val name: String, val imageRes: Int, val color: Color)
data class Brand(val name: String, val logoRes: Int)

@Composable
fun HomeScreen(favoritesViewModel: FavoritesViewModel, cartViewModel: CartViewModel) {
    val categories = listOf(
        Category("Fruits", R.drawable.fruits, Color(0xFFFFE0E0)),
        Category("Légumes", R.drawable.legumes, Color(0xFFE0FFE0)),
        Category("Viandes", R.drawable.viande, Color(0xFFFFE0B2)),
        Category("Boissons", R.drawable.boissons, Color(0xFFE1F5FE)),
        Category("Épicerie", R.drawable.epicerie, Color(0xFFF3E5F5)),
        Category("Surgelés", R.drawable.surgele, Color(0xFFE0F7FA)),
        Category("Frais", R.drawable.produit_frais, Color(0xFFFFF9C4)),
        Category("Laiterie", R.drawable.produits_laitiers, Color(0xFFF5F5F5)),
        Category("Charcut.", R.drawable.charcuterie, Color(0xFFFBE9E7))
    )

    val brands = listOf(
        Brand("Lidl", R.drawable.lidl_logo_svg),
        Brand("Carrefour", R.drawable.carrefour_logo_1982),
        Brand("E.Leclerc", R.drawable.e_leclerc_logo_svg),
        Brand("Auchan", R.drawable.logo_auchan__2015__svg),
        Brand("Inter", R.drawable.nouveau_logo_intermarche),
        Brand("Casino", R.drawable.casino_supermarket_logo),
        Brand("Netto", R.drawable.french_netto_logo_2019_svg),
        Brand("Monoprix", R.drawable.monoprix),
        Brand("Franprix", R.drawable.franprix)
    )

    val modernCarouselImages = listOf(
        R.drawable.promos1, // Image plus générique
        R.drawable.carrefour_0 // Une image d'enseigne
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // CARROUSEL "PRO 2026"
        item {
            val pagerState = rememberPagerState(pageCount = { modernCarouselImages.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
            ) { page ->
                Image(
                    painter = painterResource(id = modernCarouselImages[page]),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // ENSEIGNES
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Nos Enseignes")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(brands) { brand ->
                    BrandCard(brand)
                }
            }
        }

        // CATÉGORIES
        item {
            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader("Rayons")
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                categories.chunked(3).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { cat ->
                            ModernCategoryItem(cat, modifier = Modifier.weight(1f))
                        }
                        if (rowItems.size < 3) {
                            repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BrandCard(brand: Brand) {
    Surface(
        modifier = Modifier
            .size(85.dp)
            .shadow(3.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = brand.logoRes),
                contentDescription = brand.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun ModernCategoryItem(category: Category, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(75.dp)
                .background(category.color, CircleShape)
                .padding(15.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = category.imageRes),
                contentDescription = category.name,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}
