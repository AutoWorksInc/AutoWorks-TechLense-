package ca.autoworks.techlense.evidence

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

data class EvidenceLaunchers(
    val takePhoto: () -> Unit,
    val takeVideo: () -> Unit
)

@Composable
fun rememberEvidenceLaunchers(
    createUri: (EvidenceMediaType) -> Uri,
    onCaptured: (EvidenceMediaType, Uri) -> Unit
): EvidenceLaunchers {
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    val photo = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = photoUri
        if (success && uri != null) onCaptured(EvidenceMediaType.PHOTO, uri)
        photoUri = null
    }
    val video = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
        val uri = videoUri
        if (success && uri != null) onCaptured(EvidenceMediaType.VIDEO, uri)
        videoUri = null
    }

    return EvidenceLaunchers(
        takePhoto = {
            val uri = createUri(EvidenceMediaType.PHOTO)
            photoUri = uri
            photo.launch(uri)
        },
        takeVideo = {
            val uri = createUri(EvidenceMediaType.VIDEO)
            videoUri = uri
            video.launch(uri)
        }
    )
}