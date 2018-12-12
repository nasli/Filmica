package io.nasli.filmica.view.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.nasli.filmica.R
import io.nasli.filmica.data.Film
import io.nasli.filmica.data.FilmsRepo
import io.nasli.filmica.view.details.DetailsActivity
import io.nasli.filmica.view.films.TAG_SEARCH
import io.nasli.filmica.view.util.ItemOffsetDecoration
import io.nasli.filmica.view.util.afterTextChanged
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.layout_error.*
import kotlinx.android.synthetic.main.layout_no_results.*
import java.util.*

class SearchFragment : Fragment() {

    lateinit var listener: SearchFragment.OnItemClickListener
    private var timer: Timer? = null

    val list: RecyclerView by lazy {

        val instance = view!!.findViewById<RecyclerView>(R.id.search)
        instance.addItemDecoration(ItemOffsetDecoration(R.dimen.offset_grid))
        instance.setHasFixedSize(true)
        instance
    }

    val adapter: SearchAdapter by lazy {
        val instance = SearchAdapter { film ->
            this.listener.onItemClicked(film, TAG_SEARCH)
        }
        instance
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is SearchFragment.OnItemClickListener) {
            listener = context
        }
    }


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_search, container, false)

        //return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.adapter = adapter
        var textToSearch = ""


        editTextSearch.afterTextChanged {
            timer = Timer()
            textToSearch = it

            reload(it)
        }

        btnRetry?.setOnClickListener() { reload(textToSearch) }

    }


    fun showDetails(filmId: String) {

        val intentToDetails = Intent(this.context, DetailsActivity::class.java)
        intentToDetails.putExtra("id", filmId)
        intentToDetails.putExtra("tag", TAG_SEARCH)

        startActivity(intentToDetails)
    }

    override fun onResume() {
        super.onResume()

        // this.reload()
    }

    fun reload(textToSearch: String) {

        layoutError?.visibility = View.GONE
        layoutNoResults?.visibility = View.GONE

        if (textToSearch.length >= 3) {

            progress?.visibility = View.VISIBLE

            FilmsRepo.searchFilms(textToSearch, context!!,
                    { films ->
                        progress?.visibility = View.GONE
                        layoutError?.visibility = View.GONE
                        adapter.setFilms(films)

                        if (films.count() > 0) {
                            layoutNoResults?.visibility = View.GONE
                            list.visibility = View.VISIBLE
                        } else {
                            layoutNoResults?.visibility = View.VISIBLE
                            list.visibility = View.INVISIBLE
                        }
                    },
                    { error ->
                        progress?.visibility = View.GONE
                        list.visibility = View.INVISIBLE
                        layoutError?.visibility = View.VISIBLE
                        layoutNoResults?.visibility = View.GONE
                        error.printStackTrace()
                    })
        } else {

            progress?.visibility = View.GONE
            adapter.setFilms(mutableListOf())
        }
    }

    interface OnItemClickListener {
        fun onItemClicked(film: Film, tag: String)
    }
}