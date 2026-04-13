import isel.leic.utils.Time
import java.io.Serial

object ScoreDisplay { // Controla o mostrador de pontuação.
    // Inicia a classe, estabelecendo os valores iniciais.
    fun init(){
        off(true)
    }

    // Envia comando para atualizar o valor do mostrador de pontuação
    fun setScore(value: Int) {
       // SerialEmitter.send(SerialEmitter.Destination.SCORE, value, 7)
        val valueString = value.toString().reversed()
        for(i in valueString.indices){
            val data = i + valueString[i].code.shl(3)
            SerialEmitter.send(SerialEmitter.Destination.SCORE,data,7)
        }
        SerialEmitter.send(SerialEmitter.Destination.SCORE,0x06,7)
    }

    // Envia comando para desativar/ativar a visualização do mostrador de pontuação
    fun off(value: Boolean) {
        if (value) SerialEmitter.send(SerialEmitter.Destination.SCORE, 0x0F, 7)
        else SerialEmitter.send(SerialEmitter.Destination.SCORE, 0x07, 7)
    }

    // Envia para o cada dígito do Score o número 0
    fun zero(){
        SerialEmitter.send(SerialEmitter.Destination.SCORE, 0x00, 7)
        SerialEmitter.send(SerialEmitter.Destination.SCORE, 0x01, 7)
        SerialEmitter.send(SerialEmitter.Destination.SCORE, 0x02, 7)
        SerialEmitter.send(SerialEmitter.Destination.SCORE, 0x03, 7)
        SerialEmitter.send(SerialEmitter.Destination.SCORE, 0x04, 7)
        SerialEmitter.send(SerialEmitter.Destination.SCORE, 0x05, 7)
    }

}
fun main(){
    ScoreDisplay.init()
    ScoreDisplay.off((true))
    ScoreDisplay.off(false)
    //ScoreDisplay.setScore(6)
    //ScoreDisplay.setScore(9)
    //ScoreDisplay.setScore(99999)
    //ScoreDisplay.setScore(111111)
    //ScoreDisplay.setScore(1234)
    //ScoreDisplay.zero()
    //ScoreDisplay.off(true)
    //ScoreDisplay.off(false)
}