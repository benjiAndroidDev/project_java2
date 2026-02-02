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
import okhttp3.logging.HttpLoggingInterceptor

@Serializable
data class SearchResponse(
    val products: List<Product> = emptyList(),
    val count: Int = 0,
    val page: Int = 0,
    @SerialName("page_size") val pageSize: Int = 0
)

@Serializable
data class Product(
    @SerialName("product_name") val name: String? = "Produit inconnu",
    @SerialName("image_front_url") val imageUrl: String? = null,
    val brands: String? = null,
    @SerialName("nutriscore_grade") val nutriscore: String? = null,
    @SerialName("ecoscore_grade") val ecoscore: String? = null,
    @SerialName("quantity") val quantity: String? = null,
    @SerialName("categories") val categories: String? = null
)
@Serializable
data class BarModel(
    val invoiceNumber: String,
    val client: Client,
    val purchase: List<PurchaseItem>,
    val totalAmount: Double
)

@Serializable
data class Client(
    val name: String,
    val email: String,
    val address: String
)

@Serializable
data class PurchaseItem(
    val item: String,
    val quantity: Int,
    val price: Double
)
interface OpenFoodFactsApi {
    /**
     * Recherche avancée v2
     */
    @GET("api/v2/search")
    suspend fun searchProducts(
        @Query("search_terms") searchTerms: String? = null,
        @Query("fields") fields: String = "product_name,image_front_url,brands,nutriscore_grade,ecoscore_grade,quantity,categories",
        @Query("page_size") pageSize: Int = 24,
        @Query("lc") language: String = "fr",
        @Query("cc") country: String = "fr"
    ): SearchResponse

    companion object {
        private val json = Json { 
            ignoreUnknownKeys = true 
            coerceInputValues = true
        }

        fun create(): OpenFoodFactsApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val contentType = "application/json".toMediaType()
            return Retrofit.Builder()
                .baseUrl("https://world.openfoodfacts.org/")
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(OpenFoodFactsApi::class.java)
        }
    }
}
