package com.example.coinswatcher

import android.content.Context


class MyPreferences(context: Context) {

    val PREFERENCES = "CoinsWatcherPrefs"

    val RUN_COUNTER = "Counter"

    val SELECTED_URL_NUM = "Sel_URL"

    val preference = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)


    fun getRunCount() : Int {
        return preference.getInt(RUN_COUNTER, 0)
    }

    fun setRunCount(count: Int) {
        val editor = preference.edit()
        editor.putInt(RUN_COUNTER, count)
        editor.apply()
    }

    fun getSelectedURLNum() : Int {
        return preference.getInt(SELECTED_URL_NUM, 1)
    }

    fun setSelectedURLNum(count: Int) {
        val editor = preference.edit()
        editor.putInt(SELECTED_URL_NUM, count)
        editor.apply()
    }

}