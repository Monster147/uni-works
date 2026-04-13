import isel.leic.utils.Time
//Classe responsável pelo modo de manutenção do jogo
object Maintenance{
   private const val MOUT_MASK = 0x80
    //Inicia a classe
    fun init(){
        maintenanceOn()
    }
    //Ativa o modo manutenção
    //O que está documentado é para ajudar na validação dos testes
    fun maintenanceOn():Boolean{//fun maintenanceOn():Int
        if (HAL.isBit(MOUT_MASK)) {
            Time.sleep(1)
            return true
           // return 1
        }
        return false
      // return 0
    }
}
fun main(){
    Maintenance.init()
    while(true) {
        println(Maintenance.maintenanceOn())
        Time.sleep(1000)
    }
}