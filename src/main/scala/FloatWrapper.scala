package ChiselFloat

import Chisel._

// Wraps a Chisel Flo or Dbl datatype to allow easy
// extraction of the different parts (sign, exponent, mantissa)

class FloatWrapper(val num: Bits) {
    val (sign, exponent, mantissa) = num.width match {
        case 32 => (num(31).toBool(),
                    num(30, 23).toUInt(),
                    Cat(Bits(1, 1), num(22, 0).toUInt()))
        case 64 => (num(63).toBool(),
                    num(62, 52).toUInt(),
                    Cat(Bits(1, 1), num(51, 0).toUInt()))
    }
}
