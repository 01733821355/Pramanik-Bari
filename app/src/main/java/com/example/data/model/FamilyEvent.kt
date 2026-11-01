package com.example.data.model

data class FamilyEvent(
    val id: String,
    val title: String,
    val date: String,
    val location: String = "",
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val photoDriveIds: List<String> = emptyList(),
    val createdByName: String = "",
    val isSynced: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Resolves Google Drive file IDs or raw photo URLs to direct loadable preview URLs
     */
    fun getResolvedPhotoUrls(): List<String> {
        val resolved = mutableListOf<String>()

        // First resolve by drive IDs if available
        for (driveId in photoDriveIds) {
            val trimmed = driveId.trim()
            if (trimmed.isNotBlank()) {
                resolved.add("https://lh3.googleusercontent.com/d/$trimmed")
            }
        }

        // Then check photoUrls
        for (url in photoUrls) {
            val trimmed = url.trim()
            if (trimmed.isNotBlank()) {
                val driveId = FamilyMember.extractDriveId(trimmed)
                val directUrl = if (driveId.isNotBlank()) {
                    "https://lh3.googleusercontent.com/d/$driveId"
                } else {
                    trimmed
                }
                if (!resolved.contains(directUrl)) {
                    resolved.add(directUrl)
                }
            }
        }

        return resolved
    }
}
