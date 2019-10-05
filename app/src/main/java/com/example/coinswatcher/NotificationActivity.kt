package com.example.coinswatcher

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.coinswatcher.utils.CoinInfo

class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val isStopLoss: Boolean = intent.getBooleanExtra("isStopLoss" , false)
        val coinInfo: CoinInfo = intent.getParcelableExtra("CoinInfo")

        if(coinInfo != null) {
            findViewById<TextView>(R.id.tv_dlg_notification_coin_symbol_val).text = coinInfo.symbol
            findViewById<TextView>(R.id.tv_dlg_notification_coin_price_val).text = coinInfo.price_usd.toString()

            if(isStopLoss) {
                findViewById<TextView>(R.id.tv_dlg_notification_coin_symbol_val).setTextColor(Color.RED)
                findViewById<TextView>(R.id.tv_dlg_notification_coin_price_val).setTextColor(Color.RED)
            } else {
                findViewById<TextView>(R.id.tv_dlg_notification_coin_symbol_val).setTextColor(Color.GREEN)
                findViewById<TextView>(R.id.tv_dlg_notification_coin_price_val).setTextColor(Color.GREEN)
            }
        }
    }



    fun onCloseActivity(view: View) {
        finish()
    }
}
