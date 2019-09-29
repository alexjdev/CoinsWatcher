package com.example.coinswatcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast





class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        val radioGrp = findViewById<RadioGroup>(R.id.settings_url_radioGroup)

        var radioBtn1 = findViewById<RadioButton>(R.id.settings_url_radioButton1)
        var radioBtn2 = findViewById<RadioButton>(R.id.settings_url_radioButton2)
        var radioBtn3 = findViewById<RadioButton>(R.id.settings_url_radioButton3)

        val myPrefs = MyPreferences(this)

        when (myPrefs.getSelectedURLNum()) {
            0 -> radioBtn1.isChecked = true

            1 -> radioBtn2.isChecked = true

            2 -> radioBtn3.isChecked = true
        }


        radioGrp.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {

                R.id.settings_url_radioButton1 -> checkedRadioBtn1(myPrefs)

                R.id.settings_url_radioButton2 -> checkedRadioBtn2(myPrefs)

                R.id.settings_url_radioButton3 -> checkedRadioBtn3(myPrefs)

                else -> {
                    Toast.makeText(
                        applicationContext, "Error_",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }



    }

    fun onCloseActivity(view: View) {
        finish()
    }

    private fun checkedRadioBtn1(pref: MyPreferences) {
        saveMyPreferences(0, pref)
    }

    private fun checkedRadioBtn2(pref: MyPreferences) {
        saveMyPreferences(1, pref)
    }

    private fun checkedRadioBtn3(pref: MyPreferences) {
        saveMyPreferences(2, pref)
    }

    private fun saveMyPreferences(i: Int, pref: MyPreferences) {
        pref.setSelectedURLNum(i)
    }
}
