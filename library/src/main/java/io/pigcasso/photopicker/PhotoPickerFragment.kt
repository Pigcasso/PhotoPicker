package io.pigcasso.photopicker

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView


/**
 * @author Zhu Liang
 */
class PhotoPickerFragment : Fragment(), PhotoPickerContract.View {

    private lateinit var mAdapter: PhotosAdapter

    companion object {
        private val TAG = PhotoPickerFragment::class.java.simpleName
    }

    private lateinit var mPresenter: PhotoPickerContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = PhotosAdapter(arrayListOf())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_photo_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridView = findViewById<GridView>(R.id.gridView)
        gridView!!.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        mPresenter.start()
    }

    override fun setPresenter(presenter: PhotoPickerContract.Presenter) {
        mPresenter = presenter
    }

    override fun setLoadingIndicator(active: Boolean) {

    }

    override fun showNoAlbums() {

    }

    private class PhotosAdapter(private var mAlbums: List<Album>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var rowView = convertView
            if (rowView == null) {
                val inflater = LayoutInflater.from(parent.context)
                rowView = inflater.inflate(R.layout.grid_item_photo, parent, false)
                rowView.tag = ViewHolder(rowView)
            }

            val viewHolder = rowView!!.tag as ViewHolder
            val album = getItem(position)

            return rowView
        }

        override fun getItem(position: Int): Album {
            return mAlbums[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mAlbums.size
        }

        fun replaceData(albums: List<Album>) {
            setList(albums)
            notifyDataSetChanged()
        }

        private fun setList(albums: List<Album>) {
            mAlbums = albums
        }

        private class ViewHolder(itemView: View) {
            val iconView: ImageView = itemView.findViewById(android.R.id.icon)
            val checkBox: ImageView = itemView.findViewById(android.R.id.checkbox)
        }
    }
}