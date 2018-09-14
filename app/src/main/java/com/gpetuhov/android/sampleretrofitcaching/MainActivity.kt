package com.gpetuhov.android.sampleretrofitcaching

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gpetuhov.android.samplemoshi.retrofit.QuakeService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.launch
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.anko.toast
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cacheSize = (5 * 1024 * 1024).toLong()
        val myCache = Cache(cacheDir, cacheSize)

        val okHttpClient = OkHttpClient.Builder()
                // Specify the cache we created earlier.
                .cache(myCache)
                // Add an Interceptor to the OkHttpClient.
                .addInterceptor { chain ->

                    // Get the request from the chain.
                    var request = chain.request()

                    request = if (hasNetwork(this)!!)
                        /*
                        *  If there is Internet, get the cache that was stored 30 seconds ago.
                        *  If the cache is older than 5 seconds, then discard it,
                        *  and indicate an error in fetching the response.
                        *  The 'max-age' attribute is responsible for this behavior.
                        */
                        request.newBuilder().header("Cache-Control", "public, max-age=" + 30).build()
                    else
                        /*
                        *  If there is no Internet, get the cache that was stored 7 days ago.
                        *  If the cache is older than 7 days, then discard it,
                        *  and indicate an error in fetching the response.
                        *  The 'max-stale' attribute is responsible for this behavior.
                        *  The 'only-if-cached' attribute indicates to not retrieve new data; fetch the cache only instead.
                        */
                        request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build()
                    // End of if-else statement

                    // Add the modified request to the chain.
                    chain.proceed(request)
                }
                .addNetworkInterceptor(
                        HttpLoggingInterceptor().setLevel(
                                HttpLoggingInterceptor.Level.BASIC
                        )
                )
                .build()

        val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("https://earthquake.usgs.gov/fdsnws/event/1/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        button.setOnClickListener {
            val quakeService = retrofit.create(QuakeService::class.java)

            GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT, null, {
                val result = quakeService.getQuakes("geojson", "10").execute()

                launch(Dispatchers.Main) {
                    val quakeResult = result.body()
                    val recentQuakeLocation = quakeResult?.quakeList?.firstOrNull()?.quakeProperties?.location

                    toast(recentQuakeLocation ?: "Error downloading quakes")
                }
            })
        }
    }

    fun hasNetwork(context: Context): Boolean? {
        var isConnected: Boolean? = false // Initial Value
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected)
            isConnected = true
        return isConnected
    }
}
