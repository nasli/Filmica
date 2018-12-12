package io.nasli.filmica.view.search

import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.view.View
import com.squareup.picasso.Picasso
import io.nasli.filmica.R
import io.nasli.filmica.data.Film
import io.nasli.filmica.view.util.BaseFilmAdapter
import io.nasli.filmica.view.util.BaseFilmHolder
import io.nasli.filmica.view.util.SimpleTarget
import kotlinx.android.synthetic.main.item_search.view.*

class SearchAdapter(itemClickListener: ((Film) -> Unit)? = null):
        BaseFilmAdapter<SearchAdapter.SearchViewHolder>(
                layoutItem = R.layout.item_search,
                holderCreator = {view -> SearchViewHolder(view, itemClickListener)}
        ) {

    class  SearchViewHolder(
            view: View,
            listener: ((Film) -> Unit)? = null
    ): BaseFilmHolder(view, listener) {

        override fun bindFilm(film: Film) {
            super.bindFilm(film)

            with(itemView) {
                labelTitle.text = film.title
                labelOverview.text = film.overview
                labelVotes.text = film.voteRating.toString()
                loadImage()
            }
        }

        private fun loadImage() {
            val target = SimpleTarget(
                    successCallback = { bitmap, from ->
                        itemView.imgPoster.setImageBitmap(bitmap)
                       // setColorFrom(bitmap)
                    }
            )

            itemView.imgPoster.tag = target

            Picasso.get()
                    .load(film?.getPosterUrl())
                    .error(R.drawable.placeholder)
                    .into(target)
        }

      /*  private fun setColorFrom(bitmap: Bitmap) {
            Palette.from(bitmap).generate { palette ->
                val defaultColor = ContextCompat.getColor(itemView.context, R.color.colorPrimary)
                val swatch = palette?.vibrantSwatch ?: palette?.dominantSwatch
                val color = swatch?.rgb ?: defaultColor

                itemView.setBackgroundColor(color)
            }
        }*/
    }
}