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

        window.statusBarColor = black
        window.navigationBarColor = black

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(12))
            setBackgroundColor(black)
        }

        val heading = TextView(this).apply {
            text = "B L A C K H O L E"
            textSize = 19f
            letterSpacing = 0.12f
            gravity = Gravity.CENTER
            setTextColor(silver)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        val subtitle = TextView(this).apply {
            text = "YOUR SPACE  •  YOUR CONTROL"
            textSize = 9f
            letterSpacing = 0.18f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(0, dp(5), 0, dp(15))
        }

        val clock = TextClock(this).apply {
            format12Hour = "hh:mm"
            format24Hour = "HH:mm"
            textSize = 53f
            gravity = Gravity.CENTER
            setTextColor(silver)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }

        val date = TextClock(this).apply {
            format12Hour = "EEEE  •  dd MMMM"
            format24Hour = "EEEE  •  dd MMMM"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(muted)
            setPadding(0, 0, 0, dp(22))
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            overScrollMode = View.OVER_SCROLL_NEVER
            addView(content)
        }

        root.addView(heading)
        root.addView(subtitle)
        root.addView(clock)
        root.addView(date)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        setContentView(root)

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
            addText(content, "HOME  /  CONTROL CENTER", 10f, neon, true)
            addText(content, "Your space.", 27f, silver, true)
            addText(content, "Everything you need, in one place.", 12f, muted)

            val search = EditText(this).apply {
                hint = "Search installed applications..."
                setHintTextColor(muted)
                setTextColor(silver)
                textSize = 14f
                maxLines = 1
                setSingleLine(true)
                isFocusable = false
                setPadding(dp(17), 0, dp(17), 0)
                background = roundedBox(panel, 18f)
                setOnClickListener { showApps("") }
            }

            content.addView(
                search,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(54)
                ).apply {
                    setMargins(0, dp(18), 0, dp(16))
                }
            )

            val lokiCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(17), dp(14), dp(17), dp(14))
                background = roundedBox(Color.rgb(9, 25, 36), 22f)
                isClickable = true
                isFocusable = true
                setOnClickListener { showLoki() }
            }

            val lokiInfo = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            addText(lokiInfo, "✦  LOKI AI", 17f, neon, true)
            addText(lokiInfo, "Your personal assistant", 11f, muted)
            addText(lokiInfo, "Tap to start a conversation", 10f, silver)

            lokiCard.addView(
                lokiInfo,
                LinearLayout.LayoutParams(0, dp(76), 1f)
            )

            val lokiOrb = TextView(this).apply {
                text = "L"
                textSize = 24f
                gravity = Gravity.CENTER
                setTextColor(neon)
                typeface = Typeface.DEFAULT_BOLD
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.rgb(8, 35, 49))
                    setStroke(dp(2), neon)
                }
            }

            lokiCard.addView(
                lokiOrb,
                LinearLayout.LayoutParams(dp(62), dp(62))
            )

            content.addView(
                lokiCard,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(108)
                ).apply {
                    setMargins(0, 0, 0, dp(20))
                }
            )

            addText(content, "QUICK ACCESS", 10f, muted, true)

            val shortcuts = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }

            fun makeShortcut(
                title: String,
                subtitle: String,
                action: () -> Unit
            ): LinearLayout {
                return LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(14), dp(12), dp(10), dp(12))
                    background = roundedBox(panel, 18f)
                    isClickable = true
                    isFocusable = true
                    setOnClickListener { action() }

                    addText(this, title, 13f, silver, true)
                    addText(this, subtitle, 10f, muted)
                }
            }

            shortcuts.addView(
                makeShortcut("APPS  →", "All applications") {
                    showApps("")
                },
                LinearLayout.LayoutParams(0, dp(82), 1f).apply {
                    setMargins(0, dp(8), dp(5), 0)
                }
            )

            shortcuts.addView(
                makeShortcut("SECURITY  🔒", "Hidden apps & PIN") {
                    LokiSecurity.openSecurityMenu(this@MainActivity) {
                        showApps("")
                    }
                },
                LinearLayout.LayoutParams(0, dp(82), 1f).apply {
                    setMargins(dp(5), dp(8), 0, 0)
                }
            )

            content.addView(
                shortcuts,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            addText(content, "BLACKHOLE  •  YOUR SPACE, YOUR CONTROL", 9f, muted)
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

                val icon = ImageView(this@MainActivity).apply {
                    setImageDrawable(info.loadIcon(packageManager))
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }

                addView(
                    icon,
                    LinearLayout.LayoutParams(dp(44), dp(44))
                )

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
