import isel.leic.UsbPort
import isel.leic.utils.Time
import java.security.Key

object KBD { // Ler teclas. Métodos retornam ‘0’..’9’,’#’,’*’ ou NONE.
    const val NONE = 0;
    const val DVAL_MASK = 0x10
    const val KACK_MASK = 0x80
    val MASK = 0x0F
    val ARRAY = arrayListOf('1', '4', '7', '*',
        '2', '5', '8', '0',
        '3', '6', '9', '#')
    var KEY_PRESSED = false

    // Inicia a classe
    fun init() {
        var KEY_PRESSED = HAL.isBit(DVAL_MASK)
    }

    // Retorna de imediato a tecla premida ou NONE se não há tecla premida.
    fun getKey(): Char{
        if(HAL.isBit(DVAL_MASK)){
            //if(HAL.isBit(DVAL_MASK) || HAL.isBit(!DVAL_MASK))
            //
            val key = ARRAY[HAL.readBits(MASK).xor(0b0101)]
            HAL.setBits(KACK_MASK)
            while(HAL.isBit(DVAL_MASK)){
                Time.sleep(1)
            }
            HAL.clrBits(KACK_MASK)
            return key
        }
        return NONE.toChar()
    }

    // Retorna a tecla premida, caso ocorra antes do ‘timeout’ (representado em milissegundos), ou NONE caso contrário.
    fun waitKey(timeout: Long): Char {
        val START = Time.getTimeInMillis()
        while (Time.getTimeInMillis() - START < timeout) {
            val key = getKey()
            if (key != NONE.toChar()) return key
        }
        return NONE.toChar()
    }
}
fun main(){
    HAL.init()
    KBD.init()
    while(true){
        var k = KBD.getKey()
        if (k != KBD.NONE.toChar()) println(k)
    }
    //println(KBD.waitKey(16000))
    //println(KBD.waitKey(3000))
    //println(KBD.waitKey(10000))
    //println(KBD.waitKey(1000))
    /*while (true){
        val k = KBD.getKey()
        if( k != KBD.NONE.toChar()) print(k)
    }*/
}