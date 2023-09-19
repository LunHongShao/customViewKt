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
    val inter=data.iterator()
    while (inter.hasNext()) {
        if (inter.next().toInt()<=10){
            inter.remove()
        }
    }
    println(data)

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
        data.add(LineView.LindData(0, 77f,id=0))
        data.add(LineView.LindData(1, 77f,id=1))
        data.add(LineView.LindData(2, 77f,id=2))
        data.add(LineView.LindData(3, 77f,id=3))
        data.add(LineView.LindData(4, 10f,id=4))
        data.add(LineView.LindData(5, 20f,id=5))
        data.add(LineView.LindData(6, 10f,id=6))
        data.add(LineView.LindData(7, 90f,id=7))
        data.add(LineView.LindData(8, 75f,id=8))
        data.add(LineView.LindData(9, 62f,id=9))
        data.add(LineView.LindData(1692784194000, 77f,id=10))
        for (i in 11 until 10000){
            data.add(LineView.LindData(i.toLong(), 77f,id=i))
        }
        lineView.data = data
        test()
    }
    fun test() {
//        Thread.sleep(2000)
    }
}