package com.example.upermarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.upermarket.ui.theme.UpermarketTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarouselExample_MultiBrowse(

            )
            UpermarketTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onTimeout = { showSplash = false },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Contenu principal après le splash
                    SimpleSearchBar(
                        textFieldState = remember { TextFieldState() },
                        onSearch = { },
                        searchResults = listOf("Exemple de produit 1", "Exemple de produit 2")
                    )
                }
            }
        }
    }


    @Composable
    fun SplashScreen(
        onTimeout: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        var visible by remember { mutableStateOf(true) }

        LaunchedEffect(key1 = Unit) {
            delay(3000)
            visible = false
            delay(3000) // Petit délai pour l'animation de sortie
            onTimeout()
        }

        AnimatedVisibility(
            visible = visible,
        ) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.androidparty),
                        contentDescription = "Upermarket Logo",
                        modifier = Modifier.size(600.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black // Changé en noir car le fond est blanc
                    )
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SimpleSearchBar(
        textFieldState: TextFieldState,
        onSearch: (String) -> Unit,
        searchResults: List<String>,
        modifier: Modifier = Modifier
    ) {
        var expanded by rememberSaveable { mutableStateOf(false) }

        Box(
            modifier
                .fillMaxSize()
                .semantics { isTraversalGroup = true }
                .width(19.dp)
        ) {
            SearchBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .semantics { traversalIndex = 0f },
                inputField = {
                    SearchBarDefaults.InputField(
                        query = textFieldState.text.toString(),
                        onQueryChange = { textFieldState.edit { replace(0, length, it) } },
                        onSearch = {
                            onSearch(textFieldState.text.toString())
                            expanded = false
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text("All the products") }
                    )

                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    searchResults.forEach { result ->
                        ListItem(
                            headlineContent = { Text(result) },
                            modifier = Modifier
                                .clickable {
                                    textFieldState.edit { replace(0, length, result) }
                                    expanded = false
                                }
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselExample_MultiBrowse() {
    data class CarouselItem(
        val id: Int,
        @DrawableRes val imageResId: Int,
        val contentDescription: String
    )

    val items = remember {
        listOf(
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),
            CarouselItem(0, R.drawable.cupcake, "Cupcake"),
            CarouselItem(1, R.drawable.donut, "Donut"),
            CarouselItem(2, R.drawable.eclair, "Eclair"),
            CarouselItem(3, R.drawable.froyo, "Froyo"),
            CarouselItem(4, R.drawable.gingerbread, "Gingerbread"),


        )
    }

    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState {  items.count() },
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 16.dp, bottom = 16.dp),

       preferredItemWidth = 186.dp,
        itemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { i ->
        val item = items[i]
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
                .maskClip(MaterialTheme.shapes.extraLarge),
              painter = painterResource(id = item.imageResId),
              contentDescription = item.contentDescription,
              contentScale = ContentScale.Crop

        )
    }
}



}
