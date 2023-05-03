package com.example.sandboxspr14

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.*
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import android.os.VibrationEffect
import android.os.Vibrator


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    companion object {
        // Число миллисекунд в одной секунде
        private const val DELAY = 1000L
    }

    private var mainThreadHandler: Handler? = null

    private var editText: EditText? = null
    private var startTimerButton: Button? = null
    private var resetTimerButton: Button? = null
    private var secondsLeftTextView: TextView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Создаём Handler, привязанный к ГЛАВНОМУ потоку
        mainThreadHandler = Handler(Looper.getMainLooper())

        editText = findViewById(R.id.editText)
        startTimerButton = findViewById(R.id.startTimerButton)
        resetTimerButton = findViewById(R.id.resetTimerButton)
        secondsLeftTextView = findViewById(R.id.secondsLeftTextView)
        // Навешиваем click listener на кнопку
        startTimerButton?.setOnClickListener {
            // Считали количество секунд
            val secondsCount = editText?.text?.toString()?.takeIf { it.isNotBlank() }?.toLong() ?: 0L

            // Если можно запустить таймер — запускаем
            if (secondsCount <= 0) {
                showMessage("Can't start timer with no time!")
            } else {
                startTimer(secondsCount)
                startTimerButton?.isEnabled = false
            }
        }

        resetTimerButton?.setOnClickListener {
            mainThreadHandler?.removeCallbacksAndMessages(null)
            secondsLeftTextView?.text = "00:00"
            startTimerButton?.isEnabled = true
        }

    }

    private fun startTimer(duration: Long) {
        // Запоминаем время начала таймера
        val startTime = System.currentTimeMillis()

        // И отправляем задачу в Handler
        // Число секунд из EditText'а переводим в миллисекунды
        mainThreadHandler?.post(
            createUpdateTimerTask(startTime, duration * DELAY)
        )
    }


    private fun createUpdateTimerTask(startTime: Long, duration: Long): Runnable {
        return object : Runnable {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                // Сколько прошло времени с момента запуска таймера
                val elapsedTime = System.currentTimeMillis() - startTime
                val vibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                // Сколько осталось до конца
                val remainingTime = duration - elapsedTime

                if (remainingTime > 0) {
                    // Если всё ещё отсчитываем секунды —
                    // обновляем UI и снова планируем задачу
                    val seconds = remainingTime / DELAY
                    secondsLeftTextView?.text = String.format("%d:%02d", seconds / 60, seconds % 60)
                    mainThreadHandler?.postDelayed(this, DELAY)
                } else {
                    // Если таймер окончен, выводим текст
                    secondsLeftTextView?.text = "Done!"
                    startTimerButton?.isEnabled = true
                    vibrator.vibrate(vibrationEffect)
                    showMessage("Done!")
                }
            }
        }
    }

    private fun showMessage(text: String) {
        val rootView = findViewById<View>(android.R.id.content)?.rootView
        if (rootView != null) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

}