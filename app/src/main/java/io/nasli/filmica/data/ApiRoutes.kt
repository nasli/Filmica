package io.nasli.filmica.data

import android.net.Uri
import io.nasli.filmica.BuildConfig

object ApiRoutes {

    fun discoverUrl(
            page: Int = 1,
            language: String = "en-US",
            sort: String = "popularity-desc"
    ) : String {
        return getUriBuilder()
                .appendPath("discover")
                .appendPath("movie")
                .appendQueryParameter("language", language)
                .appendQueryParameter("sort_by", sort)
                .appendQueryParameter("page", page.toString())
                .appendQueryParameter("include_adult", "false")
                .appendQueryParameter("include_video", "false")
                .build()
                .toString()
    }

    fun trendingUrl(
            page: Int = 1
    ) : String {
        return getUriBuilder()
                .appendPath("trending")
                .appendPath("movie")
                .appendPath("week")
                .appendQueryParameter("page", page.toString())
                .build()
                .toString()
    }

    fun searchUrl(
            query: String,
            page: Int = 1,
            language: String = "en-US"
            ) : String {
        return getUriBuilder()
                .appendPath("search")
                .appendPath("movie")
                .appendQueryParameter("language", language)
                .appendQueryParameter("query",query)
                .appendQueryParameter("page", page.toString())
                .appendQueryParameter("include_adult", "false")
                .build()
                .toString()
    }

    private fun getUriBuilder() =
            Uri.Builder()
                    .scheme("https")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendQueryParameter("api_key", BuildConfig.MovieDBApiKey)
}