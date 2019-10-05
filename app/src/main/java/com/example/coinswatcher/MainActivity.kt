package com.example.coinswatcher

import android.app.*
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
import com.example.coinswatcher.utils.DatabaseHelper
import com.example.coinswatcher.utils.JsonViaHttpHandler
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import kotlin.collections.ArrayList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import java.lang.ref.WeakReference


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


    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelID = "com.example.coinswatcher"
    private val description = "NotificationDescription"
    private var mNotificationId: Int = 1000

    private val LOG_TAG = "debugLog"

    private var requestJSONData: RequestJSONData? = null

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
        Log.d(LOG_TAG,"runCounter $runCounter")

        recyclerView_main.layoutManager = LinearLayoutManager(this)
        recyclerView_main.adapter = MainAdapter()


        recyclerView_main.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerView_main!!, object : ClickListener {

            override fun onClick(view: View, position: Int) {
                Log.d(LOG_TAG,"onClick position $position coin= ${coinInfoList.get(position).toString()}")
                val secondIntent = Intent(mActivity, DetailedCoinActivity::class.java)
                secondIntent.putExtra("CoinInfo", coinInfoList.get(position))
                startActivity(secondIntent)
            }

            override fun onLongClick(view: View?, position: Int) {
                Log.d(LOG_TAG,"onLongClick position $position")
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
        Log.d(LOG_TAG,"initState")
        coinInfoList.clear()
        //
        if (isNetworkConnected) {
            requestJSONData = RequestJSONData(this)
            (requestJSONData as RequestJSONData).execute()
        } else {
            Toast.makeText(applicationContext, "No Internet Connection Yet!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if(requestJSONData != null) {
            (requestJSONData as RequestJSONData).cancel(true)
        }
    }



    public fun getSelectedURL(): String {

        val myPref = MyPreferences(this)
        return URLS[myPref.getSelectedURLNum()]
    }

    //////////////////////////////////////////////////////////

    companion object {
        class RequestJSONData internal constructor(context: MainActivity) :
            AsyncTask<Void, Void, String>() {

            private val activityRef: WeakReference<MainActivity> = WeakReference(context)

            private  val LOG_TAG = "debugLogThread"

            //@Override
            override fun onPreExecute() {
                super.onPreExecute()

                val activity = activityRef.get()
                if (activity == null || activity.isFinishing) return

                activity.progressDialog = ProgressDialog(activity)
                activity.progressDialog.setMessage("Please wait...")
                activity.progressDialog.setCancelable(false)
                activity.progressDialog.show()
            }

            override fun doInBackground(vararg p0: Void?): String {

                val activity = activityRef.get()
                if (activity == null || activity.isFinishing) return ""

                var jsonViaHttpHandler =  JsonViaHttpHandler()

                while (true) {

                    //REQUEST DATA FROM DB FIRST
                    if(activity.databaseHelper != null) {
                        val allList = activity.databaseHelper!!.allCoinInfoDBLimitList
                        Log.d(LOG_TAG,"ALL Limits for coins size of allList ${allList.size}")
                        allList.forEach {
                            Log.d(LOG_TAG,"FOUND LIMIT FOR: ${it.symbol}")
                        }
                    }


                    activity.coinInfoList.clear()
                    var jsonString = jsonViaHttpHandler.sendHTTPRequest(activity.getSelectedURL())
                    Log.d(LOG_TAG,"Received JSON String: $jsonString")
                    if (jsonString != null) {
                        try {

                            var counter = 0
                            val jsonarray = JSONArray(jsonString)
                            for (i in 0 until jsonarray.length()) {
                                val jsonObj = jsonarray.getJSONObject(i)
                                Log.d(LOG_TAG,"Parsing Array's ITEM: ${jsonObj.toString()}")

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
                                activity.coinInfoList.add(coinInfo)

                                counter++

                                activity.checkWatchedCoin(coinInfo)
                            }
                            Log.d(LOG_TAG,"Found ITEMS: $counter")


                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Log.d(LOG_TAG,"Json parsing error: ")
                        }
                    } else {
                        Log.d(LOG_TAG,"Could not get json from server.")
                    }

                    publishProgress()
                    Thread.sleep(activity.PERIOD_OF_REQUEST_DATA)
//                delay(PERIOD_OF_REQUEST_DATA)


                }//End of permanent loop

                return ""//null
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)

                val activity = activityRef.get()
                if (activity == null || activity.isFinishing) return

                if( activity.coinInfoList.size > 0) {
                    val mAdapter = MainAdapter()
                    mAdapter.setNewCoinInfoList(activity.coinInfoList)
                    activity.recyclerView_main.adapter = mAdapter
                }

                if (activity.progressDialog.isShowing()) {
                    activity.progressDialog.dismiss()
                }
            }

            override fun onProgressUpdate(vararg values: Void?) {
                super.onProgressUpdate(*values)

                val activity = activityRef.get()
                if (activity == null || activity.isFinishing) return

                if( activity.coinInfoList.size > 0) {
                    val mAdapter = MainAdapter()
                    mAdapter.setNewCoinInfoList(activity.coinInfoList)
                    activity.recyclerView_main.adapter = mAdapter
                }

                if (activity.progressDialog.isShowing()) {
                    activity.progressDialog.dismiss()
                }
            }

            override fun onCancelled() {
                super.onCancelled()
                Log.w(LOG_TAG,"onCancelled")
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


    public fun checkWatchedCoin(coinInfo: CoinInfo): Boolean {
        if(coinInfo.symbol != null && coinInfo.price_usd > 0 && coinInfo.price_btc > 0) {
            Log.d(LOG_TAG,"checkWatchedCoin: $coinInfo")
            if(databaseHelper != null) {
                var coinLimits = databaseHelper!!.getCoinInfoDBLimitFromArray(coinInfo.symbol)
                if(coinLimits != null) {
                    val stopLoss = coinLimits!!.stopLoss_usd
                    val takeProfit = coinLimits!!.takeProfit_usd

                    //WRONG CONDITION FOR TESTING
                    if(stopLoss < coinInfo.price_usd) {
                        Log.d(LOG_TAG,"STOP LOSS FOR ${coinInfo.symbol} current price: ${coinInfo.price_usd} stop price: $stopLoss")
                        //Toast.makeText(applicationContext, "STOP LOSS FOR ${coinInfo.symbol}", Toast.LENGTH_SHORT).show()
                        setNotificationForCoin(coinInfo, true)
                    }

                    //WRONG CONDITION FOR TESTING
                    if(takeProfit > coinInfo.price_usd) {
                        Log.d(LOG_TAG,"TAKE PROFIT FOR ${coinInfo.symbol} current price: ${coinInfo.price_usd} take price: $takeProfit")
                        //Toast.makeText(applicationContext, "TAKE PROFIT FOR ${coinInfo.symbol}", Toast.LENGTH_SHORT).show()
                        setNotificationForCoin(coinInfo, false)
                    }
                }
            }
        }
        return false
    }

    private fun setNotificationForCoin(coinInfo: CoinInfo, isStopLoss: Boolean) {

        mNotificationId++
        val notifTitle = if (isStopLoss) "WARNING, WARNING(Stop loss)" else "NOTIFICATION(Take profit)"

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


            //val intent = Intent(this, LauncherActivity::class.java)
            val intent = Intent(this, NotificationActivity::class.java)

            intent.putExtra("CoinInfo", coinInfo)
            intent.putExtra("isStopLoss", isStopLoss)

        val pendingIntent = PendingIntent.getActivity(this, mNotificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationChannel =
                    NotificationChannel(channelID, description, NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.enableLights(true)
                notificationChannel.lightColor = Color.GREEN
                notificationChannel.enableVibration(true)
                notificationManager.createNotificationChannel(notificationChannel)

                builder = Notification.Builder(this, channelID)
                    .setContentTitle(notifTitle)
                    .setContentText("Current price of ${coinInfo.name} is ${coinInfo.price_usd}")
                    .setSmallIcon(R.drawable.ic_stat_name)//.setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setLights(Color.GREEN, 1000, 500)
                    .setColor( if(isStopLoss) Color.RED else Color.GREEN)

            } else {

                builder = Notification.Builder(this)
                    .setContentTitle(notifTitle)
                    .setContentText("Current price of ${coinInfo.name} is ${coinInfo.price_usd}")
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
            }

            notificationManager.notify(mNotificationId, builder.build())

    }

    override fun onDestroy() {
        super.onDestroy()
        //
    }

}
