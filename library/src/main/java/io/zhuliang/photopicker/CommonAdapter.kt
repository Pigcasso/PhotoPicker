package io.zhuliang.photopicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * @author Zhu Liang
 */
abstract class CommonAdapter<T>(private val resource: Int, private var mValues: List<T>) : BaseAdapter() {

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
        bindView(viewHolder, value, position)
        return rowView
    }

    protected abstract fun bindView(viewHolder: ViewHolder, value: T, position: Int)

    fun replaceData(values: List<T>) {
        setList(values)
        notifyDataSetChanged()
    }

    private fun setList(values: List<T>) {
        mValues = values
    }

    fun getItemCount() = mValues.size

    fun getValues() = mValues

    class ViewHolder(private val itemView: View) {

        fun <V : View> findViewById(id: Int): V? {
            return itemView.findViewById(id)
        }
    }
}