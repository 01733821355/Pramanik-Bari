package com.example.domain

import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import java.util.LinkedList
import java.util.Queue

data class TreeNode(
    val member: FamilyMember,
    val x: Float,
    val y: Float,
    val width: Float = 170f,
    val height: Float = 165f,
    val isExpanded: Boolean = true,
    val hasChildren: Boolean = false,
    val isHighlighted: Boolean = false,
    val isAncestorOfSelected: Boolean = false,
    val isDescendantOfSelected: Boolean = false,
    val purushNumber: Int = 1
)

data class CoupleConnection(
    val personAId: String,
    val personBId: String,
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val midX: Float,
    val midY: Float,
    val isHighlighted: Boolean = false
)

data class BranchConnection(
    val parentMidX: Float,
    val parentMidY: Float,
    val childTopX: Float,
    val childTopY: Float,
    val isHighlighted: Boolean = false
)

data class GenerationHeader(
    val generation: Int,
    val titleBn: String,
    val titleEn: String,
    val y: Float,
    val count: Int
)

data class TreeLayoutResult(
    val nodes: List<TreeNode>,
    val coupleLines: List<CoupleConnection>,
    val branchLines: List<BranchConnection>,
    val generationHeaders: List<GenerationHeader>,
    val totalWidth: Float,
    val totalHeight: Float,
    val generationCounts: Map<Int, Int>
)

object TreeLayoutCalculator {
    const val NODE_WIDTH = 175f
    const val NODE_HEIGHT = 165f
    const val HORIZONTAL_SPACING = 50f
    const val VERTICAL_LEVEL_SPACING = 270f
    const val COUPLE_SPACING = 28f

    fun toPurushLabel(gen: Int, isBn: Boolean): String {
        if (!isBn) return "Generation $gen"
        return when (gen) {
            1 -> "১ম পুরুষ"
            2 -> "২য় পুরুষ"
            3 -> "৩য় পুরুষ"
            4 -> "৪র্থ পুরুষ"
            5 -> "৫ম পুরুষ"
            6 -> "৬ষ্ঠ পুরুষ"
            7 -> "৭ম পুরুষ"
            8 -> "৮ম পুরুষ"
            9 -> "৯ম পুরুষ"
            10 -> "১০ম পুরুষ"
            else -> "${AgeCalculator.toBengaliDigits(gen)}তম পুরুষ"
        }
    }

    fun parseBengaliOrLatinInt(input: String?): Int {
        if (input.isNullOrBlank()) return 1
        val clean = input.trim()
            .replace("১", "1")
            .replace("২", "2")
            .replace("৩", "3")
            .replace("৪", "4")
            .replace("৫", "5")
            .replace("৬", "6")
            .replace("৭", "7")
            .replace("৮", "8")
            .replace("৯", "9")
            .replace("০", "0")
            .filter { it.isDigit() }
        return clean.toIntOrNull() ?: 1
    }

