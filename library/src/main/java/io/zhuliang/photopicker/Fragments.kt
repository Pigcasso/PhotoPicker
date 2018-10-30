package io.zhuliang.photopicker

import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast

/**
 * @author Zhu Liang
 */

fun <V : View> androidx.fragment.app.Fragment.findViewById(id: Int): V? {
    return view?.findViewById(id)
}

fun androidx.fragment.app.Fragment.showToast(text: CharSequence) {
    if (context != null) {
        Toast.makeText(context!!, text, Toast.LENGTH_SHORT).show()
    }
}