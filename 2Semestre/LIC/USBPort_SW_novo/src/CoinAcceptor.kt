import isel.leic.utils.Time
//Responsável por ler a colocação das moedas para permitir ao utilizador jogar.
object CoinAcceptor {
    private const val COIN_MASK = 0x40
    private const val ACCEPT_MASK = 0x40
    //Inicia a classe
    fun init() {
        acceptCoin()
    }
    //Lê a entrada coin, se o botão for premido (COIN_MASK) permite que o jogo comece
    /*fun acceptCoin():Int com os return documentados permitem ver que a função funciona quando a função é
    submetida a teste.*/
    fun acceptCoin():Boolean{ //fun acceptCoin():Int
        if (HAL.isBit(COIN_MASK)) {
            Time.sleep(10)
            HAL.setBits(ACCEPT_MASK)
            while (HAL.isBit(COIN_MASK)) {
                Time.sleep(1)
            }
            HAL.clrBits(ACCEPT_MASK)
           return true//1
        }
       return false //0
    }
}

fun main() {
    CoinAcceptor.init()
    while(true) {
        println(CoinAcceptor.acceptCoin())
        Time.sleep(1000)
    }
}