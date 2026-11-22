package org.blackhole.launcher.loki

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import java.security.MessageDigest
import java.security.SecureRandom

object LokiSecurity {
    private const val PREFS = "blackhole_security"
    private const val PIN_HASH = "pin_hash"
    private const val PIN_SALT = "pin_salt"
    private const val HIDDEN = "hidden_packages"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun hasPin(context: Context): Boolean =
        !prefs(context).getString(PIN_HASH, null).isNullOrBlank()

    private fun hash(pin: String, salt: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest((salt + pin).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun isHidden(context: Context, packageName: String): Boolean =
        prefs(context).getStringSet(HIDDEN, emptySet())
            ?.contains(packageName) == true

    fun hidePackage(context: Context, packageName: String) {
        val set = prefs(context).getStringSet(HIDDEN, emptySet())
            ?.toMutableSet() ?: mutableSetOf()
        set.add(packageName)
        prefs(context).edit().putStringSet(HIDDEN, set).apply()
    }

    private fun unhidePackage(context: Context, packageName: String) {
        val set = prefs(context).getStringSet(HIDDEN, emptySet())
            ?.toMutableSet() ?: mutableSetOf()
        set.remove(packageName)
        prefs(context).edit().putStringSet(HIDDEN, set).apply()
    }

    fun startupGate(activity: Activity, onSuccess: () -> Unit) {
        if (hasPin(activity)) requirePin(activity, onSuccess)
        else onSuccess()
    }

    fun requirePin(activity: Activity, onSuccess: () -> Unit) {
        if (!hasPin(activity)) {
            setupPin(activity, onSuccess)
            return
        }

        val input = EditText(activity).apply {
            hint = "Enter launcher PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle("HOSTLOKI LOCK")
            .setMessage("Enter your PIN to continue.")
            .setView(input)
            .setPositiveButton("UNLOCK", null)
            .setNegativeButton("CANCEL", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val savedSalt = prefs(activity).getString(PIN_SALT, "") ?: ""
                val savedHash = prefs(activity).getString(PIN_HASH, "") ?: ""
                val entered = input.text.toString()

                if (entered.length in 4..8 &&
                    hash(entered, savedSalt) == savedHash
                ) {
                    dialog.dismiss()
                    onSuccess()
                } else {
                    input.error = "Incorrect PIN"
                }
            }
        }
        dialog.show()
    }

    private fun setupPin(activity: Activity, onSuccess: () -> Unit) {
        val layout = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 8, 40, 4)
        }

        val first = EditText(activity).apply {
            hint = "Create 4–8 digit PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        val second = EditText(activity).apply {
            hint = "Confirm PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        layout.addView(first)
        layout.addView(second)

        val dialog = AlertDialog.Builder(activity)
            .setTitle("SET HOSTLOKI PIN")
            .setMessage("Choose a PIN you can remember.")
            .setView(layout)
            .setPositiveButton("SAVE", null)
            .setNegativeButton("CANCEL", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val pin = first.text.toString()
                if (pin.length !in 4..8 || !pin.all { it.isDigit() }) {
                    first.error = "Use 4 to 8 digits"
                    return@setOnClickListener
                }
                if (pin != second.text.toString()) {
                    second.error = "PINs do not match"
                    return@setOnClickListener
                }

                val saltBytes = ByteArray(16)
                SecureRandom().nextBytes(saltBytes)
                val salt = saltBytes.joinToString("") { "%02x".format(it) }

                prefs(activity).edit()
                    .putString(PIN_SALT, salt)
                    .putString(PIN_HASH, hash(pin, salt))
                    .apply()

                dialog.dismiss()
                Toast.makeText(activity, "PIN saved", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
        }
        dialog.show()
    }

    fun openSecurityMenu(activity: Activity, onApps: () -> Unit) {
        val options = arrayOf("Set / Change PIN", "Manage Hidden Apps", "Close")
        AlertDialog.Builder(activity)
            .setTitle("HOSTLOKI SECURITY")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        if (hasPin(activity)) {
                            requirePin(activity) { setupPin(activity) {} }
                        } else {
                            setupPin(activity) {}
                        }
                    }
                    1 -> requirePin(activity) { showHiddenApps(activity) }
                }
            }
            .show()
    }

    private fun showHiddenApps(activity: Activity) {
        val hidden = prefs(activity).getStringSet(HIDDEN, emptySet())
            ?.toList() ?: emptyList()

        if (hidden.isEmpty()) {
            Toast.makeText(activity, "No hidden apps", Toast.LENGTH_SHORT).show()
            return
        }

        val labels = hidden.map { packageName ->
            try {
                activity.packageManager.getApplicationLabel(
                    activity.packageManager.getApplicationInfo(packageName, 0)
                ).toString()
            } catch (_: Exception) {
                packageName
            }
        }.toTypedArray()

        AlertDialog.Builder(activity)
            .setTitle("Hidden Apps — tap to unhide")
            .setItems(labels) { _, index ->
                unhidePackage(activity, hidden[index])
                Toast.makeText(activity, "App unhidden", Toast.LENGTH_SHORT).show()
                showHiddenApps(activity)
            }
            .setNegativeButton("DONE", null)
            .show()
    }
}
