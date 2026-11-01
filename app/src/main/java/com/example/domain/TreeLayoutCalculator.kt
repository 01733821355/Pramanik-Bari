package com.example.domain

import com.example.data.model.FamilyMember
import com.example.data.model.Gender

data class TreeNode(
    val member: FamilyMember,
    val x: Float,
    val y: Float,
    val width: Float = 170f,
    val height: Float = 160f,
    val isExpanded: Boolean = true,
    val hasChildren: Boolean = false,
    val isHighlighted: Boolean = false,
    val isAncestorOfSelected: Boolean = false,
    val isDescendantOfSelected: Boolean = false
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

data class TreeLayoutResult(
    val nodes: List<TreeNode>,
    val coupleLines: List<CoupleConnection>,
    val branchLines: List<BranchConnection>,
    val totalWidth: Float,
    val totalHeight: Float,
    val generationCounts: Map<Int, Int>
)

object TreeLayoutCalculator {
    const val NODE_WIDTH = 170f
    const val NODE_HEIGHT = 165f
    const val HORIZONTAL_SPACING = 55f
    const val VERTICAL_LEVEL_SPACING = 240f
    const val COUPLE_SPACING = 25f

    fun calculateLayout(
        allMembers: List<FamilyMember>,
        collapsedMemberIds: Set<String> = emptySet(),
        selectedMemberId: String? = null
    ): TreeLayoutResult {
        if (allMembers.isEmpty()) {
            return TreeLayoutResult(emptyList(), emptyList(), emptyList(), 800f, 600f, emptyMap())
        }

        val memberMap = allMembers.associateBy { it.id }

        // Determine generations (1 = oldest ancestors, 2, 3...)
        val computedGenerations = mutableMapOf<String, Int>()

        fun getGen(member: FamilyMember, visited: MutableSet<String> = mutableSetOf()): Int {
            if (visited.contains(member.id)) return 1
            visited.add(member.id)
            if (member.fatherId != null && memberMap.containsKey(member.fatherId)) {
                return (computedGenerations[member.fatherId] ?: getGen(memberMap[member.fatherId]!!, visited)) + 1
            }
            if (member.motherId != null && memberMap.containsKey(member.motherId)) {
                return (computedGenerations[member.motherId] ?: getGen(memberMap[member.motherId]!!, visited)) + 1
            }
            return if (member.generation > 0) member.generation else 1
        }

        for (m in allMembers) {
            computedGenerations[m.id] = getGen(m)
        }

        // Adjust spouse generations to match
        for (m in allMembers) {
            m.spouseId?.let { sId ->
                val g1 = computedGenerations[m.id] ?: 1
                val g2 = computedGenerations[sId] ?: 1
                val commonGen = minOf(g1, g2)
                computedGenerations[m.id] = commonGen
                computedGenerations[sId] = commonGen
            }
        }

        // Check ancestry & descendants for selected member highlighting
        val ancestors = mutableSetOf<String>()
        val descendants = mutableSetOf<String>()

        if (selectedMemberId != null && memberMap.containsKey(selectedMemberId)) {
            // Find ancestors
            fun traceAncestors(id: String) {
                val m = memberMap[id] ?: return
                m.fatherId?.let {
                    ancestors.add(it)
                    traceAncestors(it)
                }
                m.motherId?.let {
                    ancestors.add(it)
                    traceAncestors(it)
                }
            }
            traceAncestors(selectedMemberId)

            // Find descendants
            fun traceDescendants(id: String) {
                val m = memberMap[id] ?: return
                for (cId in m.childrenIds) {
                    descendants.add(cId)
                    traceDescendants(cId)
                }
            }
            traceDescendants(selectedMemberId)
        }

        // Filter out members who are hidden because their parent branch is collapsed
        val hiddenMembers = mutableSetOf<String>()
        fun markHiddenChildren(id: String) {
            val m = memberMap[id] ?: return
            for (cId in m.childrenIds) {
                hiddenMembers.add(cId)
                markHiddenChildren(cId)
            }
        }

        for (collapsedId in collapsedMemberIds) {
            markHiddenChildren(collapsedId)
        }

        val visibleMembers = allMembers.filter { !hiddenMembers.contains(it.id) }
        val byGeneration = visibleMembers.groupBy { computedGenerations[it.id] ?: 1 }
            .toSortedMap()

        val nodes = mutableListOf<TreeNode>()
        val coupleLines = mutableListOf<CoupleConnection>()
        val branchLines = mutableListOf<BranchConnection>()

        var maxTreeWidth = 800f
        var currentY = 80f

        // Keep track of member center coordinates
        val nodeCenters = mutableMapOf<String, Pair<Float, Float>>()

        for ((gen, membersInGen) in byGeneration) {
            // Group couples side-by-side
            val placedInRow = mutableSetOf<String>()
            val rowUnits = mutableListOf<List<FamilyMember>>()

            for (m in membersInGen) {
                if (placedInRow.contains(m.id)) continue
                val sId = m.spouseId
                if (sId != null && membersInGen.any { it.id == sId } && !placedInRow.contains(sId)) {
                    val spouse = membersInGen.first { it.id == sId }
                    // Place Male first, then Female
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

            var startX = 80f
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

                    val hasKids = m1.childrenIds.isNotEmpty() || m2.childrenIds.isNotEmpty()
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
                            member = m1,
                            x = x1,
                            y = y1,
                            isExpanded = !isCollapsed,
                            hasChildren = hasKids,
                            isHighlighted = m1.id == selectedMemberId,
                            isAncestorOfSelected = ancestors.contains(m1.id),
                            isDescendantOfSelected = descendants.contains(m1.id)
                        )
                    )

                    nodes.add(
                        TreeNode(
                            member = m2,
                            x = x2,
                            y = y2,
                            isExpanded = !isCollapsed,
                            hasChildren = hasKids,
                            isHighlighted = m2.id == selectedMemberId,
                            isAncestorOfSelected = ancestors.contains(m2.id),
                            isDescendantOfSelected = descendants.contains(m2.id)
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

                    val hasKids = m.childrenIds.isNotEmpty()
                    val isCollapsed = collapsedMemberIds.contains(m.id)

                    nodes.add(
                        TreeNode(
                            member = m,
                            x = x,
                            y = y,
                            isExpanded = !isCollapsed,
                            hasChildren = hasKids,
                            isHighlighted = m.id == selectedMemberId,
                            isAncestorOfSelected = ancestors.contains(m.id),
                            isDescendantOfSelected = descendants.contains(m.id)
                        )
                    )

                    startX += NODE_WIDTH + HORIZONTAL_SPACING
                }
            }

            currentY += VERTICAL_LEVEL_SPACING
        }

        // Generate branch connections (parents down to children)
        for (node in nodes) {
            val child = node.member
            val fatherId = child.fatherId
            val motherId = child.motherId

            var parentMidX: Float? = null
            var parentMidY: Float? = null

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
            totalWidth = maxOf(maxTreeWidth + 200f, 1200f),
            totalHeight = maxOf(currentY + 150f, 900f),
            generationCounts = visibleMembers.groupBy { computedGenerations[it.id] ?: 1 }.mapValues { it.value.size }
        )
    }
}
