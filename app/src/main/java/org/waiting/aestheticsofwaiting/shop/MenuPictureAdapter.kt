package org.waiting.aestheticsofwaiting.shop

import android.content.Context
import android.net.Uri
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import org.waiting.aestheticsofwaiting.R
import java.util.*

/**
 * Created by d on 2017-02-03.
 */
class MenuPictureAdapter(val mContext: Context) : PagerAdapter() {

    private val imageList = ArrayList<Uri>()
    private val inflater = LayoutInflater.from(mContext)

    fun add(uri: Uri) {
        imageList.add(uri)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val image = inflater.inflate(R.layout.item_menu_image, container, false) as ImageView
//        image.setImageResource(R.drawable.logo_half)
//        Log.e("LOAD", imageList[position].toString())
        Glide.with(mContext)
                .load(imageList[position])
                .into(image)
        container?.addView(image)
        return image
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view === `object`
    }

    override fun getCount(): Int = imageList.size

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        (container as ViewPager).removeView(`object` as View)
    }

}