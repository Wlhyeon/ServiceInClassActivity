package edu.temple.myapplication

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log

@Suppress("ControlFlowWithEmptyBody")
class TimerService : Service() {

    private var isRunning = false
    private var timerHandler: Handler? = null
    private var paused = false
    private lateinit var t: TimerThread

    inner class TimerBinder : Binder() {

        val isRunning: Boolean
            get() = this@TimerService.isRunning

        val paused: Boolean
            get() = this@TimerService.paused

        fun start(startValue: Int) {
            if (this@TimerService.paused) {
                this@TimerService.paused = false
                this@TimerService.isRunning = true
            } else if (!this@TimerService.isRunning) {
                if (::t.isInitialized) t.interrupt()
                this@TimerService.start(startValue)
            }
        }


        fun setHandler(handler: Handler) {
            timerHandler = handler
        }

        fun stop() {
            if (::t.isInitialized && this@TimerService.isRunning) {
                t.interrupt()
                this@TimerService.isRunning = false
                this@TimerService.paused = false
            }
        }


        fun pause() {
            this@TimerService.pause()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("TimerService status", "Created")
    }

    override fun onBind(intent: Intent): IBinder {
        return TimerBinder()
    }

    fun start(startValue: Int) {
        t = TimerThread(startValue)
        t.start()
    }

    fun pause() {
        if (::t.isInitialized && isRunning) {
            paused = true
            isRunning = false
        }
    }

    inner class TimerThread(private val startValue: Int) : Thread() {
        override fun run() {
            isRunning = true
            try {
                for (i in startValue downTo 1) {
                    Log.d("Countdown", i.toString())
                    timerHandler?.sendEmptyMessage(i)
                    while (paused);
                    sleep(1000)
                }
                isRunning = false
            } catch (e: InterruptedException) {
                Log.d("Timer interrupted", e.toString())
                isRunning = false
                paused = false
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (::t.isInitialized) {
            t.interrupt()
        }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TimerService status", "Destroyed")
    }
}
