package io.zhuliang.photopicker.api

/**
 * @author Zhu Liang
 */
interface Choice<out Multiple, out Single> {

    fun multipleChoice(): Multiple

    fun singleChoice(): Single
}