package com.Upermarket.upermarket

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel,
    scanHistoryManager: ScanHistoryManager
) {
    val context = LocalContext.current
    var hasCamPermission by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { isGranted -> 
        hasCamPermission = isGranted 
    }

    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCamPermission) {
        CameraScanner(cartViewModel, favoritesViewModel, scanHistoryManager)
    } else {
        PermissionDeniedScreen { launcher.launch(Manifest.permission.CAMERA) }
    }
}

@Composable
private fun PermissionDeniedScreen(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Rounded.NoPhotography, contentDescription = null, tint = Color.White, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Accès Caméra Requis", style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
            Text("Pour scanner des produits, Upermarket a besoin d'accéder à votre caméra.", color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.padding(top=8.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) { 
                Text("Autoriser la caméra") 
            }
        }
    }
}

@Composable
private fun CameraScanner(
    cartViewModel: CartViewModel,
    favoritesViewModel: FavoritesViewModel,
    scanHistoryManager: ScanHistoryManager
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember { BarcodeScanning.getClient() }
    val api = remember { OpenFoodFactsApi.create() }
    
    var scannedProduct by remember { mutableStateOf<Product?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { PreviewView(it).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (scannedProduct == null && !isProcessing) {
                                    processImageProxy(scanner, imageProxy) { barcode ->
                                        isProcessing = true
                                        scope.launch {
                                            try {
                                                api.getProductByBarcode(barcode).product?.let {
                                                    scannedProduct = it
                                                    scanHistoryManager.addToHistory(it)
                                                }
                                            } finally { isProcessing = false }
                                        }
                                    }
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                        cameraControl = camera.cameraControl
                    } catch (e: Exception) { Log.e("ScanScreen", "Binding failed", e) }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        IconButton(
            onClick = { torchEnabled = !torchEnabled; cameraControl?.enableTorch(torchEnabled) },
            modifier = Modifier.align(Alignment.TopEnd).padding(32.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape)
        ) {
            Icon(if (torchEnabled) Icons.Rounded.FlashlightOff else Icons.Rounded.FlashlightOn, null, tint = Color.White)
        }

        if (scannedProduct != null) {
            ProductDetailSheet(
                product = scannedProduct!!,
                isFavorite = favoritesViewModel.isFavorite(scannedProduct!!),
                onToggleFavorite = { favoritesViewModel.toggleFavorite(scannedProduct!!) },
                onAddToCart = { price -> 
                    cartViewModel.addToCart(scannedProduct!!, price)
                    scannedProduct = null
                },
                onDismiss = { scannedProduct = null }
            )
        } else if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        } else {
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(200.dp).align(Alignment.Center).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp)))
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(scanner: BarcodeScanner, imageProxy: ImageProxy, onBarcodeFound: (String) -> Unit) {
    imageProxy.image?.let { mediaImage ->
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes -> barcodes.firstOrNull()?.rawValue?.let(onBarcodeFound) }
            .addOnCompleteListener { imageProxy.close() }
    } ?: imageProxy.close()
}
