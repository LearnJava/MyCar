package ru.konstantin.mycar.animation.rally.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.konstantin.mycar.R
import ru.konstantin.mycar.animation.rally.SceneView.FireView

class MainActivity: AppCompatActivity() {
    /** Исходные данные */ //region
//    private lateinit var buttonFireOnOff: Button
    private var isFireOn: Boolean = false
    private lateinit var fireWall: FireView
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}