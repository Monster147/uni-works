import isel.leic.UsbPort

object HAL{// Virtualiza o acesso ao sistema UsbPort
var lastOutput = 0
    // Inicia a classe
    fun init() = UsbPort.write(lastOutput)
    // Retorna true se o bit tiver o valor lógico ‘1’
    fun isBit(mask: Int): Boolean = UsbPort.read().and(mask) != 0
    // Retorna os valores dos bits representados por mask presentes no UsbPort
    fun readBits(mask: Int): Int = UsbPort.read().and(mask)
    // Coloca os bits representados por mask no valor lógico ‘1’
    fun setBits(mask:Int){
        val op =(lastOutput.or(mask))
        UsbPort.write(op)
        lastOutput = op
    }
    // Coloca os bits representados por mask no valor lógico ‘0’
    fun clrBits(mask:Int){
        val op = (lastOutput).and((mask).inv())
        UsbPort.write(op)
        lastOutput = op
    }
    // Escreve nos bits representados por mask os valores dos bits correspondentes em value
    fun writeBits(mask: Int,value:Int){
        val newMask = value and mask
        val clear = (lastOutput).and(mask.inv())
        val write = clear.or(newMask)
        UsbPort.write(write)
        lastOutput = write
    }
}

fun main(){
    HAL.init()
    while (true){
        println(HAL.readBits(0xFF))
    }
}