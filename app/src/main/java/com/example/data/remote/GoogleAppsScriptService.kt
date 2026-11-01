package com.example.data.remote

import android.util.Log
import com.example.data.model.FamilyEvent
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GoogleAppsScriptService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun fetchMembers(webAppUrl: String): Result<List<FamilyMember>> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Web App URL is empty"))
        }

        try {
            val url = if (webAppUrl.contains("?")) "$webAppUrl&action=getMembers" else "$webAppUrl?action=getMembers"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
            val json = JSONObject(body)
            val status = json.optString("status", "")
            if (status != "success" && !json.has("members") && !json.has("data")) {
                val errorMsg = json.optString("message", "Unknown error from Apps Script")
                return@withContext Result.failure(Exception(errorMsg))
            }

            val membersArray = json.optJSONArray("data") ?: json.optJSONArray("members") ?: JSONArray()
            val membersList = mutableListOf<FamilyMember>()

            for (i in 0 until membersArray.length()) {
                val item = membersArray.getJSONObject(i)
                val id = item.optString("id", "")
                if (id.isBlank()) continue

                val childrenList = mutableListOf<String>()
                val childrenJson = item.opt("childrenIds")
                if (childrenJson is JSONArray) {
                    for (c in 0 until childrenJson.length()) {
                        childrenList.add(childrenJson.getString(c).trim())
                    }
                } else if (childrenJson is String && childrenJson.isNotBlank()) {
                    childrenList.addAll(childrenJson.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                }

                val siblingsList = mutableListOf<String>()
                val siblingsJson = item.opt("brotherSisterIds")
                if (siblingsJson is JSONArray) {
                    for (s in 0 until siblingsJson.length()) {
                        siblingsList.add(siblingsJson.getString(s).trim())
                    }
                } else if (siblingsJson is String && siblingsJson.isNotBlank()) {
                    siblingsList.addAll(siblingsJson.split(",").map { it.trim() }.filter { it.isNotEmpty() })
                }

                val member = FamilyMember(
                    id = id,
                    fullName = item.optString("fullName", ""),
                    gender = try {
                        Gender.valueOf(item.optString("gender", "MALE").uppercase())
                    } catch (e: Exception) {
                        Gender.MALE
                    },
                    dob = item.optString("dob", ""),
                    dod = item.optString("dod", ""),
                    isDeceased = item.optBoolean("isDeceased", false),
                    photoUrl = item.optString("photoUrl", ""),
                    driveFileId = item.optString("driveFileId", ""),
                    fatherId = item.optString("fatherId", "").takeIf { it.isNotBlank() },
                    fatherName = item.optString("fatherName", "").takeIf { it.isNotBlank() },
                    motherId = item.optString("motherId", "").takeIf { it.isNotBlank() },
                    motherName = item.optString("motherName", "").takeIf { it.isNotBlank() },
                    spouseId = item.optString("spouseId", "").takeIf { it.isNotBlank() },
                    spouseName = item.optString("spouseName", "").takeIf { it.isNotBlank() },
                    childrenIds = childrenList,
                    brotherSisterIds = siblingsList,
                    grandfatherName = item.optString("grandfatherName", "").takeIf { it.isNotBlank() },
                    grandmotherName = item.optString("grandmotherName", "").takeIf { it.isNotBlank() },
                    village = item.optString("village", ""),
                    address = item.optString("address", ""),
                    phone = item.optString("phone", ""),
                    occupation = item.optString("occupation", ""),
                    bloodGroup = item.optString("bloodGroup", ""),
                    biography = item.optString("biography", ""),
                    notes = item.optString("notes", ""),
                    generation = item.optInt("generation", 1),
                    isSynced = true,
                    updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
                )
                membersList.add(member)
            }

            Result.success(membersList)
        } catch (e: Exception) {
            Log.e("GoogleAppsScript", "Failed to fetch members", e)
            Result.failure(e)
        }
    }

    suspend fun saveMember(webAppUrl: String, member: FamilyMember): Result<Boolean> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Web App URL is empty"))
        }

        try {
            val payload = JSONObject().apply {
                put("action", "saveMember")
                put("id", member.id)
                put("fullName", member.fullName)
                put("gender", member.gender.name)
                put("dob", member.dob)
                put("dod", member.dod)
                put("isDeceased", member.isDeceased)
                put("photoUrl", member.photoUrl)
                put("driveFileId", member.driveFileId)
                put("fatherId", member.fatherId ?: "")
                put("fatherName", member.fatherName ?: "")
                put("motherId", member.motherId ?: "")
                put("motherName", member.motherName ?: "")
                put("spouseId", member.spouseId ?: "")
                put("spouseName", member.spouseName ?: "")
                put("childrenIds", JSONArray(member.childrenIds))
                put("brotherSisterIds", JSONArray(member.brotherSisterIds))
                put("grandfatherName", member.grandfatherName ?: "")
                put("grandmotherName", member.grandmotherName ?: "")
                put("village", member.village)
                put("address", member.address)
                put("phone", member.phone)
                put("occupation", member.occupation)
                put("bloodGroup", member.bloodGroup)
                put("biography", member.biography)
                put("notes", member.notes)
                put("generation", member.generation)
                put("updatedAt", member.updatedAt)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            val status = json.optString("status", "")
            if (status == "success") {
                Result.success(true)
            } else {
                Result.failure(Exception(json.optString("message", "Save failed")))
            }
        } catch (e: Exception) {
            Log.e("GoogleAppsScript", "Failed to save member", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMember(webAppUrl: String, memberId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Web App URL is empty"))
        }

        try {
            val payload = JSONObject().apply {
                put("action", "deleteMember")
                put("id", memberId)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            if (json.optString("status", "") == "success") {
                Result.success(true)
            } else {
                Result.failure(Exception(json.optString("message", "Delete failed")))
            }
        } catch (e: Exception) {
            Log.e("GoogleAppsScript", "Failed to delete member", e)
            Result.failure(e)
        }
    }

    suspend fun uploadPhotoToDrive(
        webAppUrl: String,
        memberId: String,
        fileName: String,
        mimeType: String,
        base64Data: String
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Web App URL is empty"))
        }

        try {
            val payload = JSONObject().apply {
                put("action", "uploadPhoto")
                put("memberId", memberId)
                put("fileName", fileName)
                put("mimeType", mimeType)
                put("base64Data", base64Data)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            if (json.optString("status", "") == "success") {
                val fileId = json.optString("fileId", "")
                val driveUrl = json.optString("driveUrl", "https://lh3.googleusercontent.com/d/$fileId")
                Result.success(Pair(fileId, driveUrl))
            } else {
                Result.failure(Exception(json.optString("message", "Photo upload failed")))
            }
        } catch (e: Exception) {
            Log.e("GoogleAppsScript", "Failed to upload photo", e)
            Result.failure(e)
        }
    }

    suspend fun fetchEvents(webAppUrl: String): Result<List<FamilyEvent>> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Web App URL is empty"))
        }

        try {
            val url = if (webAppUrl.contains("?")) "$webAppUrl&action=getEvents" else "$webAppUrl?action=getEvents"
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
            val json = JSONObject(body)
            val status = json.optString("status", "")
            if (status != "success" && !json.has("events") && !json.has("data")) {
                val errorMsg = json.optString("message", "Unknown error from Apps Script")
                return@withContext Result.failure(Exception(errorMsg))
            }

            val eventsArray = json.optJSONArray("events") ?: json.optJSONArray("data") ?: JSONArray()
            val eventsList = mutableListOf<FamilyEvent>()

            for (i in 0 until eventsArray.length()) {
                val item = eventsArray.getJSONObject(i)
                val id = item.optString("id", "")
                val title = item.optString("title", "")
                // If it returned members table by fallback, skip
                if (id.isBlank() || title.isBlank()) continue

                val photoUrls = mutableListOf<String>()
                val photoUrlsJson = item.opt("photoUrls")
                if (photoUrlsJson is JSONArray) {
                    for (p in 0 until photoUrlsJson.length()) {
                        photoUrls.add(photoUrlsJson.getString(p).trim())
                    }
                } else if (photoUrlsJson is String && photoUrlsJson.isNotBlank()) {
                    photoUrls.addAll(photoUrlsJson.split("|||", ",").map { it.trim() }.filter { it.isNotEmpty() })
                }

                val photoDriveIds = mutableListOf<String>()
                val photoDriveIdsJson = item.opt("photoDriveIds")
                if (photoDriveIdsJson is JSONArray) {
                    for (p in 0 until photoDriveIdsJson.length()) {
                        photoDriveIds.add(photoDriveIdsJson.getString(p).trim())
                    }
                } else if (photoDriveIdsJson is String && photoDriveIdsJson.isNotBlank()) {
                    photoDriveIds.addAll(photoDriveIdsJson.split("|||", ",").map { it.trim() }.filter { it.isNotEmpty() })
                }

                val event = FamilyEvent(
                    id = id,
                    title = title,
                    date = item.optString("date", ""),
                    location = item.optString("location", ""),
                    description = item.optString("description", ""),
                    photoUrls = photoUrls,
                    photoDriveIds = photoDriveIds,
                    createdByName = item.optString("createdByName", ""),
                    isSynced = true,
                    updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
                )
                eventsList.add(event)
            }

            Result.success(eventsList)
        } catch (e: Exception) {
            Log.e("GoogleAppsScript", "Failed to fetch events", e)
            Result.failure(e)
        }
    }

    suspend fun saveEvent(webAppUrl: String, event: FamilyEvent): Result<Boolean> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Web App URL is empty"))
        }

        try {
            val payload = JSONObject().apply {
                put("action", "saveEvent")
                put("id", event.id)
                put("title", event.title)
                put("date", event.date)
                put("location", event.location)
                put("description", event.description)
                put("photoUrls", JSONArray(event.photoUrls))
                put("photoDriveIds", JSONArray(event.photoDriveIds))
                put("createdByName", event.createdByName)
                put("updatedAt", event.updatedAt)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            val status = json.optString("status", "")
            if (status == "success") {
                Result.success(true)
            } else {
                Result.failure(Exception(json.optString("message", "Save event failed")))
            }
        } catch (e: Exception) {
            Log.e("GoogleAppsScript", "Failed to save event", e)
            Result.failure(e)
        }
    }

    suspend fun deleteEvent(webAppUrl: String, eventId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (webAppUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Web App URL is empty"))
        }

        try {
            val payload = JSONObject().apply {
                put("action", "deleteEvent")
                put("id", eventId)
            }

            val body = payload.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder().url(webAppUrl).post(body).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val responseBody = response.body?.string() ?: ""
            val json = JSONObject(responseBody)
            if (json.optString("status", "") == "success") {
                Result.success(true)
            } else {
                Result.failure(Exception(json.optString("message", "Delete event failed")))
            }
        } catch (e: Exception) {
            Log.e("GoogleAppsScript", "Failed to delete event", e)
            Result.failure(e)
        }
    }
}
