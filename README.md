# Floating Point Chisel Modules

This library implements pipelined floating point arithmetic units in the
[Chisel](https://chisel.eecs.berkeley.edu/) hardware description language.
They can be used as third-party IP blocks in your Chisel or Verilog hardware
descriptions.

There are modules for floating point addition and multiplication.
Both single-precision and double-precision numbers are supported.

## Classes

 * FPMult32, FPMult64 - Single-precision and double-precision multipliers with
   single-cycle pipeline latency
 * FPAdd32, FPAdd64 - Single-precision and double-precision adders with
   three cycle pipeline latency

Note: These modules have not been thoroughly verified. Though the basic
functionality is known to work, there may be corner cases which are not
handled properly. In particular, there is no support for NaN, infinity, or
denormalized numbers other than zero. There is also no exponent overflow or
underflow detection. If you care about these things, do not use these modules.

## Example Usage

    val adder = Module(new FPAdd32())
    adder.io.a := FIRST_INPUT
    adder.io.b := SECOND_INPUT
    OUTPUT := adder.io.res

The other modules have the same interface.

## License

Copyright © 2014 Howard Zhehao Mao

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this 
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation 
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
