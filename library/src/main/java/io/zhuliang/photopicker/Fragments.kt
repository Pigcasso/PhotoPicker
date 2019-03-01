package io.zhuliang.photopicker

import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

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

fun Fragment.showToast(@StringRes resId: Int) {
    if (context != null) {
        Toast.makeText(context!!, resId, Toast.LENGTH_SHORT).show()
    }
}