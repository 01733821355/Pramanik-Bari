package com.example.data.repository

import android.content.Context
import android.util.Base64
import com.example.data.local.AppDatabase
import com.example.data.local.EventDao
import com.example.data.local.EventEntity
import com.example.data.local.FamilyMemberDao
import com.example.data.local.FamilyMemberEntity
import com.example.data.model.FamilyEvent
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.remote.GoogleAppsScriptService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

enum class ConnectionType {
    SET_AS_FATHER,
    SET_AS_MOTHER,
    SET_AS_SPOUSE,
    SET_AS_CHILD,
    SET_AS_SIBLING
}

class FamilyRepository(
    context: Context,
    private val dao: FamilyMemberDao = AppDatabase.getInstance(context).familyMemberDao(),
    private val eventDao: EventDao = AppDatabase.getInstance(context).eventDao(),
    private val cloudService: GoogleAppsScriptService = GoogleAppsScriptService()
) {
    val membersFlow: Flow<List<FamilyMember>> = dao.getAllMembersFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val eventsFlow: Flow<List<FamilyEvent>> = eventDao.getAllEventsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun clearDemoDataIfNeeded() = withContext(Dispatchers.IO) {
        val all = dao.getAllMembers()
        // If there are residual demo records from previous initial seeds, clear them
        val hasDemo = all.any { it.fullName.contains("মৌলভী জমির উদ্দিন") || it.fullName.contains("Moulvi Jamir") }
        if (hasDemo) {
            dao.clearAll()
        }
    }

    suspend fun getAllMembers(): List<FamilyMember> = withContext(Dispatchers.IO) {
        dao.getAllMembers().map { it.toDomain() }
    }

    suspend fun getMemberById(id: String): FamilyMember? = withContext(Dispatchers.IO) {
        dao.getMemberById(id)?.toDomain()
    }

    suspend fun saveMember(member: FamilyMember, cloudUrl: String? = null): Result<FamilyMember> = withContext(Dispatchers.IO) {
        try {
            // Save locally first
            dao.insertOrUpdate(FamilyMemberEntity.fromDomain(member))

            // Update cross-references: if father is specified, ensure child is in father's childrenIds
            member.fatherId?.let { fId ->
                dao.getMemberById(fId)?.toDomain()?.let { father ->
                    if (!father.childrenIds.contains(member.id)) {
                        val updatedChildren = father.childrenIds + member.id
                        dao.insertOrUpdate(FamilyMemberEntity.fromDomain(father.copy(childrenIds = updatedChildren)))
                    }
                }
            }

            // If mother is specified, ensure child is in mother's childrenIds
            member.motherId?.let { mId ->
                dao.getMemberById(mId)?.toDomain()?.let { mother ->
                    if (!mother.childrenIds.contains(member.id)) {
                        val updatedChildren = mother.childrenIds + member.id
                        dao.insertOrUpdate(FamilyMemberEntity.fromDomain(mother.copy(childrenIds = updatedChildren)))
                    }
                }
            }

            // If spouse is specified, update mutual spouse relation
            member.spouseId?.let { sId ->
                dao.getMemberById(sId)?.toDomain()?.let { spouse ->
                    if (spouse.spouseId != member.id) {
                        dao.insertOrUpdate(FamilyMemberEntity.fromDomain(spouse.copy(spouseId = member.id, spouseName = member.fullName)))
                    }
                }
            }

            // Sync to Google Sheets if cloud URL is provided
            if (!cloudUrl.isNullOrBlank()) {
                cloudService.saveMember(cloudUrl, member)
            }

            Result.success(member)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connectRelationship(
        sourceId: String,
        targetId: String,
        connectionType: ConnectionType,
        customGeneration: Int?,
        cloudUrl: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val source = dao.getMemberById(sourceId)?.toDomain() ?: return@withContext Result.failure(Exception("Source not found"))
            val target = dao.getMemberById(targetId)?.toDomain() ?: return@withContext Result.failure(Exception("Target not found"))

            var updatedSource = source
            var updatedTarget = target

            when (connectionType) {
                ConnectionType.SET_AS_FATHER -> {
                    // source is father of target
                    updatedSource = source.copy(
                        gender = Gender.MALE,
                        childrenIds = (source.childrenIds + target.id).distinct()
                    )
                    updatedTarget = target.copy(
                        fatherId = source.id,
                        fatherName = source.fullName,
                        generation = maxOf(source.generation + 1, target.generation)
                    )
                }

                ConnectionType.SET_AS_MOTHER -> {
                    // source is mother of target
                    updatedSource = source.copy(
                        gender = Gender.FEMALE,
                        childrenIds = (source.childrenIds + target.id).distinct()
                    )
                    updatedTarget = target.copy(
                        motherId = source.id,
                        motherName = source.fullName,
                        generation = maxOf(source.generation + 1, target.generation)
                    )
                }

                ConnectionType.SET_AS_SPOUSE -> {
                    // source and target are spouses
                    val sourceGen = minOf(source.generation, target.generation)
                    updatedSource = source.copy(
                        spouseId = target.id,
                        spouseName = target.fullName,
                        generation = sourceGen
                    )
                    updatedTarget = target.copy(
                        spouseId = source.id,
                        spouseName = source.fullName,
                        generation = sourceGen
                    )
                }

                ConnectionType.SET_AS_CHILD -> {
                    // source is child of target
                    val newGen = target.generation + 1
                    updatedSource = source.copy(
                        fatherId = if (target.gender == Gender.MALE) target.id else source.fatherId,
                        fatherName = if (target.gender == Gender.MALE) target.fullName else source.fatherName,
                        motherId = if (target.gender == Gender.FEMALE) target.id else source.motherId,
                        motherName = if (target.gender == Gender.FEMALE) target.fullName else source.motherName,
                        generation = newGen
                    )
                    updatedTarget = target.copy(
                        childrenIds = (target.childrenIds + source.id).distinct()
                    )
                }

                ConnectionType.SET_AS_SIBLING -> {
                    // source and target are siblings
                    val sameGen = target.generation
                    updatedSource = source.copy(
                        brotherSisterIds = (source.brotherSisterIds + target.id).distinct(),
                        fatherId = source.fatherId ?: target.fatherId,
                        fatherName = source.fatherName ?: target.fatherName,
                        motherId = source.motherId ?: target.motherId,
                        motherName = source.motherName ?: target.motherName,
                        generation = sameGen
                    )
                    updatedTarget = target.copy(
                        brotherSisterIds = (target.brotherSisterIds + source.id).distinct(),
                        fatherId = target.fatherId ?: source.fatherId,
                        fatherName = target.fatherName ?: source.fatherName,
                        motherId = target.motherId ?: source.motherId,
                        motherName = target.motherName ?: source.motherName,
                        generation = sameGen
                    )
                }
            }

            if (customGeneration != null) {
                updatedSource = updatedSource.copy(generation = customGeneration)
            }

            dao.insertOrUpdate(FamilyMemberEntity.fromDomain(updatedSource))
            dao.insertOrUpdate(FamilyMemberEntity.fromDomain(updatedTarget))

            if (!cloudUrl.isNullOrBlank()) {
                cloudService.saveMember(cloudUrl, updatedSource)
                cloudService.saveMember(cloudUrl, updatedTarget)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMember(memberId: String, cloudUrl: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            dao.deleteById(memberId)
            if (!cloudUrl.isNullOrBlank()) {
                cloudService.deleteMember(cloudUrl, memberId)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncWithCloud(cloudUrl: String): Result<Int> = withContext(Dispatchers.IO) {
        if (cloudUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Cloud URL not configured"))
        }

        val result = cloudService.fetchMembers(cloudUrl)
        result.fold(
            onSuccess = { cloudMembers ->
                if (cloudMembers.isNotEmpty()) {
                    dao.clearAll()
                    dao.insertAll(cloudMembers.map { FamilyMemberEntity.fromDomain(it) })
                    Result.success(cloudMembers.size)
                } else {
                    // Cloud sheet is empty
                    Result.success(0)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }

    suspend fun uploadPhotoToDrive(
        cloudUrl: String,
        memberId: String,
        bytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        if (cloudUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Cloud URL is not configured."))
        }
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val uploadRes = cloudService.uploadPhotoToDrive(
            webAppUrl = cloudUrl,
            memberId = memberId,
            fileName = "member_${memberId}.jpg",
            mimeType = mimeType,
            base64Data = base64
        )
        uploadRes.fold(
            onSuccess = { (fileId, directUrl) ->
                dao.getMemberById(memberId)?.toDomain()?.let { m ->
                    val updated = m.copy(photoUrl = directUrl, driveFileId = fileId)
                    dao.insertOrUpdate(FamilyMemberEntity.fromDomain(updated))
                }
                Result.success(directUrl)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun getAllEvents(): List<FamilyEvent> = withContext(Dispatchers.IO) {
        eventDao.getAllEvents().map { it.toDomain() }
    }

    suspend fun saveEvent(event: FamilyEvent, cloudUrl: String? = null): Result<FamilyEvent> = withContext(Dispatchers.IO) {
        try {
            eventDao.insertOrUpdate(EventEntity.fromDomain(event))
            if (!cloudUrl.isNullOrBlank()) {
                cloudService.saveEvent(cloudUrl, event)
            }
            Result.success(event)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(eventId: String, cloudUrl: String? = null): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            eventDao.deleteById(eventId)
            if (!cloudUrl.isNullOrBlank()) {
                cloudService.deleteEvent(cloudUrl, eventId)
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadEventPhotoToDrive(
        cloudUrl: String,
        eventId: String,
        bytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        if (cloudUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Cloud URL is not configured."))
        }
        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        val fileName = "event_${eventId}_${System.currentTimeMillis()}.jpg"
        cloudService.uploadPhotoToDrive(
            webAppUrl = cloudUrl,
            memberId = eventId,
            fileName = fileName,
            mimeType = mimeType,
            base64Data = base64
        )
    }

    suspend fun syncEventsWithCloud(cloudUrl: String): Result<Int> = withContext(Dispatchers.IO) {
        if (cloudUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Cloud URL not configured"))
        }

        val result = cloudService.fetchEvents(cloudUrl)
        result.fold(
            onSuccess = { cloudEvents ->
                if (cloudEvents.isNotEmpty()) {
                    eventDao.clearAll()
                    eventDao.insertAll(cloudEvents.map { EventEntity.fromDomain(it) })
                    Result.success(cloudEvents.size)
                } else {
                    Result.success(0)
                }
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}
