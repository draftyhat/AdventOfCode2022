/* compile with
   kotlinc Day01.kt -include-runtime -d Day01.jar && java -jar Day01.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=2
val YEAR=2022
val INPUTDIR="../input"

val score_win = 6
val score_draw = 3
val score_lose = 0
val score_rock = 1
val score_paper = 2
val score_scissors = 3

/* A,X -> rock
   B,Y -> paper
   C,A -> scissors */

val scoreMap = mapOf(
 "A X" to score_draw + score_rock,
 "A Y" to score_win + score_paper,
 "A Z" to score_lose + score_scissors,
 "B X" to score_lose + score_rock,
 "B Y" to score_draw + score_paper,
 "B Z" to score_win + score_scissors,
 "C X" to score_win + score_rock,
 "C Y" to score_lose + score_paper,
 "C Z" to score_draw + score_scissors,
)


fun Part1(input:String) : Boolean {
  var score = 0
  /* for each turn (each line in the file) */
  for(line in input.split("\n")) {
    if(line == "")
      continue
    val turnscore = scoreMap[line]
    if(turnscore == null)
      throw Exception("ERROR: cannot process line $line")
    score += scoreMap[line]!!
  }
  
  println("Score: $score")
  return true
}

fun Part2(input:String) : Boolean {
  return false
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
