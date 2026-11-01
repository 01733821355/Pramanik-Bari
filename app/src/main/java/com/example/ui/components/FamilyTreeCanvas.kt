package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.domain.AgeCalculator
import com.example.domain.TreeLayoutCalculator
import com.example.domain.TreeNode
import com.example.ui.localization.FamilyStrings
import com.example.ui.theme.DeceasedBadgeColor
import com.example.ui.theme.EmeraldLineColor
import com.example.ui.theme.FemaleNodeBgDark
import com.example.ui.theme.FemaleNodeBgLight
import com.example.ui.theme.FemaleNodeColor
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldenLineColor
import com.example.ui.theme.MaleNodeBgDark
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.Button
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.MaleNodeBgLight
import com.example.ui.theme.MaleNodeColor
import kotlin.math.roundToInt

@Composable
fun FamilyTreeCanvas(
    members: List<FamilyMember>,
    collapsedBranches: Set<String>,
    selectedMember: FamilyMember?,
    zoomScale: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    isBn: Boolean,
    onTransform: (panX: Float, panY: Float, zoom: Float) -> Unit,
    onResetTransform: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onToggleBranch: (String) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onMemberClick: (FamilyMember) -> Unit,
    onMemberLongClick: (FamilyMember) -> Unit = {},
    onMemberSelectHighlight: (FamilyMember) -> Unit,
    onAddFirstMember: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (members.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pramanik_bari_icon_1793460110911),
                    contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isBn) "বংশলতিকায় এখনও কোনো সদস্য নেই" else "No Members in Family Tree",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isBn) "নতুন সদস্য যোগ করে প্রোফাইলে বা নোডে লং প্রেস করে পিতা-মাতা, স্বামী-স্ত্রী অথবা ভাই-বোনের সম্পর্ক যুক্ত করুন।"
                    else "Add family members and long press any node to set parent, spouse, or sibling relationships.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onAddFirstMember,
                    modifier = Modifier.testTag("btn_empty_tree_add_member")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBn) "প্রথম সদস্য যোগ করুন" else "Add First Member")
                }
            }
        }
        return
    }

    var searchNodeQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val layoutResult by remember(members, collapsedBranches, selectedMember?.id) {
        derivedStateOf {
            TreeLayoutCalculator.calculateLayout(
                allMembers = members,
                collapsedMemberIds = collapsedBranches,
                selectedMemberId = selectedMember?.id
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Interactive Canvas Layer with Zoom and Pan
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        onTransform(pan.x, pan.y, zoom)
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = zoomScale,
                        scaleY = zoomScale,
                        translationX = panOffsetX,
                        translationY = panOffsetY
                    )
            ) {
                // Background Connection Lines
                Canvas(
                    modifier = Modifier
                        .size(
                            width = layoutResult.totalWidth.dp,
                            height = layoutResult.totalHeight.dp
                        )
                ) {
                    val defaultLineColor = Color(0xFF9E9E9E)

                    // Draw Couple Connection Lines
                    for (c in layoutResult.coupleLines) {
                        val strokeColor = if (c.isHighlighted) GoldenLineColor else Color(0xFFC5A059)
                        val strokeWidth = if (c.isHighlighted) 4.5f else 2.5f

                        // Dual parallel lines for marriage union bond
                        drawLine(
                            color = strokeColor,
                            start = Offset(c.x1, c.y1 - 2.5f),
                            end = Offset(c.x2, c.y2 - 2.5f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = strokeColor,
                            start = Offset(c.x1, c.y1 + 2.5f),
                            end = Offset(c.x2, c.y2 + 2.5f),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }

                    // Draw Parent to Child Branch Lines
                    for (b in layoutResult.branchLines) {
                        val strokeColor = if (b.isHighlighted) EmeraldLineColor else defaultLineColor
                        val strokeWidth = if (b.isHighlighted) 4f else 2f

                        val path = Path().apply {
                            moveTo(b.parentMidX, b.parentMidY)
                            // Stepped orthogonal line: down halfway, horizontal to child, then down to child top
                            val midY = (b.parentMidY + b.childTopY) / 2
                            lineTo(b.parentMidX, midY)
                            lineTo(b.childTopX, midY)
                            lineTo(b.childTopX, b.childTopY)
                        }

                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Node Views
                for (node in layoutResult.nodes) {
                    TreeNodeCard(
                        node = node,
                        isBn = isBn,
                        onClick = { onMemberClick(node.member) },
                        onLongClick = { onMemberLongClick(node.member) },
                        onToggleCollapse = { onToggleBranch(node.member.id) },
                        modifier = Modifier.offset {
                            IntOffset(node.x.dp.roundToPx(), node.y.dp.roundToPx())
                        }
                    )
                }
            }
        }

        // Top Control Overlay
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = FamilyStrings.tabTree(isBn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${members.size} ${if (isBn) "জন" else "members"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            modifier = Modifier.testTag("tree_search_toggle")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search tree")
                        }
                        FilledTonalIconButton(
                            onClick = onZoomIn,
                            modifier = Modifier.size(36.dp).testTag("tree_zoom_in")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom in")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = onZoomOut,
                            modifier = Modifier.size(36.dp).testTag("tree_zoom_out")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom out")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        FilledTonalIconButton(
                            onClick = onResetTransform,
                            modifier = Modifier.size(36.dp).testTag("tree_reset_zoom")
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset view")
                        }
                    }
                }

                AnimatedVisibility(visible = isSearchExpanded) {
                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        OutlinedTextField(
                            value = searchNodeQuery,
                            onValueChange = { searchNodeQuery = it },
                            placeholder = { Text(FamilyStrings.search(isBn), fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("tree_search_input")
                        )
                        if (searchNodeQuery.isNotBlank()) {
                            val matches = members.filter { it.fullName.contains(searchNodeQuery, ignoreCase = true) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (match in matches.take(3)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.clickable {
                                            onMemberSelectHighlight(match)
                                        }
                                    ) {
                                        Text(
                                            text = match.fullName.split(" ").firstOrNull() ?: match.fullName,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Expand / Collapse Branch Buttons & Quick Relationship Tip
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${(zoomScale * 100).toInt()}% • " + if (isBn) "লং-প্রেস: সম্পর্ক" else "Long-press: Relation",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Row {
                        Text(
                            text = FamilyStrings.expandAll(isBn),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onExpandAll() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("tree_expand_all")
                        )
                        Text(" | ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = FamilyStrings.collapseAll(isBn),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { onCollapseAll() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("tree_collapse_all")
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreeNodeCard(
    node: TreeNode,
    isBn: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val m = node.member
    val isMale = m.gender == Gender.MALE
    val isFemale = m.gender == Gender.FEMALE
    val isDeceased = m.isDeceased

    val borderStrokeColor = when {
        node.isHighlighted -> GoldAccent
        node.isAncestorOfSelected -> GoldenLineColor
        node.isDescendantOfSelected -> EmeraldLineColor
        isMale -> MaleNodeColor
        isFemale -> FemaleNodeColor
        else -> MaterialTheme.colorScheme.outline
    }

    val borderWidth = if (node.isHighlighted || node.isAncestorOfSelected || node.isDescendantOfSelected) 2.5.dp else 1.dp

    Card(
        modifier = modifier
            .width(node.width.dp)
            .height(node.height.dp)
            .border(borderWidth, borderStrokeColor, RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
            .testTag("node_${m.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (node.isHighlighted) 8.dp else 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top badges (Gen + Age + Deceased)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Gen ${m.generation}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }

                val ageBadge = AgeCalculator.getCompactAgeBadge(m.dob, m.dod, m.isDeceased, isBn)
                if (ageBadge.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isDeceased) DeceasedBadgeColor else GoldAccent.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = ageBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDeceased) Color.White else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else if (isDeceased) {
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

            // Circular Photo or Initial Badge
            val resolvedUrl = m.getResolvedImageUrl()
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        if (isMale) MaleNodeColor else FemaleNodeColor,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (resolvedUrl.isNotBlank()) {
                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = m.fullName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (isMale) Color(0xFFE3F2FD) else Color(0xFFFCE4EC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isMale) MaleNodeColor else FemaleNodeColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Member Name
            Text(
                text = m.fullName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 11.sp,
                lineHeight = 13.sp
            )

            // Bottom Branch toggle or ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = m.id,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                if (node.hasChildren) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onToggleCollapse() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (node.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle branch",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
