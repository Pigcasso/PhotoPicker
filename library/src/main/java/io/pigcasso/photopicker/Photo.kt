package io.pigcasso.photopicker

import java.io.File

/**
 * @author Zhu Liang
 */
data class Photo(var absolutePath: String,
                 val name: String,
                 val lastModified: Long,
                 val contentLength: Long,
                 val isDirectory: Boolean) {
    constructor(file: File) : this(file.absolutePath,
            file.name,
            file.lastModified(),
            file.length(),
            file.isDirectory)
}