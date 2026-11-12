package org.blackhole.launcher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var content: LinearLayout
    private lateinit var searchBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(8, 10, 18)
        window.navigationBarColor = Color.rgb(8, 10, 18)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 16)
            setBackgroundColor(Color.rgb(8, 10, 18))
        }

        val heading = TextView(this).apply {
            text = "BLACKHOLE"
            textSize = 27f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "YOUR SPACE. YOUR CONTROL."
            textSize = 10f
            setTextColor(Color.rgb(120, 150, 190))
            gravity = Gravity.CENTER
            letterSpacing = 0.25f
        }

        val clock = TextClock(this).apply {
            format12Hour = "hh:mm a"
            format24Hour = "HH:mm"
            textSize = 42f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 25, 0, 0)
        }

        val date = TextClock(this).apply {
            format12Hour = "EEEE, dd MMMM"
            format24Hour = "EEEE, dd MMMM"
            textSize = 13f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scrollView = ScrollView(this).apply {
            isFillViewport = true
            addView(content)
        }

        root.addView(
            heading,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(subtitle)
        root.addView(clock)
        root.addView(date)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
        showHome()
    }

    private fun showHome() {
        content.removeAllViews()

        addText(content, "Welcome back", 22f, Color.WHITE, true)
        addText(
            content,
            "Your Android experience, redesigned.",
            13f,
            Color.LTGRAY
        )

        searchBox = EditText(this).apply {
            hint = "Search installed apps..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 15f
            maxLines = 1
            setSingleLine(true)
            setPadding(18, 12, 18, 12)
            background = roundedBox(Color.rgb(25, 30, 43))
        }

        content.addView(
            searchBox,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                55
            ).apply {
                setMargins(0, 24, 0, 12)
            }
        )

        addActionButton(content, "SEARCH APPS") {
            showApps(searchBox.text.toString())
        }

        addActionButton(content, "ALL INSTALLED APPS") {
            showApps("")
        }

        addActionButton(content, "LOKI AI ASSISTANT") {
            showLoki()
        }

        addText(
            content,
            "BLACKHOLE LAUNCHER  •  VERSION 1.0",
            10f,
            Color.rgb(100, 115, 140)
        )
    }

    private fun showApps(filter: String) {
        content.removeAllViews()

        addText(content, "Your Apps", 24f, Color.WHITE, true)
        addText(content, "Tap an app to open it.", 13f, Color.LTGRAY)

        addActionButton(content, "BACK TO HOME") {
            showHome()
        }

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager
            .queryIntentActivities(launcherIntent, 0)
            .distinctBy { it.activityInfo.packageName }
            .sortedBy {
                it.loadLabel(packageManager).toString().lowercase()
            }

        val filteredApps = apps.filter {
            val appName = it.loadLabel(packageManager).toString()
            appName.contains(filter, ignoreCase = true) ||
                it.activityInfo.packageName.contains(
                    filter,
                    ignoreCase = true
                )
        }

        if (filteredApps.isEmpty()) {
            addText(
                content,
                "No matching apps found.",
                15f,
                Color.LTGRAY
            )
            return
        }

        for (app in filteredApps) {
            val appName = app.loadLabel(packageManager).toString()
            val packageName = app.activityInfo.packageName

            addActionButton(content, appName) {
                try {
                    val launchIntent =
                        packageManager.getLaunchIntentForPackage(packageName)

                    if (launchIntent != null) {
                        startActivity(launchIntent)
                    } else {
                        Toast.makeText(
                            this,
                            "Unable to open this app",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Could not open app",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showLoki() {
        content.removeAllViews()

        addText(content, "LOKI", 30f, Color.WHITE, true)
        addText(
            content,
            "YOUR AI ASSISTANT",
            11f,
            Color.rgb(90, 180, 255)
        )

        addText(
            content,
            """
            Hello! I am Loki.

            Your personal assistant is coming soon.

            Gemini AI connection is not configured yet.
            """.trimIndent(),
            16f,
            Color.LTGRAY
        )

        addActionButton(content, "BACK TO HOME") {
            showHome()
        }
    }

    private fun addText(
        parent: LinearLayout,
        message: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ) {
        val textView = TextView(this).apply {
            text = message
            textSize = size
            setTextColor(color)
            setPadding(4, 12, 4, 12)

            if (bold) {
                typeface = Typeface.DEFAULT_BOLD
            }
        }

        parent.addView(
            textView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun addActionButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val actionButton = Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = roundedBox(Color.rgb(25, 39, 62))
            setPadding(12, 10, 12, 10)
            setOnClickListener {
                action()
            }
        }

        parent.addView(
            actionButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        )
    }

    private fun roundedBox(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 22f
        }
    }
}
