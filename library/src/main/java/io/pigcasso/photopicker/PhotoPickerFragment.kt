package io.pigcasso.photopicker

import android.Manifest
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.ListPopupWindow
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest
import java.io.File


/**
 * @author Zhu Liang
 */
class PhotoPickerFragment : Fragment(), PhotoPickerContract.View {

    private lateinit var mPhotoAdapter: PhotosAdapter
    private lateinit var mAlbumsAdapter: AlbumsAdapter

    companion object {
        private const val PERMISSIONS = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val RC_READ_EXTERNAL_STORAGE = 1
    }

    private lateinit var mPresenter: PhotoPickerContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAlbumsAdapter = AlbumsAdapter(arrayListOf())
        mPhotoAdapter = PhotosAdapter(arrayListOf())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_photo_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridView = findViewById<GridView>(R.id.gridView)
        gridView!!.adapter = mPhotoAdapter

        findViewById<View>(R.id.tv_photo_picker_selected_album_label)!!.setOnClickListener {
            showAlbumPicker()
        }
    }

    override fun onResume() {
        super.onResume()
        if (EasyPermissions.hasPermissions(context!!, PERMISSIONS)) {
            mPresenter.start()
        } else {
            EasyPermissions.requestPermissions(PermissionRequest.Builder(this, RC_READ_EXTERNAL_STORAGE, PERMISSIONS).build())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private fun requestPermissions() {
        if (EasyPermissions.hasPermissions(context!!, PERMISSIONS)) {
            mPresenter.start()
        } else {
            EasyPermissions.requestPermissions(this, "rationale",
                    RC_READ_EXTERNAL_STORAGE, PERMISSIONS)
        }
    }

    override fun setPresenter(presenter: PhotoPickerContract.Presenter) {
        mPresenter = presenter
    }

    override fun setLoadingIndicator(active: Boolean) {
        val loadingIndicator = findViewById<View>(R.id.loadingIndicator)
        loadingIndicator?.post({
            loadingIndicator.visibility = if (active) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        })
    }

    override fun showNoAlbums() {

    }

    override fun showAlbums(albums: List<Album>) {
        mAlbumsAdapter.replaceData(albums)
    }

    override fun showPhotos(album: Album, photos: List<Photo>) {
        mPhotoAdapter.replaceData(photos)
    }

    override fun setSelectedAlbumLabel(album: Album) {
        val selectedAlbumTv = findViewById<TextView>(R.id.tv_photo_picker_selected_album_label)
        selectedAlbumTv?.text = album.directory.name
    }

    override fun showAlbumPicker() {
        val gridView = findViewById<GridView>(R.id.gridView) ?: return

        val popupWindow = ListPopupWindow(context!!)
        popupWindow.width = gridView.width
        popupWindow.height = gridView.height

        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.anchorView = view!!.findViewById(R.id.popup_anchor)
        popupWindow.setContentWidth((gridView.height * 0.9).toInt())
        popupWindow.isModal = true
        popupWindow.setAdapter(mAlbumsAdapter)
        popupWindow.setOnItemClickListener { _, _, position, _ ->
            val album = mAlbumsAdapter.getItem(position)
            setSelectedAlbumLabel(album)
            mPresenter.loadPhotos(album)
            popupWindow.dismiss()
        }
        popupWindow.show()
    }

    private class PhotosAdapter(private var mPhotos: List<Photo>) : BaseAdapter() {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var rowView = convertView
            if (rowView == null) {
                val inflater = LayoutInflater.from(parent.context)
                rowView = inflater.inflate(R.layout.grid_item_photo, parent, false)
                rowView.tag = ViewHolder(rowView)
            }

            val viewHolder = rowView!!.tag as ViewHolder
            val photo = getItem(position)

            Glide.with(parent).load(File(photo.absolutePath)).into(viewHolder.iconView)

            return rowView
        }

        override fun getItem(position: Int): Photo {
            return mPhotos[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return mPhotos.size
        }

        fun replaceData(photos: List<Photo>) {
            setList(photos)
            notifyDataSetChanged()
        }

        private fun setList(photos: List<Photo>) {
            mPhotos = photos
        }

        private class ViewHolder(itemView: View) {
            val iconView: ImageView = itemView.findViewById(android.R.id.icon)
            val checkBox: ImageView = itemView.findViewById(android.R.id.checkbox)
        }
    }

    private class AlbumsAdapter(private var mAlbums: List<Album>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var rowView = convertView
            if (rowView == null) {
                val inflater = LayoutInflater.from(parent.context)
                rowView = inflater.inflate(R.layout.list_item_album, parent, false)

                val viewHolder = ViewHolder(rowView)
                rowView.tag = viewHolder
            }

            val viewHolder = rowView!!.tag as ViewHolder
            val album = getItem(position)

            viewHolder.nameView.text = album.directory.name
            Glide.with(parent).load(File(album.cover.absolutePath)).into(viewHolder.coverView)

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

        private class ViewHolder(itemView: View) {
            val coverView: ImageView = itemView.findViewById(R.id.iv_photo_picker_album_cover)
            val nameView: TextView = itemView.findViewById(R.id.tv_photo_picker_album_name)
        }

        fun replaceData(albums: List<Album>) {
            setList(albums)
            notifyDataSetChanged()
        }

        private fun setList(albums: List<Album>) {
            mAlbums = albums
        }
    }
}