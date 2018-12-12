package io.nasli.filmica.view.details

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.view.*
import android.widget.Button
import android.widget.Toast
import com.squareup.picasso.Picasso
import io.nasli.filmica.R
import io.nasli.filmica.data.Film
import io.nasli.filmica.data.FilmsRepo
import io.nasli.filmica.view.util.SimpleTarget
import kotlinx.android.synthetic.main.fragment_details.*
import kotlinx.android.synthetic.main.layout_placeholder.*


class DetailsFragment: Fragment() {

    var onFilmSavedListener: OnFilmSavedListener? = null

    companion object {
        fun newInstance(id: String, tag: String): DetailsFragment {
            val instance = DetailsFragment()
            val args = Bundle()
            args.putString("id", id)
            args.putString("tag", tag)
            instance.arguments = args

            return instance
        }
    }

    private var film: Film? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnFilmSavedListener)
            onFilmSavedListener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {

        inflater.inflate(R.menu.menu_details, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        var id = arguments?.getString("id") ?: ""
        var tag = arguments?.getString("tag") ?: ""

        if (id == "-1") {
            layoutPlaceholder.visibility = View.VISIBLE
            details.visibility = View.GONE
        } else {
            layoutPlaceholder.visibility = View.GONE
            details.visibility = View.VISIBLE
        }

        film = FilmsRepo.findFilmById(id, tag)

        film?.let {
            with(it) {
                labelTitle.text = title
                labelOverview.text = overview
                labelGenre.text = genre
                labelRelease.text = release

                loadImage(it)
            }
        }


       btnAdd.setOnClickListener {
           film?.let {
               FilmsRepo.saveFilm(context!!, it) {

                   val snackbar = Snackbar.make(view!!, getString(R.string.label_snackbar_added), Snackbar.LENGTH_LONG)
                   val snackbarView = snackbar.view
                   snackbarView.findViewById<Button>(android.support.design.R.id.snackbar_action).setTextColor(Color.CYAN)
                   snackbar.setAction( getString(R.string.label_undo))
                           {
                               FilmsRepo.deleteFilm(context!!, film!!) {
                                   onFilmSavedListener?.onFilmSaved(it)
                                   Toast.makeText(context, getString(R.string.lable_toast_film_not_added), Toast.LENGTH_SHORT).show()
                               }
                           }
                   .show()
                   onFilmSavedListener?.onFilmSaved(it)
               }
           }
       }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
       // if (item.itemId == R.id.action_share) { }
        item.takeIf { item?.itemId == R.id.action_share }?.let { menuItem ->
            shareFilm()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun shareFilm() {
        val intent = Intent(Intent.ACTION_SEND)

        film?.let {
            val text = getString(R.string.template_share, it.title, it.voteRating)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, text)
        }

        startActivity(Intent.createChooser(intent, getString(R.string.title_share)))
    }

    private fun loadImage(film: Film) {
        val target = SimpleTarget(
                successCallback = { bitmap, from ->
                    imgPoster.setImageBitmap(bitmap)
                    setColorFrom(bitmap)
                }
        )

        imgPoster.tag = target

        Picasso.get()
                .load(film?.getPosterUrl())
                .error(R.drawable.placeholder)
                .into(target)
    }

    private fun setColorFrom(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            val defaultColor = ContextCompat.getColor(context!! , R.color.colorPrimary)
            val swatch = palette?.vibrantSwatch ?: palette?.dominantSwatch
            val color = swatch?.rgb ?: defaultColor
            val overlayColor = Color.argb(
                    (Color.alpha(color) * 0.5).toInt(),
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
            )

            overlay.setBackgroundColor(overlayColor)
            btnAdd.backgroundTintList = ColorStateList.valueOf(color)

        }
    }

    interface OnFilmSavedListener {
        fun onFilmSaved(film: Film)
    }
}