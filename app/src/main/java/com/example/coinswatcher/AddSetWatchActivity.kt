package com.example.coinswatcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.coinswatcher.utils.CoinInfo
import com.example.coinswatcher.utils.CoinInfoDBLimit
import com.example.coinswatcher.utils.DatabaseHelper

class AddSetWatchActivity : AppCompatActivity() {

    private var databaseHelper: DatabaseHelper? = null

    lateinit var coinInfo: CoinInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_set_watch)

        databaseHelper = DatabaseHelper(this)

        val asIntent = this.intent
        coinInfo = asIntent.getParcelableExtra<CoinInfo>("CoinInfo")


        findViewById<TextView>(R.id.tv_dlg_add_set_coin_name_val).text = coinInfo.name
        findViewById<TextView>(R.id.tv_dlg_add_set_coin_symbol_val).text = coinInfo.symbol
        findViewById<TextView>(R.id.tv_dlg_add_set_coin_price_btc_val).text = coinInfo.price_btc.toString()
        findViewById<TextView>(R.id.tv_dlg_add_set_coin_price_usd_val).text = coinInfo.price_usd.toString()

        val coinInfoDBLimit: CoinInfoDBLimit? = databaseHelper!!.getCoinInfoDBLimit(coinInfo.symbol!!)
        if(coinInfoDBLimit != null) {
            findViewById<TextView>(R.id.te_dlg_add_set_coin_stop_loss_val).text =
                coinInfoDBLimit!!.stopLoss_usd.toString()
            findViewById<TextView>(R.id.te_dlg_add_set_coin_take_profit_val).text =
                coinInfoDBLimit!!.takeProfit_usd.toString()
        }

    }

    fun onSaveChanges(view: View) {
        if(checkUserData()) {

            val stopLossText: String = findViewById<EditText>(R.id.te_dlg_add_set_coin_stop_loss_val).text.toString()
            val takeProfitText: String = findViewById<EditText>(R.id.te_dlg_add_set_coin_take_profit_val).text.toString()
            val stopLoss: Double = if(stopLossText.equals("")) 0.0 else stopLossText.toDouble()
            val takeProfit: Double = if(takeProfitText.equals("")) 0.0 else  takeProfitText.toDouble()

            databaseHelper!!.addCoinInfoDBLimit(CoinInfoDBLimit(coinInfo.symbol!!, stopLoss, takeProfit))

            Toast.makeText(applicationContext, "Saved", Toast.LENGTH_SHORT).show()

            finish()
        }
    }

    fun onCloseActivity(view: View) {
        finish()
    }

    private fun checkUserData(): Boolean {

        val stopLossText: String = findViewById<EditText>(R.id.te_dlg_add_set_coin_stop_loss_val).text.toString()
        val takeProfitText: String = findViewById<EditText>(R.id.te_dlg_add_set_coin_take_profit_val).text.toString()
        val stopLoss: Double = if(stopLossText.equals("")) 0.0 else stopLossText.toDouble()
        val takeProfit: Double = if(takeProfitText.equals("")) 0.0 else  takeProfitText.toDouble()

//        if(stopLoss <= 0 || takeProfit <= 0) {
//            Toast.makeText(applicationContext, "Stop loss and take profit values must be > 0", Toast.LENGTH_SHORT).show()
//            return false
//        }

        if(coinInfo != null && coinInfo.price_usd <= stopLoss) {
            Toast.makeText(applicationContext, "Stop loss value must be less then current price ${coinInfo.price_usd}", Toast.LENGTH_SHORT).show()
            return false
        }

        if(coinInfo != null && coinInfo.price_usd >= takeProfit) {
            Toast.makeText(applicationContext, "Take profit value must be greater then current price ${coinInfo.price_usd}", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
