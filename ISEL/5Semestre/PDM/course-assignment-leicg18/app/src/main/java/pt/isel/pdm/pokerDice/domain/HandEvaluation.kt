package pt.isel.pdm.pokerDice.domain

data class HandEvaluation(
    val category: HandCategory,
    val ranks: List<Int>,
) : Comparable<HandEvaluation> {
    override fun compareTo(other: HandEvaluation): Int {
        val catComp = category.rank.compareTo(other.category.rank)
        if (catComp != 0) return catComp

        val maxLen = maxOf(ranks.size, other.ranks.size)
        for (i in 0 until maxLen) {
            val aVal = ranks.getOrNull(i) ?: 0
            val bVal = other.ranks.getOrNull(i) ?: 0
            if (aVal != bVal) {
                return aVal.compareTo(bVal)
            }
        }
        return 0
    }
}
