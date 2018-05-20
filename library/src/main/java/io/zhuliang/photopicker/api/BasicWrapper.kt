package io.zhuliang.photopicker.api

import android.content.Context

/**
 * @author Zhu Liang
 */
abstract class BasicWrapper<Result, Cancel>(protected val mContext: Context) {

    protected var mRequestCode: Int = -1
    protected lateinit var mResult: Action<Result>
    protected lateinit var mCancel: Action<Cancel>

    fun requestCode(requestCode: Int): BasicWrapper<Result, Cancel> {
        mRequestCode = requestCode
        return this
    }

    fun onResult(result: Action<Result>): BasicWrapper<Result, Cancel> {
        mResult = result
        return this
    }

    fun onCancel(cancel: Action<Cancel>): BasicWrapper<Result, Cancel> {
        mCancel = cancel
        return this
    }

    abstract fun start()
}