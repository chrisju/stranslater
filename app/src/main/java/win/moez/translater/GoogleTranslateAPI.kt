package win.moez.translater

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object GoogleTranslateAPI {
    private const val API_KEY = "YOUR_GOOGLE_CLOUD_API_KEY"
    private const val BASE_URL = "https://translation.googleapis.com/language/translate/v2"

    private val client = OkHttpClient()

    suspend fun translate(text: String, targetLang: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
                val url = "$BASE_URL?q=$encodedText&target=$targetLang&format=text&key=$API_KEY"

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                val responseBody = response.body?.string()
                val jsonResponse = JSONObject(responseBody ?: "")
                jsonResponse.getJSONObject("data")
                    .getJSONArray("translations")
                    .getJSONObject(0)
                    .getString("translatedText")
            } catch (e: Exception) {
                Log.e("GoogleTranslateAPI", "Translation failed", e)
                "翻译失败"
            }
        }
    }
}
