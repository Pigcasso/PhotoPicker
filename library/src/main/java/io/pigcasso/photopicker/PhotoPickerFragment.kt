package io.pigcasso.photopicker

import android.Manifest
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
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
import java.lang.ref.WeakReference


/**
 * @author Zhu Liang
 */
class PhotoPickerFragment : Fragment() {

    private var mSelectedAlbum: Album? = null
    private lateinit var mPhotoAdapter: PhotosAdapter
    private lateinit var mAlbumsAdapter: AlbumsAdapter

    companion object {
        private const val PERMISSIONS = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val RC_READ_EXTERNAL_STORAGE = 1
    }

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
            loadAlbums()
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
            loadAlbums()
        } else {
            EasyPermissions.requestPermissions(this, "rationale",
                    RC_READ_EXTERNAL_STORAGE, PERMISSIONS)
        }
    }

    fun setLoadingIndicator(active: Boolean) {
        val loadingIndicator = findViewById<View>(R.id.loadingIndicator)
        loadingIndicator?.post({
            loadingIndicator.visibility = if (active) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
        })
    }

    private fun loadAlbums() {
        if (mSelectedAlbum == null) {
            AlbumsAsyncTask(this).execute()
        }
    }

    private fun loadPhotos(album: Album) {

    }

    fun showNoAlbums() {

    }

    fun showAlbums(albums: List<Album>) {
        mAlbumsAdapter.replaceData(albums)

        if (mSelectedAlbum == null) {
            val album = albums[0]
            setSelectedAlbum(album)
            PhotosAsyncTask(this).execute(album)
        }
    }

    fun showPhotos(photos: List<Photo>) {
        mPhotoAdapter.replaceData(photos)
    }

    private fun setSelectedAlbum(album: Album) {
        mSelectedAlbum = album
        val selectedAlbumTv = findViewById<TextView>(R.id.tv_photo_picker_selected_album_label)
        selectedAlbumTv?.text = album.directory.name
    }

    fun showAlbumPicker() {
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
            setSelectedAlbum(album)
            loadPhotos(album)
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

    private class AlbumsAsyncTask(fragment: PhotoPickerFragment) : AsyncTask<Void, Void, List<Album>>() {
        private val mReference = WeakReference(fragment)

        override fun onPreExecute() {
            super.onPreExecute()
            mReference.get()?.setLoadingIndicator(true)
        }

        override fun doInBackground(vararg params: Void?): List<Album> {
            val fragment = mReference.get() ?: return listOf()
            val context = fragment.context ?: return listOf()
            return PhotosRepository(context).listAlbums()
        }

        override fun onPostExecute(result: List<Album>) {
            super.onPostExecute(result)

            if (result.isEmpty()) {
                mReference.get()?.showNoAlbums()
            } else {
                mReference.get()?.showAlbums(result)
            }
            mReference.get()?.setLoadingIndicator(false)
        }
    }

    private class PhotosAsyncTask(fragment: PhotoPickerFragment) : AsyncTask<Album, Void, List<Photo>>() {
        private val mReference = WeakReference(fragment)

        override fun doInBackground(vararg params: Album): List<Photo> {
            val fragment = mReference.get() ?: return listOf()
            val context = fragment.context ?: return listOf()
            return PhotosRepository(context).listPhotoInfos(params[0])
        }

        override fun onPostExecute(result: List<Photo>) {
            super.onPostExecute(result)

            mReference.get()?.showPhotos(result)
        }
    }
}