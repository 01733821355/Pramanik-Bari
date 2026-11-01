package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun MembersListView(
    members: List<FamilyMember>,
    searchQuery: String,
    isBn: Boolean,
    onSearchChange: (String) -> Unit,
    onMemberClick: (FamilyMember) -> Unit,
    onAddMember: () -> Unit,
    modifier: Modifier = Modifier
) {
    var genderFilter by remember { mutableStateOf<Gender?>(null) }
    var livingFilter by remember { mutableStateOf<Boolean?>(null) } // true=living, false=deceased

    val filteredList = remember(members, searchQuery, genderFilter, livingFilter) {
        members.filter { m ->
            val matchQuery = searchQuery.isBlank() ||
                    m.fullName.contains(searchQuery, ignoreCase = true) ||
                    m.village.contains(searchQuery, ignoreCase = true) ||
                    m.occupation.contains(searchQuery, ignoreCase = true) ||
                    m.id.contains(searchQuery, ignoreCase = true)

            val matchGender = genderFilter == null || m.gender == genderFilter
            val matchLiving = livingFilter == null || (if (livingFilter == true) !m.isDeceased else m.isDeceased)

            matchQuery && matchGender && matchLiving
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMember,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_member")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Member")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text(FamilyStrings.search(isBn)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("member_list_search_input")
            )

            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    InputChip(
                        selected = genderFilter == null && livingFilter == null,
                        onClick = {
                            genderFilter = null
                            livingFilter = null
                        },
                        label = { Text(if (isBn) "সকল (${members.size})" else "All (${members.size})") }
                    )
                }
                item {
                    InputChip(
                        selected = genderFilter == Gender.MALE,
                        onClick = { genderFilter = if (genderFilter == Gender.MALE) null else Gender.MALE },
                        label = { Text(FamilyStrings.male(isBn)) }
                    )
                }
                item {
                    InputChip(
                        selected = genderFilter == Gender.FEMALE,
                        onClick = { genderFilter = if (genderFilter == Gender.FEMALE) null else Gender.FEMALE },
                        label = { Text(FamilyStrings.female(isBn)) }
                    )
                }
                item {
                    InputChip(
                        selected = livingFilter == true,
                        onClick = { livingFilter = if (livingFilter == true) null else true },
                        label = { Text(FamilyStrings.livingMembers(isBn)) }
                    )
                }
                item {
                    InputChip(
                        selected = livingFilter == false,
                        onClick = { livingFilter = if (livingFilter == false) null else false },
                        label = { Text(FamilyStrings.deceasedMembers(isBn)) }
                    )
                }
            }

            // Member Cards List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isBn) "কোন সদস্য খুঁজে পাওয়া যায়নি" else "No members found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredList, key = { it.id }) { member ->
                        MemberCardItem(
                            member = member,
                            isBn = isBn,
                            onClick = { onMemberClick(member) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCardItem(
    member: FamilyMember,
    isBn: Boolean,
    onClick: () -> Unit
) {
    val isMale = member.gender == Gender.MALE
    val resolvedUrl = member.getResolvedImageUrl()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("member_card_${member.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (isMale) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)),
                contentAlignment = Alignment.Center
            ) {
                if (resolvedUrl.isNotBlank()) {
                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = member.fullName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isMale) MaleNodeColor else FemaleNodeColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val ageBadge = AgeCalculator.getCompactAgeBadge(member.dob, member.dod, member.isDeceased, isBn)
                        if (ageBadge.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (member.isDeceased) DeceasedBadgeColor else GoldAccent.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = ageBadge,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (member.isDeceased) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Gen ${member.generation}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ID: ${member.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    if (member.village.isNotBlank()) {
                        Text(
                            text = "• ${member.village}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1
                        )
                    }

                    if (member.isDeceased) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DeceasedBadgeColor
                        ) {
                            Text(
                                text = if (isBn) "মরহুম" else "Late",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (member.occupation.isNotBlank()) {
                    Text(
                        text = member.occupation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
