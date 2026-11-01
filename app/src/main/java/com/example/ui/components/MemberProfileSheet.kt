package com.example.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.domain.AgeCalculator
import com.example.ui.localization.FamilyStrings
import com.example.ui.theme.DeceasedBadgeColor
import com.example.ui.theme.FemaleNodeColor
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MaleNodeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileSheet(
    member: FamilyMember,
    allMembers: List<FamilyMember>,
    isBn: Boolean,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onEdit: (FamilyMember) -> Unit,
    onDelete: (String) -> Unit,
    onNavigateToMember: (FamilyMember) -> Unit,
    onAddRelative: (parentId: String?, spouseId: String?) -> Unit,
    onConnectQuickRel: (FamilyMember) -> Unit = {},
    onUploadPhotoBytes: (memberId: String, bytes: ByteArray) -> Unit,
    onUploadPhotoBitmap: (memberId: String, bitmap: Bitmap) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showPhotoSourceChooser by remember { mutableStateOf(false) }

    // Gallery Picker
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bytes = inputStream?.readBytes()
                if (bytes != null) {
                    onUploadPhotoBytes(member.id, bytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera Capture Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            onUploadPhotoBitmap(member.id, it)
        }
    }

    val memberMap = remember(allMembers) { allMembers.associateBy { it.id } }
    val father = member.fatherId?.let { memberMap[it] }
    val mother = member.motherId?.let { memberMap[it] }
    val spouse = member.spouseId?.let { memberMap[it] }
    val children = remember(member, memberMap) {
        member.childrenIds.mapNotNull { memberMap[it] }
    }
    val siblings = remember(member, memberMap) {
        member.brotherSisterIds.mapNotNull { memberMap[it] }
    }

    val isMale = member.gender == Gender.MALE
    val resolvedPhotoUrl = member.getResolvedImageUrl()

    // Calculated Age
    val ageResult = remember(member.dob, member.dod, member.isDeceased) {
        AgeCalculator.calculateAge(member.dob, member.dod, member.isDeceased)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("member_profile_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header Hero Banner with Large Photo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(top = 24.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Large Circular Profile Photo with Camera / Gallery overlay
                    Box(
                        modifier = Modifier
                            .size(116.dp)
                            .clip(CircleShape)
                            .border(
                                3.dp,
                                if (isMale) MaleNodeColor else FemaleNodeColor,
                                CircleShape
                            )
                            .clickable { showPhotoSourceChooser = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (resolvedPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = resolvedPhotoUrl,
                                contentDescription = member.fullName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isMale) MaleNodeColor else FemaleNodeColor,
                                    modifier = Modifier.size(68.dp)
                                )
                            }
                        }

                        // Photo camera/gallery action badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Camera or Gallery",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = member.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    // Calculated Age & Deceased status badge
                    if (ageResult != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (member.isDeceased) DeceasedBadgeColor else GoldAccent,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = if (isBn) ageResult.first else ageResult.second,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (member.isDeceased) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "ID: ${member.id}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "Gen ${member.generation}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (member.isDeceased) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DeceasedBadgeColor
                            ) {
                                Text(
                                    text = if (isBn) "মরহুম" else "Deceased",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions (Call, Edit, Delete Member) - Open to all users!
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (member.phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("action_call_member")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(FamilyStrings.callPhone(isBn))
                    }
                }

                FilledTonalButton(
                    onClick = {
                        onDismiss()
                        onConnectQuickRel(member)
                    },
                    modifier = Modifier.testTag("action_connect_rel_member")
                ) {
                    Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "সম্পর্ক" else "Link")
                }

                Button(
                    onClick = { onEdit(member) },
                    modifier = Modifier.testTag("action_edit_member")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(FamilyStrings.editMember(isBn))
                }

                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("action_delete_member")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isBn) "মুছুন" else "Delete")
                }
            }

            // Personal Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "ব্যক্তিগত তথ্যাবলি" else "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ProfileInfoRow(
                        label = FamilyStrings.dob(isBn),
                        value = member.dob.ifBlank { if (isBn) "তথ্য নেই" else "Not specified" }
                    )

                    // Death Date and Age at Death
                    if (member.isDeceased) {
                        ProfileInfoRow(
                            label = FamilyStrings.dod(isBn),
                            value = member.dod.ifBlank { if (isBn) "মৃত্যুতারিখ উল্লেখ নেই" else "Not recorded" }
                        )

                        if (ageResult != null) {
                            ProfileInfoRow(
                                label = if (isBn) "জীবনকাল / বয়স" else "Lifespan / Age",
                                value = if (isBn) ageResult.first else ageResult.second
                            )
                        }
                    } else if (ageResult != null) {
                        ProfileInfoRow(
                            label = if (isBn) "বর্তমান বয়স" else "Current Age",
                            value = if (isBn) ageResult.first else ageResult.second
                        )
                    }

                    ProfileInfoRow(
                        label = FamilyStrings.village(isBn),
                        value = member.village.ifBlank { if (isBn) "প্রামাণিক বাড়ি" else "Pramanik Bari" }
                    )

                    if (member.address.isNotBlank()) {
                        ProfileInfoRow(label = FamilyStrings.address(isBn), value = member.address)
                    }

                    if (member.occupation.isNotBlank()) {
                        ProfileInfoRow(label = FamilyStrings.occupation(isBn), value = member.occupation)
                    }

                    if (member.bloodGroup.isNotBlank()) {
                        ProfileInfoRow(label = FamilyStrings.bloodGroup(isBn), value = member.bloodGroup)
                    }

                    if (member.grandfatherName?.isNotBlank() == true) {
                        ProfileInfoRow(label = FamilyStrings.grandfather(isBn), value = member.grandfatherName)
                    }

                    if (member.grandmotherName?.isNotBlank() == true) {
                        ProfileInfoRow(label = FamilyStrings.grandmother(isBn), value = member.grandmotherName)
                    }
                }
            }

            // Family Connections (Parents, Spouse, Children, Siblings)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "পারিবারিক আত্মীয়তার সম্পর্ক" else "Kinship & Relationships",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Father
                    KinChipRow(
                        title = FamilyStrings.father(isBn),
                        person = father,
                        fallbackName = member.fatherName,
                        isBn = isBn,
                        onClick = { father?.let { onNavigateToMember(it) } }
                    )

                    // Mother
                    KinChipRow(
                        title = FamilyStrings.mother(isBn),
                        person = mother,
                        fallbackName = member.motherName,
                        isBn = isBn,
                        onClick = { mother?.let { onNavigateToMember(it) } }
                    )

                    // Spouse
                    KinChipRow(
                        title = FamilyStrings.spouse(isBn),
                        person = spouse,
                        fallbackName = member.spouseName,
                        isBn = isBn,
                        onClick = { spouse?.let { onNavigateToMember(it) } }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Children
                    Text(
                        text = "${FamilyStrings.children(isBn)} (${children.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (children.isEmpty()) {
                        Text(
                            text = if (isBn) "কোন সন্তান তালিকাভুক্ত নেই" else "No children registered",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            for (c in children) {
                                KinPersonTile(person = c, onClick = { onNavigateToMember(c) })
                            }
                        }
                    }

                    // Anyone can add child!
                    TextButton(
                        onClick = {
                            if (member.gender == Gender.MALE) onAddRelative(member.id, member.spouseId)
                            else onAddRelative(member.spouseId, member.id)
                        },
                        modifier = Modifier.padding(top = 4.dp).testTag("add_child_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isBn) "সন্তান যুক্ত করুন" else "Add Child")
                    }

                    // Siblings
                    if (siblings.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        Text(
                            text = "${FamilyStrings.siblings(isBn)} (${siblings.size})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            for (s in siblings) {
                                KinPersonTile(person = s, onClick = { onNavigateToMember(s) })
                            }
                        }
                    }
                }
            }

            // Biography & Notes
            if (member.biography.isNotBlank() || member.notes.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (member.biography.isNotBlank()) {
                            Text(
                                text = FamilyStrings.biography(isBn),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = member.biography, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (member.notes.isNotBlank()) {
                            Text(
                                text = FamilyStrings.notes(isBn),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = member.notes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    // Photo Source Dialog (Camera vs Gallery)
    if (showPhotoSourceChooser) {
        AlertDialog(
            onDismissRequest = { showPhotoSourceChooser = false },
            title = { Text(if (isBn) "ছবি যুক্ত করুন" else "Add Member Photo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(if (isBn) "ক্যামেরা দিয়ে নতুন ছবি তুলুন অথবা গ্যালারি থেকে নির্বাচন করুন:" else "Take a new photo with camera or choose from gallery:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                showPhotoSourceChooser = false
                                cameraLauncher.launch(null)
                            },
                            modifier = Modifier.weight(1f).testTag("btn_camera_capture")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "ক্যামেরা" else "Camera")
                        }

                        OutlinedButton(
                            onClick = {
                                showPhotoSourceChooser = false
                                galleryPickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f).testTag("btn_gallery_pick")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBn) "গ্যালারি" else "Gallery")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPhotoSourceChooser = false }) {
                    Text(FamilyStrings.cancel(isBn))
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(FamilyStrings.deleteMember(isBn)) },
            text = {
                Text(
                    if (isBn) "আপনি কি নিশ্চিতভাবে ${member.fullName} এর রেকর্ড মুছে ফেলতে চান? এটি বংশলতিকা ও গুগল শিট ডাটাবেজ থেকেও মুছে যাবে।"
                    else "Are you sure you want to delete ${member.fullName}? This will remove them from the family tree and Google Sheets database."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(member.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "হ্যাঁ, মুছুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(FamilyStrings.cancel(isBn))
                }
            }
        )
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
fun KinChipRow(
    title: String,
    person: FamilyMember?,
    fallbackName: String?,
    isBn: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        if (person != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clickable { onClick() }
            ) {
                Text(
                    text = person.fullName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        } else if (!fallbackName.isNullOrBlank()) {
            Text(text = fallbackName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        } else {
            Text(
                text = if (isBn) "উল্লেখ নেই" else "None",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun KinPersonTile(person: FamilyMember, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val resolved = person.getResolvedImageUrl()
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (person.gender == Gender.MALE) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)),
                contentAlignment = Alignment.Center
            ) {
                if (resolved.isNotBlank()) {
                    AsyncImage(
                        model = resolved,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (person.gender == Gender.MALE) MaleNodeColor else FemaleNodeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = person.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = "ID: ${person.id} • Gen ${person.generation}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
