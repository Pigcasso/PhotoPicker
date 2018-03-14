package io.pigcasso.photopicker

/**
 * @author Zhu Liang
 */
interface PhotoPickerContract {

    interface Presenter : BasePresenter {
        /**
         * @param forceUpdate 是否显示相册选择器
         */
        fun loadAlbums(forceUpdate: Boolean)

        fun loadPhotos(album: Album)
    }

    interface View : BaseView<Presenter> {
        fun setLoadingIndicator(active: Boolean)
        fun showNoAlbums()
        fun showAlbums(albums: List<Album>)
        fun setSelectedAlbumLabel(album: Album)
        /**
         * 显示相册选择器
         */
        fun showAlbumPicker()

        fun showPhotos(album: Album, photos: List<Photo>)
    }
}