    fun calculateLayout(
        allMembers: List<FamilyMember>,
        collapsedMemberIds: Set<String> = emptySet(),
        selectedMemberId: String? = null
    ): TreeLayoutResult {
        if (allMembers.isEmpty()) {
            return TreeLayoutResult(emptyList(), emptyList(), emptyList(), emptyList(), 800f, 600f, emptyMap())
        }

        val memberMap = allMembers.associateBy { it.id }

        // 1. Cycle-safe generation calculation (BFS from roots)
        val computedGenerations = mutableMapOf<String, Int>()
        val childrenOf = mutableMapOf<String, MutableSet<String>>()
        for (m in allMembers) {
            childrenOf[m.id] = mutableSetOf()
        }

        for (m in allMembers) {
            m.fatherId?.let { fId ->
                if (memberMap.containsKey(fId) && fId != m.id) {
                    childrenOf[fId]?.add(m.id)
                }
            }
            m.motherId?.let { mId ->
                if (memberMap.containsKey(mId) && mId != m.id) {
                    childrenOf[mId]?.add(m.id)
                }
            }
            for (cId in m.childrenIds) {
                if (memberMap.containsKey(cId) && cId != m.id) {
                    childrenOf[m.id]?.add(cId)
                }
            }
        }

        // Roots are members whose parents are not in the memberMap
        val roots = allMembers.filter { m ->
            val hasFather = m.fatherId != null && memberMap.containsKey(m.fatherId)
            val hasMother = m.motherId != null && memberMap.containsKey(m.motherId)
            !hasFather && !hasMother
        }

        val visitedGen = mutableSetOf<String>()
        val queue: Queue<Pair<String, Int>> = LinkedList()

        for (r in roots) {
            val rootGen = if (r.generation > 0) r.generation else 1
            computedGenerations[r.id] = rootGen
            visitedGen.add(r.id)
            queue.add(Pair(r.id, rootGen))
        }

        while (queue.isNotEmpty()) {
            val (currentId, currentGen) = queue.poll()!!
            val currentMember = memberMap[currentId] ?: continue

            // Spouses share same generation
            currentMember.spouseId?.let { sId ->
                if (memberMap.containsKey(sId) && !visitedGen.contains(sId)) {
                    computedGenerations[sId] = currentGen
                    visitedGen.add(sId)
                    for (cId in (childrenOf[sId] ?: emptySet())) {
                        if (!visitedGen.contains(cId)) {
                            computedGenerations[cId] = currentGen + 1
                            visitedGen.add(cId)
                            queue.add(Pair(cId, currentGen + 1))
                        }
                    }
                }
            }

            for (childId in (childrenOf[currentId] ?: emptySet())) {
                if (!visitedGen.contains(childId)) {
                    computedGenerations[childId] = currentGen + 1
                    visitedGen.add(childId)
                    queue.add(Pair(childId, currentGen + 1))
                }
            }
        }

        // Any unvisited nodes (islands/disconnected)
        for (m in allMembers) {
            if (!computedGenerations.containsKey(m.id)) {
                computedGenerations[m.id] = if (m.generation > 0) m.generation else 1
            }
        }

        // Final spouse sync (ensure spouses are placed on same generation level)
        for (m in allMembers) {
            m.spouseId?.let { sId ->
                if (memberMap.containsKey(sId)) {
                    val g1 = computedGenerations[m.id] ?: 1
                    val g2 = computedGenerations[sId] ?: 1
                    val minG = minOf(g1, g2)
                    computedGenerations[m.id] = minG
                    computedGenerations[sId] = minG
                }
            }
        }

        // 2. Cycle-safe ancestor & descendant discovery
        val ancestors = mutableSetOf<String>()
        val descendants = mutableSetOf<String>()

        if (selectedMemberId != null && memberMap.containsKey(selectedMemberId)) {
            val ancQueue: Queue<String> = LinkedList()
            ancQueue.add(selectedMemberId)
            val ancVisited = mutableSetOf(selectedMemberId)
            while (ancQueue.isNotEmpty()) {
                val currId = ancQueue.poll()!!
                val m = memberMap[currId] ?: continue
                m.fatherId?.let { fId ->
                    if (memberMap.containsKey(fId) && ancVisited.add(fId)) {
                        ancestors.add(fId)
                        ancQueue.add(fId)
                    }
                }
                m.motherId?.let { mId ->
                    if (memberMap.containsKey(mId) && ancVisited.add(mId)) {
                        ancestors.add(mId)
                        ancQueue.add(mId)
                    }
                }
            }

            val descQueue: Queue<String> = LinkedList()
            descQueue.add(selectedMemberId)
            val descVisited = mutableSetOf(selectedMemberId)
            while (descQueue.isNotEmpty()) {
                val currId = descQueue.poll()!!
                val m = memberMap[currId] ?: continue
                for (cId in m.childrenIds) {
                    if (memberMap.containsKey(cId) && descVisited.add(cId)) {
                        descendants.add(cId)
                        descQueue.add(cId)
                    }
                }
                for (other in allMembers) {
                    if ((other.fatherId == currId || other.motherId == currId) && descVisited.add(other.id)) {
                        descendants.add(other.id)
                        descQueue.add(other.id)
                    }
                }
            }
        }

        // 3. Cycle-safe collapse filter
        val hiddenMembers = mutableSetOf<String>()
        for (collapsedId in collapsedMemberIds) {
            val collapseQueue: Queue<String> = LinkedList()
            collapseQueue.add(collapsedId)
            val collapseVisited = mutableSetOf(collapsedId)
            while (collapseQueue.isNotEmpty()) {
                val currId = collapseQueue.poll()!!
                val m = memberMap[currId] ?: continue
                for (cId in m.childrenIds) {
                    if (memberMap.containsKey(cId) && collapseVisited.add(cId)) {
                        hiddenMembers.add(cId)
                        collapseQueue.add(cId)
                    }
                }
                for (other in allMembers) {
                    if ((other.fatherId == currId || other.motherId == currId) && collapseVisited.add(other.id)) {
                        hiddenMembers.add(other.id)
                        collapseQueue.add(other.id)
                    }
                }
            }
        }

        val visibleMembers = allMembers.filter { !hiddenMembers.contains(it.id) }
        val byGeneration = visibleMembers.groupBy { computedGenerations[it.id] ?: 1 }
            .toSortedMap()

        val nodes = mutableListOf<TreeNode>()
        val coupleLines = mutableListOf<CoupleConnection>()
        val branchLines = mutableListOf<BranchConnection>()
        val generationHeaders = mutableListOf<GenerationHeader>()

        var maxTreeWidth = 800f
        var currentY = 100f
        val nodeCenters = mutableMapOf<String, Pair<Float, Float>>()

        for ((gen, membersInGen) in byGeneration) {
            // Group couples side-by-side (Husband on left, Wife on right)
            val placedInRow = mutableSetOf<String>()
            val rowUnits = mutableListOf<List<FamilyMember>>()

            // Sort members so siblings belonging to the same parents stay clustered together
            val sortedMembersInGen = membersInGen.sortedWith(
                compareBy<FamilyMember> { it.fatherId ?: "" }
                    .thenBy { it.motherId ?: "" }
                    .thenBy { it.fullName }
            )

            for (m in sortedMembersInGen) {
                if (placedInRow.contains(m.id)) continue
                val sId = m.spouseId
                if (sId != null && membersInGen.any { it.id == sId } && !placedInRow.contains(sId)) {
                    val spouse = membersInGen.first { it.id == sId }
                    val pair = if (m.gender == Gender.MALE) listOf(m, spouse) else listOf(spouse, m)
                    rowUnits.add(pair)
                    placedInRow.add(m.id)
                    placedInRow.add(sId)
                } else {
                    rowUnits.add(listOf(m))
                    placedInRow.add(m.id)
                }
            }

            // Calculate total row width
            var rowWidth = 0f
            for (unit in rowUnits) {
                if (unit.size == 2) {
                    rowWidth += (NODE_WIDTH * 2) + COUPLE_SPACING
                } else {
                    rowWidth += NODE_WIDTH
                }
                rowWidth += HORIZONTAL_SPACING
            }
            if (rowWidth > maxTreeWidth) {
                maxTreeWidth = rowWidth
            }

            // Add Generation Header
            val purushBn = toPurushLabel(gen, isBn = true)
            val purushEn = toPurushLabel(gen, isBn = false)
            generationHeaders.add(
                GenerationHeader(
                    generation = gen,
                    titleBn = purushBn,
                    titleEn = purushEn,
                    y = currentY - 55f,
                    count = membersInGen.size
                )
            )

            var startX = 120f
            for (unit in rowUnits) {
                if (unit.size == 2) {
                    val m1 = unit[0]
                    val m2 = unit[1]

                    val x1 = startX
                    val y1 = currentY
                    val center1X = x1 + NODE_WIDTH / 2
                    val center1Y = y1 + NODE_HEIGHT / 2
                    nodeCenters[m1.id] = Pair(center1X, center1Y)

                    val x2 = startX + NODE_WIDTH + COUPLE_SPACING
                    val y2 = currentY
                    val center2X = x2 + NODE_WIDTH / 2
                    val center2Y = y2 + NODE_HEIGHT / 2
                    nodeCenters[m2.id] = Pair(center2X, center2Y)

                    val midX = (center1X + center2X) / 2
                    val midY = center1Y

                    val hasKids = (childrenOf[m1.id]?.isNotEmpty() == true) || (childrenOf[m2.id]?.isNotEmpty() == true)
                    val isCollapsed = collapsedMemberIds.contains(m1.id) || collapsedMemberIds.contains(m2.id)

                    val isCoupleHighlighted = (m1.id == selectedMemberId || m2.id == selectedMemberId) ||
                            (ancestors.contains(m1.id) && ancestors.contains(m2.id))

                    coupleLines.add(
                        CoupleConnection(
                            personAId = m1.id,
                            personBId = m2.id,
                            x1 = x1 + NODE_WIDTH,
                            y1 = center1Y,
                            x2 = x2,
                            y2 = center2Y,
                            midX = midX,
                            midY = midY,
                            isHighlighted = isCoupleHighlighted
                        )
                    )

                    nodes.add(
                        TreeNode(
                            member = m1.copy(generation = gen),
                            x = x1,
                            y = y1,
                            isExpanded = !isCollapsed,
                            hasChildren = hasKids,
                            isHighlighted = m1.id == selectedMemberId,
                            isAncestorOfSelected = ancestors.contains(m1.id),
                            isDescendantOfSelected = descendants.contains(m1.id),
                            purushNumber = gen
                        )
                    )

                    nodes.add(
                        TreeNode(
                            member = m2.copy(generation = gen),
                            x = x2,
                            y = y2,
                            isExpanded = !isCollapsed,
                            hasChildren = hasKids,
                            isHighlighted = m2.id == selectedMemberId,
                            isAncestorOfSelected = ancestors.contains(m2.id),
                            isDescendantOfSelected = descendants.contains(m2.id),
                            purushNumber = gen
                        )
                    )

                    startX += (NODE_WIDTH * 2) + COUPLE_SPACING + HORIZONTAL_SPACING
                } else {
                    val m = unit[0]
                    val x = startX
                    val y = currentY
                    val centerX = x + NODE_WIDTH / 2
                    val centerY = y + NODE_HEIGHT / 2
                    nodeCenters[m.id] = Pair(centerX, centerY)

                    val hasKids = childrenOf[m.id]?.isNotEmpty() == true
                    val isCollapsed = collapsedMemberIds.contains(m.id)

                    nodes.add(
                        TreeNode(
                            member = m.copy(generation = gen),
                            x = x,
                            y = y,
                            isExpanded = !isCollapsed,
                            hasChildren = hasKids,
                            isHighlighted = m.id == selectedMemberId,
                            isAncestorOfSelected = ancestors.contains(m.id),
                            isDescendantOfSelected = descendants.contains(m.id),
                            purushNumber = gen
                        )
                    )

                    startX += NODE_WIDTH + HORIZONTAL_SPACING
                }
            }

            currentY += VERTICAL_LEVEL_SPACING
        }

        // 4. Generate Branch Lines (Connecting parents down to children)
        for (node in nodes) {
            val child = node.member
            val fatherId = child.fatherId
            val motherId = child.motherId

            var parentMidX: Float? = null
            var parentMidY: Float? = null

            // First check if both father & mother are placed as a couple
            if (fatherId != null && motherId != null) {
                val couple = coupleLines.find {
                    (it.personAId == fatherId && it.personBId == motherId) ||
                    (it.personAId == motherId && it.personBId == fatherId)
                }
                if (couple != null) {
                    parentMidX = couple.midX
                    parentMidY = couple.midY + (NODE_HEIGHT / 2)
                }
            }

            // If not found as a couple, use single parent or mid-point
            if (parentMidX == null) {
                val fCenter = fatherId?.let { nodeCenters[it] }
                val mCenter = motherId?.let { nodeCenters[it] }
                when {
                    fCenter != null && mCenter != null -> {
                        parentMidX = (fCenter.first + mCenter.first) / 2
                        parentMidY = fCenter.second + (NODE_HEIGHT / 2)
                    }
                    fCenter != null -> {
                        parentMidX = fCenter.first
                        parentMidY = fCenter.second + (NODE_HEIGHT / 2)
                    }
                    mCenter != null -> {
                        parentMidX = mCenter.first
                        parentMidY = mCenter.second + (NODE_HEIGHT / 2)
                    }
                }
            }

            if (parentMidX != null && parentMidY != null) {
                val childCenter = nodeCenters[child.id]
                if (childCenter != null) {
                    val childTopX = childCenter.first
                    val childTopY = childCenter.second - (NODE_HEIGHT / 2)
                    val isHighlighted = (child.id == selectedMemberId || descendants.contains(child.id)) &&
                            (ancestors.contains(fatherId) || ancestors.contains(motherId) || fatherId == selectedMemberId || motherId == selectedMemberId)

                    branchLines.add(
                        BranchConnection(
                            parentMidX = parentMidX,
                            parentMidY = parentMidY,
                            childTopX = childTopX,
                            childTopY = childTopY,
                            isHighlighted = isHighlighted
                        )
                    )
                }
            }
        }

        return TreeLayoutResult(
            nodes = nodes,
            coupleLines = coupleLines,
            branchLines = branchLines,
            generationHeaders = generationHeaders,
            totalWidth = maxOf(maxTreeWidth + 260f, 1200f),
            totalHeight = maxOf(currentY + 180f, 950f),
            generationCounts = visibleMembers.groupBy { computedGenerations[it.id] ?: 1 }.mapValues { it.value.size }
        )
    }
}
