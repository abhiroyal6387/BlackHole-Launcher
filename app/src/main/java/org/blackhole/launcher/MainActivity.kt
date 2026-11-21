package org.blackhole.launcher

import org.blackhole.launcher.loki.LokiClient
import org.blackhole.launcher.loki.LokiMessage
import org.blackhole.launcher.loki.LokiActionEngine
import org.blackhole.launcher.loki.LokiSecurity

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import android.widget.FrameLayout
import android.text.InputType
import android.content.SharedPreferences
import android.graphics.drawable.Drawable

class MainActivity : Activity() {

    private lateinit var content: LinearLayout

    private lateinit var lokiMessagesView: LinearLayout
    private lateinit var lokiInput: EditText
    private lateinit var lokiSendButton: TextView

    private val lokiHistory = mutableListOf<LokiMessage>()
    private var lokiBusy = false

    private val black = Color.rgb(5, 5, 7)
    private val panel = Color.rgb(19, 20, 24)
    private val silver = Color.rgb(220, 225, 233)
    private val muted = Color.rgb(135, 142, 153)
    private val neon = Color.rgb(53, 217, 255)




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(29, 28, 57)
        window.navigationBarColor = Color.BLACK

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(12), dp(18), dp(16))
            setBackgroundColor(Color.rgb(29, 28, 57))
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            addView(content)
        }

        setContentView(scroll)

        LokiSecurity.startupGate(this) {
            showHome()
        }
    }

    private fun addHolographicOrb(size: Int = 184) {
        val stage = FrameLayout(this)

        fun makeRing(diameter: Int, stroke: Int): TextView {
            return TextView(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.TRANSPARENT)
                    setStroke(dp(stroke), neon)
                }
                elevation = dp(5).toFloat()
            }
        }

        val outer = makeRing(154, 1)
        val middle = makeRing(128, 2)

        val core = TextView(this).apply {
            text = "LOKI"
            textSize = 17f
            gravity = Gravity.CENTER
            setTextColor(silver)
            typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(8, 23, 34))
                setStroke(dp(2), neon)
            }
            elevation = dp(12).toFloat()
        }

        stage.addView(
            outer,
            FrameLayout.LayoutParams(dp(154), dp(154), Gravity.CENTER)
        )
        stage.addView(
            middle,
            FrameLayout.LayoutParams(dp(128), dp(128), Gravity.CENTER)
        )
        stage.addView(
            core,
            FrameLayout.LayoutParams(dp(94), dp(94), Gravity.CENTER)
        )

        content.addView(
            stage,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(size)
            )
        )

        stage.alpha = 0f
        stage.animate().alpha(1f).setDuration(650).start()
    }

    private fun showHome() {
        animateScreen {
            val navy = Color.rgb(29, 28, 57)
            val teal = Color.rgb(0, 224, 190)
            val white = Color.rgb(239, 241, 250)
            val soft = Color.rgb(174, 177, 199)

            content.setBackgroundColor(navy)

            val topLine = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            addText(topLine, "HostLoki", 13f, white, true)

            val topSpacer = View(this)
            topLine.addView(
                topSpacer,
                LinearLayout.LayoutParams(0, dp(1), 1f)
            )

            val statusDot = TextView(this).apply {
                text = "●"
                textSize = 12f
                setTextColor(teal)
            }
            topLine.addView(statusDot)
            content.addView(topLine)

            val hero = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(22), 0, dp(8))
            }

            val leftBar = View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(teal, Color.rgb(0, 105, 112))
                ).apply {
                    cornerRadius = dp(3).toFloat()
                }
            }

            hero.addView(
                leftBar,
                LinearLayout.LayoutParams(dp(42), dp(142))
            )

            val heroInfo = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(12), 0, dp(5), 0)
            }

            val day = TextClock(this).apply {
                format12Hour = "EEEE"
                format24Hour = "EEEE"
                textSize = 29f
                letterSpacing = 0.12f
                gravity = Gravity.CENTER
                setTextColor(white)
                typeface = Typeface.create("serif", Typeface.BOLD)
            }

            val underline = View(this).apply {
                setBackgroundColor(teal)
            }

            val date = TextClock(this).apply {
                format12Hour = "dd MMMM yyyy"
                format24Hour = "dd MMMM yyyy"
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(soft)
                setPadding(0, dp(7), 0, 0)
            }

            heroInfo.addView(
                day,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            heroInfo.addView(
                underline,
                LinearLayout.LayoutParams(dp(150), dp(2)).apply {
                    gravity = Gravity.CENTER
                    topMargin = dp(5)
                }
            )
            heroInfo.addView(date)

            hero.addView(
                heroInfo,
                LinearLayout.LayoutParams(0, dp(142), 1f)
            )

            val circle = TextView(this).apply {
                text = "●"
                textSize = 17f
                gravity = Gravity.CENTER
                setTextColor(teal)
            }
            hero.addView(
                circle,
                LinearLayout.LayoutParams(dp(32), dp(32))
            )

            content.addView(hero)

            val infoSection = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(12), 0, dp(12))
            }

            val secondBar = View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(teal, Color.rgb(0, 105, 112))
                ).apply {
                    cornerRadius = dp(3).toFloat()
                }
            }

            infoSection.addView(
                secondBar,
                LinearLayout.LayoutParams(dp(42), dp(112))
            )

            val message = TextView(this).apply {
                text = "YOUR SPACE. YOUR APPS.\\n\\nEverything you need,\\nright at your fingertips."
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(soft)
                setLineSpacing(dp(3).toFloat(), 1f)
                setPadding(dp(12), 0, dp(8), 0)
            }

            infoSection.addView(
                message,
                LinearLayout.LayoutParams(0, dp(112), 1f)
            )

            val smallMark = TextView(this).apply {
                text = "○"
                textSize = 22f
                setTextColor(teal)
            }
            infoSection.addView(smallMark)

            content.addView(infoSection)

            val searchButton = TextView(this).apply {
                text = "⌕    SEARCH APPS"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(navy)
                typeface = Typeface.DEFAULT_BOLD
                background = GradientDrawable().apply {
                    setColor(teal)
                    cornerRadius = dp(13).toFloat()
                }
                isClickable = true
                isFocusable = true
                setOnClickListener { showApps("") }
            }

            content.addView(
                searchButton,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(54)
                ).apply {
                    setMargins(dp(8), dp(8), dp(8), dp(24))
                }
            )

            val launchIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val homeApps = packageManager.queryIntentActivities(launchIntent, 0)
                .distinctBy { it.activityInfo.packageName }
                .filter {
                    !LokiSecurity.isHidden(this, it.activityInfo.packageName)
                }
                .sortedBy {
                    it.loadLabel(packageManager).toString().lowercase()
                }
                .take(12)

            homeApps.chunked(4).forEach { appRow ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER
                }

                appRow.forEach { info ->
                    val packageName = info.activityInfo.packageName
                    val appName = info.loadLabel(packageManager).toString()

                    val tile = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        gravity = Gravity.CENTER
                        setPadding(dp(3), dp(7), dp(3), dp(5))

                        val label = TextView(this@MainActivity).apply {
                            text = appName
                            textSize = 9f
                            gravity = Gravity.CENTER
                            setTextColor(white)
                            maxLines = 1
                            ellipsize = android.text.TextUtils.TruncateAt.END
                            setPadding(0, dp(4), 0, 0)
                        }

                        addView(
                            label,
                            LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dp(20)
                            )
                        )

                        isClickable = true
                        isFocusable = true
                        setOnClickListener {
                            packageManager.getLaunchIntentForPackage(packageName)
                                ?.let { startActivity(it) }
                        }
                    }

                    row.addView(
                        tile,
                        LinearLayout.LayoutParams(0, dp(82), 1f)
                    )
                }

                repeat(4 - appRow.size) {
                    row.addView(
                        View(this),
                        LinearLayout.LayoutParams(0, dp(82), 1f)
                    )
                }

                content.addView(
                    row,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(82)
                    )
                )
            }
        }
    }

    private fun showApps(initialFilter: String) {
        animateScreen {
            addText(content, "APPLICATIONS", 23f, silver, true)
            addText(content, "YOUR APP LIBRARY", 10f, muted)

            addButton(content, "← HOME") {
                showHome()
            }

            val search = EditText(this).apply {
                hint = "Search applications..."
                setHintTextColor(muted)
                setTextColor(silver)
                textSize = 14f
                setSingleLine(true)
                setPadding(dp(16), 0, dp(16), 0)
                background = roundedBox(panel, 16f)
            }

            content.addView(
                search,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(50)
                ).apply {
                    setMargins(0, dp(12), 0, dp(14))
                }
            )

            val grid = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }
            content.addView(grid)

            search.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    populateApps(grid, s?.toString() ?: "")
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            search.setText(initialFilter)
            search.setSelection(search.text.length)
            populateApps(grid, initialFilter)
        }
    }

    private fun populateApps(grid: LinearLayout, filter: String) {
        grid.removeAllViews()

        val launchIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager.queryIntentActivities(launchIntent, 0)
            .distinctBy { it.activityInfo.packageName }
            .filter {
                !LokiSecurity.isHidden(this, it.activityInfo.packageName)
            }
            .filter {
                it.loadLabel(packageManager).toString()
                    .contains(filter, ignoreCase = true)
            }
            .sortedBy {
                it.loadLabel(packageManager).toString().lowercase()
            }

        if (apps.isEmpty()) {
            addText(grid, "NO APPLICATIONS FOUND", 13f, muted)
            return
        }

        var row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        apps.forEachIndexed { index, info ->
            val packageName = info.activityInfo.packageName
            val appName = info.loadLabel(packageManager).toString()

            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(4), dp(12), dp(4), dp(12))
                isClickable = true
                isFocusable = true
                background = roundedBox(panel, 16f)

                val label = TextView(this@MainActivity).apply {
                    text = appName
                    textSize = 10f
                    gravity = Gravity.CENTER
                    setTextColor(silver)
                    maxLines = 2
                    ellipsize = android.text.TextUtils.TruncateAt.END
                    setPadding(dp(2), dp(7), dp(2), 0)
                }

                addView(
                    label,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(34)
                    )
                )

                setOnClickListener {
                    try {
                        packageManager.getLaunchIntentForPackage(packageName)
                            ?.let { startActivity(it) }
                            ?: Toast.makeText(
                                this@MainActivity,
                                "Unable to open app",
                                Toast.LENGTH_SHORT
                            ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "Could not open app",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                setOnLongClickListener {
                    LokiSecurity.requirePin(this@MainActivity) {
                        LokiSecurity.hidePackage(
                            this@MainActivity,
                            packageName
                        )
                        Toast.makeText(
                            this@MainActivity,
                            "$appName hidden from BlackHole",
                            Toast.LENGTH_SHORT
                        ).show()
                        populateApps(grid, filter)
                    }
                    true
                }
            }

            row.addView(
                cell,
                LinearLayout.LayoutParams(0, dp(102), 1f).apply {
                    setMargins(dp(3), dp(3), dp(3), dp(3))
                }
            )

            if (index % 4 == 3 || index == apps.lastIndex) {
                while (row.childCount < 4) {
                    row.addView(
                        View(this),
                        LinearLayout.LayoutParams(0, dp(102), 1f)
                    )
                }

                grid.addView(
                    row,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(108)
                    )
                )

                if (index != apps.lastIndex) {
                    row = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL
                    }
                }
            }
        }
    }

    private fun showLoki() {
        animateScreen {
            addText(content, "LOKI AI", 25f, silver, true)
            addText(content, "YOUR PERSONAL ASSISTANT", 10f, neon, true)

            addHolographicOrb(122)

            addButton(content, "<- BACK TO HOME") {
                showHome()
            }

            lokiMessagesView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(5), dp(8), dp(5), dp(8))
            }

            val chatScroll = ScrollView(this).apply {
                isFillViewport = true
                overScrollMode = View.OVER_SCROLL_NEVER
                addView(lokiMessagesView)
            }

            content.addView(
                chatScroll,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(300)
                ).apply {
                    setMargins(0, dp(8), 0, dp(8))
                }
            )

            if (lokiHistory.isEmpty()) {
                addLokiBubble(
                    "model",
                    "Namaste! Main Loki hoon. ✦\n\nHindi, English ya Hinglish mein baat karo. Aaj kya karna hai?"
                )
            } else {
                lokiHistory.forEach {
                    addLokiBubble(it.role, it.text)
                }
            }

            val inputRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            lokiInput = EditText(this).apply {
                hint = "Message Loki..."
                setHintTextColor(muted)
                setTextColor(silver)
                textSize = 14f
                maxLines = 3
                setPadding(dp(14), dp(8), dp(14), dp(8))
                background = roundedBox(panel, 16f)
            }

            inputRow.addView(
                lokiInput,
                LinearLayout.LayoutParams(0, dp(54), 1f)
            )

            lokiSendButton = TextView(this).apply {
                text = "SEND"
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(neon)
                setPadding(dp(14), 0, dp(14), 0)
                background = roundedBox(panel, 16f)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    sendLokiMessage()
                }
            }

            inputRow.addView(
                lokiSendButton,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    dp(54)
                ).apply {
                    setMargins(dp(8), 0, 0, 0)
                }
            )

            content.addView(inputRow)

            addText(content, "GEMINI AI  /  TERMUX BACKEND", 9f, muted)
        }
    }


    private fun addLokiBubble(role: String, message: String) {
        val userMessage = role == "user"

        val bubble = TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(silver)
            setPadding(dp(14), dp(11), dp(14), dp(11))
            maxWidth = dp(285)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(16).toFloat()
                setColor(
                    if (userMessage) Color.rgb(15, 38, 48)
                    else panel
                )
                setStroke(
                    dp(1),
                    if (userMessage) neon else Color.rgb(65, 75, 85)
                )
            }
        }

        lokiMessagesView.addView(
            bubble,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = if (userMessage) Gravity.END else Gravity.START
                setMargins(dp(3), dp(5), dp(3), dp(5))
            }
        )
    }

    private fun sendLokiMessage() {
        val message = lokiInput.text.toString().trim()

        if (message.isEmpty() || lokiBusy) return

        lokiBusy = true

        lokiHistory.add(LokiMessage("user", message))
        addLokiBubble("user", message)

        lokiInput.setText("")

        // Local Android action engine
        val actionResult = LokiActionEngine.execute(this, message)

        if (actionResult != null) {
            lokiBusy = false
            lokiHistory.add(LokiMessage("model", actionResult))
            addLokiBubble("model", actionResult)
            lokiSendButton.isEnabled = true
            lokiSendButton.alpha = 1f
            return
        }
        lokiSendButton.isEnabled = false
        lokiSendButton.alpha = 0.5f

        val thinking = TextView(this).apply {
            text = "Loki is thinking..."
            textSize = 12f
            setTextColor(muted)
            setPadding(dp(12), dp(10), dp(12), dp(10))
        }

        lokiMessagesView.addView(thinking)

        LokiClient.sendMessage(lokiHistory.toList()) { reply, error ->

            lokiMessagesView.removeView(thinking)

            lokiBusy = false
            lokiSendButton.isEnabled = true
            lokiSendButton.alpha = 1f

            if (reply != null) {
                lokiHistory.add(LokiMessage("model", reply))
                addLokiBubble("model", reply)
            } else {
                addLokiBubble(
                    "model",
                    error ?: "Loki abhi reply nahi kar pa raha."
                )
            }
        }
    }

    private fun animateScreen(build: () -> Unit) {
        content.animate().cancel()
        content.animate()
            .alpha(0f)
            .translationY(dp(8).toFloat())
            .setDuration(110)
            .withEndAction {
                content.removeAllViews()
                build()
                content.alpha = 0f
                content.translationY = dp(10).toFloat()
                content.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(230)
                    .start()
            }
            .start()
    }

    private fun addText(
        parent: LinearLayout,
        message: String,
        size: Float,
        color: Int,
        bold: Boolean = false
    ) {
        val view = TextView(this).apply {
            text = message
            textSize = size
            setTextColor(color)
            setPadding(dp(3), dp(9), dp(3), dp(5))
            if (bold) {
                typeface = Typeface.create(
                    "sans-serif-medium", Typeface.NORMAL
                )
            }
        }
        parent.addView(view)
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = TextView(this).apply {
            text = label
            textSize = 12f
            letterSpacing = 0.05f
            gravity = Gravity.CENTER
            setTextColor(silver)
            setPadding(dp(15), dp(16), dp(15), dp(16))
            background = roundedBox(panel, 18f)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(54)
            ).apply {
                setMargins(0, dp(7), 0, dp(7))
            }
        )
    }

    private fun roundedBox(color: Int, radius: Float): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius.toInt()).toFloat()
            setStroke(dp(1), Color.rgb(39, 41, 47))
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
