// Speed of man in pixels per frame, in horizontal and vertical directions
const val MOVE_SPEED = CELL_WIDTH / 6
const val CLIMBING_SPEED = CELL_HEIGHT / 4
const val INITIAL_JUMPING_SPEED = CELL_HEIGHT/2
const val JUMPING_SPEED = CELL_HEIGHT/SPRITE_HEIGHT

// Limits of the board in pixels
// Point(MAX_X, MAX_Y) == Cell(GRID_HEIGHT-1, GRID_WIDTH-1).toPoint()
const val MAX_X = (GRID_WIDTH - 1) * CELL_WIDTH
const val MAX_Y = (GRID_HEIGHT - 1) * CELL_HEIGHT

/**
 * Represents the Man in the game.
 * @property pos is the position in the board.
 * @property faced the direction the man is facing
 */
data class Man(
    val pos: Point,
    val faced: Direction,
    val vel : Speed,
    val salto: Boolean
)

/**
 * Creates the Man in the cell
 */
fun createMan(cell: Cell) = Man(
    pos = cell.toPoint(),
    faced = Direction.LEFT,
    vel = Speed(0, 0),
    salto = false
)
//Ao ser executada, o Homem anda para a esquerda, criando vários homens para a execução da função, depende da função "podeesq"
fun Man.andaesq(game: Game): Man =
    if (salto == false && vel.isZero() && podeesq(game)) {
        val myVel=Speed(-MOVE_SPEED, 0)
        copy(vel = myVel, faced = Direction.LEFT)
    }
    else this

//Ao ser executada, o Homem anda para a direita, criando vários homens para a execução da função, depende da função "podedir"
fun Man.andadir(game: Game): Man =
    if (salto == false && vel.isZero() && podedir(game)) {
        val myVel = Speed(MOVE_SPEED, 0)
        copy(vel = myVel, faced = Direction.RIGHT)
    }
    else this

/*Verifica se o Homem pode seguir caminho para a direita
Caso a próxima célula à direita seja um "floor", o Homem poderá realizar o seu movimento.
Se não for o Homem permanecerá parado*/
fun Man.podedir(game: Game): Boolean{
    val CheckCel = pos.toCell() + Direction.RIGHT
    return CheckCel !in game.floor && pos.x < MAX_X
}

/*Verifica se o Homem pode seguir caminho para a direita
Caso a próxima célula à esquerda seja um "floor", o Homem poderá realizar o seu movimento.
Se não for o Homem permanecerá parado*/
fun Man.podeesq(game: Game): Boolean{
    val CheckCel = pos.toCell() + Direction.LEFT
    return CheckCel !in game.floor && pos.x > 0
}

/* A função "stepsDX" dependendo das condições reúnidas o Homem pode agir de duas maneiras:
permanece parado se o Homem não estiver a saltar e tiver velocidade 0;
movimento para a direita ou para a esquerda se a velocidade do Homem for diferente de 0
ou se a próxima célula detetada for um "floor"
ou se a próxima célula detetada for um "stair";
o Homem recebe a indicação de que poderá realizar um salto*/
fun Man.stepsDX(game: Game):Man{
    if (salto == false && vel.isZero()) return this
    val novaPos = (pos + vel).limitToArea(MAX_X, MAX_Y)
    val CheckCel = novaPos.copy(y=novaPos.y+CELL_HEIGHT).toCell()
    val novoplayer = copy(pos = novaPos, vel = vel.stopIfInCell(novaPos))
    return if (novoplayer.vel.dx!=0 || CheckCel in game.floor || CheckCel in game.stairs) novoplayer
    else novoplayer.copy(vel = Speed(0,0), salto = true)
}

//Caso seja chamada, o Homem desce pelas escadas, depende da função estaescada.
fun Man.desceescada(stairs: List<Cell>) =
    if (estaescada(stairs) && vel.isZero()) {
        val myVel=Speed(0, CLIMBING_SPEED)
        copy(vel = myVel, faced = Direction.UP)
    }
    else this

//Caso seja chamada, o Homem sobe pelas escadas, depende da função estaescada.
fun Man.sobeescada(stairs: List<Cell>) =
    if (estaescada(stairs) && vel.isZero()) {
        val myVel = Speed(0, -CLIMBING_SPEED)
        copy(vel = myVel, faced = Direction.DOWN)
    }
    else this

// Caso seja chamada, o Homem salta
fun Man.salta(game: Game) : Man =
    if (salto == false && faced.isHorizontal()) {
        val player = if (faced== Direction.LEFT) -MOVE_SPEED else MOVE_SPEED
        copy(salto= true, vel = Speed(player, -INITIAL_JUMPING_SPEED))
    }
    else this

//Verifica se o Homem encontra-se dentro das escadas
fun Man.estaescada(stairs: List<Cell>):Boolean=
    pos.toCell() in stairs && pos.toCell()+Direction.UP in stairs

/*Esta dunção controla os movimentos do Homem para o eixo dos y's.
Representando um papel fundamental para os saltos, "stepsDY"
simula os movimentos verticais do Homem,considerando a aceleração do salto, sempre limitada à velocidade máxima de salto (JUMPING_SPEED)
e verifica se a próxima célula após o movimento será um "floor" ou um "stair", de modo a poder parar ou continuar o seu movimento.
Para um "floor" ou um "stair", o movimento acaba, para uma célula "vazia" o movimento continua.*/
fun Man.stepsDY(game: Game):Man{
    val novoDY = if(vel.dy == INITIAL_JUMPING_SPEED) vel.dy else vel.dy+JUMPING_SPEED//vel.dy + if (vel.dy < INITIAL_JUMPING_SPEED) JUMPING_SPEED else 0
    val proxPos = (pos + vel.copy(dy = novoDY)).toCell()
    val CheckNextCel = if (vel.dx > 0) proxPos+Direction.RIGHT else proxPos
    if (CheckNextCel in game.floor){
        val novoDX = -vel.dx
        val newVel = Speed(novoDX, 1)
        val newPos = (pos + newVel).limitToArea(MAX_X, MAX_Y)
        return copy(pos = newPos, vel = newVel, salto= false)
    }
    val novaPos = (pos + vel.copy(dy=novoDY)).limitToArea(MAX_X, MAX_Y)
    val CheckCel = novaPos.copy(y=novaPos.y+CELL_HEIGHT).toCell()
    if (queda(CheckCel, game) && vel.dy>0) {
        val novoy = CheckCel.toPoint().y - CELL_HEIGHT
        return copy(pos = novaPos.copy(y = novoy), salto = false, vel = vel.copy(dy = 0))
    }
    return copy(pos = novaPos, vel = vel.copy(dy=novoDY))
}

//A função "queda" é encarregue de fazer com que o Homem caia caso durante o seu movimento não sejam detetados "floor" ou "stairs"
fun queda(celula: Cell, game: Game) = celula in game.floor || (celula in game.stairs && (celula+Direction.RIGHT in game.floor || celula+Direction.LEFT in game.floor))

//Dá indicação ao Homem se ele pode andar ou saltar
fun Man.doSteps(game: Game) =
    if (salto) stepsDY(game)
    else stepsDX(game)

