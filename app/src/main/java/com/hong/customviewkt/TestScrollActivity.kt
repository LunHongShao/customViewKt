package com.hong.customviewkt

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class TestScrollActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_scroll)
    }
    companion object{
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, TestScrollActivity::class.java)
            context.startActivity(starter)
        }
    }
}