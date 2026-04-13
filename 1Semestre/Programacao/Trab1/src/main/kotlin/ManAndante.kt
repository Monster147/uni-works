import pt.isel.canvas.Canvas

data class Point(val x: Int, val y: Int)

operator fun Point.plus(p: Point): Point = Point(this.x+p.x, y+p.y )

operator fun Point.minus(p: Point) =  Point(x-p.x, y-p.y )

fun Point.delta(dx:Int)= Point(x/*+dx*/,y)

fun Man.process(i:Int): Man { // Esta função faz com que o homem se mexa para direita ou para a esquerda. Em que cada movimento o homem tem o estado em 1.
    return if(i==1 && this.state == 0)
        Man(this.pos.delta(-DX),i,dx,state=1, dy= dy, salto=false)
    else
        if(i==-1  && this.state == 0) Man(this.pos.delta(DX), i, dx,state=1,dy= dy, salto=false)
        else Man(this.pos,this.direction, this.dx, this.state,dy=this.dy, salto=false)
}

fun Canvas.drawManMoving(man: Man) { // Esta função faz com que o ele desenhe o homem segundo a sua direção, ou seja, se for -1 desenha o homem virado para a direita, se for 1, desenha-o para esquerda
    erase()
    drawGridLines()
    drawBaseFloor()
    if (man.direction==-1) drawImage("manR", man.pos.x,man.pos.y,  CELL_WIDTH,  CELL_HEIGHT *2)
    else drawImage("manL", man.pos.x,man.pos.y,  CELL_WIDTH,  CELL_HEIGHT *2)
}

fun Man.moveprocess(dir:Int): Man { // Esta função é a função que vai manter o movimento do homem dentro dos limites da arena
    return if (dir==1 && this.pos.x - CELL_WIDTH <0 ) this
    else if(dir==-1 && this.pos.x + CELL_WIDTH > 9* CELL_WIDTH ) this else this.process(dir)
}