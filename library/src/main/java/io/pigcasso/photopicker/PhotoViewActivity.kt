package io.pigcasso.photopicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.github.chrisbanes.photoview.PhotoView

/**
 * @author Zhu Liang
 */
class PhotoViewActivity : AppCompatActivity() {

    private var mCurrentItem: Int = 0
    private lateinit var mPhotos: ArrayList<String>
    private lateinit var mCheckedPhotos: HashMap<String, Boolean>
    private lateinit var mCheckBoxIv: ImageView

    private lateinit var checkboxOutlineDrawable: Drawable
    private lateinit var checkboxDrawable: Drawable

    companion object {
        private const val EXTRA_PHOTOS = "extra.PHOTOS"
        const val EXTRA_CHECKED_PHOTOS = "extra.CHECKED_PHOTOS"

        fun makeIntent(context: Context, photos: ArrayList<String>): Intent {
            val intent = Intent(context, PhotoViewActivity::class.java)
            intent.putStringArrayListExtra(EXTRA_PHOTOS, photos)
            return intent
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_photo_view)

        checkboxOutlineDrawable = ThemeConfig.tint(this, R.drawable.ic_check_box_outline_blank_black_24dp, PhotoPicker.themeConfig.checkboxOutlineColor)
        checkboxDrawable = ThemeConfig.tint(this, R.drawable.ic_check_box_black_24dp, PhotoPicker.themeConfig.checkboxColor)

        val currentItem = savedInstanceState?.getInt(EXTRA_CURRENT_ITEM) ?: 0
        mCurrentItem = currentItem

        mPhotos = intent.getStringArrayListExtra(EXTRA_PHOTOS)

        mCheckedPhotos = if (savedInstanceState == null) {
            val checkedPhotos = HashMap<String, Boolean>()
            mPhotos.forEach({ it ->
                checkedPhotos[it] = true
            })
            checkedPhotos
        } else {
            savedInstanceState.getSerializable(EXTRA_CHECKED_PHOTOS) as HashMap<String, Boolean>
        }

        val viewPager = findViewById<ViewPager>(R.id.vp_photo_view_gallery)
        viewPager.adapter = PhotoDetailsAdapter(this, mPhotos)
        viewPager.currentItem = currentItem
        supportActionBar?.title = getString(R.string.module_photo_view_preview, currentItem + 1, mPhotos.size)

        viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                mCurrentItem = position

                val adapter = viewPager.adapter
                val size = adapter?.count
                supportActionBar?.title = getString(R.string.module_photo_view_preview, position + 1, size)

                val photo = mPhotos[position]
                val isChecked = isCheckedPhoto(photo)
                setCheckedPhoto(photo, isChecked)
            }
        })

        mCheckBoxIv = findViewById(R.id.iv_photo_view_checkbox)
        val checkBoxTv = findViewById<View>(R.id.tv_photo_view_checkbox)
        val listener = View.OnClickListener {
            val photo = mPhotos[viewPager.currentItem]
            val isChecked = isCheckedPhoto(photo)
            setCheckedPhoto(photo, !isChecked)
        }
        mCheckBoxIv.setOnClickListener(listener)
        checkBoxTv.setOnClickListener(listener)

        val photo = mPhotos[viewPager.currentItem]
        setCheckedPhoto(photo, isCheckedPhoto(photo))
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(EXTRA_CURRENT_ITEM, mCurrentItem)
        outState?.putSerializable(EXTRA_CHECKED_PHOTOS, mCheckedPhotos)
    }

    override fun onBackPressed() {
        val data = Intent()
        val checkedPhotos = ArrayList<String>()
        mPhotos.forEach({ it ->
            if (mCheckedPhotos[it] == true) {
                checkedPhotos.add(it)
            }
        })
        data.putStringArrayListExtra(EXTRA_CHECKED_PHOTOS, checkedPhotos)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun setCheckedPhoto(photo: String, isChecked: Boolean) {
        mCheckedPhotos[photo] = isChecked
        mCheckBoxIv.setImageDrawable(if (isChecked) {
            checkboxDrawable
        } else {
            checkboxOutlineDrawable
        })
    }

    private fun isCheckedPhoto(photo: String): Boolean {
        return mCheckedPhotos[photo] ?: false
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