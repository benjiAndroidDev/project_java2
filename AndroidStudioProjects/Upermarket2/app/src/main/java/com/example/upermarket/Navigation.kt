package com.example.upermarket

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// Icônes modernes 2026 - Vraies icônes Material 3
enum class Destination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
    val contentDescription: String
) {
    HOME(
        "home", 
        Icons.Filled.Home,  // Maison classique mais efficace
        Icons.Outlined.Home, 
        "Accueil", 
        "Accueil"
    ),
    SEARCH(
        "search", 
        Icons.Filled.TravelExplore,  // Globe avec loupe - moderne
        Icons.Outlined.TravelExplore, 
        "Explorer", 
        "Explorer"
    ),
    SCAN(
        "scan", 
        Icons.Filled.QrCodeScanner, 
        Icons.Outlined.QrCodeScanner, 
        "Scanner", 
        "Scanner"
    ),
    VIP(
        "vip", 
        Icons.Filled.WorkspacePremium,  // Badge premium moderne
        Icons.Outlined.WorkspacePremium, 
        "Premium", 
        "Premium"
    ),
    SETTINGS(
        "settings", 
        Icons.Filled.ManageAccounts,  // Gestion compte plus moderne
        Icons.Outlined.ManageAccounts, 
        "Compte", 
        "Mon Compte"
    )
}
