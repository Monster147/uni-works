import pt.isel.canvas.*

private const val FRAME_TIME = 1000 // [milliseconds]
const val MAN_STEP = CELL_WIDTH
const val DX = CELL_WIDTH/6
const val DY = CELL_HEIGHT/SPRITE_HEIGHT

/*Na data class Man temos definido: o ponto onde ele se encontra, a sua direção que varia entre 1 quando está virado para
  a esquerda, -1 quando está virado para a direita e 0 quando está no salto, o valor da sua velocidade horizontal,
  a quantidade de passos que ele dá, o seu estado que varia entre 0 quando está parado, 1 quando se movimenta e
  2 quando está a saltar, o dy que representa a sua velocidade horizontal e a verificação para saber se está no salto ou não*/
data class Man(val pos: Point, val direction:Int, val dx:Int,val steps:Int=1, val state:Int=0, val dy:Int, val salto:Boolean)

fun Canvas.draw(man: Man){ // Esta função apaga a arena e, de seguida, desenha a arena completa com o chão e com as células, juntamente com o homem
    erase()
    drawManMoving(man)
    drawGridLines()
    drawBaseFloor()
}

fun main() {
    onStart {
        val arena = createArena()
        val x =(GRID_WIDTH * CELL_WIDTH)/2 // in (0 .. (GRID_WIDTH * CELL_WIDTH))
        val y = (((GRID_HEIGHT * CELL_HEIGHT)/2)+ CELL_HEIGHT)
        val pos = Point(x,y) // Utiliza os valores de x e y acima para ditar a posição original do homem
        val dx = DX // Definição do que será a velocidade horizontal
        val dy = DY // Definição do que será a velocidade vertical
        var homem = Man(pos, 1, dx, state = 0, dy = dy, salto=false)
        /* A variável homem utiliza a data class Man que tem como posição o valor de pos, a sua direção que é 1 (o homem
        inicialmente está virado para a esquerda), o seu dx assume o valor de dx, o estado incial dele é 0, ou seja, quando está parado, o seu dy
        assume o valor de dy e incialmente o salto assume o valor de false visto que ele está parado inicialmente */
        arena.draw(homem) // Aqui é chamada a função draw com a variável do homem, para desenhar a arena completa e o homem usando a variavel homem
        arena.onKeyPressed{ ke ->
            if (ke.code == 'Q'.code) arena.close() // Quando a tecla Q é pressionada o programa termina
            else {
                if (homem.state == 0) {
                    val dir = if (ke.code == pt.isel.canvas.LEFT_CODE) { // Quando pressionada a seta para a esquerda a direção muda para 1
                        1
                    } else {
                        if (ke.code == pt.isel.canvas.RIGHT_CODE) { // Quando pressionada a seta para a direita a direção muda para -1
                            -1
                        } else {
                            4
                        }
                    }
                    homem = homem.moveprocess(dir) //Chama a função moveprocess que previne o homem de andar para além das bordas da arena
                    if (homem.salto == false && ke.code == 32) { // Quando pressionada a barra de espaços e se o valor para o salto for false, então ele faz outro homem em que o seu state é 2 e o valor para o salto é true
                        homem=homem.copy(salto=true, state=2)
                    }
                    else{}
                }
            }
            //  arena.drawManMoving(homem)
        }
        arena.onTimeProgress(30){
            /* Este if e else dizem respeito ao movimento do homem com as imagens intermédias durante o seu movimento.
               Se ele detetar que a direção do homem é 1, ou seja, para a esquerda e o seu estado é 1, ou seja, ele está em movimento, então
               ele vai fazendo cópias do homem segundo os passos que ele dá, neste caso são 6 passos logo 6 imagens. O else funciona
               desta maneira mas só que para a direita, ou seja, quando a direção do homem é -1 */
            if (homem.direction == 1 && homem.steps<=6 && homem.state == 1) {
                homem = homem.copy(pos = homem.pos - Point(DX, 0), steps=homem.steps + 1)
                if (homem.steps==7) homem=homem.copy(state=0)
            }
            else{
                if (homem.direction == -1 && homem.steps<=6 && homem.state == 1){
                    homem = homem.copy(pos = homem.pos - Point(-DX, 0), steps=homem.steps + 1)
                    if (homem.steps==7) homem=homem.copy(state=0)
                }
            }
            /*Se ele detetar que a direção do homem é 1 (para a esquerda) e o seu estado é 2, ou seja, a realizar um salto, então
               ele vai para cima. Por outras palavras, ele retira um y, que neste caso é o DY, para fazer o tal salto. Para realizar o movimento para baixo
               decidimos implementar os mesmos passos para o seu movimento para a esquerda e para a direita. Neste caso, fizemos com que ele desse
               no máximo 96 passos, sendo que de 0 a 32 ele faz o salto para cima e de 33 a 96 faz o movimento para baixo. No passo 96, o código
               faz uma cópia do homem, mas só que com o estado em 0, ou seja, parado. Neste caso ele faz um salto virado para a esquerda.
               O else funciona desta maneira, mas só que para a direita, ou seja, quando a direção do homem for -1, fazendo um salto virado para a direita*/
            if (homem.direction == 1 && homem.state == 2 && homem.salto == true && homem.steps<=95) {
                homem = homem.copy(pos = homem.pos + Point(0, -DY), steps=homem.steps + 1)
                if (homem.steps >= 33)
                    homem = homem.copy(pos= homem.pos + Point(0, 2*DY), steps=homem.steps + 1)
                if (homem.steps == 96)
                    homem = homem.copy(state=0)
            }
            else {
                if (homem.direction == -1 && homem.state == 2 && homem.salto == true && homem.steps <= 95) {
                    homem = homem.copy(pos = homem.pos + Point(0, -DY), steps = homem.steps + 1)
                    if (homem.steps >= 33)
                        homem = homem.copy(pos = homem.pos + Point(0, 2 * DY), steps = homem.steps + 1)
                    if (homem.steps == 96)
                        homem = homem.copy(state = 0)
                }
            }
            arena.draw(homem) //Aqui o código chama a função draw que desenhará na arena: o chão, as linhas da grelha e o homem, utilizado a variável homem
        }
    }
    onFinish { println("Bye.") }
}