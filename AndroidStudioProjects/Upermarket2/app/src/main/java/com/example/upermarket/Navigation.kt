package com.example.upermarket

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val contentDescription: String
) {
    HOME("home", Icons.Rounded.Home, Icons.Outlined.Home, "Accueil", "Accueil"),
    SEARCH("search", Icons.Rounded.Search, Icons.Outlined.Search, "Recherche", "Recherche"),
    VIP("vip", Icons.Rounded.Star, Icons.Outlined.Star, "VIP", "VIP"),
    SETTINGS("settings", Icons.Rounded.Settings, Icons.Outlined.Settings, "Paramètres", "Paramètres")
}
