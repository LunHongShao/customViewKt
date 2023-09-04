package com.hong.customviewkt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

fun main() {
    val data= mutableListOf<String>()
    for (i in 0 until 20){
        data.add(i.toString())
    }
    val newList=data.subList(0,10)
    println(data)
    println(newList)
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val lineView = findViewById<LineView2>(R.id.lineView)
        val data = mutableListOf<LineView.LindData>()
//        for (i in 0 until 11){
//
//        }
        data.add(LineView.LindData(1692784194000, 100f))
        data.add(LineView.LindData(1692784194000, 80f))
        data.add(LineView.LindData(1692784194000, 100f))
        data.add(LineView.LindData(1692784194000, 80f))
        data.add(LineView.LindData(1692784194000, 10f))
        data.add(LineView.LindData(1692784194000, 20f))
        data.add(LineView.LindData(1692784194000, 10f))
        data.add(LineView.LindData(1692784194000, 90f))
        data.add(LineView.LindData(1692784194000, 75f))
        data.add(LineView.LindData(1692784194000, 62f))
        data.add(LineView.LindData(1692784194000, 77f))
        for (i in 0 until 100){
            data.add(LineView.LindData(1692784194000, 77f))
        }
        lineView.data = data
        test()
    }
    fun test() {
//        Thread.sleep(2000)
    }
}