package com.example

import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.domain.RelationshipCalculator
import com.example.domain.TreeLayoutCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testDriveUrlExtraction() {
        val driveUrl = "https://drive.google.com/file/d/1AbCdEfGhIjKlMnOpQrStUvWxYz/view?usp=sharing"
        val member = FamilyMember(
            id = "PB-0001",
            fullName = "Test Member",
            photoUrl = driveUrl
        )
        val resolved = member.getResolvedImageUrl()
        assertTrue(resolved.contains("1AbCdEfGhIjKlMnOpQrStUvWxYz"))
        assertTrue(resolved.startsWith("https://lh3.googleusercontent.com/d/"))
    }

    @Test
    fun testRelationshipCalculatorFatherSon() {
        val father = FamilyMember(
            id = "P1",
            fullName = "Father Person",
            gender = Gender.MALE,
            childrenIds = listOf("P2")
        )
        val son = FamilyMember(
            id = "P2",
            fullName = "Son Person",
            gender = Gender.MALE,
            fatherId = "P1"
        )
        val all = listOf(father, son)

        val result = RelationshipCalculator.findRelationship(all, "P2", "P1")
        assertNotNull(result)
        assertEquals("Father", result?.relationTitleEn)
        assertTrue(result?.relationTitleBn?.contains("পিতা") == true || result?.relationTitleBn?.contains("বাবা") == true)
    }

    @Test
    fun testTreeLayoutCalculatorNotEmpty() {
        val m1 = FamilyMember(id = "P1", fullName = "Root", gender = Gender.MALE, childrenIds = listOf("P2"))
        val m2 = FamilyMember(id = "P2", fullName = "Child", gender = Gender.FEMALE, fatherId = "P1")
        val layout = TreeLayoutCalculator.calculateLayout(listOf(m1, m2))
        assertEquals(2, layout.nodes.size)
        assertEquals(1, layout.branchLines.size)
    }

    @Test
    fun testBengaliDigitsAndAgeCalculation() {
        val bnDigits = com.example.domain.AgeCalculator.toBengaliDigits(25)
        assertEquals("২৫", bnDigits)

        val livingAge = com.example.domain.AgeCalculator.calculateAge("1995-05-10", "", false)
        assertNotNull(livingAge)
        assertTrue(livingAge!!.first.contains("বয়স"))

        val deceasedAge = com.example.domain.AgeCalculator.calculateAge("1940-01-01", "2010-06-15", true)
        assertNotNull(deceasedAge)
        assertTrue(deceasedAge!!.first.contains("প্রয়াত"))
        assertEquals("Passed away at age 70", deceasedAge.second)
    }
}
