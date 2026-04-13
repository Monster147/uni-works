object SerialEmitter { // Envia tramas para os diferentes módulos Serial Receiver.
        enum class Destination { LCD, SCORE }

        private var LCDSEL_MASK = 0x01
        private val SLCK_MASK = 0x10
        private var SDX_MASK = 0x08
        private val SC_SET = 0x02
        private var NOTSS_MASK = 0

        // Inicia a classe
        fun init() {
            HAL.setBits(LCDSEL_MASK)
            HAL.setBits(SC_SET)
        }

        // Envia uma trama para o SerialReceiver identificado o destino em addr,os bits de dados em
        // ‘data’ e em size o número de bits a enviar.
        fun send(addr: Destination, data: Int, size: Int) {
            var COUNT = 0
           if(addr == Destination.LCD){NOTSS_MASK = LCDSEL_MASK}
           else{NOTSS_MASK = SC_SET}
           HAL.clrBits(NOTSS_MASK)
            for (i in 0 until size){
                val bit = data.and(1.shl(i))
                if(bit == 0) HAL.clrBits(SDX_MASK)
                else{
                    HAL.setBits(SDX_MASK)
                    COUNT++
                }
                HAL.setBits(SLCK_MASK)
                HAL.clrBits(SLCK_MASK)
            }
            if(COUNT%2 == 0) HAL.clrBits(SDX_MASK) else HAL.setBits(SDX_MASK)
            HAL.setBits(SLCK_MASK)
            HAL.clrBits(SLCK_MASK)
            HAL.setBits(NOTSS_MASK)
        }
}
fun main(){
        SerialEmitter.init()
        SerialEmitter.send(SerialEmitter.Destination.LCD,0x001,9)
        //SerialEmitter.send(SerialEmitter.Destination.LCD,0x1FF,9)
        //SerialEmitter.send(SerialEmitter.Destination.LCD,0x000,9)
}