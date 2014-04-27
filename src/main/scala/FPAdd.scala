package ChiselFloat

import Chisel._

class SatLeftShift(val m: Int, val n: Int) extends Module {
    val io = new Bundle {
        val shiftin = UInt(INPUT, m)
        val shiftby = UInt(INPUT, n)
        val shiftout = UInt(OUTPUT, m)
    }

    io.shiftout := Mux(io.shiftby > UInt(m), UInt(0), io.shiftin >> io.shiftby)
}

class FPAdd(val n: Int) extends Module {
    val io = new Bundle {
        val a = Bits(INPUT, n)
        val b = Bits(INPUT, n)
        val res = Bits(OUTPUT, n)
    }

    val a_wrap = new FloatWrapper(io.a)
    val b_wrap = new FloatWrapper(io.b)

    val exp_diff = a_wrap.exponent - b_wrap.exponent
    val b_larger = Reg(Bool())
    val mant_shift = Reg(UInt(width = a_wrap.exponent.width))
    val stage1_exp = Reg(UInt(width = a_wrap.exponent.width))
    val stage1_manta = Reg(next = a_wrap.mantissa)
    val stage1_mantb = Reg(next = b_wrap.mantissa)
    val stage1_sign = Reg(Bool())
    val stage1_sub = Reg(next = (a_wrap.sign ^ b_wrap.sign))

    // In stage 1, we subtract the exponents
    // This will tell us which number is larger
    // as well as what we need to shift the smaller mantissa by

    // b is larger
    when (exp_diff(n - 1) === UInt(1)) {
        // absolute value
        mant_shift := -exp_diff
        //mant_shift := (~exp_diff) + UInt(1)
        b_larger := Bool(true)
        stage1_exp := b_wrap.exponent
        stage1_sign := b_wrap.sign
    } .otherwise {
        mant_shift := exp_diff
        b_larger := Bool(false)
        stage1_exp := a_wrap.exponent
        stage1_sign := a_wrap.sign
    }

    val larger_mant = UInt(width = a_wrap.mantissa.width)
    val smaller_mant = UInt(width = a_wrap.mantissa.width)

    when (b_larger) {
        larger_mant := stage1_mantb
        smaller_mant := stage1_manta
    } .otherwise {
        larger_mant := stage1_manta
        smaller_mant := stage1_mantb
    }

    val shifter = Module(new SatLeftShift(larger_mant.width, mant_shift.width))
    shifter.io.shiftin := smaller_mant
    shifter.io.shiftby := mant_shift

    val shifted_mant = shifter.io.shiftout

    // add one bit so that we can detect negatives / overflow
    val stage2_manta = Reg(next = Cat(UInt(0, 1), larger_mant))
    val stage2_mantb = Reg(next = Cat(UInt(0, 1), shifted_mant))
    val stage2_sign = Reg(next = stage1_sign)
    val stage2_sub = Reg(next = stage1_sub)
    val stage2_exp = Reg(next = stage1_exp)

    // in stage 2 we subtract or add the mantissas
    // we must also detect overflows and adjust sign/exponent appropriately

    val mant_sum = Reg(UInt(width = stage2_manta.width))

    when (stage2_sub) {
        mant_sum := stage2_manta - stage2_mantb
    } .otherwise {
        mant_sum := stage2_manta + stage2_mantb
    }

    // here we drop the overflow bit
    val stage3_mant_sum = Reg(UInt(width = mant_sum.width - 1))
    val stage3_sign = Reg(Bool())
    val stage3_exp = Reg(UInt(width = stage2_exp.width))

    // this may happen if the operands were of opposite sign
    // but had the same exponent
    when (mant_sum(mant_sum.width - 1) === UInt(1)) {
        when (stage2_sub) {
            stage3_mant_sum := -mant_sum(mant_sum.width - 2, 0)
            stage3_sign := !stage2_sign
            stage3_exp := stage2_exp
        } .otherwise {
            // if the sum overflowed, we need to shift back by one
            // and increment the exponent
            stage3_mant_sum := mant_sum(mant_sum.width - 1, 1)
            stage3_exp := stage2_exp + UInt(1)
            stage3_sign := stage2_sign
        }
    } .otherwise {
        stage3_mant_sum := mant_sum(mant_sum.width - 2, 0)
        stage3_sign := stage2_sign
        stage3_exp := stage2_exp
    }

    // finally in stage 3 we normalize mantissa and exponent
    // we need to reverse the sum, since we want the find the most
    // significant 1 instead of the least significant 1
    val norm_shift = PriorityEncoder(Reverse(stage3_mant_sum))
    val res_mant = (stage3_mant_sum >> norm_shift)(stage3_mant_sum.width - 2, 0)
    val res_exp = stage3_exp - norm_shift

    io.res := Cat(stage3_sign, res_exp, res_mant)
}
