package dev.hogballs.impatience

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import dev.hogballs.impatience.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val prefs by lazy {
        getSharedPreferences("impatience_settings", Context.MODE_PRIVATE)
    }

    private data class ThemeSwatch(val view: View, val key: String, val color: Int)
    private lateinit var swatches: List<ThemeSwatch>

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        swatches = listOf(
            // Dark row
            ThemeSwatch(binding.themePlains, "white", Color.parseColor("#c9a84c")),
            ThemeSwatch(binding.themeIsland, "blue", Color.parseColor("#1565c0")),
            ThemeSwatch(binding.themeSwamp, "black", Color.parseColor("#6200ea")),
            ThemeSwatch(binding.themeMountain, "red", Color.parseColor("#c62828")),
            ThemeSwatch(binding.themeForest, "green", Color.parseColor("#2e7d32")),
            // Light row
            ThemeSwatch(binding.themePlainsLight, "white_light", Color.parseColor("#e8d5a3")),
            ThemeSwatch(binding.themeIslandLight, "blue_light", Color.parseColor("#90caf9")),
            ThemeSwatch(binding.themeSwampLight, "black_light", Color.parseColor("#b39ddb")),
            ThemeSwatch(binding.themeMountainLight, "red_light", Color.parseColor("#ef9a9a")),
            ThemeSwatch(binding.themeForestLight, "green_light", Color.parseColor("#a5d6a7")),
        )

        loadSettings()
        setupListeners()
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("impatience_settings", Context.MODE_PRIVATE)
        val theme = prefs.getString("color_theme", "black") ?: "black"
        setTheme(MainActivity.resolveTheme(theme))
    }

    private fun loadSettings() {
        val interval = prefs.getInt("timer_interval_seconds", 60)
        val notificationType = prefs.getString("notification_type", "tts_drink")
        val customText = prefs.getString("custom_tts_text", "")
        val selectedTheme = prefs.getString("color_theme", "black") ?: "black"

        binding.editTimerInterval.setText(interval.toString())

        when (notificationType) {
            "tts_drink" -> binding.radioTtsDrink.isChecked = true
            "alarm" -> binding.radioAlarm.isChecked = true
            "custom_tts" -> binding.radioCustomTts.isChecked = true
        }

        binding.editCustomTts.setText(customText)
        binding.editCustomTts.visibility =
            if (notificationType == "custom_tts") View.VISIBLE else View.GONE

        updateThemeSwatches(selectedTheme)
    }

    private fun updateThemeSwatches(selectedTheme: String) {
        for (swatch in swatches) {
            swatch.view.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(swatch.color)
                if (swatch.key == selectedTheme) {
                    setStroke(6, Color.WHITE)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        for (swatch in swatches) {
            swatch.view.setOnClickListener { selectTheme(swatch.key) }
        }

        binding.radioGroupSound.setOnCheckedChangeListener { _, checkedId ->
            val type = when (checkedId) {
                R.id.radioAlarm -> "alarm"
                R.id.radioCustomTts -> "custom_tts"
                else -> "tts_drink"
            }
            prefs.edit().putString("notification_type", type).apply()
            binding.editCustomTts.visibility =
                if (type == "custom_tts") View.VISIBLE else View.GONE
        }
    }

    private fun selectTheme(theme: String) {
        prefs.edit().putString("color_theme", theme).apply()
        recreate()
    }

    override fun onPause() {
        super.onPause()
        saveSettings()
    }

    private fun saveSettings() {
        val interval = binding.editTimerInterval.text.toString().toIntOrNull() ?: 60
        prefs.edit()
            .putInt("timer_interval_seconds", interval.coerceAtLeast(1))
            .putString("custom_tts_text", binding.editCustomTts.text.toString())
            .apply()
    }
}
