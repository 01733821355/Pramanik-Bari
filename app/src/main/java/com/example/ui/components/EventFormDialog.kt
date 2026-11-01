package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.FamilyEvent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PramanikGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventFormDialog(
    event: FamilyEvent?,
    language: String,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        date: String,
        location: String,
        description: String,
        existingUrls: List<String>,
        existingDriveIds: List<String>,
        newBitmaps: List<Bitmap>
    ) -> Unit
) {
    val context = LocalContext.current

    val initialDate = remember(event) {
        event?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var title by remember(event) { mutableStateOf(event?.title ?: "") }
    var date by remember(event) { mutableStateOf(initialDate) }
    var location by remember(event) { mutableStateOf(event?.location ?: "") }
    var description by remember(event) { mutableStateOf(event?.description ?: "") }

    val existingPhotoUrls = remember(event) {
        mutableStateListOf<String>().apply {
            if (event != null) addAll(event.getResolvedPhotoUrls())
        }
    }
    val existingDriveIds = remember(event) {
        mutableStateListOf<String>().apply {
            if (event != null) addAll(event.photoDriveIds)
        }
    }

    val newBitmaps = remember { mutableStateListOf<Bitmap>() }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            newBitmaps.add(bitmap)
        }
    }

    // Gallery launcher (Multiple images)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        for (uri in uris) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                newBitmaps.add(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(16.dp),
        title = {
            Text(
                text = if (event == null) {
                    if (language == "bn") "নতুন পারিবারিক ইভেন্ট যোগ করুন" else "Add New Family Event"
                } else {
                    if (language == "bn") "ইভেন্ট সম্পাদন করুন" else "Edit Family Event"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PramanikGreenDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Event Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (language == "bn") "ইভেন্টের নাম *" else "Event Title *") },
                    placeholder = { Text(if (language == "bn") "উদাঃ পারিবারিক পুনর্মিলনী ২০২৪, বিয়ে" else "e.g. Annual Reunion, Wedding") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input")
                )

                // Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(if (language == "bn") "তারিখ (YYYY-MM-DD) *" else "Date (YYYY-MM-DD) *") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = GoldAccent)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_date_input")
                )

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(if (language == "bn") "স্থান / গ্রাম" else "Location / Village") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PramanikGreenDark)
                    },
                    placeholder = { Text(if (language == "bn") "উদাঃ প্রামাণিক বাড়ি, নাটোর" else "e.g. Pramanik Bari") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_location_input")
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (language == "bn") "বিবরণ ও স্মৃতিচারণ" else "Description & Memories") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_description_input")
                )

                // Photo Selection Header
                Text(
                    text = if (language == "bn")
                        "ইভেন্টের ছবি (সরাসরি গুগল ড্রাইভে সংরক্ষিত হবে)"
                    else
                        "Event Photos (Stored permanently in Google Drive)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Buttons: Camera & Gallery
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f),
                        enabled = !isUploading
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = PramanikGreenDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == "bn") "ক্যামেরা" else "Camera")
                    }

                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isUploading
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == "bn") "গ্যালারি" else "Gallery")
                    }
                }

                // Photo Previews
                val totalPhotos = existingPhotoUrls.size + newBitmaps.size
                if (totalPhotos > 0) {
                    Text(
                        text = if (language == "bn") "নির্বাচিত ছবি ($totalPhotos টি):" else "Selected Photos ($totalPhotos):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Existing Photos
                        items(existingPhotoUrls.toList()) { url ->
                            Box(modifier = Modifier.size(80.dp)) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Existing photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val index = existingPhotoUrls.indexOf(url)
                                            if (index >= 0) {
                                                existingPhotoUrls.removeAt(index)
                                                if (index < existingDriveIds.size) {
                                                    existingDriveIds.removeAt(index)
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // New Bitmaps
                        items(newBitmaps.toList()) { bmp ->
                            Box(modifier = Modifier.size(80.dp)) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "New photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.65f),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                ) {
                                    IconButton(
                                        onClick = { newBitmaps.remove(bmp) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (isUploading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PramanikGreenDark,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text(
                            text = if (language == "bn")
                                "ছবিগুলো গুগল ড্রাইভে আপলোড হচ্ছে..."
                            else
                                "Uploading photos to Google Drive...",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PramanikGreenDark
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            title.trim(),
                            date.trim(),
                            location.trim(),
                            description.trim(),
                            existingPhotoUrls.toList(),
                            existingDriveIds.toList(),
                            newBitmaps.toList()
                        )
                    }
                },
                enabled = title.isNotBlank() && !isUploading,
                colors = ButtonDefaults.buttonColors(containerColor = PramanikGreenDark),
                modifier = Modifier.testTag("save_event_button")
            ) {
                Text(
                    text = if (language == "bn") "সংরক্ষণ করুন" else "Save Event",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text(if (language == "bn") "বাতিল" else "Cancel")
            }
        }
    )
}
