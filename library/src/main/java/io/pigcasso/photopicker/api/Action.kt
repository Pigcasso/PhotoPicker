package io.pigcasso.photopicker.api

/**
 * @author Zhu Liang
 */
interface Action<in T> {

    fun onAction(requestCode: Int, result: T)

}