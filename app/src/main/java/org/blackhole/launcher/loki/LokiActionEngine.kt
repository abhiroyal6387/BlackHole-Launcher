package org.blackhole.launcher.loki

import android.app.Activity
import android.content.Intent

object LokiActionEngine {

    fun execute(activity: Activity, command: String): String? {

        val text = command.trim().lowercase()

        val openWords = listOf(
            "open", "launch", "start",
            "kholo", "khol do", "chalao",
            " खोलो", " खोल दो"
        )

        if (openWords.none { text.contains(it) }) {
            return null
        }

        val searchName = text
            .replace(Regex("(?i)open|launch|start|kholo|khol do|chalao|खोलो|खोल दो"), "")
            .trim()

        if (searchName.isBlank()) {
            return "Kaunsi app open karni hai? 😈"
        }

        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val apps = activity.packageManager
                .queryIntentActivities(intent, 0)

            val matches = apps.filter {
                it.loadLabel(activity.packageManager)
                    .toString()
                    .contains(searchName, ignoreCase = true)
            }

            when {
                matches.isEmpty() -> {
                    "App nahi mili: $searchName"
                }

                matches.size > 1 -> {
                    "Multiple apps mili hain. Naam thoda specific batao."
                }

                else -> {
                    val info = matches.first()
                    val packageName = info.activityInfo.packageName

                    val launchIntent =
                        activity.packageManager
                            .getLaunchIntentForPackage(packageName)

                    if (launchIntent != null) {
                        activity.startActivity(launchIntent)
                        "Done! ${info.loadLabel(activity.packageManager)} open kar raha hoon. 😎"
                    } else {
                        "Is app ko launch nahi kar paya."
                    }
                }
            }

        } catch (e: Exception) {
            "App open nahi ho payi: ${e.message}"
        }
    }
}
