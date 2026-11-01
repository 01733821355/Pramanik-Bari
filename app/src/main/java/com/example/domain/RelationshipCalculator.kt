package com.example.domain

import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import java.util.LinkedList
import java.util.Queue

enum class EdgeType(val labelEn: String, val labelBn: String) {
    FATHER("Father", "পিতা"),
    MOTHER("Mother", "মাতা"),
    SON("Son", "পুত্র"),
    DAUGHTER("Daughter", "কন্যা"),
    HUSBAND("Husband", "স্বামী"),
    WIFE("Wife", "স্ত্রী"),
    BROTHER("Brother", "ভাই"),
    SISTER("Sister", "বোন"),
    CHILD("Child", "সন্তান"),
    SPOUSE("Spouse", "দম্পতি")
}

data class PathStep(
    val fromMember: FamilyMember,
    val toMember: FamilyMember,
    val edgeType: EdgeType
)

data class RelationshipResult(
    val personA: FamilyMember,
    val personB: FamilyMember,
    val path: List<PathStep>,
    val relationTitleEn: String,
    val relationTitleBn: String,
    val explanationEn: String,
    val explanationBn: String
)

object RelationshipCalculator {

    fun findRelationship(
        allMembers: List<FamilyMember>,
        fromId: String,
        toId: String
    ): RelationshipResult? {
        if (fromId == toId) return null
        val memberMap = allMembers.associateBy { it.id }
        val start = memberMap[fromId] ?: return null
        val target = memberMap[toId] ?: return null

        // Build adjacency graph with typed edges
        val adjacency = mutableMapOf<String, MutableList<Pair<String, EdgeType>>>()
        for (m in allMembers) {
            adjacency.putIfAbsent(m.id, mutableListOf())

            // Father edge
            m.fatherId?.let { fId ->
                if (memberMap.containsKey(fId)) {
                    adjacency[m.id]?.add(Pair(fId, EdgeType.FATHER))
                    adjacency.putIfAbsent(fId, mutableListOf())
                    val reverse = if (m.gender == Gender.FEMALE) EdgeType.DAUGHTER else EdgeType.SON
                    adjacency[fId]?.add(Pair(m.id, reverse))
                }
            }

            // Mother edge
            m.motherId?.let { mId ->
                if (memberMap.containsKey(mId)) {
                    adjacency[m.id]?.add(Pair(mId, EdgeType.MOTHER))
                    adjacency.putIfAbsent(mId, mutableListOf())
                    val reverse = if (m.gender == Gender.FEMALE) EdgeType.DAUGHTER else EdgeType.SON
                    adjacency[mId]?.add(Pair(m.id, reverse))
                }
            }

            // Spouse edge
            m.spouseId?.let { sId ->
                if (memberMap.containsKey(sId)) {
                    val spouse = memberMap[sId]
                    val edge = if (spouse?.gender == Gender.FEMALE) EdgeType.WIFE else EdgeType.HUSBAND
                    val reverseEdge = if (m.gender == Gender.FEMALE) EdgeType.WIFE else EdgeType.HUSBAND
                    adjacency[m.id]?.add(Pair(sId, edge))
                    adjacency.putIfAbsent(sId, mutableListOf())
                    adjacency[sId]?.add(Pair(m.id, reverseEdge))
                }
            }

            // Children edges
            for (cId in m.childrenIds) {
                if (memberMap.containsKey(cId)) {
                    val child = memberMap[cId]
                    val edge = if (child?.gender == Gender.FEMALE) EdgeType.DAUGHTER else EdgeType.SON
                    val reverseEdge = if (m.gender == Gender.FEMALE) EdgeType.MOTHER else EdgeType.FATHER
                    adjacency[m.id]?.add(Pair(cId, edge))
                    adjacency.putIfAbsent(cId, mutableListOf())
                    adjacency[cId]?.add(Pair(m.id, reverseEdge))
                }
            }

            // Siblings
            for (sId in m.brotherSisterIds) {
                if (memberMap.containsKey(sId)) {
                    val sib = memberMap[sId]
                    val edge = if (sib?.gender == Gender.FEMALE) EdgeType.SISTER else EdgeType.BROTHER
                    val reverseEdge = if (m.gender == Gender.FEMALE) EdgeType.SISTER else EdgeType.BROTHER
                    adjacency[m.id]?.add(Pair(sId, edge))
                    adjacency.putIfAbsent(sId, mutableListOf())
                    adjacency[sId]?.add(Pair(m.id, reverseEdge))
                }
            }
        }

        // BFS traversal
        val queue: Queue<List<Pair<String, EdgeType>>> = LinkedList()
        val visited = mutableSetOf<String>()

        visited.add(start.id)
        queue.add(emptyList())

        var shortestRawPath: List<Pair<String, EdgeType>>? = null

        while (queue.isNotEmpty()) {
            val currentPath = queue.poll() ?: break
            val currentId = if (currentPath.isEmpty()) start.id else currentPath.last().first

            if (currentId == target.id && currentPath.isNotEmpty()) {
                shortestRawPath = currentPath
                break
            }

            val neighbors = adjacency[currentId] ?: emptyList()
            for (next in neighbors) {
                if (!visited.contains(next.first)) {
                    visited.add(next.first)
                    val newPath = currentPath + next
                    queue.add(newPath)
                }
            }
        }

        if (shortestRawPath == null) return null

        // Build PathStep list
        val steps = mutableListOf<PathStep>()
        var prevMember = start
        for (step in shortestRawPath) {
            val nextMember = memberMap[step.first] ?: continue
            steps.add(PathStep(prevMember, nextMember, step.second))
            prevMember = nextMember
        }

        val titles = resolveRelationTitles(steps, target.gender)

        val explanationEn = buildExplanationEn(start.fullName, target.fullName, steps, titles.first)
        val explanationBn = buildExplanationBn(start.fullName, target.fullName, steps, titles.second)

        return RelationshipResult(
            personA = start,
            personB = target,
            path = steps,
            relationTitleEn = titles.first,
            relationTitleBn = titles.second,
            explanationEn = explanationEn,
            explanationBn = explanationBn
        )
    }

