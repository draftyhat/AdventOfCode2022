/* compile with
   kotlinc Day09.kt -include-runtime -d Day09.jar && java -jar Day09.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream
import draftyhat.Point

val DAY=9
val YEAR=2022
val INPUTDIR="../input"

fun moveHead(input: String, headStart: Point, tailStart: Point): Int {
  /* grid goes from bottom left (0,0) to top right 
     The grid class prints out as if from top left to bottom right, so
     it will appear upside down. */
  var head = headStart
  var tail = tailStart
  val tailPositions = mutableSetOf<Point>()

  /* read each movement line */
  for(line in input.trim().split('\n')) {
    /* parse number of moves */
    val nmoves = line.split(" ")[1].toInt()
    println("Moving ${line[0]} x $nmoves")
    /* parse direction */
    when(line[0]) {
      'R' -> { /* move right */
        for(headColumn in (head.x + 1)..(head.x + nmoves)) {
          if(tail.x < (headColumn - 1)) {
            /* going to need to move tail */
            tail = Point(headColumn - 1, head.y)
            println("  Head at ($headColumn, ${head.y}); moving tail to $tail")
            tailPositions.add(tail)
          }
        }
        /* preserve head position for next move */
        head = Point(head.x + nmoves, head.y)
      }
      'L' -> { /* move left */
        for(headColumn in (head.x - 1) downTo (head.x - nmoves)) {
          if(tail.x > (headColumn + 1)) {
            tail = Point(headColumn + 1, head.y)
            println("  Head at ($headColumn, ${head.y}); moving tail to $tail")
            tailPositions.add(tail)
          }
        }
        head = Point(head.x - nmoves, head.y)
      }
      'U' -> { /* move up */
        for(headRow in (head.y + 1)..(head.y + nmoves)) {
          if(tail.y < (headRow - 1)) {
            tail = Point(head.x, headRow - 1)
            println("  Head at (${head.x}, $headRow); moving tail to $tail")
            tailPositions.add(tail)
          }
        }
        head = Point(head.x, head.y + nmoves)
      }
      'D' -> { /* move down */
        for(headRow in (head.y - 1) downTo (head.y - nmoves)) {
          if(tail.y > (headRow + 1)) {
            tail = Point(head.x, headRow + 1)
            println("  Head at (${head.x}, $headRow); moving tail to $tail")
            tailPositions.add(tail)
          }
        }
        head = Point(head.x, head.y - nmoves)
      }
    }
  }
  println(tailPositions)
  return tailPositions.size
}

fun Part1(input:String) : Boolean {
  val nPositions = moveHead(input, Point(0, 0), Point(0, 0))
  println("Tail covered $nPositions positions")
  return true
}

/* get new tail position if head moves in one of the four cardinal positions
 * (diagonals not supported) */
fun getNewTailPosition(head: Point, tail: Point): Point? {
  var newTail: Point? = null
  if(tail.x < head.x - 1) {
    newTail = Point(head.x - 1, head.y)
  } else if(tail.x > head.x + 1) {
    newTail = Point(head.x + 1, head.y)
  } else if(tail.y < head.y - 1) {
    newTail = Point(head.x, head.y - 1)
  } else if(tail.y > head.y + 1) {
    newTail = Point(head.x, head.y + 1)
  }

  return newTail
}

fun moveHeadReturnPositions(inputPositions: MutableList<Point>,
    headStart: Point, tailStart: Point): MutableList<Point> {
  /* grid goes from bottom left (0,0) to top right 
     The grid class prints out as if from top left to bottom right, so
     it will appear upside down. */
  var tail = tailStart
  var newPositions = mutableListOf<Point>(tailStart)
  var newTail: Point?

  /* move to each position, dragging the tail
     If the head moves diagonally away from the tail, process two moves, one
     each in the cardinal directions. */
  for(position in inputPositions) {
    /* If the head is moving diagonally away from the tail, split this move
     * into two moves */
    if(Math.abs(position.x - tail.x) == 2 && Math.abs(position.y - tail.y) == 2) {
      val halfPosition = Point(position.x - (position.x - tail.x)/2, position.y)
      newTail = getNewTailPosition(halfPosition, tail)
      if(newTail != null) {
        newPositions.add(newTail)
        tail = newTail
      }
      newTail = getNewTailPosition(position, tail)
      if(newTail != null) {
        newPositions.add(newTail)
        tail = newTail
      }
    } else {
      newTail = getNewTailPosition(position, tail)
      if(newTail != null) {
        newPositions.add(newTail)
        tail = newTail
      }
    }

    //println("$position  $tail")
  }

  return newPositions
}

fun Part2(input:String) : Boolean {
  val ropeLength = 10

  /* translate directions into a list of points */
  var position = Point(0,0)
  var positionList = mutableListOf<Point>(position)
  for(line in input.trim().split('\n')) {
    val direction = line[0]
    val nmoves = line.split(" ")[1].toInt()
    for(i in 0 until nmoves) {
      when(direction) {
        'R' -> { position = Point(position.x + 1, position.y) }
        'L' -> { position = Point(position.x - 1, position.y) }
        'U' -> { position = Point(position.x, position.y - 1) }
        'D' -> { position = Point(position.x, position.y + 1) }
      }
      positionList.add(position)
    }
  }

  /* run the rope, one knot at a time */
  //println(positionList.joinToString(""))
  for(i in 1 until ropeLength) {
    positionList = moveHeadReturnPositions(
        positionList, Point(0, 0), Point(0, 0))
    println(positionList.joinToString(""))
  }

  val positionSet = positionList.toSet()
  println("Knot $ropeLength traveled ${positionSet.size} Planck units")

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
