package com.example.coinswatcher

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coinswatcher.utils.CoinInfo
import com.example.coinswatcher.utils.CoinInfoDBLimit
import com.example.coinswatcher.utils.DatabaseHelper
import com.example.coinswatcher.utils.JsonViaHttpHandler

import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask
//import kotlinx.coroutines.experimental.*

class MainActivity : AppCompatActivity() {

    //val url = "https://api.coinmarketcap.com/v1/ticker/?limit=10" //HAS LIMIT FOR REQUESTS FROM THE SAME IP A 24 HOURS
    //val url = "https://api.myjson.com/bins/15tntl"    //USE THIS URL IF NO INTERNET CONNECTION OR EXCEDED LIMIT ON coinmarketcap.com
    //var url = "https://jsonblob.com/api/jsonBlob/efedddf5-d628-11e9-94e5-6308170feee5"    //USE THIS URL IF NO INTERNET CONNECTION OR EXCEDED LIMIT ON coinmarketcap.com

    val URLS = listOf<String>("https://api.coinmarketcap.com/v1/ticker/?limit=100",
        "https://api.myjson.com/bins/15tntl",
        "https://jsonblob.com/api/jsonBlob/efedddf5-d628-11e9-94e5-6308170feee5")

    private var coinInfoList = ArrayList<CoinInfo>()

    private var mActivity: MainActivity? = null


