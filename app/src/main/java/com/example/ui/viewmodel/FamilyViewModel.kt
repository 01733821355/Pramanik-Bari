package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FamilyEvent
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.preferences.AppPreferences
import com.example.data.remote.AppsScriptBackendTemplate
import com.example.data.repository.FamilyRepository
import com.example.domain.RelationshipCalculator
import com.example.domain.RelationshipResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

enum class AppTab {
    DASHBOARD,
    TREE,
    MEMBERS,
    EVENTS,
    RELATIONSHIP
}

class FamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FamilyRepository(application)
    private val preferences = AppPreferences(application)

    val members: StateFlow<List<FamilyMember>> = repository.membersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val events: StateFlow<List<FamilyEvent>> = repository.eventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _language = MutableStateFlow("bn") // default Bengali
    val language: StateFlow<String> = _language.asStateFlow()

    // Unrestricted access for all users - no login or password required
    private val _userRole = MutableStateFlow("UNRESTRICTED")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    // Default Google Apps Script URL provided by the user
    private val _appsScriptUrl = MutableStateFlow(AppsScriptBackendTemplate.DEFAULT_WEB_APP_URL)
    val appsScriptUrl: StateFlow<String> = _appsScriptUrl.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    // Emotional Intro State
    private val _showIntro = MutableStateFlow(true)
    val showIntro: StateFlow<Boolean> = _showIntro.asStateFlow()

    // Interactive Tree States
    private val _collapsedBranches = MutableStateFlow<Set<String>>(emptySet())
    val collapsedBranches: StateFlow<Set<String>> = _collapsedBranches.asStateFlow()

    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()

    private val _isProfileOpen = MutableStateFlow(false)
    val isProfileOpen: StateFlow<Boolean> = _isProfileOpen.asStateFlow()

    private val _isMemberFormOpen = MutableStateFlow(false)
    val isMemberFormOpen: StateFlow<Boolean> = _isMemberFormOpen.asStateFlow()

    private val _memberToEdit = MutableStateFlow<FamilyMember?>(null)
    val memberToEdit: StateFlow<FamilyMember?> = _memberToEdit.asStateFlow()

    // Event Management States
    private val _isEventFormOpen = MutableStateFlow(false)
    val isEventFormOpen: StateFlow<Boolean> = _isEventFormOpen.asStateFlow()

    private val _eventToEdit = MutableStateFlow<FamilyEvent?>(null)
    val eventToEdit: StateFlow<FamilyEvent?> = _eventToEdit.asStateFlow()

    private val _selectedEventForGallery = MutableStateFlow<FamilyEvent?>(null)
    val selectedEventForGallery: StateFlow<FamilyEvent?> = _selectedEventForGallery.asStateFlow()

    private val _fullscreenPhotoUrl = MutableStateFlow<String?>(null)
    val fullscreenPhotoUrl: StateFlow<String?> = _fullscreenPhotoUrl.asStateFlow()

    private val _isUploadingEventPhotos = MutableStateFlow(false)
    val isUploadingEventPhotos: StateFlow<Boolean> = _isUploadingEventPhotos.asStateFlow()

    // Relationship Finder States
    private val _personAId = MutableStateFlow<String?>(null)
    val personAId: StateFlow<String?> = _personAId.asStateFlow()

    private val _personBId = MutableStateFlow<String?>(null)
    val personBId: StateFlow<String?> = _personBId.asStateFlow()

    private val _relationshipResult = MutableStateFlow<RelationshipResult?>(null)
    val relationshipResult: StateFlow<RelationshipResult?> = _relationshipResult.asStateFlow()

    // Quick Relationship & Tree Positioning Dialog State (Triggered on Node Long-Press)
    private val _memberForQuickRel = MutableStateFlow<FamilyMember?>(null)
    val memberForQuickRel: StateFlow<FamilyMember?> = _memberForQuickRel.asStateFlow()

    // Canvas Transform
    private val _treeZoom = MutableStateFlow(1f)
    val treeZoom: StateFlow<Float> = _treeZoom.asStateFlow()

    private val _treePanX = MutableStateFlow(0f)
    val treePanX: StateFlow<Float> = _treePanX.asStateFlow()

    private val _treePanY = MutableStateFlow(0f)
    val treePanY: StateFlow<Float> = _treePanY.asStateFlow()

    val filteredMembers: StateFlow<List<FamilyMember>> = combine(members, searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter {
                it.fullName.lowercase().contains(q) ||
                it.village.lowercase().contains(q) ||
                it.phone.contains(q) ||
                it.occupation.lowercase().contains(q) ||
                it.id.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            // Strictly zero demo data - clear any previous legacy demo data if present
            repository.clearDemoDataIfNeeded()
            _language.value = preferences.languageFlow.first()
            val savedUrl = preferences.appsScriptUrlFlow.first()
            val effectiveUrl = if (savedUrl.isBlank()) AppsScriptBackendTemplate.DEFAULT_WEB_APP_URL else savedUrl
            _appsScriptUrl.value = effectiveUrl

            // Background initial cloud sync
            syncWithCloud(silent = true)
        }
    }

    fun openQuickRelationship(member: FamilyMember) {
        _memberForQuickRel.value = member
    }

    fun closeQuickRelationship() {
        _memberForQuickRel.value = null
    }

    fun connectRelationship(
        sourceId: String,
        targetId: String,
        type: com.example.data.repository.ConnectionType,
        customGen: Int?
    ) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.connectRelationship(sourceId, targetId, type, customGen, _appsScriptUrl.value)
            _isSyncing.value = false
            res.fold(
                onSuccess = {
                    _memberForQuickRel.value = null
                    _syncMessage.value = if (_language.value == "bn") "সম্পর্ক ও বংশলতিকার অবস্থান সফলভাবে সেট করা হয়েছে" else "Relationship and tree position updated"
                },
                onFailure = { err ->
                    _syncMessage.value = err.localizedMessage ?: "Failed to connect relationship"
                }
            )
        }
    }

    fun dismissIntro() {
        _showIntro.value = false
    }

    fun replayIntro() {
        _showIntro.value = true
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleLanguage() {
        viewModelScope.launch {
            val newLang = if (_language.value == "bn") "en" else "bn"
            _language.value = newLang
            preferences.setLanguage(newLang)
        }
    }

    fun selectMember(member: FamilyMember) {
        _selectedMember.value = member
        _isProfileOpen.value = true
    }

    fun selectMemberForTreeHighlight(member: FamilyMember) {
        _selectedMember.value = member
    }

    fun closeProfile() {
        _isProfileOpen.value = false
    }

    fun openAddMember(defaultFatherId: String? = null, defaultSpouseId: String? = null) {
        val currentCount = members.value.size
        val newId = FamilyMember.generateMemberId(existingCount = currentCount)
        val defaultFather = defaultFatherId?.let { fId -> members.value.find { it.id == fId } }
        val defaultSpouse = defaultSpouseId?.let { sId -> members.value.find { it.id == sId } }

        _memberToEdit.value = FamilyMember(
            id = newId,
            fullName = "",
            fatherId = defaultFather?.id,
            fatherName = defaultFather?.fullName,
            spouseId = defaultSpouse?.id,
            spouseName = defaultSpouse?.fullName,
            generation = if (defaultFather != null) defaultFather.generation + 1 else 1
        )
        _isMemberFormOpen.value = true
    }

    fun openEditMember(member: FamilyMember) {
        _memberToEdit.value = member
        _isMemberFormOpen.value = true
    }

    fun closeMemberForm() {
        _isMemberFormOpen.value = false
        _memberToEdit.value = null
    }

    fun saveMember(member: FamilyMember) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.saveMember(member, _appsScriptUrl.value)
            _isSyncing.value = false
            res.fold(
                onSuccess = { saved ->
                    _selectedMember.value = saved
                    _isMemberFormOpen.value = false
                    _memberToEdit.value = null
                    _syncMessage.value = if (_language.value == "bn") "সদস্য তথ্য সফলভাবে সংরক্ষিত হয়েছে" else "Member saved successfully"
                },
                onFailure = { err ->
                    _syncMessage.value = err.localizedMessage ?: "Failed to save"
                }
            )
        }
    }

    fun deleteMember(memberId: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.deleteMember(memberId, _appsScriptUrl.value)
            _isSyncing.value = false
            res.fold(
                onSuccess = {
                    if (_selectedMember.value?.id == memberId) {
                        _selectedMember.value = null
                        _isProfileOpen.value = false
                    }
                    _syncMessage.value = if (_language.value == "bn") "সদস্য সফলভাবে মুছে ফেলা হয়েছে" else "Member deleted successfully"
                },
                onFailure = { err ->
                    _syncMessage.value = err.localizedMessage ?: "Failed to delete"
                }
            )
        }
    }

    fun syncWithCloud(silent: Boolean = false) {
        val url = _appsScriptUrl.value
        if (url.isBlank()) return

        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.syncWithCloud(url)
            repository.syncEventsWithCloud(url)
            _isSyncing.value = false
            res.fold(
                onSuccess = { count ->
                    if (!silent) {
                        _syncMessage.value = if (_language.value == "bn") "$count জন সদস্য ও ইভেন্ট সিঙ্ক হয়েছে" else "Successfully synced $count members and events"
                    }
                },
                onFailure = { err ->
                    if (!silent) {
                        _syncMessage.value = err.localizedMessage ?: "Cloud sync failed"
                    }
                }
            )
        }
    }

    fun uploadPhotoToDrive(memberId: String, bytes: ByteArray, mimeType: String = "image/jpeg") {
        val url = _appsScriptUrl.value
        viewModelScope.launch {
            _isSyncing.value = true
            val res = repository.uploadPhotoToDrive(url, memberId, bytes, mimeType)
            _isSyncing.value = false
            res.fold(
                onSuccess = { directUrl ->
                    _selectedMember.value?.let { current ->
                        if (current.id == memberId) {
                            _selectedMember.value = current.copy(photoUrl = directUrl)
                        }
                    }
                    _syncMessage.value = if (_language.value == "bn") "ছবি সফলভাবে গুগল ড্রাইভে সংরক্ষিত হয়েছে!" else "Photo permanently stored in Google Drive!"
                },
                onFailure = { err ->
                    _syncMessage.value = err.localizedMessage ?: "Photo upload failed"
                }
            )
        }
    }

    fun uploadBitmapPhoto(memberId: String, bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        uploadPhotoToDrive(memberId, outputStream.toByteArray(), "image/jpeg")
    }

    fun clearSyncMessage() {
        _syncMessage.value = null
    }

    fun toggleBranch(memberId: String) {
        val current = _collapsedBranches.value.toMutableSet()
        if (current.contains(memberId)) {
            current.remove(memberId)
        } else {
            current.add(memberId)
        }
        _collapsedBranches.value = current
    }

    fun expandAllBranches() {
        _collapsedBranches.value = emptySet()
    }

    fun collapseAllBranches() {
        val withKids = members.value.filter { it.childrenIds.isNotEmpty() }.map { it.id }.toSet()
        _collapsedBranches.value = withKids
    }

    fun setPersonA(id: String) {
        _personAId.value = id
        calculateRelationship()
    }

    fun setPersonB(id: String) {
        _personBId.value = id
        calculateRelationship()
    }

    fun calculateRelationship() {
        val a = _personAId.value
        val b = _personBId.value
        if (a != null && b != null && a != b) {
            val res = RelationshipCalculator.findRelationship(members.value, a, b)
            _relationshipResult.value = res
        } else {
            _relationshipResult.value = null
        }
    }

    fun updateTreeTransform(panDeltaX: Float, panDeltaY: Float, zoomFactor: Float) {
        _treePanX.value += panDeltaX
        _treePanY.value += panDeltaY
        _treeZoom.value = (_treeZoom.value * zoomFactor).coerceIn(0.35f, 2.5f)
    }

    fun resetTreeTransform() {
        _treePanX.value = 0f
        _treePanY.value = 0f
        _treeZoom.value = 1f
    }

    fun zoomIn() {
        _treeZoom.value = (_treeZoom.value * 1.2f).coerceAtMost(2.5f)
    }

    fun zoomOut() {
        _treeZoom.value = (_treeZoom.value / 1.2f).coerceAtLeast(0.35f)
    }

    fun openEventForm(event: FamilyEvent? = null) {
        _eventToEdit.value = event
        _isEventFormOpen.value = true
    }

    fun closeEventForm() {
        _isEventFormOpen.value = false
        _eventToEdit.value = null
    }

    fun openEventGallery(event: FamilyEvent) {
        _selectedEventForGallery.value = event
    }

    fun closeEventGallery() {
        _selectedEventForGallery.value = null
        _fullscreenPhotoUrl.value = null
    }

    fun openFullscreenPhoto(url: String) {
        _fullscreenPhotoUrl.value = url
    }

    fun closeFullscreenPhoto() {
        _fullscreenPhotoUrl.value = null
    }

    fun saveEvent(
        title: String,
        date: String,
        location: String,
        description: String,
        existingPhotoUrls: List<String>,
        existingPhotoDriveIds: List<String>,
        newPhotoBitmaps: List<Bitmap>,
        createdByName: String = ""
    ) {
        viewModelScope.launch {
            _isUploadingEventPhotos.value = true
            try {
                val eventId = _eventToEdit.value?.id ?: "EVT-${System.currentTimeMillis()}"
                val uploadedUrls = existingPhotoUrls.toMutableList()
                val uploadedDriveIds = existingPhotoDriveIds.toMutableList()
                val cloudUrl = _appsScriptUrl.value

                for (bitmap in newPhotoBitmaps) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val bytes = stream.toByteArray()
                    val uploadRes = repository.uploadEventPhotoToDrive(cloudUrl, eventId, bytes)
                    uploadRes.onSuccess { (fileId, directUrl) ->
                        uploadedDriveIds.add(fileId)
                        uploadedUrls.add(directUrl)
                    }
                }

                val event = FamilyEvent(
                    id = eventId,
                    title = title,
                    date = date,
                    location = location,
                    description = description,
                    photoUrls = uploadedUrls,
                    photoDriveIds = uploadedDriveIds,
                    createdByName = createdByName,
                    isSynced = true,
                    updatedAt = System.currentTimeMillis()
                )

                repository.saveEvent(event, cloudUrl)
                _syncMessage.value = if (_language.value == "bn") "ইভেন্ট ও ছবি সফলভাবে গুগল ড্রাইভে সংরক্ষিত হয়েছে!" else "Event and photos saved to Google Drive!"
                closeEventForm()
            } catch (e: Exception) {
                _syncMessage.value = "ইভেন্ট সংরক্ষণ ত্রুটি: ${e.localizedMessage}"
            } finally {
                _isUploadingEventPhotos.value = false
            }
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            val cloudUrl = _appsScriptUrl.value
            repository.deleteEvent(eventId, cloudUrl)
            if (_selectedEventForGallery.value?.id == eventId) {
                closeEventGallery()
            }
            _syncMessage.value = if (_language.value == "bn") "ইভেন্ট মুছে ফেলা হয়েছে" else "Event deleted"
        }
    }
}
