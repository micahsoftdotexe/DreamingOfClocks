package com.micahsoftdotexe.dreamingofclocks.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Helper class to handle image selection with proper permissions for different Android versions.
 * Uses the built-in Photo Picker on Android 13+ and falls back to legacy picker on older versions.
 */
class ImagePickerHelper(private val context: Context) {

    /**
     * Returns the appropriate permissions to request based on Android version.
     */
    fun getRequiredPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                // Android 14+: Request partial media access
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                )
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13: Request media images permission
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
            }
            else -> {
                // Android 12 and below: Use legacy storage permission
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    /**
     * Checks if we need to request permissions.
     */
    fun needsPermissionRequest(): Boolean {
        return getRequiredPermissions().any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if we have the necessary permissions to read images.
     */
//    fun hasImagePermissions(): Boolean {
//        return getRequiredPermissions().all {
//            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
//        }
//    }

    /**
     * Shows a toast message to the user.
     */
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Composable that provides launchers for image picking and permission requests.
 */
@Composable
fun rememberImagePickerLaunchers(
    onImageSelected: (Uri?) -> Unit,
    onPermissionDenied: () -> Unit = {}
): ImagePickerLaunchers {
    val context = LocalContext.current
    val helper = remember { ImagePickerHelper(context) }

    // Photo picker launcher (Android 13+)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Take persistable URI permission so we can access it later
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some URIs don't support persistable permissions, but we can still use them
            }
            onImageSelected(uri)
        } else {
            helper.showToast("No image selected")
        }
    }

    // Legacy image picker launcher (Android 12 and below, or fallback)
    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Ignore
            }
            onImageSelected(uri)
        } else {
            helper.showToast("No image selected")
        }
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            launchImagePicker(photoPickerLauncher, legacyPickerLauncher)
        } else {
            helper.showToast("Permission denied. Cannot select images.")
            onPermissionDenied()
        }
    }

    return ImagePickerLaunchers(
        helper = helper,
        photoPickerLauncher = photoPickerLauncher,
        legacyPickerLauncher = legacyPickerLauncher,
        permissionLauncher = permissionLauncher
    )
}

/**
 * Launches the appropriate image picker based on Android version.
 */
private fun launchImagePicker(
    photoPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    legacyPickerLauncher: ManagedActivityResultLauncher<String, Uri?>
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Use the Photo Picker on Android 13+
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    } else {
        // Use legacy picker on older versions
        legacyPickerLauncher.launch("image/*")
    }
}

/**
 * Container for all image picker launchers and helper.
 */
data class ImagePickerLaunchers(
    val helper: ImagePickerHelper,
    val photoPickerLauncher: ManagedActivityResultLauncher<PickVisualMediaRequest, Uri?>,
    val legacyPickerLauncher: ManagedActivityResultLauncher<String, Uri?>,
    val permissionLauncher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    /**
     * Requests image selection, handling permissions if needed.
     */
    fun requestImageSelection() {
        if (helper.needsPermissionRequest()) {
            permissionLauncher.launch(helper.getRequiredPermissions())
        } else {
            launchImagePicker(photoPickerLauncher, legacyPickerLauncher)
        }
    }
}
