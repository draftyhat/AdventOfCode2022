/* compile with
   kotlinc Day20.kt -include-runtime -d Day20.jar && java -jar Day20.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=20
val YEAR=2022
val INPUTDIR="../input"

class MixNumber(var value: Long, var originalPosition: Long, var position: Long) {
  override fun toString(): String {
    return "$position] $value ($originalPosition)"
  }
}

fun readNumbers(input: String): MutableList<MixNumber> {
  val numbers = mutableListOf<MixNumber>()
  var position = 0L
  for(line in input.trim().split('\n'))
  {
    numbers.add(MixNumber(line.toLong(), position, position))
    position++
  }

  return numbers
}

/* returns position of 0 */
fun mixNumbers(numbers: MutableList<MixNumber>, mixOrder: List<MixNumber>): Long {
  var zeroOriginalPosition = 0L

  val nNumbers = mixOrder.size.toLong()
  for(number in mixOrder) {
    if(number.value == 0L) {
      zeroOriginalPosition = number.originalPosition
      continue
    }

    /* "move" all numbers between this number and its final position. We
       actually do move them in the numbers list, by way of putting number
       in its new spot; we just need to record the correct indices.
       in this loop, though, we'll just update the indices */
    /* example
          1, 2, -3, 3, -2, 0, 4  list size 7
          move 1 to index [0+1=1] 2,1,-3,3,-2,0,4
          move 2 to index [0+2=2] 1,-3,2,3,-2,0,4
          move -3 to index [1-3=-2%7=5] 1,-3,2,3,-2,-3,0,4 */
    /* frickin' Kotlin and its one way for loops ??? */
    /* also note the modulo operator, %, may return a negative number.
       Use the function mod . */

    /* this is a circular list; the last position == the first position */
    val newPosition = (number.position + number.value).mod(nNumbers - 1)
    var addend = 0L
    var shiftStart = 0L
    var shiftEnd = 0L
    /* if moving this number right */
    if(number.value > 0L) {
      /* if no wrap, shift numbers between 1 position to left */
      if(number.position < newPosition) {
        shiftStart = number.position + 1
        shiftEnd = newPosition
        addend = -1
      } else {
        /* else wrap. Shift numbers between end position and start
           position to right */
        shiftStart = newPosition
        shiftEnd = number.position - 1
        addend = 1
      }
    }
    /* if moving this number left */
    else {
      /* if no wrap, move numbers between 1 position to right */
      if(newPosition < number.position) {
        shiftStart = newPosition
        shiftEnd = number.position - 1
        addend = 1
      }
      /* if wrapping around, move all numbers between start position
         and end position 1 to left */
      else {
        shiftStart = number.position + 1
        shiftEnd = newPosition
        addend = -1
      }
    }
    for(movingNumberIndex in shiftStart..shiftEnd) {
      numbers[movingNumberIndex.toInt()].position += addend
    }

    /* remove number from numbers */
    numbers.removeAt(number.position.toInt())

    /* add moved number to numbers in the correct position */
    numbers.add(newPosition.toInt(), number)

    /* update moved number's index */
    number.position = newPosition
  }

  return mixOrder[zeroOriginalPosition.toInt()].position
}

fun Part1(input:String) : Boolean {
  val numbers = readNumbers(input)
  val mixOrder = numbers.toList()
  val zeroPosition = mixNumbers(numbers, mixOrder).toInt()
  println("0: ${numbers[zeroPosition]}  " +
      "1000: ${numbers[(zeroPosition + 1000L).mod(numbers.size)]}  " +
      "2000: ${numbers[(zeroPosition + 2000L).mod(numbers.size)]}  " +
      "3000: ${numbers[(zeroPosition + 3000L).mod(numbers.size)]}  ")
  println("Final value: " + (
      numbers[(zeroPosition + 1000L).mod(numbers.size)].value +
      numbers[(zeroPosition + 2000L).mod(numbers.size)].value +
      numbers[(zeroPosition + 3000L).mod(numbers.size)].value).toString())
  return true
}

fun Part2(input:String) : Boolean {
  val decryptionKey = 811589153
  val numbers = readNumbers(input)
  for(number in numbers)
    number.value *= decryptionKey
  var zeroPosition = 0
  val mixOrder = numbers.toList()
  for(mixRound in 0 until 10) {
    zeroPosition = mixNumbers(numbers, mixOrder).toInt()
    println("round $mixRound: " + numbers.joinToString("\n"))
  }

  println("0: ${numbers[zeroPosition]}  " +
      "1000: ${numbers[(zeroPosition + 1000L).mod(numbers.size)]}  " +
      "2000: ${numbers[(zeroPosition + 2000L).mod(numbers.size)]}  " +
      "3000: ${numbers[(zeroPosition + 3000L).mod(numbers.size)]}  ")
  println("Final value: " + (
      numbers[(zeroPosition + 1000L).mod(numbers.size)].value +
      numbers[(zeroPosition + 2000L).mod(numbers.size)].value +
      numbers[(zeroPosition + 3000L).mod(numbers.size)].value).toString())
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
