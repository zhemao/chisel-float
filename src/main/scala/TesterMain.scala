package ChiselFloat

import Chisel._

object TesterMain {
    def main(args: Array[String]) {
        val testArgs = args.slice(1, args.length)
        args(0) match {
            case "FPMult32" =>
                chiselMainTest(testArgs, () => Module(new FPMult32())) {
                    c => new FPMult32Test(c)
                }
            case "FPMult64" =>
                chiselMainTest(testArgs, () => Module(new FPMult64())) {
                    c => new FPMult64Test(c)
                }
            case "FPAdd32" =>
                chiselMainTest(testArgs, () => Module(new FPAdd32())) {
                    c => new FPAdd32Test(c)
                }
            case "FPAdd64" =>
                chiselMainTest(testArgs, () => Module(new FPAdd64())) {
                    c => new FPAdd64Test(c)
                }
        }
    }
}
