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
import retrofit2.http.Path
import okhttp3.Interceptor

@Serializable
data class SearchResponse(
    val products: List<Product> = emptyList(),
    val count: Int = 0,
    val page: Int = 0,
    @SerialName("page_size") val pageSize: Int = 0
)

@Serializable
data class ProductResponse(
    val code: String? = null,
    val product: Product? = null,
    val status: Int? = null,
    @SerialName("status_verbose") val statusVerbose: String? = null
)

@Serializable
data class Product(
    val code: String? = null,
    @SerialName("product_name") val name: String? = "Produit inconnu",
    @SerialName("image_front_url") val imageUrl: String? = null,
    val brands: String? = null,
    @SerialName("nutriscore_grade") val nutriscore: String? = null,
    @SerialName("ecoscore_grade") val ecoscore: String? = null,
    @SerialName("nova_group") val novaGroup: Int? = null,
    val quantity: String? = null,
    val categories: String? = null,
    val labels: String? = null,
    @SerialName("ingredients_text") val ingredients: String? = null
)

interface OpenFoodFactsApi {
    @GET("api/v2/search")
    suspend fun searchProducts(
        @Query("search_terms") searchTerms: String? = null,
        @Query("categories_tags") category: String? = null,
        @Query("brands_tags") brand: String? = null,
        @Query("sort_by") sortBy: String = "unique_scans_n",
        @Query("fields") fields: String = "code,product_name,image_front_url,brands,nutriscore_grade,ecoscore_grade,nova_group,quantity,categories,labels,ingredients_text",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
        @Query("lc") language: String = "fr",
        @Query("cc") country: String = "fr"
    ): SearchResponse

    @GET("api/v2/product/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String = "code,product_name,image_front_url,brands,nutriscore_grade,ecoscore_grade,nova_group,quantity,categories,labels,ingredients_text",
        @Query("lc") language: String = "fr",
        @Query("cc") country: String = "fr"
    ): ProductResponse

    companion object {
        private val json = Json { 
            ignoreUnknownKeys = true 
            coerceInputValues = true
        }

        fun create(): OpenFoodFactsApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", "Upermarket - Android - Version 1.0")
                        .build()
                    chain.proceed(request)
                }
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