    private fun resolveRelationTitles(steps: List<PathStep>, targetGender: Gender): Pair<String, String> {
        val edgeCodes = steps.map {
            when (it.edgeType) {
                EdgeType.FATHER -> "F"
                EdgeType.MOTHER -> "M"
                EdgeType.SON -> "S"
                EdgeType.DAUGHTER -> "D"
                EdgeType.HUSBAND -> "H"
                EdgeType.WIFE -> "W"
                EdgeType.BROTHER -> "B"
                EdgeType.SISTER -> "Si"
                EdgeType.CHILD -> "C"
                EdgeType.SPOUSE -> "Sp"
            }
        }.joinToString("-")

        return when (edgeCodes) {
            "F" -> Pair("Father", "বাবা / পিতা")
            "M" -> Pair("Mother", "মা / মাতা")
            "H" -> Pair("Husband", "স্বামী")
            "W" -> Pair("Wife", "স্ত্রী")
            "S" -> Pair("Son", "পুত্র / ছেলে")
            "D" -> Pair("Daughter", "কন্যা / মেয়ে")
            "B" -> Pair("Brother", "ভাই")
            "Si" -> Pair("Sister", "বোন")

            // Grandparents
            "F-F" -> Pair("Paternal Grandfather", "দাদা")
            "F-M" -> Pair("Paternal Grandmother", "দাদি")
            "M-F" -> Pair("Maternal Grandfather", "নানা")
            "M-M" -> Pair("Maternal Grandmother", "নানি")

            // Grandchildren
            "S-S", "D-S" -> Pair("Grandson", "নাতি")
            "S-D", "D-D" -> Pair("Granddaughter", "নাতনি")

            // Great Grandparents / Children
            "F-F-F", "M-F-F" -> Pair("Great Grandfather", "পরদাদা / পরনানা")
            "S-S-S", "D-S-S" -> Pair("Great Grandson", "পুতি")

            // Uncles & Aunts
            "F-B" -> Pair("Paternal Uncle (Chacha)", "চাচা")
            "F-Si" -> Pair("Paternal Aunt (Phuphu)", "ফুফু")
            "M-B" -> Pair("Maternal Uncle (Mama)", "মামা")
            "M-Si" -> Pair("Maternal Aunt (Khala)", "খালা")

            // Cousins
            "F-B-S" -> Pair("Paternal Cousin (Chachato Bhai)", "চাচাতো ভাই")
            "F-B-D" -> Pair("Paternal Cousin (Chachato Bon)", "চাচাতো বোন")
            "F-Si-S" -> Pair("Paternal Cousin (Phuphato Bhai)", "ফুফাতো ভাই")
            "F-Si-D" -> Pair("Paternal Cousin (Phuphato Bon)", "ফুফাতো বোন")
            "M-B-S" -> Pair("Maternal Cousin (Mamato Bhai)", "মামাতো ভাই")
            "M-B-D" -> Pair("Maternal Cousin (Mamato Bon)", "মামাতো বোন")
            "M-Si-S" -> Pair("Maternal Cousin (Khalato Bhai)", "খালাতো ভাই")
            "M-Si-D" -> Pair("Maternal Cousin (Khalato Bon)", "খালাতো বোন")

            // Nephews & Nieces
            "B-S" -> Pair("Nephew (Bhatija)", "ভাতিজা")
            "B-D" -> Pair("Niece (Bhatiji)", "ভাতিজি")
            "Si-S" -> Pair("Nephew (Bhagne)", "ভাগ্নে")
            "Si-D" -> Pair("Niece (Bhagni)", "ভাগ্নি")

            // In-laws
            "H-F", "W-F" -> Pair("Father-in-law", "শ্বশুর")
            "H-M", "W-M" -> Pair("Mother-in-law", "শাশুড়ি")
            "S-W" -> Pair("Daughter-in-law", "পুত্রবধূ")
            "D-H" -> Pair("Son-in-law", "জামাতা")
            "H-B" -> Pair("Brother-in-law (Bhashur / Debor)", "ভাসুর / দেবর")
            "W-B" -> Pair("Brother-in-law (Shala)", "শালা")
            "H-Si" -> Pair("Sister-in-law (Nanad)", "ননদ")
            "W-Si" -> Pair("Sister-in-law (Shalika)", "শালিকা")

            else -> {
                val lastStep = steps.last()
                val genDiff = steps.count { it.edgeType == EdgeType.SON || it.edgeType == EdgeType.DAUGHTER } -
                              steps.count { it.edgeType == EdgeType.FATHER || it.edgeType == EdgeType.MOTHER }
                when {
                    genDiff == 0 -> Pair("Kin / Relative (${lastStep.toMember.gender.name.lowercase()})", "পারিবারিক আত্মীয়")
                    genDiff > 0 -> Pair("Descendant Kin (${lastStep.toMember.gender.name.lowercase()})", "অনুধাবন প্রজন্ম")
                    else -> Pair("Ancestral Kin (${lastStep.toMember.gender.name.lowercase()})", "পূর্বসূরি আত্মীয়")
                }
            }
        }
    }

    private fun buildExplanationEn(aName: String, bName: String, steps: List<PathStep>, title: String): String {
        val chain = steps.joinToString(" -> ") { "${it.edgeType.labelEn} (${it.toMember.fullName.split(" ").firstOrNull() ?: ""})" }
        return "$bName is the $title of $aName. Kinship chain: $aName -> $chain."
    }

    private fun buildExplanationBn(aName: String, bName: String, steps: List<PathStep>, title: String): String {
        val chain = steps.joinToString(" ➔ ") { "${it.edgeType.labelBn} (${it.toMember.fullName.split(" ").firstOrNull() ?: ""})" }
        return "$bName হলেন $aName এর $title। সম্পর্কের সংযোগধারা: $aName ➔ $chain।"
    }
}
