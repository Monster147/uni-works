import isel.leic.utils.Time
import kotlin.math.E

object LCD { // Escreve no LCD usando a interface a 8 bits.
    private const val LINES = 2
    private const val COLS = 16 // Dimensão do display.
    private const val SERIAL_INTERFACE = true // Define se a interface é Série ou Paralela
    private const val E_MASK = 0x20
    private const val RS_MASK = 0x40
    private const val CLK_REG_MASK = 0x10
    private const val DATA_MASK = 0x0F
    const val DEBUG = false

    // Escreve um byte de comando/dados no LCD em paralelo
        private fun writeByteParallel(rs: Boolean, data: Int) {
        if (rs) HAL.setBits(RS_MASK) else HAL.clrBits(RS_MASK)
        val hi = data.shr(4)
        HAL.writeBits(DATA_MASK, hi)
        HAL.clrBits(CLK_REG_MASK)
        HAL.setBits(CLK_REG_MASK)
        HAL.writeBits(DATA_MASK, data)
        HAL.setBits(E_MASK)
        HAL.clrBits(CLK_REG_MASK)
        HAL.setBits(CLK_REG_MASK)
        HAL.clrBits(E_MASK)
    }

    // Escreve um byte de comando/dados no LCD em série
     private fun writeByteSerial(rs: Boolean, data: Int){
        if (rs) {
            SerialEmitter.send(SerialEmitter.Destination.LCD,1 + data.shl(1),9)}
        else{
            SerialEmitter.send(SerialEmitter.Destination.LCD, 0 + data.shl(1),9)}
}
    // Escreve um byte de comando/dados no LCD
    private fun writeByte(rs: Boolean, data: Int){
        if(SERIAL_INTERFACE) writeByteSerial(rs, data)
        else writeByteParallel(rs, data)
    }

    // Escreve um comando no LCD
    private fun writeCMD(data: Int){
        writeByte(false, data)
    } // rs = 0

    // Escreve um dado no LCD
    private fun writeDATA(data: Int){
        writeByte(true, data)
    } // rs = 1

    // Envia a sequência de iniciação para comunicação a 8 bits.
    fun init() {
        Time.sleep(20)
        writeCMD(0x30)
        Time.sleep(5)
        writeCMD(0x30)
        Time.sleep(1)
        writeCMD(0x30)
        writeCMD(0x38)
        writeCMD(0x08)
        writeCMD(0x01)
        Time.sleep(2)
        writeCMD(0x06)
        writeCMD(0x0E)
    }

    // Escreve um caráter na posição corrente.
    fun write(c: Char){
        if(DEBUG) print(c)
        writeDATA(c.code)
    }

    // Escreve uma string na posição corrente.
    fun write(text: String){
        for(i in 0 .. text.length-1){
            val car = text.get(i)
            if(DEBUG) println(car)
            write(car)
        }
    }
    // Envia comando para posicionar cursor (‘line’:0..LINES-1 , ‘column’:0..COLS-1)
    fun cursor(line: Int, column: Int){
        if(column in 0 .. COLS-1){
            var adress = column
            if(line in 0 .. LINES - 1 && line==1) adress += 0x40
            writeCMD(0x80 or adress)
        }
    }

    // Envia comando para limpar o ecrã e posicionar o cursor em (0,0)
    fun clear(){
        writeCMD(0x01)
    }
}

fun main(){
    HAL.init()
    SerialEmitter.init()
    LCD.init()
    //LCD.writeByteParallel(true, 0x34) //DATA = 00110100
    //LCD.writeByteParallel(true, 0x73) //DATA = 01110011
    //LCD.writeByteParallel(true, 0xA4) //DATA = 10100100
    //LCD.writeByteParallel(false, 0xB8) //DATA = 10111000
    //LCD.write('A')
    //LCD.write('l')
    //LCD.write('N')
    //LCD.write('e')
    //LCD.write("fe142318aq")
    //LCD.write("48746133fgrb")
    //LCD.write("BR2A3SG8R4GK6U")
    //LCD.write("10010")
    //LCD.write("       ")
    //LCD.write("|______-__|")
    //LCD.cursor(1,2)
    //LCD.write('a')
    //LCD.cursor(0,3)
    //LCD.write("VD3VD9WA8ER")
    //LCD.clear()
    //LCD.cursor(1, 18)
    //LCD.cursor(4, 6)
    //LCD.cursor(3, 32)
}