package com.example.coinswatcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)


        val myPreferences = MyPreferences(this)
        var runCounter = myPreferences.getRunCount()

        findViewById<TextView>(R.id.tv_dlg_about_counter_val).text = runCounter.toString()

    }

    fun onCloseActivity(view: View) {
        finish()
    }

}
