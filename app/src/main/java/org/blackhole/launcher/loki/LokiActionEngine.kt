package org.blackhole.launcher.loki

import android.app.Activity
import android.content.Intent
import java.util.Locale

object LokiActionEngine {

    private val actionWords = listOf(
        "open", "launch", "start", "run",
        "kholo", "khol", "khol do", "kholna",
        "chalao", "shuru karo", "start karo",
        "खोलो", "खोल", "खोल दो", "चलाओ",
        "चालू करो", "शुरू करो"
    )

    private val fillerWords = listOf(
        "please", "mere phone me", "mere phone mein",
        "mere mobile me", "mere mobile mein",
        "app", "application", "ko", "zara",
        "jara", "abhi", "the", "hai"
    )

    fun execute(activity: Activity, command: String): String? {
        val text = command.lowercase(Locale.ROOT).trim()

        if (actionWords.none { text.contains(it) }) return null

        var searchName = text

        (actionWords + fillerWords)
            .sortedByDescending { it.length }
            .forEach { word ->
                searchName = searchName.replace(word, " ")
            }

        searchName = searchName
            .replace(Regex("[^\\p{L}\\p{N} ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (searchName.isBlank()) {
            return "Kaunsi application kholni hai?"
        }

        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val apps = activity.packageManager
                .queryIntentActivities(intent, 0)

            val matches = apps.filter { info ->
                val label = info.loadLabel(activity.packageManager)
                    .toString()
                    .lowercase(Locale.ROOT)

                label == searchName || label.contains(searchName)
            }.distinctBy { it.activityInfo.packageName }

            when {
                matches.isEmpty() ->
                    "App nahi mili: $searchName. App ka naam dobara bolo."

                matches.size > 1 -> {
                    val names = matches.take(5).joinToString(", ") {
                        it.loadLabel(activity.packageManager).toString()
                    }
                    "Ek se zyada apps mili hain: $names. Exact naam bolo."
                }

                else -> {
                    val info = matches.first()
                    val appName = info.loadLabel(activity.packageManager).toString()
                    val launchIntent = activity.packageManager
                        .getLaunchIntentForPackage(info.activityInfo.packageName)

                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        activity.startActivity(launchIntent)
                        "$appName open kar raha hoon!"
                    } else {
                        "$appName ko open nahi kar paya."
                    }
                }
            }
        } catch (e: Exception) {
            "App open nahi ho payi: ${e.message ?: "Unknown error"}"
        }
    }
}
