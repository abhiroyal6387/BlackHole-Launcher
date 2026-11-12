package org.blackhole.launcher

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
            letterSpacing = 0.03f
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
        showHome()
    }

    private fun showHome() {
        animateScreen {
            addText(content, "WELCOME BACK", 11f, muted, true)
            addText(content, "Your world. One touch away.", 21f, silver, true)
            addText(
                content,
                "A quieter, cleaner way to use your phone.",
                12f,
                muted
            )

            val search = EditText(this).apply {
                hint = "Search your apps"
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
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(54)
                ).apply {
                    setMargins(0, dp(24), 0, dp(14))
                }
            )

            addButton(content, "OPEN APP DRAWER  →") {
                showApps("")
            }

            addButton(content, "LOKI  •  AI ASSISTANT") {
                showLoki()
            }

            addText(content, "QUICK NOTE", 10f, muted, true)
            addText(
                content,
                "Your launcher is ready. Open the app drawer to explore your installed apps.",
                12f,
                muted
            )

            addText(
                content,
                "HOSTLOKI  /  VERSION 1.1",
                9f,
                muted
            )
        }
    }

    private fun showApps(initialFilter: String) {
        animateScreen {
            addText(content, "APPLICATIONS", 22f, silver, true)
            addText(content, "Your installed apps", 12f, muted)

            addButton(content, "←  HOME") {
                showHome()
            }

            val search = EditText(this).apply {
                hint = "Type an app name..."
                setHintTextColor(muted)
                setTextColor(silver)
                textSize = 14f
                maxLines = 1
                setSingleLine(true)
                setPadding(dp(16), 0, dp(16), 0)
                background = roundedBox(panel, 16f)
            }

            content.addView(
                search,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(50)
                ).apply {
                    setMargins(0, dp(14), 0, dp(16))
                }
            )

            val grid = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }
            content.addView(grid)

            fun updateApps(query: String) {
                populateApps(grid, query)
            }

            search.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    updateApps(s?.toString() ?: "")
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            search.setText(initialFilter)
            search.setSelection(search.text.length)
            updateApps(initialFilter)
        }
    }

    private fun populateApps(grid: LinearLayout, filter: String) {
        grid.removeAllViews()

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager.queryIntentActivities(intent, 0)
            .distinctBy { it.activityInfo.packageName }
            .filter {
                it.loadLabel(packageManager).toString()
                    .contains(filter, ignoreCase = true)
            }
            .sortedBy {
                it.loadLabel(packageManager).toString().lowercase()
            }

        if (apps.isEmpty()) {
            addText(grid, "No matching apps found.", 14f, muted)
            return
        }

        apps.forEach { info ->
            val appName = info.loadLabel(packageManager).toString()
            val packageName = info.activityInfo.packageName

            val item = TextView(this).apply {
                text = appName
                textSize = 15f
                letterSpacing = 0.025f
                gravity = Gravity.CENTER_VERTICAL
                setTextColor(silver)
                setPadding(dp(13), dp(15), dp(13), dp(15))
                minHeight = dp(54)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    try {
                        val launch = packageManager
                            .getLaunchIntentForPackage(packageName)

                        if (launch != null) {
                            startActivity(launch)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Unable to open app",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "Could not open app",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            grid.addView(
                item,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            val divider = View(this).apply {
                setBackgroundColor(Color.rgb(48, 51, 58))
            }

            grid.addView(
                divider,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(1)
                ).apply {
                    setMargins(dp(12), 0, dp(12), 0)
                }
            )

            item.alpha = 0f
            item.animate().alpha(1f).setDuration(180).start()
        }
    }

    private fun showLoki() {
        animateScreen {
            addText(content, "LOKI", 32f, silver, true)
            addText(content, "YOUR AI ASSISTANT", 10f, muted, true)

            addButton(content, "<- BACK TO HOME") {
                showHome()
            }

            lokiMessagesView = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(4), dp(8), dp(4), dp(8))
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
                    dp(340)
                ).apply {
                    setMargins(0, dp(8), 0, dp(8))
                }
            )

            if (lokiHistory.isEmpty()) {
                addLokiBubble(
                    "model",
                    "Namaste! Main Loki hoon. 😈\n\nBolo, aaj kya karna hai?"
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
                setTextColor(silver)
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

            addText(
                content,
                "GEMINI AI  /  CONNECTED THROUGH TERMUX",
                9f,
                muted
            )
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
            background = roundedBox(
                if (userMessage) Color.rgb(34, 37, 44)
                else panel,
                16f
            )
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
