package io.nasli.filmica.view.trending

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.nasli.filmica.R
import io.nasli.filmica.data.Film
import io.nasli.filmica.data.FilmsRepo
import io.nasli.filmica.view.details.DetailsActivity
import io.nasli.filmica.view.films.TAG_TRENDING
import io.nasli.filmica.view.util.ItemOffsetDecoration
import kotlinx.android.synthetic.main.fragment_trending.*
import kotlinx.android.synthetic.main.layout_error.*

class TrendingFragment : Fragment() {

    lateinit var listener: TrendingFragment.OnItemClickListener

    private var page = 1

    val list: RecyclerView by lazy {

        val instance = view!!.findViewById<RecyclerView>(R.id.trending)
        instance.addItemDecoration(ItemOffsetDecoration(R.dimen.offset_grid))
        instance.setHasFixedSize(true)
        instance
    }

    val adapter: TrendingAdapter by lazy {
        val instance = TrendingAdapter { film ->
            this.listener.onItemClicked(film, TAG_TRENDING)
        }
        instance
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is TrendingFragment.OnItemClickListener) {
            listener = context
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_trending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.adapter = adapter

        setRecyclerViewScrollListener()

        btnRetry?.setOnClickListener() { reload() }

    }

    fun showDetails(filmId: String) {

        val intentToDetails = Intent(this.context, DetailsActivity::class.java)
        intentToDetails.putExtra("id", filmId)
        intentToDetails.putExtra("tag", TAG_TRENDING)

        startActivity(intentToDetails)
    }

    override fun onResume() {
        super.onResume()

        this.reload()
    }

    fun reload(page: Int = 1) {

        layoutError?.visibility = View.INVISIBLE
        progress?.visibility = View.VISIBLE

        FilmsRepo.trendingFilms(page, context!!,
                { films ->
                    progress?.visibility = View.INVISIBLE
                    layoutError?.visibility = View.INVISIBLE
                    list.visibility = View.VISIBLE
                    adapter.setFilms(films)
                },
                { error ->
                    progress?.visibility = View.INVISIBLE
                    list.visibility = View.INVISIBLE
                    layoutError?.visibility = View.VISIBLE
                    error.printStackTrace()
                })
    }

    interface OnItemClickListener {
        fun onItemClicked(film: Film, tag: String)
    }


    private fun setRecyclerViewScrollListener() {

        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(list: RecyclerView, newState: Int) {
                super.onScrollStateChanged(list, newState)

                val linearLayout: LinearLayoutManager = list.layoutManager as LinearLayoutManager
                val lastItemPosition = linearLayout.findLastVisibleItemPosition()
                val totalItemCount = list!!.layoutManager!!.itemCount
                if (totalItemCount == lastItemPosition + 1) {
                    page++
                    reload(page)
                    adapter.notifyItemRangeInserted(lastItemPosition + 1, adapter.itemCount)
                }
            }
        })
    }
}