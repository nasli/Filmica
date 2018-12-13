package io.nasli.filmica.view.films

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import io.nasli.filmica.R
import io.nasli.filmica.data.Film
import io.nasli.filmica.view.details.DetailsActivity
import io.nasli.filmica.view.details.DetailsFragment
import io.nasli.filmica.view.search.SearchFragment
import io.nasli.filmica.view.trending.TrendingFragment
import io.nasli.filmica.view.watchlist.WatchlistFragment
import kotlinx.android.synthetic.main.activity_films.*
import kotlinx.android.synthetic.main.layout_placeholder.*

const val TAG_FILMS = "films"
const val TAG_WATCHLIST = "watchlist"
const val TAG_TRENDING = "trending"
const val TAG_SEARCH = "search"


class FilmsActivity : AppCompatActivity(),
        FilmsFragment.OnItemClickListener, TrendingFragment.OnItemClickListener,
        WatchlistFragment.OnItemClickListener, SearchFragment.OnItemClickListener,
        WatchlistFragment.OnFilmSavedListener, DetailsFragment.OnFilmSavedListener {

    private lateinit var filmsFragment: FilmsFragment
    private lateinit var watchlistFragment: WatchlistFragment
    private lateinit var trendingFragment: TrendingFragment
    private lateinit var searchFragment: SearchFragment
    private lateinit var activeFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_films)

        if (savedInstanceState == null) {
            setupFragments()
            if (isTablet()) showDetails("-1", TAG_FILMS)
        } else {
            val activeTag = savedInstanceState.getString("active", TAG_FILMS)
            restoreFragments(activeTag)
        }

        navigation?.setOnNavigationItemSelectedListener { item ->
            val id = item.itemId

            when (id) {
                R.id.action_discover -> showMainFragment(filmsFragment)
                R.id.action_watchlist -> showMainFragment(watchlistFragment)
                R.id.action_trending -> showMainFragment(trendingFragment)
                R.id.action_search -> showMainFragment(searchFragment)
            }

            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("active", activeFragment.tag)
    }

    private fun restoreFragments(tag: String) {
        filmsFragment = supportFragmentManager.findFragmentByTag(TAG_FILMS) as FilmsFragment
        watchlistFragment = supportFragmentManager.findFragmentByTag(TAG_WATCHLIST) as WatchlistFragment
        trendingFragment = supportFragmentManager.findFragmentByTag(TAG_TRENDING) as TrendingFragment
        searchFragment = supportFragmentManager.findFragmentByTag(TAG_SEARCH) as SearchFragment

        when (tag) {
            TAG_FILMS ->  activeFragment = filmsFragment
            TAG_WATCHLIST ->  activeFragment = watchlistFragment
            TAG_TRENDING -> activeFragment = trendingFragment
            TAG_SEARCH -> activeFragment = searchFragment
        }

    }

    private fun setupFragments() {
        filmsFragment = FilmsFragment()
        watchlistFragment = WatchlistFragment()
        trendingFragment = TrendingFragment()
        searchFragment = SearchFragment()

        supportFragmentManager.beginTransaction()
                .add(R.id.container_list, filmsFragment, TAG_FILMS)
                .add(R.id.container_list, watchlistFragment, TAG_WATCHLIST)
                .add(R.id.container_list, trendingFragment, TAG_TRENDING)
                .add(R.id.container_list, searchFragment, TAG_SEARCH)
                .hide(watchlistFragment)
                .hide(trendingFragment)
                .hide(searchFragment)
                .commit()

        activeFragment = filmsFragment

    }

    override fun onFilmSaved(film: Film) {
        watchlistFragment.loadWatchlist()
    }

    private fun showMainFragment(fragment: Fragment) {

        supportFragmentManager.beginTransaction()
                .hide(activeFragment)
                .show(fragment)
                .commit()

        activeFragment = fragment

    }

    override fun onItemClicked(film: Film, tag: String) {
        showDetails(film.id, tag)
    }

    fun showDetails(id: String, tag: String) {

        if (isTablet())
            showDetailsFragment(id, tag)
        else
            launchDetailsActivity(id, tag)
    }

    private fun isTablet() = this.container_details != null

    private fun showDetailsFragment(id: String, tag: String) {
        val detailsFragment = DetailsFragment.newInstance(id, tag)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container_details, detailsFragment)
                .commit()
    }


    private fun launchDetailsActivity(id: String, tag: String) {
        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("id", id)
        intent.putExtra("tag", tag)
        startActivity(intent)

    }
}