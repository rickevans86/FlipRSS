package com.fliprss.app.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

internal interface GReaderApi {
    @FormUrlEncoded @POST("accounts/ClientLogin")
    suspend fun login(@Field("Email") username: String, @Field("Passwd") password: String,
        @Field("service") service: String = "reader",
        @Field("accountType") accountType: String = "HOSTED_OR_GOOGLE"): Response<ResponseBody>
    @GET("reader/api/0/subscription/list?output=json")
    suspend fun subscriptions(@Header("Authorization") auth: String): Response<SubscriptionList>
    @GET("reader/api/0/tag/list?output=json")
    suspend fun tags(@Header("Authorization") auth: String): Response<TagList>
    @GET("reader/api/0/unread-count?output=json")
    suspend fun counts(@Header("Authorization") auth: String): Response<CountList>
    @GET("reader/api/0/stream/contents/{stream}?output=json")
    suspend fun contents(@Header("Authorization") auth: String, @Path(value = "stream", encoded = true) stream: String,
        @Query("c") continuation: String?, @Query("xt") exclude: String? = null, @Query("n") count: Int = 50): Response<ItemList>
    @GET("reader/api/0/token")
    suspend fun token(@Header("Authorization") auth: String): Response<ResponseBody>
    @FormUrlEncoded @POST("reader/api/0/edit-tag")
    suspend fun edit(@Header("Authorization") auth: String, @Field("T") token: String,
        @Field("i") id: String, @Field("a") add: String?, @Field("r") remove: String?): Response<ResponseBody>
    @FormUrlEncoded @POST("reader/api/0/subscription/quickadd")
    suspend fun addFeed(@Header("Authorization") auth: String, @Field("T") token: String,
        @Field("quickadd") url: String): Response<QuickAddResult>
    @FormUrlEncoded @POST("reader/api/0/mark-all-as-read")
    suspend fun markAllRead(@Header("Authorization") auth: String, @Field("T") token: String,
        @Field("s") stream: String, @Field("ts") cutoffMicros: String): Response<ResponseBody>
}
internal data class QuickAddResult(val numResults: Int? = null, val streamId: String? = null)
internal data class SubscriptionList(val subscriptions: List<Subscription>? = null)
internal data class Subscription(val id: String? = null, val title: String? = null,
    val htmlUrl: String? = null, val categories: List<Tag>? = null)
internal data class TagList(val tags: List<Tag>? = null)
internal data class Tag(val id: String? = null, val label: String? = null)
internal data class CountList(val unreadcounts: List<UnreadCount>? = null)
internal data class UnreadCount(val id: String? = null, val count: Int? = null)
internal data class ItemList(val items: List<Item>? = null, val continuation: String? = null)
internal data class Link(val href: String? = null, val type: String? = null)
internal data class Content(val content: String? = null)
internal data class Origin(val streamId: String? = null, val title: String? = null, val htmlUrl: String? = null)
internal data class Item(val id: String? = null, val title: String? = null,
    val categories: List<String>? = null, val summary: Content? = null, val content: Content? = null,
    val canonical: List<Link>? = null, val alternate: List<Link>? = null,
    val enclosure: List<Link>? = null, val origin: Origin? = null,
    @SerializedName("media:content") val mediaContent: JsonElement? = null,
    @SerializedName("media:group") val mediaGroup: JsonElement? = null)
