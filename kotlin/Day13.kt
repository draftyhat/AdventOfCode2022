/* compile with
   kotlinc Day13.kt -include-runtime -d Day13.jar && java -jar Day13.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=13
val YEAR=2022
val INPUTDIR="../input"

fun readPacket(inputLine: String) : MutableList<Any> {
  val retval = mutableListOf<Any>()

  /* assume the packet is well-formed */
  var currentStack = mutableListOf<Any>(retval)
  var currentValue: Int? = null
  for(inputChar in inputLine) {
    when(inputChar) {
      '[' -> {
        val currentSpot = currentStack[0] as MutableList<Any>
        /* finish the current value, if any */
        if(currentValue != null) {
          currentSpot.add(currentValue)
          currentValue = null
        }

        /* add a new list */
        val newList = mutableListOf<Any>()
        currentSpot.add(newList)
        currentStack.add(0, newList)
      }
      ']' -> {
        if(currentValue != null) {
          /* finish the current value, if any */
          val currentSpot = currentStack[0] as MutableList<Any>
          currentSpot.add(currentValue)
          currentValue = null
        }

        /* close the current list */
        currentStack.removeAt(0)
      }
      ',' -> {
        if(currentValue != null) {
          /* finish the current value, if any */
          val currentSpot = currentStack[0] as MutableList<Any>
          currentSpot.add(currentValue)
          currentValue = null
        }
      }
      else -> {
        /* anything else is assumed to be an integer, part of the current value */
        currentValue = (currentValue ?: 0) * 10 + inputChar.toString().toInt()
      }
    }
  }

  /* we start with a top-level list which is not part of the input, gotta get
   * rid of that */
  println(retval[0])
  return retval[0] as MutableList<Any>
}

fun packetCompare(packet0: MutableList<Any>, packet1: MutableList<Any>) : Int {
  /* like a standard comparator, return 0 if equal, >0 if packet0 < packet1,
   * <0 if packet1 < packet0 */
  for(elementIndex in 0 until packet0.size) {
    /* if packet1 is smaller, return -1 */
    if(packet1.size <= elementIndex)
      return -1

    if(packet0[elementIndex]::class == Int::class) {
      val packet0int = packet0[elementIndex] as Int
      if(packet1[elementIndex]::class == Int::class) {
        val packet1int = packet1[elementIndex] as Int
        if(packet0int != packet1int)
          return packet1int - packet0int
        /* otherwise, they're equal, keep going */
      } else {
        /* packet1[elementIndex] is a list */
        /* one list, one int; convert the int to a list and compare the two lists */
        val answer = packetCompare(mutableListOf<Any>(packet0int),
            packet1[elementIndex] as MutableList<Any>)
        if(answer != 0)
          return answer
      }
    } else {
      /* packet0[elementIndex] is a list */
      var answer = 0
      if(packet1[elementIndex]::class == Int::class) {
        /* one list, one int; convert the int to a list and compare the two lists */
        answer = packetCompare(packet0[elementIndex] as MutableList<Any>,
            mutableListOf<Any>(packet1[elementIndex] as Int))
      } else {
        /* two lists */
        answer = packetCompare(packet0[elementIndex] as MutableList<Any>,
            packet1[elementIndex] as MutableList<Any>)
      }
      if(answer != 0)
        return answer
    }
  }
  if(packet1.size > packet0.size)
    return 1
  return 0
}


fun readAndComparePackets(input: String) : Int {
  var retval = 0
  val inputLines = input.trim().split('\n')
  var packetPairIndex = 1
  for(inputLineIndex in 1 until inputLines.size step 3) {
    val packet0 = readPacket(inputLines[inputLineIndex - 1])
    val packet1 = readPacket(inputLines[inputLineIndex])

    println("   comparison: ${packetCompare(packet0, packet1)}")
    if(packetCompare(packet0, packet1) >= 0)
      retval += packetPairIndex

    packetPairIndex += 1
  }
  return retval
}

fun Part1(input:String) : Boolean {
  println("Indices sum: ${readAndComparePackets(input)}")
  return false
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
