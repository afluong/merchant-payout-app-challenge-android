package com.example.androidinterview.feature.payout.presentation
import android.app.Activity
import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
fun RememberScreenshotProtection(
    onScreenshotDetected: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    DisposableEffect(Unit) @androidx.annotation.RequiresPermission(android.Manifest.permission.DETECT_SCREEN_CAPTURE) {
        val contentResolver = context.contentResolver

        // Callback pour Android 14+ (API 34)
        val screenCaptureCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Activity.ScreenCaptureCallback {
                onScreenshotDetected()
            }
        } else null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && activity != null) {
            activity.registerScreenCaptureCallback(context.mainExecutor, screenCaptureCallback!!)
        }

        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                uri?.let {
                    if (isNewScreenshot(contentResolver, it)) {
                        onScreenshotDetected()
                    }
                }
            }
        }

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )

        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && activity != null && screenCaptureCallback != null) {
                try {
                    activity.unregisterScreenCaptureCallback(screenCaptureCallback)
                } catch (_: Exception) {}
            }
            contentResolver.unregisterContentObserver(contentObserver)
        }
    }
}

private fun isNewScreenshot(contentResolver: ContentResolver, uri: Uri): Boolean {
    try {
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATE_ADDED)
        contentResolver.query(uri, projection, null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC")?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val name = cursor.getString(nameColumn)?.lowercase() ?: ""
                val dateAdded = cursor.getLong(dateColumn)
                val currentTime = System.currentTimeMillis() / 1000

                if ((currentTime - dateAdded) < 5 && (name.contains("screenshot") || name.contains("screen"))) {
                    return true
                }
            }
        }
    } catch (_: Exception) {}
    return false
}