package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diversity1
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.repository.ConnectionType
import com.example.ui.theme.FemaleNodeColor
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MaleNodeColor

@Composable
fun QuickRelationshipDialog(
    sourceMember: FamilyMember,
    allMembers: List<FamilyMember>,
    isBn: Boolean,
    onDismiss: () -> Unit,
    onConnectRelationship: (sourceId: String, targetId: String, type: ConnectionType, customGen: Int?) -> Unit
) {
    val otherMembers = remember(allMembers, sourceMember.id) {
        allMembers.filter { it.id != sourceMember.id }
    }

    var selectedTargetId by remember {
        mutableStateOf(otherMembers.firstOrNull()?.id)
    }
    var selectedTargetMenuExpanded by remember { mutableStateOf(false) }

    var selectedConnectionType by remember {
        mutableStateOf(
            if (sourceMember.gender == Gender.FEMALE) ConnectionType.SET_AS_MOTHER else ConnectionType.SET_AS_FATHER
        )
    }

    var generationInput by remember {
        mutableStateOf(sourceMember.generation.toString())
    }

    val selectedTarget = otherMembers.find { it.id == selectedTargetId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FamilyRestroom,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBn) "সম্পর্ক ও অবস্থান সংযোগ" else "Set Relationship & Position",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Source member badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (sourceMember.gender == Gender.MALE) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (sourceMember.gender == Gender.MALE) MaleNodeColor else FemaleNodeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = sourceMember.fullName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "ID: ${sourceMember.id} • Gen ${sourceMember.generation}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (otherMembers.isEmpty()) {
                    Text(
                        text = if (isBn) "সম্পর্ক স্থাপনের জন্য পরিবারের আরও সদস্য যোগ করুন।"
                        else "Add more family members to connect relationships.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    // Target Member Dropdown Selector
                    Text(
                        text = if (isBn) "কার সাথে সম্পর্ক স্থাপন করবেন:" else "Connect with member:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTargetMenuExpanded = true }
                                .testTag("quick_rel_target_dropdown"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedTarget?.let { "${it.fullName} (${it.id})" } ?: "নির্বাচন করুন",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = selectedTargetMenuExpanded,
                            onDismissRequest = { selectedTargetMenuExpanded = false }
                        ) {
                            for (m in otherMembers) {
                                DropdownMenuItem(
                                    text = { Text("${m.fullName} (${m.id}) - Gen ${m.generation}") },
                                    onClick = {
                                        selectedTargetId = m.id
                                        selectedTargetMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Connection Type Selection
                    Text(
                        text = if (isBn) "সম্পর্কের ধরন নির্বাচন করুন:" else "Select Relationship Type:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RelTypeRadioItem(
                            label = if (isBn) "${sourceMember.fullName.split(" ").firstOrNull() ?: ""} হলেন পিতা (Father)" else "Set as Father of target",
                            selected = selectedConnectionType == ConnectionType.SET_AS_FATHER,
                            onClick = {
                                selectedConnectionType = ConnectionType.SET_AS_FATHER
                                if (selectedTarget != null) {
                                    generationInput = maxOf(1, selectedTarget.generation - 1).toString()
                                }
                            }
                        )

                        RelTypeRadioItem(
                            label = if (isBn) "${sourceMember.fullName.split(" ").firstOrNull() ?: ""} হলেন মাতা (Mother)" else "Set as Mother of target",
                            selected = selectedConnectionType == ConnectionType.SET_AS_MOTHER,
                            onClick = {
                                selectedConnectionType = ConnectionType.SET_AS_MOTHER
                                if (selectedTarget != null) {
                                    generationInput = maxOf(1, selectedTarget.generation - 1).toString()
                                }
                            }
                        )

                        RelTypeRadioItem(
                            label = if (isBn) "স্বামী ও স্ত্রী (Spouses / Couple)" else "Set as Husband & Wife",
                            selected = selectedConnectionType == ConnectionType.SET_AS_SPOUSE,
                            onClick = {
                                selectedConnectionType = ConnectionType.SET_AS_SPOUSE
                                if (selectedTarget != null) {
                                    generationInput = selectedTarget.generation.toString()
                                }
                            }
                        )

                        RelTypeRadioItem(
                            label = if (isBn) "${sourceMember.fullName.split(" ").firstOrNull() ?: ""} হলেন সন্তান (Child)" else "Set as Child of target",
                            selected = selectedConnectionType == ConnectionType.SET_AS_CHILD,
                            onClick = {
                                selectedConnectionType = ConnectionType.SET_AS_CHILD
                                if (selectedTarget != null) {
                                    generationInput = (selectedTarget.generation + 1).toString()
                                }
                            }
                        )

                        RelTypeRadioItem(
                            label = if (isBn) "পরস্পর ভাই-বোন (Siblings)" else "Set as Siblings",
                            selected = selectedConnectionType == ConnectionType.SET_AS_SIBLING,
                            onClick = {
                                selectedConnectionType = ConnectionType.SET_AS_SIBLING
                                if (selectedTarget != null) {
                                    generationInput = selectedTarget.generation.toString()
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Generation level tier
                Text(
                    text = if (isBn) "পুরুষ সংখ্যা / প্রজন্মের স্তর (বংশলতিকায় অবস্থান):" else "Purush / Generation Tier Position:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = generationInput,
                    onValueChange = { generationInput = it },
                    label = { Text(if (isBn) "পুরুষ সংখ্যা (১ = ১ম পুরুষ, ২ = ২য় পুরুষ...)" else "Purush Level (1, 2, 3...)") },
                    supportingText = {
                        val parsed = com.example.domain.TreeLayoutCalculator.parseBengaliOrLatinInt(generationInput)
                        Text(
                            text = com.example.domain.TreeLayoutCalculator.toPurushLabel(parsed, isBn),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_quick_gen")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customGen = com.example.domain.TreeLayoutCalculator.parseBengaliOrLatinInt(generationInput)
                    if (selectedTargetId != null) {
                        onConnectRelationship(sourceMember.id, selectedTargetId!!, selectedConnectionType, customGen)
                    }
                    onDismiss()
                },
                enabled = selectedTargetId != null,
                modifier = Modifier.testTag("btn_save_quick_rel")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isBn) "সম্পর্ক নিশ্চিত করুন" else "Confirm & Set")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isBn) "বাতিল" else "Cancel")
            }
        }
    )
}

@Composable
fun RelTypeRadioItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
