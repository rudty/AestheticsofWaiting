package org.waiting.aestheticsofwaiting.shop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import org.waiting.aestheticsofwaiting.R
import java.util.*

/**
 * Created by d on 2017-02-01.
 */
class MenuListAdapter(context: Context, val list: ArrayList<java.util.Map.Entry<String, Any>>) : BaseAdapter() {
    val inflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(val name: TextView, val price: TextView)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var itemView = convertView
        if (convertView == null) {
            itemView = inflater.inflate(R.layout.item_menu_info, parent, false)
            val timeView = itemView.findViewById(R.id.name) as TextView
            val token = itemView.findViewById(R.id.price) as TextView
            itemView.tag = ViewHolder(timeView, token)
        }

        val holder = itemView!!.tag as ViewHolder
        val item = list[position]
        holder.name.text = item.key
        holder.price.text = item.value.toString() + "원"

        return itemView!!
    }

    override fun getItem(position: Int): Any = list[position]
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getCount(): Int = list.size

}