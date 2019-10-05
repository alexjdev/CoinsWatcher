package com.example.coinswatcher.utils

import android.util.Log

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class JsonViaHttpHandler() {

    private val LOG_TAG = "debugLogRequest"

    fun sendHTTPRequest(Urlreq: String) : String {

        val mURL = URL(Urlreq)

        val response = StringBuffer()
        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            //requestMethod = "POST"
            requestMethod = "GET" //unnecessary

//            val wr = OutputStreamWriter(getOutputStream());
//            wr.write(reqParam);
//            wr.flush();

            Log.d(LOG_TAG, "URL : $url")
            Log.d(LOG_TAG, "Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                Log.d(LOG_TAG, "Response : $response")
            }
        }
        return response.toString()
    }

}