package io.pigcasso.photopicker

import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader

/**
 * @author Zhu Liang
 */
class PhotoPickerPresenter(private val mLoader: AlbumsLoader, private val mLoaderManager: LoaderManager,
                           private val mPhotosRepository: PhotosRepository,
                           private val mPhotoPickerView: PhotoPickerContract.View) : PhotoPickerContract.Presenter, LoaderManager.LoaderCallbacks<List<Album>> {
    init {
        mPhotoPickerView.setPresenter(this)
    }

    private var mSelectedAlbum: Album? = null

    override fun start() {
        mLoaderManager.initLoader(0, null, this)
    }

    override fun loadAlbums(forceUpdate: Boolean) {
        mPhotosRepository.notifyContentObserver()
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<Album>> {
        mPhotoPickerView.setLoadingIndicator(true)
        return mLoader
    }

    override fun onLoadFinished(loader: Loader<List<Album>>?, data: List<Album>?) {
        mPhotoPickerView.setLoadingIndicator(false)

        if (data == null || data.isEmpty()) {
            mPhotoPickerView.showNoAlbums()
        } else {
            mPhotoPickerView.showAlbums(data)
            if (mSelectedAlbum == null) {
                mSelectedAlbum = data.first()
                mPhotoPickerView.showPhotos(mSelectedAlbum!!,
                        mPhotosRepository.listPhotoInfos(mSelectedAlbum!!))
                mPhotoPickerView.setSelectedAlbumLabel(mSelectedAlbum!!)
            }
        }
    }

    override fun loadPhotos(album: Album) {
        mSelectedAlbum = album
        mPhotoPickerView.showPhotos(album, mPhotosRepository.listPhotoInfos(album))
    }

    override fun onLoaderReset(loader: Loader<List<Album>>?) {
        // no-op
    }
}