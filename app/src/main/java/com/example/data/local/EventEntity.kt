package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FamilyEvent

@Entity(tableName = "family_events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val date: String,
    val location: String,
    val description: String,
    val photoUrlsRaw: String, // comma-separated or JSON
    val photoDriveIdsRaw: String, // comma-separated
    val createdByName: String,
    val isSynced: Boolean,
    val updatedAt: Long
) {
    fun toDomain(): FamilyEvent {
        val urls = if (photoUrlsRaw.isBlank()) emptyList() else photoUrlsRaw.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
        val driveIds = if (photoDriveIdsRaw.isBlank()) emptyList() else photoDriveIdsRaw.split("|||").map { it.trim() }.filter { it.isNotEmpty() }

        return FamilyEvent(
            id = id,
            title = title,
            date = date,
            location = location,
            description = description,
            photoUrls = urls,
            photoDriveIds = driveIds,
            createdByName = createdByName,
            isSynced = isSynced,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(event: FamilyEvent): EventEntity {
            return EventEntity(
                id = event.id,
                title = event.title,
                date = event.date,
                location = event.location,
                description = event.description,
                photoUrlsRaw = event.photoUrls.joinToString("|||"),
                photoDriveIdsRaw = event.photoDriveIds.joinToString("|||"),
                createdByName = event.createdByName,
                isSynced = event.isSynced,
                updatedAt = event.updatedAt
            )
        }
    }
}
