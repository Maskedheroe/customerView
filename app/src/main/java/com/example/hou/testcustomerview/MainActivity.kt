package com.example.hou.testcustomerview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.hou.tmepView.WaveView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(WaveView(this))
    }
}
