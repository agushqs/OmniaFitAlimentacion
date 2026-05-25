package com.example.omniafitalimentacion.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// "object" en Kotlin = singleton: una única instancia compartida por toda la app.
// Aquí montamos el cliente HTTP una sola vez y todo el mundo lo reutiliza.
object RetrofitClient {

    // Subdominio "es." → OFF prioriza productos vendidos en España (Hacendado,
    // Carrefour, etc.). Si usáramos "world." veríamos productos de cualquier país.
    private const val BASE_URL = "https://es.openfoodfacts.org/"

    // Interceptor de logs: imprime en Logcat la URL, headers y código de respuesta
    // de cada petición. BASIC = solo URL+code; HEADERS o BODY si necesitas más detalle.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // Un interceptor se ejecuta antes de enviar CADA request HTTP. Lo usamos para
    // añadir un header común sin tener que repetirlo en cada endpoint.
    // OpenFoodFacts pide en su política de uso que las apps se identifiquen en el
    // User-Agent para distinguirlas del tráfico anónimo.
    private val userAgentInterceptor = okhttp3.Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", "OmniaFitAlimentacion/1.0 (Android)")
            .build()
        chain.proceed(request)
    }

    // Construimos un OkHttpClient con los dos interceptors. El orden importa:
    // userAgent va primero para que el log también vea ya el header puesto.
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    // "by lazy" = la primera vez que alguien lea apiService, Kotlin construye el
    // valor; las siguientes veces devuelve el ya guardado. Si la app nunca llega
    // a usar la red, este objeto ni se crea.
    val apiService: FoodApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            // GsonConverter convierte automáticamente el JSON de la respuesta en
            // las data classes que tenemos en model/, mirando los @SerializedName.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            // .create() genera la implementación de la interface FoodApiService.
            .create(FoodApiService::class.java)
    }
}
