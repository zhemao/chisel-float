FPMult32Test: src/main/scala/FPMult.scala
	sbt "run FPMult32 --genHarness --compile --test --backend c"

FPMult64Test: src/main/scala/FPMult.scala
	sbt "run FPMult64 --genHarness --compile --test --backend c"

FPMult32.v: src/main/scala/FPMult.scala
	sbt "run FPMult32 --compile --backend v"

FPMult64.v: src/main/scala/FPMult.scala
	sbt "run FPMult64 --backend v"


clean:
	rm -f *.o *.cpp *.h FPMult32 FPMult64 *.v
