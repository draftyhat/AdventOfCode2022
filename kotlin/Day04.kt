/* compile with
   kotlinc Day01.kt -include-runtime -d Day01.jar && java -jar Day01.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=4
val YEAR=2022
val INPUTDIR="../input"

fun rangeContains(range0: Array<Int>, range1: Array<Int>): Boolean {
  if((range0[0] >= range1[0]) && (range0[1] <= range1[1]))
    return true
  if((range1[0] >= range0[0]) && (range1[1] <= range0[1]))
    return true
  return false
}
fun rangeOverlaps(range0: Array<Int>, range1: Array<Int>): Boolean {
  return !((range0[0] > range1[1]) || (range0[1] < range1[0]))
}

fun calculateAnswer(input:String,
    rangeFn: (range0:Array<Int>, range1:Array<Int>) -> Boolean) : Int {
  var count = 0
  /* for each line of input */
  for(line in input.split('\n')) {
    if(line != "") {
      val ranges = line.split(',')
      val range0String = ranges[0].split('-')
      val range1String = ranges[1].split('-')
      val range0 = arrayOf(range0String[0].toInt(), range0String[1].toInt())
      val range1 = arrayOf(range1String[0].toInt(), range1String[1].toInt())

      if(rangeFn(range0, range1))
        count += 1
    }
  }
  return count
}

fun Part1(input:String) : Boolean {
  val ncontains = calculateAnswer(input, { x,y -> rangeContains(x, y) })
  println("Fully contained regions: $ncontains")
  return true
}

fun Part2(input:String) : Boolean {
  val noverlaps = calculateAnswer(input, { x,y -> rangeOverlaps(x, y) })
  println("Overlapping regions: $noverlaps")
  return true
}

fun main(args: Array<String>) {
  var ranN = 0
  var success = true
  val daystr = DAY.toString().padStart(2, '0')

  /* run tests, part 1, and/or part2 */
  if("-t1" in args || "-t2" in args) {
    /* run test */
    println("--- running tests, part 1 ---")
    var testn = 0
    var test_success = true
    while(test_success == true) {
      var filename = "$INPUTDIR/day${daystr}_test"
      if(testn > 0)
        filename += testn.toString()

      /* try to open the file and run the test */
      val testFile = File(INPUTDIR, filename)
      if(!testFile.exists())
        test_success = false
      else {
        val testInput = testFile.inputStream().bufferedReader().use { it.readText() }
        if("-t1" in args) {
          success = Part1(testInput)
        }
        if(success && "-t2" in args) {
          success = Part2(testInput)
        }

        if(!success)
          test_success = false
        testn += 1
        ranN += 1
      }

    }

  }
  var inputFilename = "day${daystr}_input"
  val inputFile = File(INPUTDIR, inputFilename)
  if(!inputFile.exists())
    println("Please create day $DAY input filename $INPUTDIR/$inputFilename!")
  else {
    val inputText = inputFile.inputStream().bufferedReader().use { it.readText() }
    if(success == true && "-1" in args) {
      println("--- running part 1 ---")
      success = Part1(inputText)
      ranN += 1
    }
    if(success == true && "-2" in args) {
      println("--- running part 2 ---")
      success = Part2(inputText)
      ranN += 1
    }
  }

  if(ranN == 0) {
    /* print usage */
    println("Usage options:")
    println(" -t1   test part 1 input file $INPUTDIR/day${daystr}_test[n]")
    println(" -t2   test part 2 input file $INPUTDIR/day${daystr}_test[n]")
    println(" -1    run part 1 input file $INPUTDIR/day${daystr}_input")
    println(" -2    run part 2 input file $INPUTDIR/day${daystr}_input")
  } else {
    /* print stats */
    val successstring = if(success) "SUCCESS" else "FAIL"
    println("Advent of Code $YEAR Day $DAY $ranN activities: $successstring")
  }
}
