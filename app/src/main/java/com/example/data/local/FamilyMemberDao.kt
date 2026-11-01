package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY generation ASC, fullName ASC")
    fun getAllMembersFlow(): Flow<List<FamilyMemberEntity>>

    @Query("SELECT * FROM family_members ORDER BY generation ASC, fullName ASC")
    suspend fun getAllMembers(): List<FamilyMemberEntity>

    @Query("SELECT * FROM family_members WHERE id = :memberId LIMIT 1")
    suspend fun getMemberById(memberId: String): FamilyMemberEntity?

    @Query("SELECT * FROM family_members WHERE id IN (:ids)")
    suspend fun getMembersByIds(ids: List<String>): List<FamilyMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(member: FamilyMemberEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMemberEntity>)

    @Update
    suspend fun update(member: FamilyMemberEntity)

    @Query("DELETE FROM family_members WHERE id = :memberId")
    suspend fun deleteById(memberId: String)

    @Query("DELETE FROM family_members")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM family_members")
    suspend fun getMemberCount(): Int

    @Query("SELECT * FROM family_members WHERE fullName LIKE '%' || :query || '%' OR village LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR occupation LIKE '%' || :query || '%'")
    suspend fun searchMembers(query: String): List<FamilyMemberEntity>
}
