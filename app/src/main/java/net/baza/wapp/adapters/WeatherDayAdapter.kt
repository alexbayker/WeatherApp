package net.baza.wapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import net.baza.wapp.R
import net.baza.wapp.databinding.ListWeatherBinding
import net.baza.wapp.models.WeatherDay

class WeatherDayAdapter(private val weatherdays: List<WeatherDay>) : RecyclerView.Adapter<WeatherDayAdapter.MyViewHolder>()
{
    class MyViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview)
    {
        var binding: ListWeatherBinding? = null

        init
        {
            binding = DataBindingUtil.bind(itemview)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_weather, parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int)
    {
        //holder.setIsRecyclable(false)
        holder.binding?.day = weatherdays[position]
    }

    override fun getItemCount() = weatherdays.size

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemViewType(position: Int) = position
}