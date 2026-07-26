package com.example.android_2026_1

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.StringReader

data class SteamNewsResponse(
    @SerializedName(SteamApiService.KEY_APP_NEWS) val appNews: AppNews?
)

data class AppNews(
    @SerializedName(SteamApiService.KEY_NEWS_ITEMS) val newsItems: List<NewsItem>?
)

data class NewsItem(
    @SerializedName(SteamApiService.KEY_TITLE) val title: String,
    @SerializedName(SteamApiService.KEY_URL) val url: String,
    @SerializedName(SteamApiService.KEY_AUTHOR) val author: String,
    @SerializedName(SteamApiService.KEY_CONTENTS) val contents: String,
    @SerializedName(SteamApiService.KEY_FEED_NAME) val feedname: String?
)

data class SteamStoreSearchResponse(
    @SerializedName(SteamApiService.KEY_ITEMS) val items: List<SteamAppItem>?
)

data class SteamAppItem(
    @SerializedName(SteamApiService.KEY_ID) val id: Int,
    @SerializedName(SteamApiService.KEY_NAME) val name: String
)

data class WishlistGameItem(
    @SerializedName(SteamApiService.KEY_NAME) val name: String?,
    @SerializedName(SteamApiService.KEY_CAPSULE) val capsule: String?,
    @SerializedName(SteamApiService.KEY_REVIEW_DESC) val reviewDesc: String?,
    @SerializedName(SteamApiService.KEY_PRIORITY) val priority: Int?,
    @SerializedName(SteamApiService.KEY_TAGS) val tags: List<String>?,
    @SerializedName(SteamApiService.KEY_SUBS) val subs: List<WishlistSub>?,
    @SerializedName(SteamApiService.KEY_PRERELEASE) val prerelease: Int?
)

data class WishlistSub(
    @SerializedName(SteamApiService.KEY_PRICE) val price: Int?,
    @SerializedName(SteamApiService.KEY_DISCOUNT_PCT) val discountPct: Int?
)

interface SteamApiService {
    @GET(ENDPOINT_NEWS)
    suspend fun getNewsForApp(
        @Query(PARAM_APP_ID) appId: Int,
        @Query(PARAM_COUNT) count: Int = 100,
        @Query(PARAM_FORMAT) format: String = DEFAULT_FORMAT
    ): SteamNewsResponse

    companion object {
        const val BASE_URL_API = "https://api.steampowered.com/"
        const val BASE_URL_STORE = "https://store.steampowered.com/"
        const val BASE_URL_COMMUNITY = "https://steamcommunity.com/"

        const val ENDPOINT_NEWS = "ISteamNews/GetNewsForApp/v0002/"
        const val ENDPOINT_STORE_SEARCH = "api/storesearch/"
        const val ENDPOINT_COMMUNITY_ID = "id/{id}/?xml=1"
        const val ENDPOINT_COMMUNITY_PROFILES = "profiles/{id}/?xml=1"
        const val ENDPOINT_WISHLIST_ID = "wishlist/id/{id}/wishlistdata/"
        const val ENDPOINT_WISHLIST_PROFILES = "wishlist/profiles/{id}/wishlistdata/"

        const val PARAM_APP_ID = "appid"
        const val PARAM_COUNT = "count"
        const val PARAM_FORMAT = "format"
        const val PARAM_TERM = "term"
        const val PARAM_LANGUAGE = "l"
        const val PARAM_COUNTRY = "cc"
        const val PARAM_ID = "id"

        const val DEFAULT_FORMAT = "json"
        const val DEFAULT_LANGUAGE = "english"
        const val DEFAULT_COUNTRY = "US"
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        const val KEY_APP_NEWS = "appnews"
        const val KEY_NEWS_ITEMS = "newsitems"
        const val KEY_TITLE = "title"
        const val KEY_URL = "url"
        const val KEY_AUTHOR = "author"
        const val KEY_CONTENTS = "contents"
        const val KEY_FEED_NAME = "feedname"
        const val KEY_ITEMS = "items"
        const val KEY_ID = "id"
        const val KEY_NAME = "name"
        const val KEY_CAPSULE = "capsule"
        const val KEY_REVIEW_DESC = "review_desc"
        const val KEY_PRIORITY = "priority"
        const val KEY_TAGS = "tags"
        const val KEY_SUBS = "subs"
        const val KEY_PRERELEASE = "prerelease"
        const val KEY_PRICE = "price"
        const val KEY_DISCOUNT_PCT = "discount_pct"

        const val CLAN_IMAGE_REGEX = """\{STEAM_CLAN_IMAGE\}/([^\s"<\[]+)"""
        const val CLAN_IMAGE_BASE_URL = "https://clan.akamai.steamstatic.com/images/"
    }
}

