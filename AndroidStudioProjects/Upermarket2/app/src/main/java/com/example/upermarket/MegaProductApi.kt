package com.example.upermarket

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.http.Path
import retrofit2.http.Header
import android.content.Context
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import android.util.Log

// ==================== MEGA PRODUCT API 2026 ====================
// API avec des millions de produits alimentaires mondiaux

@Serializable
data class SpoonacularSearchResponse(
    val results: List<SpoonacularProduct> = emptyList(),
    val offset: Int = 0,
    val number: Int = 0,
    val totalResults: Int = 0
)

@Serializable
data class SpoonacularProduct(
    val id: Int,
    val title: String,
    val image: String? = null,
    @SerialName("imageType") val imageType: String? = null
)

@Serializable
data class UsdaFoodSearchResponse(
    val foods: List<UsdaFood> = emptyList(),
    val totalHits: Int = 0,
    val currentPage: Int = 0,
    val totalPages: Int = 0
)

@Serializable
data class UsdaFood(
    @SerialName("fdcId") val fdcId: Int,
    val description: String,
    val brandOwner: String? = null,
    val ingredients: String? = null,
    @SerialName("foodNutrients") val foodNutrients: List<UsdaNutrient>? = null
)

@Serializable
data class UsdaNutrient(
    @SerialName("nutrientId") val nutrientId: Int,
    @SerialName("nutrientName") val nutrientName: String,
    val value: Double,
    @SerialName("unitName") val unitName: String
)

@Serializable
data class EdamamFoodSearchResponse(
    val text: String,
    val parsed: List<EdamamParsed> = emptyList(),
    val hints: List<EdamamHint> = emptyList()
)

@Serializable
data class EdamamParsed(
    val food: EdamamFood
)

@Serializable
data class EdamamHint(
    val food: EdamamFood,
    val measures: List<EdamamMeasure>? = null
)

@Serializable
data class EdamamFood(
    @SerialName("foodId") val foodId: String,
    val label: String,
    val brand: String? = null,
    val category: String? = null,
    val image: String? = null,
    val nutrients: EdamamNutrients? = null
)

@Serializable
data class EdamamMeasure(
    val uri: String,
    val label: String
)

@Serializable
data class EdamamNutrients(
    @SerialName("ENERC_KCAL") val calories: Double? = null,
    @SerialName("PROCNT") val protein: Double? = null,
    @SerialName("FAT") val fat: Double? = null,
    @SerialName("CHOCDF") val carbs: Double? = null
)

// API Spoonacular - 500k+ recettes et ingrédients
interface SpoonacularApi {
    @GET("food/ingredients/search")
    suspend fun searchIngredients(
        @Query("query") query: String,
        @Query("number") number: Int = 20,
        @Query("sort") sort: String = "calories",
        @Query("sortDirection") sortDirection: String = "desc",
        @Header("X-RapidAPI-Key") apiKey: String = "demo_key"
    ): SpoonacularSearchResponse
    
    @GET("food/products/search")
    suspend fun searchProducts(
        @Query("query") query: String,
        @Query("number") number: Int = 20,
        @Header("X-RapidAPI-Key") apiKey: String = "demo_key"
    ): SpoonacularSearchResponse
}

// API USDA FoodData Central - Base de données officielle US
interface UsdaFoodApi {
    @GET("v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("dataType") dataType: String = "Branded,Survey (FNDDS)",
        @Query("pageSize") pageSize: Int = 50,
        @Query("pageNumber") pageNumber: Int = 1,
        @Query("sortBy") sortBy: String = "dataType.keyword",
        @Query("sortOrder") sortOrder: String = "asc",
        @Query("brandOwner") brandOwner: String? = null,
        @Header("X-Api-Key") apiKey: String = "DEMO_KEY"
    ): UsdaFoodSearchResponse
}

// API Edamam Food Database - 900k+ aliments
interface EdamamFoodApi {
    @GET("api/food-database/v2/parser")
    suspend fun searchFoods(
        @Query("ingr") ingredient: String,
        @Query("app_id") appId: String = "demo_id",
        @Query("app_key") appKey: String = "demo_key",
        @Query("category") category: String? = null,
        @Query("brand") brand: String? = null
    ): EdamamFoodSearchResponse
}