    private val isNetworkConnected: Boolean
        get() = (getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo != null


    lateinit var progressDialog: ProgressDialog

    private val DELAY_IN_MINUTES = 15

    private val PERIOD_OF_REQUEST_DATA = 1000*60*DELAY_IN_MINUTES.toLong()

    private var databaseHelper: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mActivity = this

        databaseHelper = DatabaseHelper(this)

        initState()

        val myPreferences = MyPreferences(this)
        var runCounter = myPreferences.getRunCount()
        runCounter++
        myPreferences.setRunCount(runCounter)
        println("runCounter $runCounter")

        recyclerView_main.layoutManager = LinearLayoutManager(this)
        recyclerView_main.adapter = MainAdapter()


        recyclerView_main.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerView_main!!, object : ClickListener {

            override fun onClick(view: View, position: Int) {
                println("onClick position $position coin= ${coinInfoList.get(position).toString()}")
                val secondIntent = Intent(mActivity, DetailedCoinActivity::class.java)
                secondIntent.putExtra("CoinInfo", coinInfoList.get(position))
                startActivity(secondIntent)
            }

            override fun onLongClick(view: View?, position: Int) {
                println("onLongClick position $position")
            }
        }))

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.mmenu_action_settings -> onSettings()
            R.id.mmenu_action_about -> onAbout()
            R.id.mmenu_action_close_app -> onCloseApp()
            else -> super.onOptionsItemSelected(item)
        }

        return true
    }


    private fun onSettings() {
        startActivity(Intent(mActivity, SettingsActivity::class.java))
    }

    private fun onAbout() {
        startActivity(Intent(mActivity, AboutActivity::class.java))
    }

    private fun onCloseApp() {
        finish()
    }

    public fun updateData(view: View) {
        initState()
    }

    private fun initState() {
        println("initState")
        coinInfoList.clear()
        //
        if (isNetworkConnected) {
            RequestJSONData().execute()
        } else {
            Toast.makeText(applicationContext, "No Internet Connection Yet!", Toast.LENGTH_SHORT).show()
        }
    }



    private fun getSelectedURL(): String {

        val myPref = MyPreferences(this)
        return URLS[myPref.getSelectedURLNum()]
    }

    //////////////////////////////////////////////////////////
    inner class RequestJSONData : AsyncTask<Void, Void, String>() {

        //@Override
        override fun onPreExecute() {
            super.onPreExecute()

            progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCancelable(false)
            progressDialog.show()
        }

        override fun doInBackground(vararg p0: Void?): String {
            var jsonViaHttpHandler =  JsonViaHttpHandler()

            while (true) {

                //REQUEST DATA FROM DB FIRST
                if(databaseHelper != null) {
                    val allList = databaseHelper!!.allCoinInfoDBLimitList
                    println("ALL Limits for coins size of allList ${allList.size}")
                    allList.forEach {
                        println("FOUND LIMIT FOR: ${it.symbol}")
                    }
                }


                coinInfoList.clear()
                var jsonString = jsonViaHttpHandler.sendHTTPRequest(getSelectedURL())
                println("Received JSON String: $jsonString")
                if (jsonString != null) {
                    try {

                        var counter = 0
                        val jsonarray = JSONArray(jsonString)
                        for (i in 0 until jsonarray.length()) {
                            val jsonObj = jsonarray.getJSONObject(i)
                            System.out.println("Parsing Array's ITEM: ${jsonObj.toString()}")

                            var coinInfo = CoinInfo(
                                jsonObj.getString("id"),
                                jsonObj.getString("name"),
                                jsonObj.getString("symbol"),
                                jsonObj.getInt("rank"),
                                jsonObj.getDouble("price_usd"),
                                jsonObj.getDouble("price_btc"),
                                jsonObj.getDouble("24h_volume_usd"),
                                jsonObj.getDouble("market_cap_usd"),
                                jsonObj.getDouble("available_supply"),
                                jsonObj.getDouble("total_supply"),
                                0.0, //if(jsonObj.getDouble("max_supply") == null) 0.0 else jsonObj.getDouble("max_supply"), //
                                jsonObj.getDouble("percent_change_1h"),
                                jsonObj.getDouble("percent_change_24h"),
                                jsonObj.getDouble("percent_change_7d"),
                                jsonObj.getLong("last_updated")

                            )
                            coinInfoList.add(coinInfo)

                            counter++

                            checkWatchedCoin(coinInfo)
                        }
                        System.out.println("Found ITEMS: $counter")


                    } catch (e: JSONException) {
                        e.printStackTrace()
                        println("Json parsing error: ")
                    }
                } else {
                    println("Could not get json from server.")
                }

                publishProgress()
                Thread.sleep(PERIOD_OF_REQUEST_DATA)
//                delay(PERIOD_OF_REQUEST_DATA)


            }//End of permanent loop

            return ""//null
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            if( coinInfoList.size > 0) {
                val mAdapter = MainAdapter()
                mAdapter.setNewCoinInfoList(coinInfoList)
                recyclerView_main.adapter = mAdapter
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss()
            }
        }

        override fun onProgressUpdate(vararg values: Void?) {
            super.onProgressUpdate(*values)

            if( coinInfoList.size > 0) {
                val mAdapter = MainAdapter()
                mAdapter.setNewCoinInfoList(coinInfoList)
                recyclerView_main.adapter = mAdapter
            }

            if (progressDialog.isShowing()) {
                progressDialog.dismiss()
            }
        }
    }
    //////////////////////////////////////////////////////////



    interface ClickListener {
        fun onClick(view: View, position: Int)

        fun onLongClick(view: View?, position: Int)
    }


    internal class RecyclerTouchListener(context: Context, recyclerView: RecyclerView, private val clickListener: ClickListener?) : RecyclerView.OnItemTouchListener {

        private val gestureDetector: GestureDetector

        init {
            gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent) {
                    val child = recyclerView.findChildViewUnder(e.x, e.y)
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child))
                    }
                }
            })
        }

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {

            val child = rv.findChildViewUnder(e.x, e.y)
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child))
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }
    }


    private fun checkWatchedCoin(coinInfo: CoinInfo): Boolean {
        if(coinInfo.symbol != null && coinInfo.price_usd > 0 && coinInfo.price_btc > 0) {
            println("checkWatchedCoin: $coinInfo")
            if(databaseHelper != null) {
                var coinLimits = databaseHelper!!.getCoinInfoDBLimitFromArray(coinInfo.symbol)
                if(coinLimits != null) {
                    val stopLoss = coinLimits!!.stopLoss_usd
                    val takeProfit = coinLimits!!.takeProfit_usd
                    if(stopLoss < coinInfo.price_usd) {
                        println("STOP LOSS FOR ${coinInfo.symbol} current price: ${coinInfo.price_usd} stop price: $stopLoss")
                        //Toast.makeText(applicationContext, "STOP LOSS FOR ${coinInfo.symbol}", Toast.LENGTH_SHORT).show()
                    }
                    if(takeProfit > coinInfo.price_usd) {
                        println("TAKE PROFIT FOR ${coinInfo.symbol} current price: ${coinInfo.price_usd} take price: $takeProfit")
                        //Toast.makeText(applicationContext, "TAKE PROFIT FOR ${coinInfo.symbol}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        //
    }

}
