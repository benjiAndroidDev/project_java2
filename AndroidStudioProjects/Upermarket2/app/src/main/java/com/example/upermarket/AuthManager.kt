package com.example.upermarket

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Écran de profil professionnel
 */
@Composable
fun ProfileScreen(
    authService: AuthService,
    onNavigateToVip: () -> Unit,
    onDismiss: () -> Unit
) {
    val user = authService.getCurrentUser() ?: return
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête du profil avec dégradé VIP si nécessaire
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    if (user.isVip)
                        Brush.linearGradient(colors = listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
                    else
                        Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary))
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name?.take(1)?.uppercase() ?: "?",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.displayName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        if (user.isVip) {
            Surface(
                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFA500), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MEMBRE VIP",
                        color = Color(0xFFFFA500),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Menu d'actions professionnelles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProfileMenuItem(
                icon = Icons.Rounded.Person,
                title = "Informations personnelles",
                subtitle = "Modifier votre profil et vos coordonnées"
            ) {
                // Action à implémenter
            }

            ProfileMenuItem(
                icon = Icons.Rounded.Favorite,
                title = "Mes favoris",
                subtitle = "Retrouvez vos produits préférés"
            ) {
                onDismiss() // Pourrait naviguer vers un onglet dédié
            }

            ProfileMenuItem(
                icon = Icons.Rounded.Star,
                title = "Programme VIP",
                subtitle = if (user.isVip) "Gérer votre abonnement" else "Débloquer les avantages exclusifs",
                color = if (user.isVip) Color(0xFFFFA500) else MaterialTheme.colorScheme.primary
            ) {
                onNavigateToVip()
            }

            ProfileMenuItem(
                icon = Icons.Rounded.Settings,
                title = "Paramètres",
                subtitle = "Notifications et préférences de l'application"
            ) {
                // Action à implémenter
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton de déconnexion
        Button(
            onClick = {
                authService.signOut()
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = null
        ) {
            Icon(Icons.Rounded.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Se déconnecter", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}
