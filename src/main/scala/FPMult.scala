package ChiselFloat

import Chisel._
import FloatUtils.{floatToBigInt, doubleToBigInt}

class MantissaRounder(val n: Int) extends Module {
    val io = new Bundle {
        val in = UInt(INPUT, n)
        val out = UInt(OUTPUT, n - 1)
    }

    io.out := io.in(n - 1, 1) + io.in(0)
}

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
    val stage2_exponent = UInt(width = a_wrap.exponent.getWidth)
    val stage2_mantissa = UInt(width = a_wrap.mantissa.getWidth - 1)

    val (mantissaLead, mantissaSize, exponentSize, exponentSub) = n match {
        case 32 => (47, 23, 8, 127)
        case 64 => (105, 52, 11, 1023)
    }

    val rounder = Module(new MantissaRounder(mantissaSize + 1))

    when (zero_reg) {
        stage2_exponent := UInt(0, exponentSize)
        rounder.io.in := UInt(0, mantissaSize + 1)
    } .elsewhen (mantissa_reg(mantissaLead) === Bits(1)) {
        stage2_exponent := exponent_reg - UInt(exponentSub - 1)
        rounder.io.in := mantissa_reg(mantissaLead - 1,
                                      mantissaLead - mantissaSize - 1)
    } .otherwise {
        stage2_exponent := exponent_reg - UInt(exponentSub)
        rounder.io.in := mantissa_reg(mantissaLead - 2,
                                      mantissaLead - mantissaSize - 2)
    }

    stage2_mantissa := rounder.io.out

    io.res := Cat(stage2_sign.toBits(),
                  stage2_exponent.toBits(),
                  stage2_mantissa.toBits())
}

class FPMult32 extends FPMult(32) {}
class FPMult64 extends FPMult(64) {}

class FPMult32Test(c: FPMult32) extends Tester(c) {
    var lastExpected = 0.0f

    poke(c.io.a, floatToBigInt(0.0f))
    poke(c.io.b, floatToBigInt(3.0f))
    step(1)

    for (i <- 0 until 8) {
        val a = rnd.nextFloat() * 10000.0f - 5000.0f
        val b = rnd.nextFloat() * 10000.0f - 5000.0f
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

class FPMult64Test(c: FPMult64) extends Tester(c) {
    var lastExpected = 0.0

    for (i <- 0 until 8) {
        val a = rnd.nextDouble() * 10000.0 - 5000.0
        val b = rnd.nextDouble() * 10000.0 - 5000.0
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
