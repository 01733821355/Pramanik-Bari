package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.ui.localization.FamilyStrings
import com.example.ui.theme.DeceasedBadgeColor
import com.example.ui.theme.FemaleNodeColor
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MaleNodeColor

@Composable
fun DashboardView(
    members: List<FamilyMember>,
    isBn: Boolean,
    onNavigateToTree: () -> Unit,
    onNavigateToMembers: () -> Unit,
    onNavigateToRelationship: () -> Unit,
    onPlayIntro: () -> Unit,
    onAddMember: () -> Unit,
    onMemberClick: (FamilyMember) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = members.size
    val maleCount = remember(members) { members.count { it.gender == Gender.MALE } }
    val femaleCount = remember(members) { members.count { it.gender == Gender.FEMALE } }
    val livingCount = remember(members) { members.count { !it.isDeceased } }
    val deceasedCount = remember(members) { members.count { it.isDeceased } }
    val generationsCount = remember(members) {
        if (members.isEmpty()) 0 else members.maxOf { it.generation }
    }
    val recentMembers = remember(members) {
        members.sortedByDescending { it.updatedAt }.take(8)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        // Hero Estate Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.pramanik_banner_1793457827547),
                contentDescription = "Pramanik Bari Heritage Estate",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x99000000),
                                Color(0xE60D3325)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = GoldAccent
                ) {
                    Text(
                        text = if (isBn) "ঐতিহ্যবাহী বংশানুক্রম" else "HERITAGE LINEAGE ARCHIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B231F),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = FamilyStrings.appTitle(isBn),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = FamilyStrings.appSubtitle(isBn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE0E0E0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Quick Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onNavigateToTree,
                modifier = Modifier.weight(1f).testTag("dashboard_btn_tree")
            ) {
                Icon(Icons.Default.AccountTree, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(FamilyStrings.tabTree(isBn))
            }

            OutlinedButton(
                onClick = onAddMember,
                modifier = Modifier.weight(1f).testTag("dashboard_btn_add_member")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(FamilyStrings.addMember(isBn))
            }
        }

        // Statistics Grid (6 Cards)
        Text(
            text = if (isBn) "বংশলতিকার সামগ্রিক পরিসংখ্যান" else "Family Lineage Statistics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Row 1: Total & Generations
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = FamilyStrings.totalMembers(isBn),
                    count = totalCount.toString(),
                    icon = Icons.Default.Groups,
                    iconTint = MaterialTheme.colorScheme.primary,
                    bgColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f).testTag("stat_total_members"),
                    onClick = onNavigateToMembers
                )
                StatCard(
                    title = FamilyStrings.totalGenerations(isBn),
                    count = generationsCount.toString(),
                    icon = Icons.Default.Layers,
                    iconTint = GoldAccent,
                    bgColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f).testTag("stat_generations"),
                    onClick = onNavigateToTree
                )
            }

            // Row 2: Male & Female
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = FamilyStrings.maleMembers(isBn),
                    count = maleCount.toString(),
                    icon = Icons.Default.Male,
                    iconTint = MaleNodeColor,
                    bgColor = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f).testTag("stat_male_members"),
                    onClick = onNavigateToMembers
                )
                StatCard(
                    title = FamilyStrings.femaleMembers(isBn),
                    count = femaleCount.toString(),
                    icon = Icons.Default.Female,
                    iconTint = FemaleNodeColor,
                    bgColor = Color(0xFFFCE4EC),
                    modifier = Modifier.weight(1f).testTag("stat_female_members"),
                    onClick = onNavigateToMembers
                )
            }

            // Row 3: Living & Deceased
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = FamilyStrings.livingMembers(isBn),
                    count = livingCount.toString(),
                    icon = Icons.Default.WbSunny,
                    iconTint = Color(0xFF2E7D32),
                    bgColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f).testTag("stat_living_members"),
                    onClick = onNavigateToMembers
                )
                StatCard(
                    title = FamilyStrings.deceasedMembers(isBn),
                    count = deceasedCount.toString(),
                    icon = Icons.Default.Nightlight,
                    iconTint = DeceasedBadgeColor,
                    bgColor = Color(0xFFECEFF1),
                    modifier = Modifier.weight(1f).testTag("stat_deceased_members"),
                    onClick = onNavigateToMembers
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Emotional Ancestral Intro Video Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onPlayIntro() }
                .testTag("dashboard_btn_play_intro"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoldAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color(0xFF1E2822),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isBn) "ঐতিহ্যের স্মৃতিকথা ও সূচনা ভিডিও" else "Watch Heritage & Lineage Intro",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isBn) "শিকড়ের সন্ধানে পূর্বপুরুষদের স্মৃতিচারণ পুনরায় দেখুন"
                        else "Replay the emotional ancestral presentation and history",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recently Added Members Carousel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = FamilyStrings.recentlyAdded(isBn),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isBn) "সব দেখুন" else "View All",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onNavigateToMembers() }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(recentMembers) { member ->
                RecentMemberCard(
                    member = member,
                    isBn = isBn,
                    onClick = { onMemberClick(member) }
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RecentMemberCard(
    member: FamilyMember,
    isBn: Boolean,
    onClick: () -> Unit
) {
    val isMale = member.gender == Gender.MALE
    val resolvedUrl = member.getResolvedImageUrl()

    Card(
        modifier = Modifier
            .width(135.dp)
            .clickable { onClick() }
            .testTag("recent_member_${member.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (isMale) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)),
                contentAlignment = Alignment.Center
            ) {
                if (resolvedUrl.isNotBlank()) {
                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isMale) MaleNodeColor else FemaleNodeColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = member.fullName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Gen ${member.generation} • ${member.id}",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
