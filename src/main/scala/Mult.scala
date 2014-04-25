package ChiselFloat

import Chisel._
import java.lang.Float.floatToIntBits
import java.lang.Double.doubleToLongBits

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

    val sign_reg = Reg(next = stage1_sign)
    val exponent_reg = Reg(next = stage1_exponent)
    val mantissa_reg = Reg(next = stage1_mantissa)

    val stage2_sign = sign_reg
    val stage2_exponent = UInt(width = a_wrap.exponent.width)
    val stage2_mantissa = UInt(width = a_wrap.mantissa.width - 1)

    n match {
        case 32 => {
            when (mantissa_reg(47) === Bits(1)) {
                stage2_exponent := exponent_reg - UInt(126)
                stage2_mantissa := mantissa_reg(46, 24)
            } .otherwise {
                stage2_exponent := exponent_reg - UInt(127)
                stage2_mantissa := mantissa_reg(45, 23)
            }
        }
        case 64 => {
            when (mantissa_reg(105) === Bits(1)) {
                stage2_exponent := exponent_reg - UInt(1022)
                stage2_mantissa := mantissa_reg(104, 53)
            } .otherwise {
                stage2_exponent := exponent_reg - UInt(1023)
                stage2_mantissa := mantissa_reg(103, 52)
            }
        }
    }

    io.res := Cat(stage2_sign.toBits(),
                  stage2_exponent.toBits(),
                  stage2_mantissa.toBits())
}

class FPMult32Test(c: FPMult) extends Tester(c) {
    var lastExpected = 0.0f

    for (i <- 0 until 8) {
        val a = rnd.nextFloat()
        val b = rnd.nextFloat()
        val expected = a * b

        poke(c.io.a, floatToIntBits(a))
        poke(c.io.b, floatToIntBits(b))
        step(1)

        if (i > 0) {
            expect(c.io.res, floatToIntBits(lastExpected))
        }
        lastExpected = expected
    }

    step(1)

    expect(c.io.res, floatToIntBits(lastExpected))
}
