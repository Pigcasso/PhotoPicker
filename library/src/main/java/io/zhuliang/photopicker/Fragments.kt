package io.zhuliang.photopicker

import android.support.v4.app.Fragment
import android.view.View
import android.widget.Toast

/**
 * @author Zhu Liang
 */

fun <V : View> Fragment.findViewById(id: Int): V? {
    return view?.findViewById(id)
}

fun Fragment.showToast(text: CharSequence) {
    if (context != null) {
        Toast.makeText(context!!, text, Toast.LENGTH_SHORT).show()
    }
}