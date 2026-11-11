package org.blackhole.launcher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.Editable
import android.text.TextWatcher

class MainActivity : Activity() {

    private val bg = Color.rgb(5, 7, 13)
    private val cyan = Color.rgb(0, 229, 255)
    private lateinit var appList: LinearLayout
    private lateinit var searchBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildHome()
    }

    private fun makeText(
        value: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    }

    private fun buildHome() {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 35, 24, 24)
            setBackgroundColor(bg)
        }

        scroll.addView(root)
        setContentView(scroll)

        root.addView(makeText("BLACKHOLE", 30f, Color.WHITE, true))
        root.addView(makeText("LAUNCHER", 15f, cyan, true))

        val clock = TextClock(this).apply {
            format12Hour = "hh:mm a"
            format24Hour = "HH:mm"
            textSize = 48f
            setTextColor(Color.WHITE)
            setPadding(0, 25, 0, 0)
        }
        root.addView(clock)

        val date = TextClock(this).apply {
            format12Hour = "EEEE, dd MMMM"
            format24Hour = "EEEE, dd MMMM"
            textSize = 14f
            setTextColor(Color.LTGRAY)
        }
        root.addView(date)

        val loki = makeText("✦  LOKI AI  —  OPEN ASSISTANT", 17f, cyan, true)
        loki.setPadding(18, 22, 18, 22)
        loki.background = roundedBox(Color.rgb(15, 28, 40))
        val lokiParams = LinearLayout.LayoutParams(-1, -2)
        lokiParams.setMargins(0, 30, 0, 25)
        root.addView(loki, lokiParams)
        loki.setOnClickListener { showLoki() }

        root.addView(makeText("YOUR APPLICATIONS", 16f, Color.WHITE, true))

        searchBox = EditText(this).apply {
            hint = "Search apps..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 16f
            maxLines = 1
inputType = android.text.InputType.TYPE_CLASS_TEXT
setPadding(16, 12, 16, 12)
background = roundedBox(Color.rgb(25, 29, 39))

        val searchParams = LinearLayout.LayoutParams(-1, -2)
        searchParams.setMargins(0, 15, 0, 18)
        root.addView(searchBox, searchParams)

        appList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(appList)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showApps(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        showApps("")
    }

    private fun roundedBox(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 22f
            setStroke(1, cyan)
        }
    }

    private fun showApps(filter: String) {
        appList.removeAllViews()

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val apps = packageManager.queryIntentActivities(intent, 0)
            .filter {
                it.loadLabel(packageManager).toString()
                    .contains(filter, ignoreCase = true)
            }
            .sortedBy {
                it.loadLabel(packageManager).toString().lowercase()
            }

        for (info in apps) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(14, 12, 14, 12)
                background = roundedBox(Color.rgb(17, 21, 31))
            }

            val icon = ImageView(this).apply {
                setImageDrawable(info.loadIcon(packageManager))
                layoutParams = LinearLayout.LayoutParams(48, 48)
            }

            val name = makeText(
                info.loadLabel(packageManager).toString(),
                16f,
                Color.WHITE,
                true
            )
            name.setPadding(18, 0, 0, 0)

            row.addView(icon)
            row.addView(name)

            val params = LinearLayout.LayoutParams(-1, -2)
            params.setMargins(0, 0, 0, 10)
            appList.addView(row, params)

            row.setOnClickListener {
                val launchIntent = packageManager
                    .getLaunchIntentForPackage(info.activityInfo.packageName)
                if (launchIntent != null) {
                    startActivity(launchIntent)
                }
            }
        }

        if (apps.isEmpty()) {
            appList.addView(
                makeText("No apps found", 15f, Color.GRAY)
            )
        }
    }

    private fun showLoki() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(25, 25, 25, 25)
            setBackgroundColor(bg)
        }

        val back = makeText("←  BACK TO HOME", 16f, cyan, true)
        back.setPadding(0, 0, 0, 35)
        back.setOnClickListener { buildHome() }

        root.addView(back)
        root.addView(makeText("✦ LOKI AI", 30f, cyan, true))
        root.addView(
            makeText(
                "Your AI assistant is coming soon.",
                16f,
                Color.WHITE
            )
        )
        root.addView(
            makeText(
                "Gemini AI connection will be added in a later version.",
                13f,
                Color.LTGRAY
            )
        )

        setContentView(root)
    }
}
