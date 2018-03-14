package io.pigcasso.photopicker

import android.support.v4.app.Fragment
import android.view.View

/**
 * @author Zhu Liang
 */

fun <V : View> Fragment.findViewById(id: Int): V? {
    return view?.findViewById(id)
}