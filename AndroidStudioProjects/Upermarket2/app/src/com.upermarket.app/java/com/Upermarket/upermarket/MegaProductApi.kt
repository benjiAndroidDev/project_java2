package com.Upermarket.upermarket

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class MegaProductSearchManager(private val context: Context) {

    companion object {
        private const val TAG = "MegaProductSearch"
    }

    private val offApi = OpenFoodFactsApi.create()

    suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        val results = mutableListOf<Product>()

        try {
            // Recherche multi-pages en parallèle pour plus de rapidité et de résultats
            val page1 = async { 
                try { offApi.searchProducts(terms = query, pageSize = 50).products } 
                catch (e: Exception) { emptyList<Product>() } 
            }
            
            // On peut ajouter d'autres sources ici si nécessaire
            
            results.addAll(page1.await())
            
            Log.d(TAG, "Search for '$query' found ${results.size} products")
        } catch (e: Exception) {
            Log.e(TAG, "Search failed", e)
        }

        results.distinctBy { it.code }
    }
}
