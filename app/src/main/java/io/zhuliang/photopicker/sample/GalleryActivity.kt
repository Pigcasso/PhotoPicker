package io.zhuliang.photopicker.sample

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * <pre>
 *     author : Julian
 *     time   : 2019/03/01
 *     desc   :
 *     version: 1.0
 * </pre>
 */

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(TextView(this).apply { text = "Gallery" })
    }
}