interface SteamStoreApiService {
    @GET(SteamApiService.ENDPOINT_STORE_SEARCH)
    suspend fun searchApps(
        @Query(SteamApiService.PARAM_TERM) term: String,
        @Query(SteamApiService.PARAM_LANGUAGE) l: String = SteamApiService.DEFAULT_LANGUAGE,
        @Query(SteamApiService.PARAM_COUNTRY) cc: String = SteamApiService.DEFAULT_COUNTRY
    ): SteamStoreSearchResponse

    @GET(SteamApiService.ENDPOINT_WISHLIST_ID)
    suspend fun getWishlistById(
        @Path(SteamApiService.PARAM_ID) id: String,
        @Header("Cookie") cookie: String? = null,
        @Header("User-Agent") userAgent: String = SteamApiService.DEFAULT_USER_AGENT
    ): Map<String, WishlistGameItem>

    @GET(SteamApiService.ENDPOINT_WISHLIST_PROFILES)
    suspend fun getWishlistByProfile(
        @Path(SteamApiService.PARAM_ID) id: String,
        @Header("Cookie") cookie: String? = null,
        @Header("User-Agent") userAgent: String = SteamApiService.DEFAULT_USER_AGENT
    ): Map<String, WishlistGameItem>
}

interface SteamCommunityApiService {
    @GET(SteamApiService.ENDPOINT_COMMUNITY_ID)
    suspend fun getUserById(
        @Path(SteamApiService.PARAM_ID) id: String,
        @Header("Cookie") cookie: String? = null,
        @Header("User-Agent") userAgent: String = SteamApiService.DEFAULT_USER_AGENT
    ): ResponseBody

    @GET(SteamApiService.ENDPOINT_COMMUNITY_PROFILES)
    suspend fun getUserByProfile(
        @Path(SteamApiService.PARAM_ID) id: String,
        @Header("Cookie") cookie: String? = null,
        @Header("User-Agent") userAgent: String = SteamApiService.DEFAULT_USER_AGENT
    ): ResponseBody
}

object RetrofitClient {
    private fun createRetrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val newsService: SteamApiService by lazy {
        createRetrofit(SteamApiService.BASE_URL_API).create(SteamApiService::class.java)
    }

    val storeService: SteamStoreApiService by lazy {
        createRetrofit(SteamApiService.BASE_URL_STORE).create(SteamStoreApiService::class.java)
    }

    val communityService: SteamCommunityApiService by lazy {
        createRetrofit(SteamApiService.BASE_URL_COMMUNITY).create(SteamCommunityApiService::class.java)
    }
}

object ImageUtils {
    private val clanImageRegex = Regex(SteamApiService.CLAN_IMAGE_REGEX)

    fun extractImageUrl(contents: String): String? {
        val match = clanImageRegex.find(contents)
        return match?.let { "${SteamApiService.CLAN_IMAGE_BASE_URL}${it.groupValues[1]}" }
    }
}

object XmlParserUtils {
    fun parseUserInfo(xml: String): UserProfile? {
        if (xml.contains("<error>") || xml.isBlank()) return null

        return try {
            val steamId64 = Regex("""<steamID64>(.*?)</steamID64>""").find(xml)?.groupValues?.get(1)
            val name = Regex("""<steamID><!\[CDATA\[(.*?)]]></steamID>|<steamID>(.*?)</steamID>""", RegexOption.DOT_MATCHES_ALL)
                .find(xml)?.let { it.groupValues[1].ifEmpty { it.groupValues[2] } }
            val avatar = Regex("""<avatarIcon><!\[CDATA\[(.*?)]]></avatarIcon>|<avatarIcon>(.*?)</avatarIcon>""", RegexOption.DOT_MATCHES_ALL)
                .find(xml)?.let { it.groupValues[1].ifEmpty { it.groupValues[2] } }
            val state = Regex("""<stateMessage><!\[CDATA\[(.*?)]]></stateMessage>|<stateMessage>(.*?)</stateMessage>""", RegexOption.DOT_MATCHES_ALL)
                .find(xml)?.let { it.groupValues[1].ifEmpty { it.groupValues[2] } }
            val summary = Regex("""<summary><!\[CDATA\[(.*?)]]></summary>|<summary>(.*?)</summary>""", RegexOption.DOT_MATCHES_ALL)
                .find(xml)?.let { it.groupValues[1].ifEmpty { it.groupValues[2] } }

            if (name.isNullOrEmpty()) return null
            UserProfile(name, avatar ?: "", state ?: "", summary ?: "", steamId64)
        } catch (e: Exception) {
            null
        }
    }
}

data class UserProfile(
    val name: String,
    val avatarUrl: String,
    val stateMessage: String,
    val summary: String = "",
    val steamId64: String? = null
)