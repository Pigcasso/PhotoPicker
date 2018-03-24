package io.pigcasso.photopicker.sample

import android.widget.ImageView
import com.bumptech.glide.Glide
import io.pigcasso.photopicker.PhotoLoader

/**
 * @author Zhu Liang
 */
class GlidePhotoLoader : PhotoLoader {
    override fun loadPhoto(imageView: ImageView, imagePath: String, viewWidth: Int, viewHeight: Int) {
        Glide.with(imageView).load(imagePath).into(imageView)
    }
}