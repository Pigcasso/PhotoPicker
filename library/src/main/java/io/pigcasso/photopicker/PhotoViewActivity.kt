package io.pigcasso.photopicker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView

/**
 * @author Zhu Liang
 */
class PhotoViewActivity : AppCompatActivity() {

    private var mCurrentItem: Int = 0

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

        val currentItem = savedInstanceState?.getInt(EXTRA_CURRENT_ITEM) ?: 0
        mCurrentItem = currentItem

        val viewPager = HackyViewPager(this)
        setContentView(viewPager)

        val photos = intent.getStringArrayListExtra(EXTRA_PHOTOS)
        viewPager.adapter = PhotoDetailsAdapter(this, photos)
        viewPager.currentItem = currentItem
        supportActionBar?.title = getString(R.string.module_photo_view_preview, currentItem + 1, photos.size)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mCurrentItem = position

                val adapter = viewPager.adapter
                val size = adapter?.count
                supportActionBar?.title = getString(R.string.module_photo_view_preview, position + 1, size)
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(EXTRA_CURRENT_ITEM, mCurrentItem)
    }

    internal class PhotoDetailsAdapter(private val context: Context, private val mValues: ArrayList<String>) : PagerAdapter() {

        private val mViewWidth: Int
        private val mViewHeight: Int

        init {
            val dm = context.resources.displayMetrics
            mViewWidth = dm.widthPixels
            mViewHeight = dm.heightPixels
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val itemView = PhotoView(context)
            val value = mValues[position]
            container.addView(itemView)
            PhotoPicker.photoLoader.loadPhoto(itemView, value, mViewWidth, mViewHeight)
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