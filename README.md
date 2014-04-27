# Floating Point Chisel Modules

This library implements pipelined floating point arithmetic units in the
[Chisel](https://chisel.eecs.berkeley.edu/) hardware description language.
They can be used as third-party IP blocks in your Chisel or Verilog hardware
descriptions.

There are modules for floating point addition and multiplication.
Both single-precision and double-precision numbers are supported.

Classes

 * FPMult32, FPMult64 - Single-precision and double-precision multipliers with
   single-cycle pipeline latency
 * FPAdd32, FPAdd64 - Single-precision and double-precision adders with
   three cycle pipeline latency

Note: These modules have not been thoroughly verified. Though the basic
functionality is known to work, there may be corner cases which are not
handled properly. In particular, there is no support for NaN, infinity, or
denormalized numbers other than zero. There is also no exponent overflow or
underflow detection. If you care about these things, do not use these modules.
