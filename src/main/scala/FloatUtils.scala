package ChiselFloat

import java.lang.Float.floatToRawIntBits
import java.lang.Double.doubleToRawLongBits
import java.math.BigInteger

object FloatUtils {
    def floatToBigInt(x: Float): BigInt = {
        val integer = floatToRawIntBits(x)
        var byte_array = new Array[Byte](5)

        byte_array(0) = 0

        for (i <- 1 to 4) {
            byte_array(i) = ((integer >> ((4 - i) * 8)) & 0xff).toByte
        }

        BigInt(new BigInteger(byte_array))
    }

    def doubleToBigInt(x: Double): BigInt = {
        val integer = doubleToRawLongBits(x)
        var byte_array = new Array[Byte](9)

        byte_array(0) = 0

        for (i <- 1 to 8) {
            byte_array(i) = ((integer >> ((8 - i) * 8)) & 0xff).toByte
        }

        BigInt(new BigInteger(byte_array))
    }

    def getExpMantWidths(n: Int): (Int, Int) = {
        n match {
            case 32 => (8, 23)
            case 64 => (11, 52)
        }
    }
}
