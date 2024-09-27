package com.betrybe.currencyview.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.betrybe.currencyview.R

class CurrencyRateAdapter(
    private val rates: Map<String, Double>,
    private val currencyList: Map<String, String>
) : RecyclerView.Adapter<CurrencyRateAdapter.CurrencyRateViewHolder>() {

    class CurrencyRateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val currencyName: TextView = itemView.findViewById(R.id.currency_name)
        val currencyRate: TextView = itemView.findViewById(R.id.currency_rate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyRateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_blank, parent, false)
        return CurrencyRateViewHolder(view)
    }

    override fun onBindViewHolder(holder: CurrencyRateViewHolder, position: Int) {
        val currency = rates.keys.toList()[position]
        val rate = rates[currency]
        val vRate = rate?.toLong()

        // Agora pegando o nome completo da moeda usando o ListaMoedas
        val currencyName = currencyList[currency] ?: currency

        holder.currencyName.text = "$currency - $currencyName"
        holder.currencyRate.text = rate.toString()

        if(vRate != null && vRate <= 1) {
            holder.currencyRate.setTextColor(holder.itemView.context.getColor(R.color.positivo))
        } else {
            holder.currencyRate.setTextColor(holder.itemView.context.getColor(R.color.negativo))
        }
    }



    override fun getItemCount(): Int = rates.size
}
