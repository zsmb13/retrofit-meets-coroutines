package co.zsmb.example

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Rocket(
    val id: Int,
    @SerializedName("rocket_name")
    val name: String
)

interface SpaceXApiWithCall {
    @GET("rockets")
    fun getRockets(): Call<List<Rocket>>
}

interface SpaceXApi {
    @GET("rockets")
    suspend fun getRockets(): List<Rocket>

    @GET("rockets")
    suspend fun getRocketsResponse(): Response<List<Rocket>>
}

private fun section(title: String) {
    println()
    println(title)
}

suspend fun main() {

    val okHttpClient = OkHttpClient()

    val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl("https://api.spacexdata.com/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val callApi = retrofit.create<SpaceXApiWithCall>()

    section("Call.await()")

    val awaitedRockets: List<Rocket> = callApi.getRockets().await()
    awaitedRockets.forEach(::println)

    section("Call.awaitResponse()")

    val awaitedResponse: Response<List<Rocket>> = callApi.getRockets().awaitResponse()
    if (awaitedResponse.code() == 200) {
        awaitedResponse.body()?.forEach(::println)
    }

    val api = retrofit.create<SpaceXApi>()

    section("suspending getRockets")

    val rockets: List<Rocket> = api.getRockets()
    rockets.forEach(::println)

    section("suspending getRocketsResponse")

    val response: Response<List<Rocket>> = api.getRocketsResponse()
    if (response.code() == 200) {
        response.body()?.forEach(::println)
    }

    // Manually shut down OkHttp dispatcher so that it doesn't hang the application for a minute
    okHttpClient.dispatcher().executorService().shutdownNow()

}
