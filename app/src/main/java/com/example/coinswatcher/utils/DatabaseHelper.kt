package com.example.coinswatcher.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.ArrayList


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    init {

        Log.d("table", CREATE_TABLE_COINS_WATCH)
    }

    val allCoinInfoDBLimitList: ArrayList<CoinInfoDBLimit>
        get() {
            val coinInfoDBLimitArrayList = ArrayList<CoinInfoDBLimit>()
            var symbol: String = ""
            var stop: Double = 0.0
            var take: Double = 0.0

            val selectQuery = "SELECT  * FROM $TABLE_COINS_WATCH"
            val db = this.readableDatabase
            val c = db.rawQuery(selectQuery, null)
            if (c.moveToFirst()) {
                do {
                    symbol = c.getString(c.getColumnIndex(KEY_SYMBOL))
                    stop = c.getDouble(c.getColumnIndex(KEY_STOP_LOSS))
                    take = c.getDouble(c.getColumnIndex(KEY_TAKE_PROFIT))
                    coinInfoDBLimitArrayList.add( CoinInfoDBLimit(symbol, stop, take) )
                } while (c.moveToNext())
                Log.d("array", coinInfoDBLimitArrayList.toString())
            }
            println("DatabaseHelper.allCoinInfoDBLimitList size= ${coinInfoDBLimitArrayList.size}")
            return coinInfoDBLimitArrayList
        }


    fun getCoinInfoDBLimitFromArray(findSymbol: String): CoinInfoDBLimit? {

        allCoinInfoDBLimitList.forEach {
            if (it.symbol.equals(findSymbol))
                return it
        }

        return null
    }

    fun getCoinInfoDBLimit(findSymbol: String): CoinInfoDBLimit? {

        var symbol: String = ""
        var stop: Double = 0.0
        var take: Double = 0.0

        val selectQuery = "SELECT * FROM $TABLE_COINS_WATCH WHERE symbol='$findSymbol'"
        val db = this.readableDatabase
        val c = db.rawQuery(selectQuery, null)
        if (c.moveToFirst()) {
            symbol = c.getString(c.getColumnIndex(KEY_SYMBOL))
            stop = c.getDouble(c.getColumnIndex(KEY_STOP_LOSS))
            take = c.getDouble(c.getColumnIndex(KEY_TAKE_PROFIT))
            Log.d("found symbol", symbol)

            return CoinInfoDBLimit(symbol, stop, take)
        }
        return null
    }

    fun dropDatabase(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS '$TABLE_COINS_WATCH'")
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_COINS_WATCH)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS '$TABLE_COINS_WATCH'")
        onCreate(db)
    }

    fun addCoinInfoDBLimit(coinLimit: CoinInfoDBLimit): Long {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(KEY_SYMBOL, coinLimit.symbol)
        values.put(KEY_STOP_LOSS, coinLimit.stopLoss_usd)
        values.put(KEY_TAKE_PROFIT, coinLimit.takeProfit_usd)


        val result = db.insert(TABLE_COINS_WATCH, null, values)
        println("addCoinInfoDBLimit result= $result")

        if(result == -1L) {//TEMPORARY HACK

            val val2 = ContentValues()
            val2.put(KEY_STOP_LOSS, coinLimit.stopLoss_usd)
            val2.put(KEY_TAKE_PROFIT, coinLimit.takeProfit_usd)

            var selectionArgs = arrayOf(coinLimit.symbol)

            var res = db.update(TABLE_COINS_WATCH, val2, "symbol=?", selectionArgs)
            println("addCoinInfoDBLimit ATTEMPT UPDATE res= $res")
        }
        return result
    }


    fun upsertCoinForWatch(coinLimit: CoinInfoDBLimit) {
        val db = this.writableDatabase

        val sqlRequest = "insert into $TABLE_COINS_WATCH(symbol, stop_loss, take_profit) values(${coinLimit.symbol}, ${coinLimit.stopLoss_usd}, ${coinLimit.takeProfit_usd})" +
                " ON CONFLICT(symbol) DO UPDATE SET" +
                " update $TABLE_COINS_WATCH set stop_loss=${coinLimit.stopLoss_usd}, take_profit=${coinLimit.takeProfit_usd} where symbol=${coinLimit.symbol};"

        println("upsertCoinForWatch sqlRequest= $sqlRequest")
        db.execSQL(sqlRequest)

        println("upsertCoinForWatch after request")
    }

//    fun upsertCoinForWatch(coinLimit: CoinInfoDBLimit) {
//        val db = this.writableDatabase
//
//        val sqlRequest = "IF EXISTS(select * from $TABLE_COINS_WATCH where symbol=${coinLimit.symbol} \n" +
//                "   update $TABLE_COINS_WATCH set stop_loss=${coinLimit.stopLoss_usd}, take_profit=${coinLimit.takeProfit_usd} where symbol=${coinLimit.symbol}\n" +
//                "ELSE\n" +
//                "   insert into $TABLE_COINS_WATCH(symbol, stop_loss, take_profit) values(${coinLimit.symbol}, ${coinLimit.stopLoss_usd}, ${coinLimit.takeProfit_usd});"
//
//        println("upsertCoinForWatch sqlRequest= $sqlRequest")
//        db.execSQL(sqlRequest)
//
//        println("upsertCoinForWatch after request")
//    }

    companion object {

        var DATABASE_NAME = "coins_database"
        private val DATABASE_VERSION = 1
        private val TABLE_COINS_WATCH = "coins_watch"
        private val KEY_SYMBOL = "symbol"
        private val KEY_STOP_LOSS = "stop_loss"
        private val KEY_TAKE_PROFIT = "take_profit"

        /*CREATE TABLE coins_watch ( symbol TEXT PRIMARY KEY , stop_loss DOUBLE, take_profit DOUBLE);*/

        private val CREATE_TABLE_COINS_WATCH = ("CREATE TABLE "
                + TABLE_COINS_WATCH + "(" + KEY_SYMBOL + " TEXT PRIMARY KEY, "
                + KEY_STOP_LOSS + " DOUBLE, "
                + KEY_TAKE_PROFIT + " DOUBLE )")
    }
}
