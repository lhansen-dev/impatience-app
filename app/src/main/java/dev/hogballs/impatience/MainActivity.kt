package dev.hogballs.impatience

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.hogballs.impatience.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var elapsedSeconds = 0
    private var isRunning = false
    private var lastDrinkCount = 0
    private lateinit var tts: TextToSpeech
    private var ttsReady = false
    private var mediaPlayer: MediaPlayer? = null
    private var currentTheme = "black"

    private var timerIntervalSeconds = 60
    private var notificationType = "tts_drink"
    private var customTtsText = "drink!"

    private val tickRunnable = object : Runnable {
        override fun run() {
            elapsedSeconds++
            updateTimerDisplay()

            val currentCount = elapsedSeconds / timerIntervalSeconds
            if (currentCount > lastDrinkCount) {
                lastDrinkCount = currentCount
                triggerDrink()
            }

            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)

        if (savedInstanceState != null) {
            elapsedSeconds = savedInstanceState.getInt("elapsedSeconds", 0)
            lastDrinkCount = savedInstanceState.getInt("lastDrinkCount", 0)
            val wasRunning = savedInstanceState.getBoolean("wasRunning", true)
            updateTimerDisplay()
            if (wasRunning) {
                startTimer()
            } else {
                binding.btnPause.text = "RESUME"
            }
        } else {
            updateTimerDisplay()
            startTimer()
        }

        binding.btnNewTurn.setOnClickListener {
            newTurn()
        }

        binding.btnPause.setOnClickListener {
            togglePause()
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("elapsedSeconds", elapsedSeconds)
        outState.putInt("lastDrinkCount", lastDrinkCount)
        outState.putBoolean("wasRunning", isRunning)
    }

    override fun onResume() {
        super.onResume()
        loadSettings()
        val prefs = getSharedPreferences("impatience_settings", Context.MODE_PRIVATE)
        val theme = prefs.getString("color_theme", "black") ?: "black"
        if (theme != currentTheme) {
            recreate()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US
            tts.setSpeechRate(0.8f)
            tts.setPitch(0.9f)
            ttsReady = true
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("impatience_settings", Context.MODE_PRIVATE)
        currentTheme = prefs.getString("color_theme", "black") ?: "black"
        setTheme(resolveTheme(currentTheme))
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("impatience_settings", Context.MODE_PRIVATE)
        timerIntervalSeconds = prefs.getInt("timer_interval_seconds", 60).coerceAtLeast(1)
        notificationType = prefs.getString("notification_type", "tts_drink") ?: "tts_drink"
        customTtsText = prefs.getString("custom_tts_text", "") ?: ""
        if (customTtsText.isEmpty()) customTtsText = "drink!"
    }

    private fun togglePause() {
        if (isRunning) {
            stopTimer()
            binding.btnPause.text = "RESUME"
        } else {
            startTimer()
            binding.btnPause.text = "PAUSE"
        }
    }

    private fun newTurn() {
        tts.stop()
        mediaPlayer?.stop()
        stopTimer()
        elapsedSeconds = 0
        lastDrinkCount = 0
        binding.drinkText.visibility = View.GONE
        binding.btnPause.text = "PAUSE"
        updateTimerDisplay()
        startTimer()
    }

    private fun startTimer() {
        if (!isRunning) {
            isRunning = true
            handler.postDelayed(tickRunnable, 1000)
        }
    }

    private fun stopTimer() {
        isRunning = false
        handler.removeCallbacks(tickRunnable)
    }

    private fun updateTimerDisplay() {
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        binding.timerText.text = String.format("%d:%02d", minutes, seconds)
    }

    private fun triggerDrink() {
        if (notificationType == "custom_tts") {
            binding.drinkText.text = customTtsText.uppercase()
        } else {
            binding.drinkText.text = "DRINK!"
        }
        binding.drinkText.visibility = View.VISIBLE

        handler.postDelayed({
            binding.drinkText.visibility = View.GONE
        }, 3000)

        vibrate()

        when (notificationType) {
            "tts_drink" -> sayDrink("drink!")
            "alarm" -> playAlarm()
            "custom_tts" -> sayDrink(customTtsText)
        }
    }

    private fun sayDrink(text: String) {
        if (ttsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "drink")
        }
    }

    private fun playAlarm() {
        mediaPlayer?.release()
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mediaPlayer = MediaPlayer.create(this, alarmUri)?.apply {
            start()
        }
        handler.postDelayed({
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }, 1000)
    }

    private fun vibrate() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300, 200, 300), -1))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 200, 300, 200, 300), -1))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        tts.stop()
        tts.shutdown()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    companion object {
        fun resolveTheme(key: String): Int = when (key) {
            "white" -> R.style.Theme_EDHDrinkTurn_Plains
            "blue" -> R.style.Theme_EDHDrinkTurn_Island
            "red" -> R.style.Theme_EDHDrinkTurn_Mountain
            "green" -> R.style.Theme_EDHDrinkTurn_Forest
            "white_light" -> R.style.Theme_EDHDrinkTurn_PlainsLight
            "blue_light" -> R.style.Theme_EDHDrinkTurn_IslandLight
            "black_light" -> R.style.Theme_EDHDrinkTurn_SwampLight
            "red_light" -> R.style.Theme_EDHDrinkTurn_MountainLight
            "green_light" -> R.style.Theme_EDHDrinkTurn_ForestLight
            else -> R.style.Theme_EDHDrinkTurn
        }
    }
}
