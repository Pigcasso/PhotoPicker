package io.zhuliang.photopicker

import android.content.Context
import android.provider.MediaStore
import java.io.File

/**
 * @author Zhu Liang
 */
class PhotosRepository(private val context: Context) {

    fun listAlbums(allPhotosAlbum: Boolean): List<Album> {
        val albums = mutableListOf<Album>()
        listAlbums(albums, allPhotosAlbum)
        return albums
    }

    /**
     * @param allPhotosAlbum 是否显示所有照片的相册
     */
    private fun listAlbums(albums: MutableList<Album>, allPhotosAlbum: Boolean) {
        // 外部存储Uri
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
        val sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " DESC"
        val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)
        val dirPaths = HashMap<String, Album>()

        val allPhotos = mutableListOf<Photo>()

        // cursor 如果为 null
        cursor ?: return

        val dataColumn = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)

        while (cursor.moveToNext()) {
            val filepath = cursor.getString(dataColumn)
            val parentFile = File(filepath).parentFile ?: continue
            val parentPath = parentFile.absolutePath
            val album =
                    if (dirPaths.contains(parentPath)) {
                        dirPaths[parentPath]!!
                    } else {
                        val temp = Album(parentFile.name, arrayListOf())
                        dirPaths[parentPath] = temp
                        albums.add(temp)
                        temp
                    }

            val photo = Photo(File(filepath))
            if (allPhotosAlbum) {
                allPhotos.add(photo)
            }
            album.photos.add(photo)
        }

        if (allPhotos.isNotEmpty()) {
            albums.add(0, Album(context.resources.getString(R.string.module_photo_picker_all_photos), allPhotos))
        }

        cursor.close()
    }

    fun listPhotos(album: Album): List<Photo> {
        /*return File(album.directory.absolutePath).listFiles(ImageFileFilter()).map { t ->
            Photo(t)
        }*/
        return album.photos
    }
}