// Convertisseur vers notre format Product
object ProductConverter {
    fun fromSpoonacular(spoon: SpoonacularProduct): Product {
        return Product(
            code = spoon.id.toString(),
            name = spoon.title,
            imageUrl = spoon.image,
            brands = "Spoonacular",
            nutriscore = null,
            ecoscore = null,
            novaGroup = null,
            quantity = null,
            categories = "Aliments",
            ingredients = null
        )
    }
    
    fun fromUsda(usda: UsdaFood): Product {
        return Product(
            code = usda.fdcId.toString(),
            name = usda.description,
            imageUrl = null,
            brands = usda.brandOwner,
            nutriscore = null,
            ecoscore = null,
            novaGroup = null,
            quantity = null,
            categories = "USDA Database",
            ingredients = usda.ingredients
        )
    }
    
    fun fromEdamam(edamam: EdamamFood): Product {
        return Product(
            code = edamam.foodId,
            name = edamam.label,
            imageUrl = edamam.image,
            brands = edamam.brand,
            nutriscore = null,
            ecoscore = null,
            novaGroup = null,
            quantity = null,
            categories = edamam.category ?: "Aliments",
            ingredients = null
        )
    }
}

class MegaProductSearchManager(private val context: Context) {
    
    companion object {
        private const val TIMEOUT_MS = 3000L
        private const val TAG = "MegaProductSearch"
    }
    
