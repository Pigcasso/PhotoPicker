package io.pigcasso.photopicker

import android.content.Context
import android.provider.MediaStore
import java.io.File
import java.io.FilenameFilter
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Zhu Liang
 */
class PhotosRepository(private val context: Context) {

    fun listAlbums(): List<Album> {
        // 外部存储Uri
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
        val selection = MediaStore.Images.ImageColumns.MIME_TYPE + " = ? or " +
                MediaStore.Images.ImageColumns.MIME_TYPE + "=?"
        val selectionArgs = arrayOf("image/jpeg", "image/png")
        val sortOrder = null
        val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        val dirPaths = HashSet<String>()
        val albums = ArrayList<Album>()
        while (cursor.moveToNext()) {
            val filepath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA))
            val parentFile = File(filepath).parentFile ?: continue
            val parentPath = parentFile.absolutePath
            if (dirPaths.contains(parentPath)) {
                continue
            }
            dirPaths.add(parentPath)
            val picSize = parentFile.list(ImageFileFilter()).size
            val album = Album(Photo(File(filepath)), Photo(parentFile), picSize)
            albums.add(album)
        }
        cursor.close()
        return albums
    }

    fun listPhotos(album: Album): List<Photo> {
        return File(album.directory.absolutePath).listFiles(ImageFileFilter()).map { t ->
            Photo(t)
        }
    }

    private class ImageFileFilter : FilenameFilter {
        override fun accept(p: File, name: String): Boolean {
            return name.endsWith(".jpg", true)
                    || name.endsWith(".jpeg", true)
                    || name.endsWith("png", true)
        }
    }
}