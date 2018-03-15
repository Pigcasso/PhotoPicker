package io.pigcasso.photopicker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import java.io.File

/**
 * @author Zhu Liang
 */
class PhotoViewActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_PHOTOS = "extra.PHOTOS"

        fun makeIntent(context: Context, photos: Collection<String>): Intent {
            val intent = Intent(context, PhotoViewActivity::class.java)
            intent.putStringArrayListExtra(EXTRA_PHOTOS, ArrayList(photos))
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewPager = HackyViewPager(this)
        setContentView(viewPager)

        val photos = intent.getStringArrayListExtra(EXTRA_PHOTOS)
        viewPager.adapter = PhotoDetailsAdapter(this, photos)
    }

    internal class PhotoDetailsAdapter(private val context: Context, private val mValues: ArrayList<String>) : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val itemView = PhotoView(context)
            val value = mValues[position]
            container.addView(itemView)
            Glide.with(container).load(File(value)).into(itemView)

            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return mValues.size
        }
    }
}