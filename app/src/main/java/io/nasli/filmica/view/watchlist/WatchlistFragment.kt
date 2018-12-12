package io.nasli.filmica.view.watchlist


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast

import kotlinx.android.synthetic.main.fragment_watchlist.*

import io.nasli.filmica.R
import io.nasli.filmica.data.Film
import io.nasli.filmica.data.FilmsRepo
import io.nasli.filmica.view.details.DetailsActivity
import io.nasli.filmica.view.films.TAG_WATCHLIST
import io.nasli.filmica.view.util.SwipeToDeleteCallback
import kotlinx.android.synthetic.main.layout_no_results.*
import kotlinx.android.synthetic.main.layout_placeholder.*

class WatchlistFragment : Fragment() {

    lateinit var onItemClicklistener: OnItemClickListener
    var onFilmSavedListener: OnFilmSavedListener? = null

    val adapter: WatchlistAdapter by lazy {
        val instance = WatchlistAdapter { film ->
            this.onItemClicklistener.onItemClicked(film, TAG_WATCHLIST)
        }
        instance
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnItemClickListener)
            onItemClicklistener = context


        if (context is OnFilmSavedListener)
            onFilmSavedListener = context
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watchlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeHandler()
        watchlist.adapter = adapter
    }

    fun showDetails(filmId: String) {

        val intentToDetails = Intent(this.context, DetailsActivity::class.java)
        intentToDetails.putExtra("id", filmId)
        intentToDetails.putExtra("tag", TAG_WATCHLIST)

        startActivity(intentToDetails)
    }

    override fun onResume() {
        super.onResume()

        loadWatchlist()
    }

    fun loadWatchlist() {
        FilmsRepo.watchlist(context!!) { films ->
            adapter.setFilms(films.toMutableList())

            if (films.count() > 0) {
                layoutPlaceholder?.visibility = View.GONE

            } else {
                layoutPlaceholder?.visibility = View.VISIBLE

            }
        }
    }

    private fun setupSwipeHandler() {
        val swipeHandler = object : SwipeToDeleteCallback() {
            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {
                deleteFilmAt(holder.adapterPosition)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(watchlist)
    }

    private fun deleteFilmAt(position: Int) {
        val film = adapter.getFilm(position)
        FilmsRepo.deleteFilm(context!!, film) {
            adapter.removeFilmAt(position)
            val snackbar = Snackbar.make(view!!, getString(R.string.label_snackbar_removed), Snackbar.LENGTH_LONG)
            val snackbarView = snackbar.view
            snackbarView.findViewById<Button>(android.support.design.R.id.snackbar_action).setTextColor(Color.CYAN)
            snackbar.setAction(getString(R.string.label_undo))
                {
                    FilmsRepo.saveFilm(context!!, film!!) {
                        onFilmSavedListener?.onFilmSaved(it)
                        Toast.makeText(context, getString(R.string.label_toast_film_not_removed), Toast.LENGTH_SHORT).show()
                    }
                }
            .show()
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(film: Film, tag: String)
    }

    interface OnFilmSavedListener {
        fun onFilmSaved(film: Film)
    }
}
