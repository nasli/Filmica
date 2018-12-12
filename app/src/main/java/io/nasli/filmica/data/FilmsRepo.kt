package io.nasli.filmica.data

import android.arch.persistence.room.Room
import android.content.Context
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import io.nasli.filmica.view.films.TAG_FILMS
import io.nasli.filmica.view.films.TAG_SEARCH
import io.nasli.filmica.view.films.TAG_TRENDING
import io.nasli.filmica.view.films.TAG_WATCHLIST
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

object FilmsRepo {
    private val films: MutableList<Film> = mutableListOf()
    private val trendingFilms: MutableList<Film> = mutableListOf()
    private val searchedFilms: MutableList<Film> = mutableListOf()
    private var lastPageDiscover: Int = 0
    private var lastPageTrending: Int = 0

    @Volatile
    private var db: AppDatabase? = null

    private fun getDbInstance(context: Context) : AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "filmica-db"
            ).build()
        }

        return db as AppDatabase
    }

    fun findFilmById(id: String, tag: String): Film? {

        var filmFound: Film? = null

        when(tag) {
            TAG_FILMS -> filmFound = films.find { film -> film.id == id }
            TAG_TRENDING -> filmFound = trendingFilms.find { film -> film.id == id }
            TAG_SEARCH -> filmFound = searchedFilms.find { film -> film.id == id }
            TAG_WATCHLIST -> {
                // TODO: use db findById, but it is async
                filmFound = films.find { film -> film.id == id }
                if (filmFound == null) {
                    filmFound = trendingFilms.find { film -> film.id == id }

                    if(filmFound == null) filmFound = searchedFilms.find { film -> film.id == id }
                }
            }
        }

        return filmFound
    }

    private fun dummyFilms() : List<Film>{
        return (0..9).map {
            Film(
                    title = "Film $it",
                    genre = "Genre $it",
                    release = "200$it-0$it-0$it",
                    voteRating = it.toDouble(),
                    overview = "Overview $it"
            )
        }
    }

    fun discoverFilms(
            page: Int,
            context: Context,
            callbackSuccess: ((MutableList<Film>) -> Unit),
            callbackError:((VolleyError) ->Unit)
    ){
        this.lastPageDiscover = page
        requestDiscoverFilms(page, callbackSuccess, callbackError, context)


        /*if (films.isEmpty()) {
            requestDiscoverFilms(callbackSuccess, callbackError, context)
        } else {
            callbackSuccess.invoke(films)
        }*/
    }


    fun trendingFilms(
            page: Int,
            context: Context,
            callbackSuccess: ((MutableList<Film>) -> Unit),
            callbackError:((VolleyError) ->Unit)
    ){
        this.lastPageTrending = page
        requestTrendingFilms(page, callbackSuccess, callbackError, context)

      /*  if (trendingFilms.isEmpty() && page != lastPageTrending) {
            this.lastPageTrending = page
            requestTrendingFilms(page, callbackSuccess, callbackError, context)
        } else {
            callbackSuccess.invoke(trendingFilms)
        }*/
    }

    fun searchFilms(
            query: String,
            context: Context,
            callbackSuccess: ((MutableList<Film>) -> Unit),
            callbackError:((VolleyError) ->Unit)
    ){

        requestSearchFilms(query, callbackSuccess, callbackError, context)

    }

    fun saveFilm(
            context: Context,
            film: Film,
            callbackSuccess: (Film) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().insertFilm(film)
            }

            async.await()
            callbackSuccess.invoke(film)
        }
    }

    fun watchlist(
            context: Context,
            callbackSuccess: (List<Film>) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                 db.filmDao().getFilms()
            }

            val films: List<Film> = async.await()
            callbackSuccess.invoke(films)
        }
    }

    fun getFilmById(
            context: Context,
            id: String,
            callbackSuccess: (Film) -> Unit
    ) {
        GlobalScope.launch (Dispatchers.Main) {
            val async = async(Dispatchers.IO){
                val db = getDbInstance(context)
                db.filmDao().getFilmBy(id)
            }
            val film: Film? = async.await()
            film?.let {
                callbackSuccess.invoke(it)
            }
        }
    }

    fun deleteFilm(
            context: Context,
            film: Film,
            callbackSuccess: (Film) -> Unit
    ) {
        GlobalScope.launch(Dispatchers.Main) {
            val async = async(Dispatchers.IO) {
                val db = getDbInstance(context)
                db.filmDao().deleteFilm(film)
            }
            async.await()
            callbackSuccess.invoke(film)
        }
    }

    private fun requestDiscoverFilms(
            page: Int,
            callbackSuccess: (MutableList<Film>) -> Unit,
            callbackError: (VolleyError) -> Unit,
            context: Context
    ) {
        val url = ApiRoutes.discoverUrl(page = page)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                { response ->
                    val newFilms = Film.parseFilms(response)
                    films.addAll(newFilms)
                    callbackSuccess.invoke(films)
                },
                { error ->
                    callbackError.invoke(error)
                })

        Volley.newRequestQueue(context)
                .add(request)
    }

    private fun requestTrendingFilms(
            page: Int,
            callbackSuccess: (MutableList<Film>) -> Unit,
            callbackError: (VolleyError) -> Unit,
            context: Context
    ) {
        val url = ApiRoutes.trendingUrl(page = page)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                {
                    response ->
                    val newFilms = Film.parseFilms(response)
                    trendingFilms.addAll(newFilms)
                    callbackSuccess.invoke(trendingFilms)
                },
                { error ->
                    callbackError.invoke(error)
                })

        Volley.newRequestQueue(context)
                .add(request)
    }

    private fun requestSearchFilms(
            query: String,
            callbackSuccess: (MutableList<Film>) -> Unit,
            callbackError: (VolleyError) -> Unit,
            context: Context
    ) {
        val url = ApiRoutes.searchUrl(query = query)
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                {
                    response ->
                    var newFilms = Film.parseFilms(response)

                    if (newFilms.size > 10) {
                        newFilms = newFilms.subList(0, 10)
                    }
                    searchedFilms.clear()
                    searchedFilms.addAll(newFilms)
                    callbackSuccess.invoke(searchedFilms)
                },
                { error ->
                    callbackError.invoke(error)
                })

        Volley.newRequestQueue(context)
                .add(request)
    }
}