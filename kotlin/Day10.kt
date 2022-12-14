/* compile with
   kotlinc Day10.kt -include-runtime -d Day10.jar && java -jar Day10.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=10
val YEAR=2022
val INPUTDIR="../input"

fun drawCRT(cycle: Int, x: Int) {
  if(cycle % 40 == 0)
    println("")
  if(Math.abs(x - (cycle % 40)) < 2)
    print("#")
  else
    print('.')
}

fun executeProgram(input: String): Int {
  var cycle = 1
  var sum = 0
  var x = 1
  drawCRT(cycle, x)
  for(line in input.trim().split('\n')) {
    drawCRT(cycle, x)

    val instruction = line.split(' ')
    when(instruction[0]) {
      "noop" -> { }
      "addx" -> {
        cycle += 1
        if((cycle % 40) == 20)
          sum += x * cycle
        x += instruction[1].toInt()

        drawCRT(cycle, x)
      }
    }
    cycle += 1
    if((cycle % 40) == 20)
      sum += x * cycle
  }

  return sum
}

fun Part1(input:String) : Boolean {
  println("Sum: ${executeProgram(input)}")
  return true
}

fun Part2(input:String) : Boolean {
  /* CRT is 40wide, 6 high, drawn top to bottom. Positions 0-39.
     sprite is 3 pixels wide, x indicates middle pixel
     CRT draws a single pixel each cycle. '.' for off, '#' for sprite
     */
  executeProgram(input)
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
