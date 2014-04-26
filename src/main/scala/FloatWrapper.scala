package ChiselFloat

import Chisel._

// Wraps a Chisel Flo or Dbl datatype to allow easy
// extraction of the different parts (sign, exponent, mantissa)

class FloatWrapper(val num: Bits) {
    val (sign, exponent, mantissa, zero) = num.width match {
        case 32 => (num(31).toBool(),
                    num(30, 23).toUInt(),
                    Cat(Bits(1, 1), num(22, 0).toUInt()),
                    num(30, 0).toUInt() === UInt(0))
        case 64 => (num(63).toBool(),
                    num(62, 52).toUInt(),
                    Cat(Bits(1, 1), num(51, 0).toUInt()),
                    num(62, 0).toUInt() === UInt(0))
    }
}
