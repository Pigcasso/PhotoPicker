package io.pigcasso.photopicker

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
        val selection = MediaStore.Images.ImageColumns.MIME_TYPE + " = ? or " +
                MediaStore.Images.ImageColumns.MIME_TYPE + "=?"
        val selectionArgs = arrayOf("image/jpeg", "image/png")
        val sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC"
        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        val dirPaths = HashMap<String, Album>()

        val allPhotos = mutableListOf<Photo>()
        while (cursor.moveToNext()) {
            val filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
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
            albums.add(0, Album("All", allPhotos))
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