    private val fastClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.SECONDS)
        .readTimeout(3, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Upermarket-Search/2.0")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    // APIs multiples pour maximiser les résultats
    private val spoonacularApi = Retrofit.Builder()
        .baseUrl("https://api.spoonacular.com/")
        .client(fastClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(SpoonacularApi::class.java)
    
    private val usdaApi = Retrofit.Builder()
        .baseUrl("https://api.nal.usda.gov/fdc/")
        .client(fastClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(UsdaFoodApi::class.java)
    
    private val edamamApi = Retrofit.Builder()
        .baseUrl("https://api.edamam.com/")
        .client(fastClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(EdamamFoodApi::class.java)
    
    suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val results = mutableListOf<Product>()
        
        // RECHERCHE OPENFOODFACTS UNIQUEMENT - BASE DE DONNÉES MONDIALE
        // Plus de 2 MILLIONS de produits disponibles !
        try {
            val offApi = com.example.upermarket.OpenFoodFactsApi.create()
            
            // Faire plusieurs requêtes en parallèle pour obtenir plus de résultats
            val page1 = async {
                try {
                    withTimeout(8000L) {
                        offApi.searchProducts(
                            searchTerms = query,
                            page = 1,
                            pageSize = 100
                        ).products
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Page 1 failed: ${e.message}")
                    emptyList()
                }
            }
            
            val page2 = async {
                try {
                    withTimeout(8000L) {
                        offApi.searchProducts(
                            searchTerms = query,
                            page = 2,
                            pageSize = 100
                        ).products
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Page 2 failed: ${e.message}")
                    emptyList()
                }
            }
            
            val page3 = async {
                try {
                    withTimeout(8000L) {
                        offApi.searchProducts(
                            searchTerms = query,
                            page = 3,
                            pageSize = 100
                        ).products
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Page 3 failed: ${e.message}")
                    emptyList()
                }
            }
            
            // Attendre toutes les pages
            val page1Results = page1.await()
            val page2Results = page2.await()
            val page3Results = page3.await()
            
            results.addAll(page1Results)
            results.addAll(page2Results)
            results.addAll(page3Results)
            
            Log.d(TAG, "OpenFoodFacts: Page1=${page1Results.size}, Page2=${page2Results.size}, Page3=${page3Results.size}")
            Log.d(TAG, "Total OpenFoodFacts results: ${results.size} for '$query'")
            
        } catch (e: Exception) {
            Log.e(TAG, "OpenFoodFacts search failed: ${e.message}", e)
        }
        
        val totalTime = System.currentTimeMillis() - startTime
        Log.d(TAG, "Search completed: ${results.size} total results in ${totalTime}ms")
        
        // Retourner TOUS les résultats uniques (jusqu'à 300 produits)
        val uniqueResults = results.distinctBy { it.code }
        Log.d(TAG, "Returning ${uniqueResults.size} unique products")
        uniqueResults
    }
    
    // Base MASSIVE de produits réalistes français/internationaux
    private suspend fun simulateSpoonacularResults(query: String): List<Product> {
        delay(200) // Simulation temps API
        
        val spoonacularProducts = listOf(
            // Pains & Viennoiseries
            Product(code = "sp001", name = "Baguette traditionnelle française", brands = "Boulangerie", categories = "Pains", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/001/front_fr.jpg"),
            Product(code = "sp002", name = "Croissant au beurre", brands = "Viennoiserie", categories = "Pâtisseries", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/002/front_fr.jpg"),
            Product(code = "sp003", name = "Pain complet bio", brands = "Biologique", categories = "Pains", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/003/front_fr.jpg"),
            Product(code = "sp004", name = "Pain de campagne au levain", brands = "Artisanal", categories = "Pains", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/004/front_fr.jpg"),
            Product(code = "sp005", name = "Brioche tressée", brands = "Pâtisserie", categories = "Viennoiseries", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/005/front_fr.jpg"),
            
            // Fromages
            Product(code = "sp010", name = "Camembert de Normandie AOP", brands = "Lactalis", categories = "Fromages", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/010/front_fr.jpg"),
            Product(code = "sp011", name = "Roquefort AOP", brands = "Société", categories = "Fromages", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/011/front_fr.jpg"),
            Product(code = "sp012", name = "Fromage de chèvre cendré", brands = "Fromagerie", categories = "Fromages", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/012/front_fr.jpg"),
            Product(code = "sp013", name = "Gruyère AOP", brands = "Suisse", categories = "Fromages", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/013/front_fr.jpg"),
            Product(code = "sp014", name = "Brie de Meaux", brands = "Fromagerie", categories = "Fromages", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/014/front_fr.jpg"),
            
            // Chocolats & Confiseries
            Product(code = "sp020", name = "Chocolat noir 85% cacao", brands = "Lindt", categories = "Chocolats", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/020/front_fr.jpg"),
            Product(code = "sp021", name = "Chocolat au lait noisettes", brands = "Milka", categories = "Chocolats", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/021/front_fr.jpg"),
            Product(code = "sp022", name = "Tablette chocolat blanc", brands = "Nestlé", categories = "Chocolats", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/022/front_fr.jpg"),
            Product(code = "sp023", name = "Macarons assortis", brands = "Ladurée", categories = "Pâtisseries", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/023/front_fr.jpg"),
            Product(code = "sp024", name = "Bonbons gélifiés fruits", brands = "Haribo", categories = "Confiseries", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/024/front_fr.jpg"),
            
            // Plats cuisinés
            Product(code = "sp030", name = "Ratatouille provençale", brands = "Conserverie", categories = "Plats cuisinés", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/030/front_fr.jpg"),
            Product(code = "sp031", name = "Confit de canard", brands = "Sud-Ouest", categories = "Charcuterie", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/031/front_fr.jpg"),
            Product(code = "sp032", name = "Cassoulet de Castelnaudary", brands = "Tradionnel", categories = "Plats cuisinés", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/032/front_fr.jpg"),
            Product(code = "sp033", name = "Coq au vin", brands = "Plat français", categories = "Plats cuisinés", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/033/front_fr.jpg"),
            Product(code = "sp034", name = "Bouillabaisse marseillaise", brands = "Méditerranéen", categories = "Plats cuisinés", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/034/front_fr.jpg"),
            
            // Condiments
            Product(code = "sp040", name = "Moutarde de Dijon forte", brands = "Maille", categories = "Condiments", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/040/front_fr.jpg"),
            Product(code = "sp041", name = "Vinaigre balsamique", brands = "Italien", categories = "Condiments", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/041/front_fr.jpg"),
            Product(code = "sp042", name = "Huile d'olive extra vierge", brands = "Méditerranéen", categories = "Huiles", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/042/front_fr.jpg"),
            Product(code = "sp043", name = "Sel de Guérande", brands = "Breton", categories = "Épices", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/043/front_fr.jpg"),
            Product(code = "sp044", name = "Herbes de Provence", brands = "Épices", categories = "Épices", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/044/front_fr.jpg"),
            
            // Vins & Spiritueux
            Product(code = "sp050", name = "Vin rouge Bordeaux", brands = "Vignobles", categories = "Vins", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/050/front_fr.jpg"),
            Product(code = "sp051", name = "Champagne brut", brands = "Champagne", categories = "Vins", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/051/front_fr.jpg"),
            Product(code = "sp052", name = "Vin blanc Sancerre", brands = "Loire", categories = "Vins", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/052/front_fr.jpg"),
            Product(code = "sp053", name = "Cognac XO", brands = "Hennessy", categories = "Spiritueux", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/053/front_fr.jpg"),
            Product(code = "sp054", name = "Armagnac Bas-Armagnac", brands = "Gascogne", categories = "Spiritueux", imageUrl = "https://images.openfoodfacts.org/images/products/000/000/054/front_fr.jpg")
        )
        
        // Filtrage amélioré : recherche partielle ET globale
        return spoonacularProducts.filter { product ->
            val searchTerms = query.lowercase().split(" ")
            val productText = listOfNotNull(
                product.name?.lowercase(),
                product.brands?.lowercase(), 
                product.categories?.lowercase()
            ).joinToString(" ")
            
            // Match si au moins un terme de recherche est trouvé
            searchTerms.any { term ->
                productText.contains(term) || 
                // Match partiel pour les termes courts
                (term.length >= 3 && productText.any { it.toString().contains(term) })
            }
        }
    }
    
    private suspend fun simulateUsdaResults(query: String): List<Product> {
        delay(300) // Simulation temps API
        
        val usdaProducts = listOf(
            // Grains & Céréales
            Product(code = "usda001", name = "Organic Quinoa Red", brands = "USDA Certified", categories = "Grains", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/001/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda002", name = "Brown Rice Long Grain", brands = "Whole Grains", categories = "Grains", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/002/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda003", name = "Organic Oats Steel Cut", brands = "Whole Foods", categories = "Grains", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/003/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda004", name = "Whole Wheat Pasta", brands = "Organic", categories = "Pasta", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/004/front_fr.jpg", nutriscore = "b"),
            
            // Poissons & Fruits de mer
            Product(code = "usda010", name = "Wild Atlantic Salmon", brands = "Fresh Seafood", categories = "Fish", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/010/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda011", name = "Pacific Cod Fillet", brands = "Wild Caught", categories = "Fish", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/011/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda012", name = "Alaskan King Crab", brands = "Premium", categories = "Seafood", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/012/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda013", name = "Fresh Tuna Steaks", brands = "Sashimi Grade", categories = "Fish", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/013/front_fr.jpg", nutriscore = "a"),
            
            // Viandes
            Product(code = "usda020", name = "Grass-Fed Ground Beef", brands = "Ranch", categories = "Meat", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/020/front_fr.jpg", nutriscore = "c"),
            Product(code = "usda021", name = "Free-Range Chicken Breast", brands = "Organic Farms", categories = "Poultry", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/021/front_fr.jpg", nutriscore = "b"),
            Product(code = "usda022", name = "Organic Turkey Breast", brands = "Farm Fresh", categories = "Poultry", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/022/front_fr.jpg", nutriscore = "b"),
            Product(code = "usda023", name = "Lamb Chops New Zealand", brands = "Premium", categories = "Meat", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/023/front_fr.jpg", nutriscore = "c"),
            
            // Fruits & Légumes
            Product(code = "usda030", name = "Organic Blueberries", brands = "Berry Farm", categories = "Fruits", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/030/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda031", name = "Fresh Avocados Hass", brands = "California Groves", categories = "Fruits", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/031/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda032", name = "Organic Spinach Baby", brands = "Green Farms", categories = "Vegetables", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/032/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda033", name = "Sweet Potatoes Organic", brands = "Farm Fresh", categories = "Vegetables", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/033/front_fr.jpg", nutriscore = "a"),
            Product(code = "usda034", name = "Broccoli Crowns Fresh", brands = "Organic", categories = "Vegetables", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/034/front_fr.jpg", nutriscore = "a"),
            
            // Produits laitiers
            Product(code = "usda040", name = "Organic Greek Yogurt Plain", brands = "Dairy Co-op", categories = "Dairy", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/040/front_fr.jpg", nutriscore = "b"),
            Product(code = "usda041", name = "Raw Milk Cheese Aged", brands = "Artisan", categories = "Cheese", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/041/front_fr.jpg", nutriscore = "c"),
            Product(code = "usda042", name = "Organic Whole Milk", brands = "Farm Fresh", categories = "Dairy", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/042/front_fr.jpg", nutriscore = "b"),
            
            // Autres
            Product(code = "usda050", name = "Raw Wildflower Honey", brands = "Local Beekeepers", categories = "Sweeteners", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/050/front_fr.jpg", nutriscore = "c"),
            Product(code = "usda051", name = "Extra Virgin Olive Oil", brands = "Mediterranean", categories = "Oils", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/051/front_fr.jpg", nutriscore = "c"),
            Product(code = "usda052", name = "Organic Coconut Oil", brands = "Tropical", categories = "Oils", imageUrl = "https://images.openfoodfacts.org/images/products/000/001/052/front_fr.jpg", nutriscore = "e")
        )
        
        // Filtrage amélioré identique
        return usdaProducts.filter { product ->
            val searchTerms = query.lowercase().split(" ")
            val productText = listOfNotNull(
                product.name?.lowercase(),
                product.brands?.lowercase(), 
                product.categories?.lowercase()
            ).joinToString(" ")
            
            searchTerms.any { term ->
                productText.contains(term) || 
                (term.length >= 3 && productText.any { it.toString().contains(term) })
            }
        }
    }
    
    private suspend fun simulateEdamamResults(query: String): List<Product> {
        delay(250) // Simulation temps API
        
        val edamamProducts = listOf(
            Product(code = "ed001", name = "Italian Pasta Primavera", brands = "Barilla", categories = "Pasta", imageUrl = "https://example.com/pasta.jpg"),
            Product(code = "ed002", name = "Greek Olive Oil", brands = "Mediterranean", categories = "Oils", imageUrl = "https://example.com/olive_oil.jpg"),
            Product(code = "ed003", name = "Spanish Manchego Cheese", brands = "La Mancha", categories = "Cheese", imageUrl = "https://example.com/manchego.jpg"),
            Product(code = "ed004", name = "Japanese Green Tea", brands = "Sencha", categories = "Beverages", imageUrl = "https://example.com/tea.jpg"),
            Product(code = "ed005", name = "Mexican Quinoa Salad", brands = "Fresh Mix", categories = "Salads", imageUrl = "https://example.com/quinoa_salad.jpg"),
            Product(code = "ed006", name = "Thai Coconut Milk", brands = "Aroy-D", categories = "Canned Goods", imageUrl = "https://example.com/coconut.jpg"),
            Product(code = "ed007", name = "Indian Basmati Rice", brands = "Royal", categories = "Rice", imageUrl = "https://example.com/basmati.jpg"),
            Product(code = "ed008", name = "German Rye Bread", brands = "Artisan", categories = "Bread", imageUrl = "https://example.com/rye.jpg"),
            Product(code = "ed009", name = "Brazilian Acai Bowl", brands = "Tropical", categories = "Superfoods", imageUrl = "https://example.com/acai.jpg"),
            Product(code = "ed010", name = "Korean Kimchi", brands = "Fermented", categories = "Pickled", imageUrl = "https://example.com/kimchi.jpg")
        )
        
        return edamamProducts.filter { 
            it.name?.contains(query, ignoreCase = true) == true ||
            it.brands?.contains(query, ignoreCase = true) == true ||
            it.categories?.contains(query, ignoreCase = true) == true
        }
    }
}