package ChiselFloat

import Chisel._

object TesterMain {
    def main(args: Array[String]) {
        val testArgs = args.slice(1, args.length)
        args(0) match {
            case "FPMult32" =>
                chiselMainTest(testArgs, () => Module(new FPMult(32))) {
                    c => new FPMult32Test(c)
                }
            case "FPMult64" =>
                chiselMainTest(testArgs, () => Module(new FPMult(64))) {
                    c => new FPMult64Test(c)
                }
        }
    }
}
