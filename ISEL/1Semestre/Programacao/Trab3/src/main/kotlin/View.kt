import pt.isel.canvas.*

// Dimensions of the sprites in the images files
// Floor, Egg, Food and Stair are 1x1 ; Man is 1x2 ; Hen is 1x3 or 2x2
const val SPRITE_WIDTH = 24  // [pixels in image file]
const val SPRITE_HEIGHT = 16 // [pixels in image file]

// Dimensions of the Arena grid
const val GRID_WIDTH = 20
const val GRID_HEIGHT = 24

// Dimensions of each cell of the Arena grid
const val VIEW_FACTOR = 2 // each cell is VIEW_FACTOR x sprite
const val CELL_WIDTH = VIEW_FACTOR * SPRITE_WIDTH   // [pixels]
const val CELL_HEIGHT = VIEW_FACTOR * SPRITE_HEIGHT  // [pixels]

/**
 * Creates a canvas with the dimensions of the arena.
 */
fun createCanvas() = Canvas(GRID_WIDTH * CELL_WIDTH, GRID_HEIGHT * CELL_HEIGHT, BLACK)

/**
 * Draw horizontal and vertical lines of the grid in arena.
 */
fun Canvas.drawGridLines() {
    (0 ..< width step CELL_WIDTH).forEach { x -> drawLine(x, 0, x, height, WHITE, 1) }
    (0 ..< height step CELL_HEIGHT).forEach { y -> drawLine(0, y, width, y, WHITE, 1) }
}

/**
 * Represents a sprite in the image.
 * Example: Sprite(2,3,1,1) is the man facing left.
 * @property row the row of the sprite in the image. (in sprites)
 * @property col the column of the sprite in the image. (in sprites)
 * @property height the height of the sprite in the image. (in sprites)
 * @property width the width of the sprite in the image. (in sprites)
 */
data class Sprite(val row: Int, val col: Int, val height: Int = 1, val width: Int = 1)

/**
 * Draw a sprite in a position of the canvas.
 * @param pos the position in the canvas (top-left of base cell).
 * @param spriteRow the row of the sprite in the image.
 * @param spriteCol the column of the sprite in the image.
 * @param spriteHeight the height of the sprite in the image.
 * @param spriteWidth the width of the sprite in the image.
 */
fun Canvas.drawSprite(pos: Point, s: Sprite) {
    val x = s.col * SPRITE_WIDTH + s.col + 1  // in pixels
    val y = s.row * SPRITE_HEIGHT + s.row + s.height
    val h = s.height * SPRITE_HEIGHT
    val w = s.width * SPRITE_WIDTH
    drawImage(
        fileName = "chuckieEgg|$x,$y,$w,$h",
        xLeft = pos.x,
        yTop = pos.y - (s.height-1) * CELL_HEIGHT,
        width = CELL_WIDTH * s.width,
        height = CELL_HEIGHT * s.height
    )
}

/**
 * Draw all the elements of the game.
 * Desenha, ainda, em caso de vitória o texto "YOU WIN" ou em caso de perca "YOU LOSE"
 */
fun Canvas.drawGame(game: Game) {
    erase()
    //drawGridLines()
    game.floor.forEach { drawSprite(it.toPoint(), Sprite(0, 0)) }
    game.stairs.forEach { drawSprite(it.toPoint(), Sprite(0, 1)) }
    game.ovos.forEach{ drawSprite(it.toPoint(), Sprite(1, 1)) }
    game.comida.forEach{ drawSprite(it.toPoint(), Sprite(1, 0)) }
    drawText(CELL_WIDTH, CELL_HEIGHT, "Score:${game.pontuacao}", YELLOW, CELL_HEIGHT)
    drawText(15*CELL_WIDTH, CELL_HEIGHT, "Time:${game.tempo}", YELLOW, CELL_HEIGHT)
    drawMan(game.man)
    if (game.victory == true) drawText(9*CELL_WIDTH, CELL_HEIGHT, "YOU WIN", GREEN, CELL_HEIGHT)
    if (game.lose == true) drawText(9*CELL_WIDTH, CELL_HEIGHT, "YOU LOSE", RED, CELL_HEIGHT)

}

/**
 * Draws the man in canvas according to the direction he is facing.
 * O desenho do homem é feito com animações controladas pelo AniSteps do Homem.
 * Dependendo do valor do AniSteps, se este for divisível por 2 desenha um dos homens a andar,
 * tanto para a esquerda ou para a direita, a subir ou a descer.
 * Caso o valor não seja divisivel por 2, desenha outro sprite do homem a subir, descer ou a andar.
 */
fun Canvas.drawMan(player: Man) {
    val sprite = when(player.faced) {
        Direction.LEFT -> {
            if(player.vel.isZero()) Sprite(2, 3, 2)
            else{
                if(player.AniSteps%2==0)Sprite(2, 2, 2) else Sprite(2, 4, 2)
            }
        }
        Direction.RIGHT ->{
            if(player.vel.isZero()) Sprite(0, 3, 2)
            else{
                if(player.AniSteps%2==0)Sprite(0, 2, 2) else Sprite(0, 4, 2)
            }
        }
        Direction.UP -> {
            if(player.vel.isZero()) Sprite(4, 0, 2)
            else{
                if(player.AniSteps%2==0)Sprite(4, 1, 2) else Sprite(4, 2, 2)
            }
        }
        Direction.DOWN -> {
            if(player.vel.isZero()) Sprite(4, 0, 2)
            else{
                if(player.AniSteps%2==0)Sprite(4, 3, 2) else Sprite(4, 4, 2)
            }
        }
    }
    drawSprite(player.pos, sprite)
}
