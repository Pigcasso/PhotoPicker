package io.pigcasso.photopicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * @author Zhu Liang
 */
abstract class CommonAdapter<T>(private val resource: Int, val mValues: List<T>) : BaseAdapter() {

    override fun getItem(position: Int) = mValues[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = mValues.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView = convertView
        if (rowView == null) {
            val layoutInflater = LayoutInflater.from(parent.context)
            rowView = layoutInflater.inflate(resource, parent, false)
            rowView.tag = ViewHolder(rowView)
        }
        val viewHolder = rowView!!.tag as ViewHolder
        val value = getItem(position)
        bindView(viewHolder, value)
        return rowView
    }

    protected abstract fun bindView(viewHolder: ViewHolder, value: T)

    class ViewHolder(val itemView: View) {
        fun <V : View> findViewById(id: Int) = itemView.findViewById<V>(id)
    }
}