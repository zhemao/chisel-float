package ChiselFloat

import Chisel._
import java.lang.Float.floatToRawIntBits
import java.lang.Double.doubleToRawLongBits
import java.math.BigInteger

class FPMult(val n: Int) extends Module {
    val io = new Bundle {
        val a = Bits(INPUT, n)
        val b = Bits(INPUT, n)
        val res = Bits(OUTPUT, n)
    }

    val a_wrap = new FloatWrapper(io.a)
    val b_wrap = new FloatWrapper(io.b)

    val stage1_sign = a_wrap.sign ^ b_wrap.sign
    val stage1_exponent = a_wrap.exponent + b_wrap.exponent
    val stage1_mantissa = a_wrap.mantissa * b_wrap.mantissa
    val stage1_zero = a_wrap.zero || b_wrap.zero

    val sign_reg = Reg(next = stage1_sign)
    val exponent_reg = Reg(next = stage1_exponent)
    val mantissa_reg = Reg(next = stage1_mantissa)
    val zero_reg = Reg(next = stage1_zero)

    val stage2_sign = sign_reg
    val stage2_exponent = UInt(width = a_wrap.exponent.width)
    val stage2_mantissa = UInt(width = a_wrap.mantissa.width - 1)

    val (mantissaLead, mantissaSize, exponentSize, exponentSub) = n match {
        case 32 => (47, 23, 8, 127)
        case 64 => (105, 52, 11, 1023)
    }

    when (zero_reg) {
        stage2_exponent := UInt(0, exponentSize)
        stage2_mantissa := UInt(0, mantissaSize)
    } .elsewhen (mantissa_reg(mantissaLead) === Bits(1)) {
        stage2_exponent := exponent_reg - UInt(exponentSub - 1)
        stage2_mantissa := mantissa_reg(mantissaLead - 1,
                                        mantissaLead - mantissaSize)
    } .otherwise {
        stage2_exponent := exponent_reg - UInt(exponentSub)
        stage2_mantissa := mantissa_reg(mantissaLead - 2,
                                        mantissaLead - mantissaSize - 1)
    }

    io.res := Cat(stage2_sign.toBits(),
                  stage2_exponent.toBits(),
                  stage2_mantissa.toBits())
}

class FPMult32Test(c: FPMult) extends Tester(c) {
    def floatToBigInt(x: Float): BigInt = {
        val integer = floatToRawIntBits(x)
        var byte_array = new Array[Byte](5)

        byte_array(0) = 0

        for (i <- 1 to 4) {
            byte_array(i) = ((integer >> ((4 - i) * 8)) & 0xff).toByte
        }

        BigInt(new BigInteger(byte_array))
    }

    var lastExpected = 0.0f

    poke(c.io.a, floatToRawIntBits(0.0f))
    poke(c.io.b, floatToRawIntBits(3.0f))
    step(1)

    for (i <- 0 until 8) {
        val a = rnd.nextFloat() * 10000000.0f - 5000000.0f
        val b = rnd.nextFloat() * 10000000.0f - 5000000.0f
        val expected = a * b

        poke(c.io.a, floatToBigInt(a))
        poke(c.io.b, floatToBigInt(b))
        step(1)

        println(s"Expecting $lastExpected or ${floatToBigInt(lastExpected)}")

        expect(c.io.res, floatToBigInt(lastExpected))
        lastExpected = expected
    }

    step(1)

    expect(c.io.res, floatToBigInt(lastExpected))
}

class FPMult64Test(c: FPMult) extends Tester(c) {
    def doubleToBigInt(x: Double): BigInt = {
        val integer = doubleToRawLongBits(x)
        var byte_array = new Array[Byte](9)

        byte_array(0) = 0

        for (i <- 1 to 8) {
            byte_array(i) = ((integer >> ((8 - i) * 8)) & 0xff).toByte
        }

        BigInt(new BigInteger(byte_array))
    }

    var lastExpected = 0.0

    for (i <- 0 until 8) {
        val a = rnd.nextDouble() * 10000000.0 - 5000000.0
        val b = rnd.nextDouble() * 10000000.0 - 5000000.0
        val expected = a * b

        poke(c.io.a, doubleToBigInt(a))
        poke(c.io.b, doubleToBigInt(b))
        step(1)

        if (i > 0) {
            expect(c.io.res, doubleToBigInt(lastExpected))
        }
        lastExpected = expected
    }

    step(1)

    expect(c.io.res, doubleToBigInt(lastExpected))
}
