package io.pigcasso.photopicker

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.ListPopupWindow
import android.view.*
import android.widget.GridView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var mAlbumsAdapter: AlbumsAdapter
    private var mPhotosAdapter: PhotosAdapter? = null
    private lateinit var mCheckedPhotos: HashSet<String>
    private var mOnPhotoPickerListener: OnPhotoPickerListener? = null

    companion object {
        private const val PERMISSIONS = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val RC_READ_EXTERNAL_STORAGE = 1
        private const val EXTRA_PHOTOS = "extra.PHOTOS"
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnPhotoPickerListener) {
            mOnPhotoPickerListener = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAlbumsAdapter = AlbumsAdapter(arrayListOf())

        mCheckedPhotos = if (savedInstanceState != null) {
            savedInstanceState.getSerializable(EXTRA_PHOTOS) as HashSet<String>
        } else {
            HashSet()
        }

        setHasOptionsMenu(true)

        mOnPhotoPickerListener?.onPhotosSelect(getAllCheckedPhotos())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_photo_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViewById<View>(R.id.tv_photo_picker_selected_album_label)!!.setOnClickListener {
            showAlbumPicker()
        }
        findViewById<View>(R.id.tv_photo_picker_selected_toggle)!!.setOnClickListener {
            toggleCheckAll()
        }
        findViewById<View>(R.id.tv_photo_picker_preview)!!.setOnClickListener {
            showPhotoPreview()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val menuItem = menu.add(R.id.group_menu_photo_picker_select_done, R.id.menu_photo_picker_select_done,
                Menu.NONE, R.string.module_photo_picker_select_done)
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_photo_picker_select_done -> {
                selectDone()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (EasyPermissions.hasPermissions(context!!, PERMISSIONS)) {
            loadAlbums()
        } else {
            EasyPermissions.requestPermissions(PermissionRequest.Builder(this, RC_READ_EXTERNAL_STORAGE, PERMISSIONS).build())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(EXTRA_PHOTOS, mCheckedPhotos)
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
        PhotosAsyncTask(this, album).execute()
    }

    fun showNoAlbums() {

    }

    fun showAlbums(albums: List<Album>) {
        mAlbumsAdapter.replaceData(albums)

        if (mSelectedAlbum == null) {
            mAlbumsAdapter.setCheckedPosition(0)
            val album = albums[0]
            setSelectedAlbum(album)
            PhotosAsyncTask(this, album).execute()
        }
    }

    fun showNoPhotos() {

    }

    fun showPhotos(photos: List<Photo>) {
        val gridView = findViewById<GridView>(R.id.gridView)
        mPhotosAdapter = PhotosAdapter(this, photos)
        gridView?.adapter = mPhotosAdapter
        updateToggleText()
    }

    private fun setSelectedAlbum(album: Album) {
        mSelectedAlbum = album
        val selectedAlbumTv = findViewById<TextView>(R.id.tv_photo_picker_selected_album_label)
        selectedAlbumTv?.text = album.name
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
            mAlbumsAdapter.setCheckedPosition(position)
            setSelectedAlbum(album)
            loadPhotos(album)
            popupWindow.dismiss()
        }
        popupWindow.show()
    }

    private fun isPhotoChecked(photo: Photo): Boolean {
        return mCheckedPhotos.contains(photo.absolutePath)
    }

    fun uncheckedPhoto(photo: Photo) {
        if (mCheckedPhotos.contains(photo.absolutePath)) {
            mCheckedPhotos.remove(photo.absolutePath)
        }
    }

    fun checkPhoto(photo: Photo) {
        if (mCheckedPhotos.contains(photo.absolutePath).not()) {
            mCheckedPhotos.add(photo.absolutePath)
        }
    }

    /**
     * 用户选好图片后，点击了完成按钮
     */
    private fun selectDone() {

        if (activity == null) return

        val photos = getAllCheckedPhotos()
        if (BuildConfig.DEBUG) {
            Toast.makeText(activity, "Checked photos count: ${photos.size}", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAllCheckedPhotos(): List<String> {
        val photos = mutableListOf<String>()
        mCheckedPhotos.forEach { it ->
            photos.add(it)
        }
        return photos
    }

    /**
     * 当前选中的相册下的图片是否全部被选中
     */
    private fun isCheckedAll(): Boolean {
        /*val photos = mCheckedPhotos[mPhotosAdapter!!.getAlbumPath()]
        return (photos == null || photos.isEmpty() || photos.size < mPhotosAdapter!!.getItemCount()).not()*/
        checkNotNull(mPhotosAdapter)
        mPhotosAdapter!!.getValues().forEach {
            if (mCheckedPhotos.contains(it.absolutePath).not()) {
                return false
            }
        }
        return true
    }

    /**
     * 当前相册下的图片全选或者全不选
     */
    private fun toggleCheckAll() {
        if (mPhotosAdapter == null) return
        // 全不选
        if (isCheckedAll()) {
            mPhotosAdapter!!.getValues().forEach {
                uncheckedPhoto(it)
            }
        } else {
            mPhotosAdapter!!.getValues().forEach {
                checkPhoto(it)
            }
        }
        mOnPhotoPickerListener?.onPhotosSelect(getAllCheckedPhotos())
        updateToggleText()
        mPhotosAdapter!!.notifyDataSetChanged()
    }

    private fun showPhotoPreview() {
        if (context == null) return
        val starter = PhotoViewActivity.makeIntent(context!!, getAllCheckedPhotos())
        startActivity(starter)
    }

    /**
     * 更新"全选"或"全不选"按钮上的文字
     */
    fun updateToggleText() {
        if (view == null || mPhotosAdapter == null) {
            return
        }
        val toggleText = view!!.findViewById<TextView>(R.id.tv_photo_picker_selected_toggle)
        toggleText.setText(if (isCheckedAll()) {
            R.string.module_photo_picker_unselected_all
        } else {
            R.string.module_photo_picker_select_all
        })
    }

    private class PhotosAdapter(private val fragment: PhotoPickerFragment, photos: List<Photo>) : CommonAdapter<Photo>(R.layout.grid_item_photo, photos) {

        override fun bindView(viewHolder: ViewHolder, value: Photo, position: Int) {
            val thumbIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_photo_thumb)!!
            Glide.with(thumbIv).load(File(value.absolutePath)).into(thumbIv)

            val checkboxIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_photo_checkbox)!!
            if (fragment.isPhotoChecked(value)) {
                checkboxIv.setImageResource(R.drawable.ic_check_box_black_24dp)
            } else {
                checkboxIv.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp)
            }
            checkboxIv.setOnClickListener({
                if (fragment.isPhotoChecked(value)) {
                    fragment.uncheckedPhoto(value)
                    checkboxIv.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp)
                } else {
                    fragment.checkPhoto(value)
                    checkboxIv.setImageResource(R.drawable.ic_check_box_black_24dp)
                }
                fragment.updateToggleText()
                fragment.mOnPhotoPickerListener?.onPhotosSelect(fragment.getAllCheckedPhotos())
            })
        }
    }

    private class AlbumsAdapter(albums: List<Album>) : CommonAdapter<Album>(R.layout.list_item_album, albums) {
        private var mCheckedPosition = -1

        override fun bindView(viewHolder: ViewHolder, value: Album, position: Int) {
            val coverIv = viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_album_cover)!!
            Glide.with(coverIv).load(File(value.photos.first().absolutePath)).into(coverIv)

            viewHolder.findViewById<TextView>(R.id.tv_photo_picker_album_name)!!.text = value.name
            viewHolder.findViewById<TextView>(R.id.tv_photo_picker_photo_count)!!.text = "${value.photos.size}"

            if (mCheckedPosition == position) {
                viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_album_checkbox)?.visibility = View.VISIBLE
            } else {
                viewHolder.findViewById<ImageView>(R.id.iv_photo_picker_album_checkbox)?.visibility = View.INVISIBLE
            }
        }

        fun setCheckedPosition(checkedPosition: Int) {
            if (mCheckedPosition != checkedPosition) {
                mCheckedPosition = checkedPosition
                notifyDataSetChanged()
            }
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
            if (result.isEmpty()) {
                mReference.get()?.showNoAlbums()
            } else {
                mReference.get()?.showAlbums(result)
            }
            mReference.get()?.setLoadingIndicator(false)
        }
    }

    private class PhotosAsyncTask(fragment: PhotoPickerFragment, private val album: Album) : AsyncTask<Void, Void, List<Photo>>() {
        private val mReference = WeakReference(fragment)

        override fun doInBackground(vararg params: Void): List<Photo> {
            val fragment = mReference.get() ?: return listOf()
            val context = fragment.context ?: return listOf()
            return PhotosRepository(context).listPhotos(album)
        }

        override fun onPostExecute(result: List<Photo>) {
            super.onPostExecute(result)

            if (result.isEmpty()) {
                mReference.get()?.showNoPhotos()
            } else {
                mReference.get()?.showPhotos(result)
            }
        }
    }

    interface OnPhotoPickerListener {
        fun onPhotosSelect(photoPaths: List<String>)
    }
}