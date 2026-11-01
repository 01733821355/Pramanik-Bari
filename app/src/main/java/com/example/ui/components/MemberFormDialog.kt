package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.domain.AgeCalculator
import com.example.ui.localization.FamilyStrings
import com.example.ui.theme.GoldAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormDialog(
    initialMember: FamilyMember,
    allMembers: List<FamilyMember>,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onSave: (FamilyMember) -> Unit,
    onUploadPhotoBitmap: ((memberId: String, bitmap: Bitmap) -> Unit)? = null,
    onUploadPhotoBytes: ((memberId: String, bytes: ByteArray) -> Unit)? = null
) {
    val context = LocalContext.current

    var fullName by remember { mutableStateOf(initialMember.fullName) }
    var gender by remember { mutableStateOf(initialMember.gender) }
    var dob by remember { mutableStateOf(initialMember.dob) }
    var dod by remember { mutableStateOf(initialMember.dod) }
    var isDeceased by remember { mutableStateOf(initialMember.isDeceased) }
    var photoUrl by remember { mutableStateOf(initialMember.photoUrl) }
    var driveFileId by remember { mutableStateOf(initialMember.driveFileId) }
    var fatherId by remember { mutableStateOf(initialMember.fatherId) }
    var fatherName by remember { mutableStateOf(initialMember.fatherName ?: "") }
    var motherId by remember { mutableStateOf(initialMember.motherId) }
    var motherName by remember { mutableStateOf(initialMember.motherName ?: "") }
    var spouseId by remember { mutableStateOf(initialMember.spouseId) }
    var spouseName by remember { mutableStateOf(initialMember.spouseName ?: "") }
    var grandfatherName by remember { mutableStateOf(initialMember.grandfatherName ?: "") }
    var grandmotherName by remember { mutableStateOf(initialMember.grandmotherName ?: "") }
    var village by remember { mutableStateOf(initialMember.village.ifBlank { "প্রামাণিক পাড়া" }) }
    var address by remember { mutableStateOf(initialMember.address) }
    var phone by remember { mutableStateOf(initialMember.phone) }
    var occupation by remember { mutableStateOf(initialMember.occupation) }
    var bloodGroup by remember { mutableStateOf(initialMember.bloodGroup) }
    var biography by remember { mutableStateOf(initialMember.biography) }
    var notes by remember { mutableStateOf(initialMember.notes) }
    var generation by remember { mutableStateOf(initialMember.generation.toString()) }

    var validationError by remember { mutableStateOf<String?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            onUploadPhotoBitmap?.invoke(initialMember.id, it)
        }
    }

    // Gallery picker
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    onUploadPhotoBytes?.invoke(initialMember.id, bytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Live Age Calculation
    val calculatedAge = remember(dob, dod, isDeceased) {
        AgeCalculator.calculateAge(dob, dod, isDeceased)
    }

    // Dropdown states
    var fatherMenuExpanded by remember { mutableStateOf(false) }
    var motherMenuExpanded by remember { mutableStateOf(false) }
    var spouseMenuExpanded by remember { mutableStateOf(false) }

    val potentialFathers = remember(allMembers, initialMember.id) {
        allMembers.filter { it.id != initialMember.id && it.gender == Gender.MALE }
    }
    val potentialMothers = remember(allMembers, initialMember.id) {
        allMembers.filter { it.id != initialMember.id && it.gender == Gender.FEMALE }
    }
    val potentialSpouses = remember(allMembers, initialMember.id) {
        allMembers.filter { it.id != initialMember.id }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (initialMember.fullName.isBlank()) FamilyStrings.addMember(isBn) else FamilyStrings.editMember(isBn),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                if (fullName.trim().isBlank()) {
                                    validationError = if (isBn) "সদস্যের নাম আবশ্যক" else "Full name is required"
                                    return@Button
                                }

                                if (fatherId == initialMember.id || motherId == initialMember.id || spouseId == initialMember.id) {
                                    validationError = if (isBn) "নিজেকে পিতা/মাতা/স্ত্রী হিসেবে নির্বাচন করা যাবে না" else "Cannot select self as relative"
                                    return@Button
                                }

                                val genInt = generation.toIntOrNull() ?: 1
                                val updated = initialMember.copy(
                                    fullName = fullName.trim(),
                                    gender = gender,
                                    dob = dob.trim(),
                                    dod = if (isDeceased) dod.trim() else "",
                                    isDeceased = isDeceased,
                                    photoUrl = photoUrl.trim(),
                                    driveFileId = driveFileId.trim(),
                                    fatherId = fatherId,
                                    fatherName = fatherName.trim().takeIf { it.isNotBlank() },
                                    motherId = motherId,
                                    motherName = motherName.trim().takeIf { it.isNotBlank() },
                                    spouseId = spouseId,
                                    spouseName = spouseName.trim().takeIf { it.isNotBlank() },
                                    grandfatherName = grandfatherName.trim().takeIf { it.isNotBlank() },
                                    grandmotherName = grandmotherName.trim().takeIf { it.isNotBlank() },
                                    village = village.trim(),
                                    address = address.trim(),
                                    phone = phone.trim(),
                                    occupation = occupation.trim(),
                                    bloodGroup = bloodGroup.trim(),
                                    biography = biography.trim(),
                                    notes = notes.trim(),
                                    generation = genInt,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updated)
                            },
                            modifier = Modifier.padding(end = 8.dp).testTag("save_member_button")
                        ) {
                            Text(FamilyStrings.save(isBn))
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (validationError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Member ID Display
                OutlinedTextField(
                    value = initialMember.id,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(FamilyStrings.memberId(isBn)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_member_id")
                )

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it; validationError = null },
                    label = { Text("${FamilyStrings.fullName(isBn)} *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_full_name")
                )

                // Gender Radio Buttons
                Text(
                    text = FamilyStrings.gender(isBn),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == Gender.MALE,
                            onClick = { gender = Gender.MALE },
                            modifier = Modifier.testTag("gender_male")
                        )
                        Text(FamilyStrings.male(isBn))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = gender == Gender.FEMALE,
                            onClick = { gender = Gender.FEMALE },
                            modifier = Modifier.testTag("gender_female")
                        )
                        Text(FamilyStrings.female(isBn))
                    }
                }

                // Life Status (Living vs Deceased)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isDeceased,
                        onCheckedChange = { isDeceased = it },
                        modifier = Modifier.testTag("checkbox_is_deceased")
                    )
                    Text(FamilyStrings.isDeceased(isBn), style = MaterialTheme.typography.bodyMedium)
                }

                // DOB & Death Date (DOD)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text(FamilyStrings.dob(isBn)) },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f).testTag("input_dob")
                    )
                    if (isDeceased) {
                        OutlinedTextField(
                            value = dod,
                            onValueChange = { dod = it },
                            label = { Text(FamilyStrings.dod(isBn)) },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.weight(1f).testTag("input_dod")
                        )
                    }
                }

                // Live Age Display Preview
                if (calculatedAge != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldAccent.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isBn) "গণনাকৃত বয়স: ${calculatedAge.first}" else "Calculated Age: ${calculatedAge.second}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photo Options: Camera & Gallery & URL
                Text(
                    text = if (isBn) "ছবি যুক্ত করার মাধ্যম (ক্যামেরা / গ্যালারি)" else "Member Photo (Camera / Gallery)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f).testTag("form_camera_btn")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "ক্যামেরা ছবি" else "Take Photo")
                    }

                    OutlinedButton(
                        onClick = { galleryPickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).testTag("form_gallery_btn")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBn) "গ্যালারি ছবি" else "From Gallery")
                    }
                }

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text(FamilyStrings.photoUrl(isBn)) },
                    placeholder = { Text("https://drive.google.com/file/d/...") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_photo_url")
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isBn) "আত্মীয়তার সংযোগ (আইডি ভিত্তিক)" else "Family Relationships (ID Linked)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Father Picker
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedTextField(
                        value = if (fatherId != null) {
                            val f = potentialFathers.find { it.id == fatherId }
                            "${f?.fullName ?: fatherName} ($fatherId)"
                        } else fatherName,
                        onValueChange = { fatherName = it; fatherId = null },
                        label = { Text(FamilyStrings.father(isBn)) },
                        trailingIcon = {
                            IconButton(onClick = { fatherMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Father")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("picker_father")
                    )
                    DropdownMenu(
                        expanded = fatherMenuExpanded,
                        onDismissRequest = { fatherMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBn) "কোন পিতা নির্বাচন নেই" else "None") },
                            onClick = {
                                fatherId = null
                                fatherName = ""
                                fatherMenuExpanded = false
                            }
                        )
                        for (p in potentialFathers) {
                            DropdownMenuItem(
                                text = { Text("${p.fullName} (${p.id})") },
                                onClick = {
                                    fatherId = p.id
                                    fatherName = p.fullName
                                    generation = (p.generation + 1).toString()
                                    fatherMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Mother Picker
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedTextField(
                        value = if (motherId != null) {
                            val m = potentialMothers.find { it.id == motherId }
                            "${m?.fullName ?: motherName} ($motherId)"
                        } else motherName,
                        onValueChange = { motherName = it; motherId = null },
                        label = { Text(FamilyStrings.mother(isBn)) },
                        trailingIcon = {
                            IconButton(onClick = { motherMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Mother")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("picker_mother")
                    )
                    DropdownMenu(
                        expanded = motherMenuExpanded,
                        onDismissRequest = { motherMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBn) "কোন মাতা নির্বাচন নেই" else "None") },
                            onClick = {
                                motherId = null
                                motherName = ""
                                motherMenuExpanded = false
                            }
                        )
                        for (p in potentialMothers) {
                            DropdownMenuItem(
                                text = { Text("${p.fullName} (${p.id})") },
                                onClick = {
                                    motherId = p.id
                                    motherName = p.fullName
                                    motherMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Spouse Picker
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    OutlinedTextField(
                        value = if (spouseId != null) {
                            val s = potentialSpouses.find { it.id == spouseId }
                            "${s?.fullName ?: spouseName} ($spouseId)"
                        } else spouseName,
                        onValueChange = { spouseName = it; spouseId = null },
                        label = { Text(FamilyStrings.spouse(isBn)) },
                        trailingIcon = {
                            IconButton(onClick = { spouseMenuExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Spouse")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("picker_spouse")
                    )
                    DropdownMenu(
                        expanded = spouseMenuExpanded,
                        onDismissRequest = { spouseMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isBn) "কোন স্বামী/স্ত্রী নির্বাচন নেই" else "None") },
                            onClick = {
                                spouseId = null
                                spouseName = ""
                                spouseMenuExpanded = false
                            }
                        )
                        for (p in potentialSpouses) {
                            DropdownMenuItem(
                                text = { Text("${p.fullName} (${p.id})") },
                                onClick = {
                                    spouseId = p.id
                                    spouseName = p.fullName
                                    generation = p.generation.toString()
                                    spouseMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Grandparents
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = grandfatherName,
                        onValueChange = { grandfatherName = it },
                        label = { Text(FamilyStrings.grandfather(isBn)) },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = grandmotherName,
                        onValueChange = { grandmotherName = it },
                        label = { Text(FamilyStrings.grandmother(isBn)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Village, Address & Phone
                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    label = { Text(FamilyStrings.village(isBn)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_village")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(FamilyStrings.address(isBn)) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(FamilyStrings.phone(isBn)) },
                        modifier = Modifier.weight(1f).testTag("input_phone")
                    )
                    OutlinedTextField(
                        value = bloodGroup,
                        onValueChange = { bloodGroup = it },
                        label = { Text(FamilyStrings.bloodGroup(isBn)) },
                        placeholder = { Text("e.g. O+, A+") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Occupation & Generation
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text(FamilyStrings.occupation(isBn)) },
                        modifier = Modifier.weight(1.5f).testTag("input_occupation")
                    )
                    OutlinedTextField(
                        value = generation,
                        onValueChange = { generation = it },
                        label = { Text(FamilyStrings.generation(isBn)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Biography & Notes
                OutlinedTextField(
                    value = biography,
                    onValueChange = { biography = it },
                    label = { Text(FamilyStrings.biography(isBn)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("input_bio")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(FamilyStrings.notes(isBn)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
