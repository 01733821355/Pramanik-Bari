package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.Celebration
import com.example.R
import com.example.ui.components.DashboardView
import com.example.ui.components.EmotionalIntroSequence
import com.example.ui.components.EventFormDialog
import com.example.ui.components.EventsView
import com.example.ui.components.FamilyTreeCanvas
import com.example.ui.components.MemberFormDialog
import com.example.ui.components.MemberProfileSheet
import com.example.ui.components.MembersListView
import com.example.ui.components.PhotoGalleryViewerDialog
import com.example.ui.components.QuickRelationshipDialog
import com.example.ui.components.RelationshipFinderView
import com.example.ui.localization.FamilyStrings
import com.example.ui.theme.GoldAccent
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.FamilyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyApp(
    viewModel: FamilyViewModel = viewModel()
) {
    val members by viewModel.members.collectAsState()
    val events by viewModel.events.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val language by viewModel.language.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMember by viewModel.selectedMember.collectAsState()
    val isProfileOpen by viewModel.isProfileOpen.collectAsState()
    val isMemberFormOpen by viewModel.isMemberFormOpen.collectAsState()
    val memberToEdit by viewModel.memberToEdit.collectAsState()
    val isEventFormOpen by viewModel.isEventFormOpen.collectAsState()
    val eventToEdit by viewModel.eventToEdit.collectAsState()
    val selectedEventForGallery by viewModel.selectedEventForGallery.collectAsState()
    val isUploadingEventPhotos by viewModel.isUploadingEventPhotos.collectAsState()
    val memberForQuickRel by viewModel.memberForQuickRel.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()
    val showIntro by viewModel.showIntro.collectAsState()
    val collapsedBranches by viewModel.collapsedBranches.collectAsState()
    val personAId by viewModel.personAId.collectAsState()
    val personBId by viewModel.personBId.collectAsState()
    val relationshipResult by viewModel.relationshipResult.collectAsState()
    val treeZoom by viewModel.treeZoom.collectAsState()
    val treePanX by viewModel.treePanX.collectAsState()
    val treePanY by viewModel.treePanY.collectAsState()

    val isBn = language == "bn"
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val snackbarHostState = remember { SnackbarHostState() }

    // Sync message snackbar
    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    // Handle Back Press
    BackHandler(enabled = isProfileOpen || isMemberFormOpen || isEventFormOpen || selectedEventForGallery != null || memberForQuickRel != null || currentTab != AppTab.DASHBOARD) {
        when {
            memberForQuickRel != null -> viewModel.closeQuickRelationship()
            selectedEventForGallery != null -> viewModel.closeEventGallery()
            isEventFormOpen -> viewModel.closeEventForm()
            isProfileOpen -> viewModel.closeProfile()
            isMemberFormOpen -> viewModel.closeMemberForm()
            currentTab != AppTab.DASHBOARD -> viewModel.setTab(AppTab.DASHBOARD)
        }
    }

    // Emotional Intro Animation Screen
    if (showIntro) {
        EmotionalIntroSequence(
            isBn = isBn,
            onEnterApp = { viewModel.dismissIntro() }
        )
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 680.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.pramanik_bari_icon_1793460110911),
                                contentDescription = "Pramanik Bari Emblem",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = FamilyStrings.appTitle(isBn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        // Play Emotional Intro button
                        IconButton(
                            onClick = { viewModel.replayIntro() },
                            modifier = Modifier.testTag("btn_top_replay_intro")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Watch Intro",
                                tint = GoldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Background Cloud Sync Trigger
                        IconButton(
                            onClick = { viewModel.syncWithCloud(silent = false) },
                            enabled = !isSyncing,
                            modifier = Modifier.testTag("btn_top_cloud_sync")
                        ) {
                            if (isSyncing) {
                                val infiniteTransition = rememberInfiniteTransition()
                                val angle by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 360f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1000, easing = LinearEasing),
                                        repeatMode = RepeatMode.Restart
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Syncing...",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.rotate(angle)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync Cloud",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Language Switcher (বাং / EN)
                        IconButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier.testTag("language_toggle_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isBn) "বাং" else "EN",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            selected = currentTab == AppTab.DASHBOARD,
                            onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text(FamilyStrings.tabDashboard(isBn), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_tab_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppTab.TREE,
                            onClick = { viewModel.setTab(AppTab.TREE) },
                            icon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                            label = { Text(FamilyStrings.tabTree(isBn), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_tab_tree")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppTab.MEMBERS,
                            onClick = { viewModel.setTab(AppTab.MEMBERS) },
                            icon = { Icon(Icons.Default.People, contentDescription = null) },
                            label = { Text(FamilyStrings.tabMembers(isBn), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_tab_members")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppTab.EVENTS,
                            onClick = { viewModel.setTab(AppTab.EVENTS) },
                            icon = { Icon(Icons.Default.Celebration, contentDescription = null) },
                            label = { Text(FamilyStrings.tabEvents(isBn), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_tab_events")
                        )
                        NavigationBarItem(
                            selected = currentTab == AppTab.RELATIONSHIP,
                            onClick = { viewModel.setTab(AppTab.RELATIONSHIP) },
                            icon = { Icon(Icons.Default.Hub, contentDescription = null) },
                            label = { Text(FamilyStrings.tabRelation(isBn), fontSize = 10.sp) },
                            modifier = Modifier.testTag("nav_tab_relation")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Wide Screen Side Navigation Rail
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationRailItem(
                            selected = currentTab == AppTab.DASHBOARD,
                            onClick = { viewModel.setTab(AppTab.DASHBOARD) },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text(FamilyStrings.tabDashboard(isBn)) }
                        )
                        NavigationRailItem(
                            selected = currentTab == AppTab.TREE,
                            onClick = { viewModel.setTab(AppTab.TREE) },
                            icon = { Icon(Icons.Default.AccountTree, contentDescription = null) },
                            label = { Text(FamilyStrings.tabTree(isBn)) }
                        )
                        NavigationRailItem(
                            selected = currentTab == AppTab.MEMBERS,
                            onClick = { viewModel.setTab(AppTab.MEMBERS) },
                            icon = { Icon(Icons.Default.People, contentDescription = null) },
                            label = { Text(FamilyStrings.tabMembers(isBn)) }
                        )
                        NavigationRailItem(
                            selected = currentTab == AppTab.EVENTS,
                            onClick = { viewModel.setTab(AppTab.EVENTS) },
                            icon = { Icon(Icons.Default.Celebration, contentDescription = null) },
                            label = { Text(FamilyStrings.tabEvents(isBn)) }
                        )
                        NavigationRailItem(
                            selected = currentTab == AppTab.RELATIONSHIP,
                            onClick = { viewModel.setTab(AppTab.RELATIONSHIP) },
                            icon = { Icon(Icons.Default.Hub, contentDescription = null) },
                            label = { Text(FamilyStrings.tabRelation(isBn)) }
                        )
                    }
                }

                // Main Content Screen
                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        AppTab.DASHBOARD -> DashboardView(
                            members = members,
                            isBn = isBn,
                            onNavigateToTree = { viewModel.setTab(AppTab.TREE) },
                            onNavigateToMembers = { viewModel.setTab(AppTab.MEMBERS) },
                            onNavigateToRelationship = { viewModel.setTab(AppTab.RELATIONSHIP) },
                            onPlayIntro = { viewModel.replayIntro() },
                            onAddMember = { viewModel.openAddMember() },
                            onMemberClick = { viewModel.selectMember(it) }
                        )

                        AppTab.TREE -> FamilyTreeCanvas(
                            members = members,
                            collapsedBranches = collapsedBranches,
                            selectedMember = selectedMember,
                            zoomScale = treeZoom,
                            panOffsetX = treePanX,
                            panOffsetY = treePanY,
                            isBn = isBn,
                            onTransform = { panX, panY, zoom -> viewModel.updateTreeTransform(panX, panY, zoom) },
                            onResetTransform = { viewModel.resetTreeTransform() },
                            onZoomIn = { viewModel.zoomIn() },
                            onZoomOut = { viewModel.zoomOut() },
                            onToggleBranch = { viewModel.toggleBranch(it) },
                            onExpandAll = { viewModel.expandAllBranches() },
                            onCollapseAll = { viewModel.collapseAllBranches() },
                            onMemberClick = { viewModel.selectMember(it) },
                            onMemberLongClick = { viewModel.openQuickRelationship(it) },
                            onMemberSelectHighlight = { viewModel.selectMemberForTreeHighlight(it) },
                            onAddFirstMember = { viewModel.openAddMember() }
                        )

                        AppTab.MEMBERS -> MembersListView(
                            members = members,
                            searchQuery = searchQuery,
                            isBn = isBn,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            onMemberClick = { viewModel.selectMember(it) },
                            onAddMember = { viewModel.openAddMember() }
                        )

                        AppTab.EVENTS -> EventsView(
                            events = events,
                            language = language,
                            onAddEvent = { viewModel.openEventForm() },
                            onEditEvent = { viewModel.openEventForm(it) },
                            onDeleteEvent = { viewModel.deleteEvent(it) },
                            onViewAlbum = { viewModel.openEventGallery(it) }
                        )

                        AppTab.RELATIONSHIP -> RelationshipFinderView(
                            members = members,
                            personAId = personAId,
                            personBId = personBId,
                            result = relationshipResult,
                            isBn = isBn,
                            onSelectPersonA = { viewModel.setPersonA(it) },
                            onSelectPersonB = { viewModel.setPersonB(it) },
                            onCalculate = { viewModel.calculateRelationship() },
                            onMemberClick = { viewModel.selectMember(it) }
                        )
                    }
                }
            }
        }

        // Quick Relationship & Tree Positioning Dialog (Triggered on node long-press)
        if (memberForQuickRel != null) {
            QuickRelationshipDialog(
                sourceMember = memberForQuickRel!!,
                allMembers = members,
                isBn = isBn,
                onDismiss = { viewModel.closeQuickRelationship() },
                onConnectRelationship = { sourceId, targetId, type, customGen ->
                    viewModel.connectRelationship(sourceId, targetId, type, customGen)
                }
            )
        }

        // Member Profile BottomSheet (Unrestricted access for all users)
        if (isProfileOpen && selectedMember != null) {
            MemberProfileSheet(
                member = selectedMember!!,
                allMembers = members,
                isBn = isBn,
                sheetState = sheetState,
                onDismiss = { viewModel.closeProfile() },
                onEdit = { viewModel.openEditMember(it) },
                onDelete = { viewModel.deleteMember(it) },
                onNavigateToMember = { viewModel.selectMember(it) },
                onAddRelative = { parentId, spouseId ->
                    viewModel.closeProfile()
                    viewModel.openAddMember(defaultFatherId = parentId, defaultSpouseId = spouseId)
                },
                onConnectQuickRel = { viewModel.openQuickRelationship(it) },
                onUploadPhotoBytes = { mId, bytes -> viewModel.uploadPhotoToDrive(mId, bytes) },
                onUploadPhotoBitmap = { mId, bitmap -> viewModel.uploadBitmapPhoto(mId, bitmap) }
            )
        }

        // Member Add / Edit Form Dialog (Camera, Gallery, Live Age, Death Date)
        if (isMemberFormOpen && memberToEdit != null) {
            MemberFormDialog(
                initialMember = memberToEdit!!,
                allMembers = members,
                isBn = isBn,
                onDismiss = { viewModel.closeMemberForm() },
                onSave = { viewModel.saveMember(it) },
                onUploadPhotoBitmap = { mId, bitmap -> viewModel.uploadBitmapPhoto(mId, bitmap) },
                onUploadPhotoBytes = { mId, bytes -> viewModel.uploadPhotoToDrive(mId, bytes) }
            )
        }

        // Event Add / Edit Form Dialog (Title, Date, Location, Multiple Photos via Camera/Gallery, Drive Upload)
        if (isEventFormOpen) {
            EventFormDialog(
                event = eventToEdit,
                language = language,
                isUploading = isUploadingEventPhotos,
                onDismiss = { viewModel.closeEventForm() },
                onSave = { title, date, location, description, existingUrls, existingDriveIds, newBitmaps ->
                    viewModel.saveEvent(
                        title = title,
                        date = date,
                        location = location,
                        description = description,
                        existingPhotoUrls = existingUrls,
                        existingPhotoDriveIds = existingDriveIds,
                        newPhotoBitmaps = newBitmaps
                    )
                }
            )
        }

        // Event Full Album & Photo Downloader Dialog
        selectedEventForGallery?.let { event ->
            PhotoGalleryViewerDialog(
                event = event,
                language = language,
                onDismiss = { viewModel.closeEventGallery() }
            )
        }
    }
}
