package io.zhuliang.photopicker

import java.io.File

/**
 * @author Zhu Liang
 */
data class Photo(var absolutePath: String,
                 val name: String) {
    constructor(file: File) : this(file.absolutePath,
            file.name)
}