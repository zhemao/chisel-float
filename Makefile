TEST_OPTIONS = --genHarness --compile --test --backend c --vcd
COMPILE_OPTIONS = --compile --backend v

executables := FPMult32 FPMult64 FPAdd32 FPMult32

test-all: $(addsuffix Test, $(executables))

verilog: $(addsuffix .v, $(executables))

FPMult32Test: src/main/scala/FPMult.scala
	sbt "run FPMult32 $(TEST_OPTIONS)"

FPMult64Test: src/main/scala/FPMult.scala
	sbt "run FPMult64 $(TEST_OPTIONS)"

FPAdd32Test: src/main/scala/FPAdd.scala
	sbt "run FPAdd32 $(TEST_OPTIONS)"

FPAdd64Test: src/main/scala/FPAdd.scala
	sbt "run FPAdd64 $(TEST_OPTIONS)"

FPMult32.v: src/main/scala/FPMult.scala
	sbt "run FPMult32 $(COMPILE_OPTIONS)"

FPMult64.v: src/main/scala/FPMult.scala
	sbt "run FPMult64 $(COMPILE_OPTIONS)"

FPAdd32.v: src/main/scala/FPAdd.scala
	sbt "run FPAdd32 $(COMPILE_OPTIONS)"

FPAdd64.v: src/main/scala/FPAdd.scala
	sbt "run FPAdd64 $(COMPILE_OPTIONS)"

clean:
	rm -f *.o *.cpp *.h $(executables) *.v *.vcd
