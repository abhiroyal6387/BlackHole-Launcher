
package org.blackhole.launcher

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.text.InputType
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private lateinit var contentLayout: LinearLayout
    private lateinit var searchBox: EditText

    private val bgColor = Color.rgb(9, 12, 20)
    private val cardColor = Color.rgb(25, 30, 43)
    private val accentColor = Color.rgb(105, 125, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor

        buildHome()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun roundedBox(color: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(18).toFloat()
        }
    }

    private fun buildHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(25), dp(20), dp(15))
            setBackgroundColor(bgColor)
        }

        setContentView(root)

        val title = TextView(this).apply {
            text = "BLACK HOLE"
            textSize = 25f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.12f
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "YOUR PERSONAL ANDROID SPACE"
            textSize = 10f
            setTextColor(Color.GRAY)
            letterSpacing = 0.15f
        }
        root.addView(subtitle)

        val clock = TextClock(this).apply {
            format12Hour = "hh:mm a"
            format24Hour = "HH:mm"
            textSize = 43f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        root.addView(
            clock,
            LinearLayout.LayoutParams(-1, dp(105))
        )

        val lokiButton = makeButton(
            "✦  OPEN LOKI AI",
            accentColor
        ) {
            showLoki()
        }

        root.addView(
            lokiButton,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                bottomMargin = dp(18)
            }
        )

        searchBox = EditText(this).apply {
            hint = "Search installed apps..."
            setHintTextColor(Color.GRAY)
            setTextColor(Color.WHITE)
            textSize = 15f
            maxLines = 1
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = roundedBox(cardColor)
        }

        root.addView(
            searchBox,
            LinearLayout.LayoutParams(-1, dp(52)).apply {
                bottomMargin = dp(15)
            }
        )

        val scrollView = ScrollView(this)

        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scrollView.addView(contentLayout)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        searchBox.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                showApps(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        showApps()
    }

    private fun makeButton(
        label: String,
        color: Int,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            text = label
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            background = roundedBox(color)

            setOnClickListener {
                action()
            }
        }
    }

    private fun showApps(query: String = "") {
        contentLayout.removeAllViews()

        val heading = TextView(this).apply {
            text = "YOUR APPLICATIONS"
            textSize = 13f
            setTextColor(accentColor)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(5), 0, dp(12))
        }

        contentLayout.addView(heading)

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
            it.loadLabel(packageManager)
                .toString()
                .contains(query, ignoreCase = true)
        }

        if (filteredApps.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No applications found"
                textSize = 15f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                setPadding(0, dp(30), 0, dp(30))
            }

            contentLayout.addView(emptyText)
            return
        }

        for (app in filteredApps) {
            val appName = app.loadLabel(packageManager).toString()
            val packageName = app.activityInfo.packageName

            val appItem = TextView(this).apply {
                text = appName
                textSize = 16f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), dp(15), dp(12), dp(15))
                background = roundedBox(cardColor)

                try {
                    setCompoundDrawablesWithIntrinsicBounds(
                        app.loadIcon(packageManager),
                        null,
                        null,
                        null
                    )
                    compoundDrawablePadding = dp(15)
                } catch (_: Exception) {
                }

                setOnClickListener {
                    try {
                        val launchIntent =
                            packageManager.getLaunchIntentForPackage(packageName)

                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "App cannot be opened",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (_: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "Unable to open app",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            contentLayout.addView(
                appItem,
                LinearLayout.LayoutParams(-1, dp(65)).apply {
                    bottomMargin = dp(10)
                }
            )
        }
    }

    private fun showLoki() {
        contentLayout.removeAllViews()

        val title = TextView(this).apply {
            text = "✦ LOKI AI"
            textSize = 25f
            setTextColor(accentColor)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(30), 0, dp(25))
        }

        contentLayout.addView(title)

        val message = TextView(this).apply {
            text = """
                Hello! I am Loki.

                Your personal assistant is coming soon.

                Gemini AI connection is not configured yet.
            """.trimIndent()

            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(25), dp(20), dp(25))
            background = roundedBox(cardColor)
        }

        contentLayout.addView(
            message,
            LinearLayout.LayoutParams(-1, -2)
        )

        val backButton = makeButton(
            "BACK TO HOME",
            accentColor
        ) {
            showApps(searchBox.text.toString())
        }

        contentLayout.addView(
            backButton,
            LinearLayout.LayoutParams(-1, dp(55)).apply {
                topMargin = dp(25)
            }
        )
    }
}
