package io.pigcasso.photopicker

import android.content.Context
import android.support.v4.content.AsyncTaskLoader
import android.util.Log

/**
 * @author Zhu Liang
 */
class AlbumsLoader(context: Context) : AsyncTaskLoader<List<Album>>(context), PhotosRepository.PhotoPickerRepositoryObserver {


    companion object {
        private val TAG = AlbumsLoader::class.java.simpleName
    }

    private val photosRepository = PhotosRepository(context)

    override fun loadInBackground(): List<Album> {
        Log.d(TAG, "loadInBackground: ")
        return photosRepository.listAlbums()
    }

    override fun deliverResult(data: List<Album>?) {
        if (isReset) {
            return
        }

        if (isStarted) {
            super.deliverResult(data)
        }
    }

    override fun onStartLoading() {
        Log.d(TAG, "onStartLoading: ")

        photosRepository.addPhotoPickerRepositoryObserver(this)

        forceLoad()
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun onReset() {
        onStopLoading()
        photosRepository.removePhotoPickerRepositoryObserver(this)
    }

    override fun onPhotoPickerChanged() {
        if (isStarted) {
            forceLoad()
        }
    }
}