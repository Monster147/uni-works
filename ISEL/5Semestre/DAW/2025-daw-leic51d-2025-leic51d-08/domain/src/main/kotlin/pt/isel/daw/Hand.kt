package pt.isel.daw

data class Hand(
    val dice: List<Dice> = emptyList(),
) {
    override fun toString(): String {
        return dice.joinToString(", ") { it.face.name }
    }
}

fun Hand.generateHand(): Hand {
    val dice = List(5) { Dice(DiceFace.entries.random()) }
    return Hand(dice)
}

fun Hand.rerollHand(
    oldHand: Hand,
    keptDice: List<Boolean>,
): Hand { // to be rerolled = false | not to reroll = true
    val newDice =
        oldHand.dice.mapIndexed { i, die ->
            if (keptDice[i]) {
                die
            } else {
                Dice(DiceFace.entries.random())
            }
        }
    return Hand(newDice)
}
