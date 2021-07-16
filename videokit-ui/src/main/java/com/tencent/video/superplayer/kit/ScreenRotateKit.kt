package com.tencent.video.superplayer.kit

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.SensorManager.SENSOR_DELAY_NORMAL


class ScreenRotateKit {
    private var sensor: Sensor? = null
    private var sensorManager: SensorManager? = null
    fun init(context: Context) {
        // 获取传感器管理器
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // 获取传感器类型
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        sensorManager!!.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {

            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {


            }

        }, sensor!!, SENSOR_DELAY_NORMAL)
    }
}