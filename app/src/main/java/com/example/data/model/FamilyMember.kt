package com.example.data.model

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

data class FamilyMember(
    val id: String,
    val fullName: String,
    val gender: Gender = Gender.MALE,
    val dob: String = "",
    val dod: String = "",
    val isDeceased: Boolean = false,
    val photoUrl: String = "",
    val driveFileId: String = "",
    val fatherId: String? = null,
    val fatherName: String? = null,
    val motherId: String? = null,
    val motherName: String? = null,
    val spouseId: String? = null,
    val spouseName: String? = null,
    val childrenIds: List<String> = emptyList(),
    val brotherSisterIds: List<String> = emptyList(),
    val grandfatherName: String? = null,
    val grandmotherName: String? = null,
    val village: String = "",
    val address: String = "",
    val phone: String = "",
    val occupation: String = "",
    val bloodGroup: String = "",
    val biography: String = "",
    val notes: String = "",
    val generation: Int = 1,
    val isSynced: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Resolves Google Drive link or fileId to direct loadable image URL
     */
    fun getResolvedImageUrl(): String {
        if (photoUrl.isBlank() && driveFileId.isBlank()) return ""

        val effectiveId = when {
            driveFileId.isNotBlank() -> driveFileId.trim()
            else -> extractDriveId(photoUrl)
        }

        return if (effectiveId.isNotBlank()) {
            "https://lh3.googleusercontent.com/d/$effectiveId"
        } else {
            photoUrl.trim()
        }
    }

    companion object {
        fun extractDriveId(url: String): String {
            if (url.isBlank()) return ""
            val patterns = listOf(
                Regex("/file/d/([a-zA-Z0-9_-]+)"),
                Regex("/d/([a-zA-Z0-9_-]+)"),
                Regex("[?&]id=([a-zA-Z0-9_-]+)"),
                Regex("^([a-zA-Z0-9_-]{25,})$")
            )
            for (p in patterns) {
                val match = p.find(url)
                if (match != null) {
                    return match.groupValues[1]
                }
            }
            return ""
        }

        fun generateMemberId(prefix: String = "PB", existingCount: Int): String {
            val seq = (existingCount + 1).toString().padStart(4, '0')
            return "$prefix-$seq"
        }
    }
}
