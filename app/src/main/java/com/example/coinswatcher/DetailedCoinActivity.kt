package com.example.coinswatcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.coinswatcher.utils.CoinInfo
import com.example.coinswatcher.utils.CoinInfoDBLimit
import com.example.coinswatcher.utils.DatabaseHelper

class DetailedCoinActivity : AppCompatActivity() {


    lateinit var coinInfo: CoinInfo

    private var databaseHelper: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_coin)

        databaseHelper = DatabaseHelper(this)

        val prIntent = this.intent

        coinInfo = prIntent.getParcelableExtra<CoinInfo>("CoinInfo")

        findViewById<TextView>(R.id.tv_dlg_detailed_rank_val).text = coinInfo.rank.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_id_val).text = coinInfo.id
        findViewById<TextView>(R.id.tv_dlg_detailed_name_val).text = coinInfo.name
        findViewById<TextView>(R.id.tv_dlg_detailed_symbol_val).text = coinInfo.symbol
        findViewById<TextView>(R.id.tv_dlg_detailed_price_usd_val).text = coinInfo.price_usd.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_price_btc_val).text = coinInfo.price_btc.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_24h_volume_usd_val).text = coinInfo.h24_volume_usd.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_market_cap_usd_val).text = coinInfo.market_cap_usd.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_available_supply_val).text = coinInfo.available_supply.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_total_supply_val).text = coinInfo.total_supply.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_max_supply_val).text = coinInfo.max_supply.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_percent_1h_val).text = coinInfo.percent_change_1h.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_percent_24h_val).text = coinInfo.percent_change_24h.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_percent_7d_val).text = coinInfo.percent_change_7d.toString()
        findViewById<TextView>(R.id.tv_dlg_detailed_last_upd_val).text = coinInfo.last_updated.toString()

        try {
            val coinInfoDBLimit: CoinInfoDBLimit? = databaseHelper!!.getCoinInfoDBLimit(coinInfo.symbol!!)

            if(coinInfoDBLimit != null) {
                findViewById<TextView>(R.id.tv_dlg_detailed_stop_loss_val).text =
                    coinInfoDBLimit!!.stopLoss_usd.toString()
                findViewById<TextView>(R.id.tv_dlg_detailed_take_profit_val).text =
                    coinInfoDBLimit!!.takeProfit_usd.toString()
            }
        } finally {
           // println("Can't obtain data from DB")
        }
    }

    fun onClickWatchCoin(view: View) {
        if(coinInfo != null) {
            val addsetwatchIntent = Intent(this, AddSetWatchActivity::class.java)
            addsetwatchIntent.putExtra("CoinInfo", coinInfo)
            startActivity(addsetwatchIntent)
        }
    }

    fun onCloseActivity(view: View) {
        finish()
    }
}
