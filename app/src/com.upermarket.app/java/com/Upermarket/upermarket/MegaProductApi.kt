package com.Upermarket.upermarket

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class MegaProductSearchManager(private val context: Context) {

    private val offApi = OpenFoodFactsApi.create()
    
    companion object {
        private val memoryCache = ConcurrentHashMap<String, List<Product>>()
    }

    suspend fun searchProducts(query: String? = null, categoryTag: String? = null): List<Product> = withContext(Dispatchers.IO) {
        val cacheKey = "tag_${categoryTag}_q_${query}"
        memoryCache[cacheKey]?.let { return@withContext it }

        val results = mutableListOf<Product>()
        
        try {
            coroutineScope {
                val tasks = mutableListOf<Deferred<SearchResponse>>()

                if (!categoryTag.isNullOrBlank()) {
                    val tag = categoryTag.lowercase()
                    
                    // 1. CHARGEMENT DE BASE MASSIVE
                    for (page in 1..3) {
                        tasks.add(async { 
                            try { offApi.searchProducts(terms = query, categoryTag = categoryTag, pageSize = 100, page = page) } 
                            catch (e: Exception) { SearchResponse() }
                        })
                    }

                    // 2. STRATÉGIE DE DIVERSITÉ TOTALE
                    when {
                        tag.contains("breads") || tag.contains("pastries") -> {
                            Log.d("MegaSearch", "Stratégie 'GIGA BURGER' activée")
                            for (p in 1..5) {
                                tasks.add(async { try { offApi.searchProducts(categoryTag = "en:hamburger-buns", pageSize = 100, page = p) } catch(e:Exception) { SearchResponse() } })
                            }
                            tasks.add(async { try { offApi.searchProducts(terms = "burger buns brioché sésame harris jacquet", pageSize = 100) } catch(e:Exception) { SearchResponse() } })
                            tasks.add(async { try { offApi.searchProducts(terms = "pain hamburger géant boulangère", pageSize = 100) } catch(e:Exception) { SearchResponse() } })
                        }
                        tag.contains("vegetables") -> {
                            val vTags = listOf("en:potatoes", "en:onions", "en:carrots", "en:tomatoes")
                            vTags.forEach { vt -> tasks.add(async { try { offApi.searchProducts(categoryTag = vt, pageSize = 100) } catch(e:Exception) { SearchResponse() } }) }
                        }
                        tag.contains("beverages") -> {
                            tasks.add(async { try { offApi.searchProducts(terms = "Monster Energy Red Bull Coca", categoryTag = "en:energy-drinks", pageSize = 100) } catch(e:Exception) { SearchResponse() } })
                        }
                        tag.contains("meats") -> {
                            Log.d("MegaSearch", "Stratégie 'Boucherie & Poitrine Master' activée")
                            val mTags = listOf("en:poultry", "en:beef", "en:pork", "en:veal", "en:lamb-meat")
                            mTags.forEach { mt -> tasks.add(async { try { offApi.searchProducts(categoryTag = mt, pageSize = 100) } catch(e:Exception) { SearchResponse() } }) }
                            
                            // AJOUT MASSIF DE POITRINE DE PORC
                            for (p in 1..3) {
                                tasks.add(async { try { offApi.searchProducts(terms = "poitrine de porc lardons bacon", pageSize = 100, page = p) } catch(e:Exception) { SearchResponse() } })
                            }
                        }
                        tag.contains("frozen") -> {
                            tasks.add(async { try { offApi.searchProducts(categoryTag = "en:frozen-pizzas", pageSize = 100) } catch(e:Exception) { SearchResponse() } })
                        }
                        tag.contains("dairies") -> {
                            tasks.add(async { try { offApi.searchProducts(categoryTag = "en:butters", pageSize = 100) } catch(e:Exception) { SearchResponse() } })
                        }
                    }
                } else if (!query.isNullOrBlank()) {
                    for (page in 1..3) {
                        tasks.add(async { try { offApi.searchProducts(terms = query, pageSize = 100, page = page) } catch(e:Exception) { SearchResponse() } })
                    }
                }

                tasks.awaitAll().forEach { resp ->
                    resp.products.let { results.addAll(it) }
                }
            }
        } catch (e: Exception) {
            Log.e("MegaSearch", "Erreur de chargement", e)
        }

        val finalResults = results.filter { !it.name.isNullOrBlank() && it.name != "Produit inconnu" }
               .distinctBy { it.code ?: it.name }
               .shuffled()
        
        if (finalResults.isNotEmpty()) { memoryCache[cacheKey] = finalResults }
        finalResults
    }
}
