package com.example.coinswatcher

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.coinswatcher.utils.CoinInfo
import kotlinx.android.synthetic.main.coin_row.view.*

class MainAdapter : RecyclerView.Adapter<CustomViewHolder>() {

    private var coinInfoList = ArrayList<CoinInfo>()

    override fun getItemCount(): Int {
        return coinInfoList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {

        val layoutInflater = LayoutInflater.from(parent?.context)
        val cellForRow = layoutInflater.inflate(R.layout.coin_row, parent, false)

        return CustomViewHolder(cellForRow)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {

        val coinInfo = coinInfoList.get(position)
        holder.view.textView_coin_rank.text = coinInfo.rank.toString()
        holder.view.textView_coin_name .text = coinInfo.name
        holder.view.textView_coin_symbol.text = coinInfo.symbol
        holder.view.textView_coin_percentage.text = coinInfo.percent_change_24h.toString()
        holder.view.textView_coin_percentage.setTextColor( if(coinInfo.percent_change_24h < 0) Color.RED else Color.GREEN)
        holder.view.textView_coin_usd_price.text = "$ ${coinInfo.price_usd.toString()}"
        holder.view.textView_coin_cap.text = "Market Cap. $ ${coinInfo.market_cap_usd.toString()} Bn"
    }

    public fun setNewCoinInfoList(lst: ArrayList<CoinInfo> ) {
        coinInfoList = lst
    }


}



class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

}