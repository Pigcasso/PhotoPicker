package io.pigcasso.photopicker

/**
 * @author Zhu Liang
 */
interface PhotoPickerContract {

    interface Presenter : BasePresenter {
        fun loadAlbums(forceUpdate: Boolean)
    }

    interface View : BaseView<Presenter> {
        fun setLoadingIndicator(active: Boolean)
        fun showNoAlbums()
        fun showPhotos(album: Album)
    }
}