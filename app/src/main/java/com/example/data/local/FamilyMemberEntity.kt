package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FamilyMember
import com.example.data.model.Gender

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val gender: String,
    val dob: String,
    val dod: String,
    val isDeceased: Boolean,
    val photoUrl: String,
    val driveFileId: String,
    val fatherId: String?,
    val fatherName: String?,
    val motherId: String?,
    val motherName: String?,
    val spouseId: String?,
    val spouseName: String?,
    val childrenIdsRaw: String, // comma separated
    val brotherSisterIdsRaw: String, // comma separated
    val grandfatherName: String?,
    val grandmotherName: String?,
    val village: String,
    val address: String,
    val phone: String,
    val occupation: String,
    val bloodGroup: String,
    val biography: String,
    val notes: String,
    val generation: Int,
    val isSynced: Boolean,
    val updatedAt: Long
) {
    fun toDomain(): FamilyMember {
        val parsedChildren = if (childrenIdsRaw.isBlank()) emptyList() else childrenIdsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val parsedSiblings = if (brotherSisterIdsRaw.isBlank()) emptyList() else brotherSisterIdsRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        return FamilyMember(
            id = id,
            fullName = fullName,
            gender = try { Gender.valueOf(gender.uppercase()) } catch (e: Exception) { Gender.MALE },
            dob = dob,
            dod = dod,
            isDeceased = isDeceased,
            photoUrl = photoUrl,
            driveFileId = driveFileId,
            fatherId = fatherId,
            fatherName = fatherName,
            motherId = motherId,
            motherName = motherName,
            spouseId = spouseId,
            spouseName = spouseName,
            childrenIds = parsedChildren,
            brotherSisterIds = parsedSiblings,
            grandfatherName = grandfatherName,
            grandmotherName = grandmotherName,
            village = village,
            address = address,
            phone = phone,
            occupation = occupation,
            bloodGroup = bloodGroup,
            biography = biography,
            notes = notes,
            generation = generation,
            isSynced = isSynced,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(member: FamilyMember): FamilyMemberEntity {
            return FamilyMemberEntity(
                id = member.id,
                fullName = member.fullName,
                gender = member.gender.name,
                dob = member.dob,
                dod = member.dod,
                isDeceased = member.isDeceased,
                photoUrl = member.photoUrl,
                driveFileId = member.driveFileId,
                fatherId = member.fatherId,
                fatherName = member.fatherName,
                motherId = member.motherId,
                motherName = member.motherName,
                spouseId = member.spouseId,
                spouseName = member.spouseName,
                childrenIdsRaw = member.childrenIds.joinToString(","),
                brotherSisterIdsRaw = member.brotherSisterIds.joinToString(","),
                grandfatherName = member.grandfatherName,
                grandmotherName = member.grandmotherName,
                village = member.village,
                address = member.address,
                phone = member.phone,
                occupation = member.occupation,
                bloodGroup = member.bloodGroup,
                biography = member.biography,
                notes = member.notes,
                generation = member.generation,
                isSynced = member.isSynced,
                updatedAt = member.updatedAt
            )
        }
    }
}
