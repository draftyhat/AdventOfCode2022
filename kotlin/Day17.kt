/* compile with
   kotlinc Grid.ky Day17.kt -include-runtime -d Day17.jar && java -jar Day17.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=17
val YEAR=2022
val INPUTDIR="../input"

/* grid is read from top left to bottom right */
class Point(
    val x: Long,
    val y: Long) {
  constructor(s: String) : this(s.trim('(').split(',')[0].toLong(),
      s.trim(')').split(',')[1].toLong())

  operator fun plus(p: Point): Point {
    return Point(x + p.x, y + p.y)
  }
  operator fun minus(p: Point): Point {
    return Point(x - p.x, y - p.y)
  }
  override operator fun equals(other: Any?): Boolean {
    return other is Point && other.x == x && other.y == y
  }
  override fun toString(): String {
    return "($x,$y)"
  }
  override fun hashCode(): Int {
    return (x.hashCode() * y).hashCode()
  }
}


/* column is 7 wide
   rocks start at maxOccupiedY + 3 */

/* 0, 0 is left bottom edge of each rock */
val rocks: Array<Array<Point>> = arrayOf(
  arrayOf(Point(0,0), Point(1,0), Point(2,0), Point(3,0)),  /* - */
  arrayOf(Point(1,0), Point(0,1), Point(1,1), Point(2,1), Point(1,2)), /* + */
  arrayOf(Point(0,0), Point(1,0), Point(2,0), Point(2,1), Point(2,2)), /* _| */
  arrayOf(Point(0,0), Point(0,1), Point(0,2), Point(0,3)),  /* | */
  arrayOf(Point(0,0), Point(0,1), Point(1,0), Point(1,1)),  /* o */
)
val maxRockHeight = 4L

fun startRock(topY: Long, rockIndex: Long): Array<Point> {
  /* translate rock to 3 above maxY and left edge + 2 */
  val retval = rocks[(rockIndex % rocks.size.toLong()).toInt()].copyOf()
  for(pindex in 0 until retval.size) {
    var originalPoint = retval[pindex]
    retval[pindex] = Point(originalPoint.x + 2, originalPoint.y + topY + 4)
  }
  return retval
}

/* shaft is grid of occupied spaces
   0,0 is bottom left
   shaft[y - shaftBottom][x] == '#' if the space is occupied; '.' else
*/

/** @brief move the indicated rock down one position
    returns null if the rock has come to rest
    Does not update columnTops */
fun rockDown(rock: Array<Point>,
    shaft: MutableList<Array<Char>>, shaftBottom: Long) : Array<Point>? {
  /* translate rock */
  for(pindex in 0 until rock.size) {
    var originalPoint = rock[pindex]
    val newPoint = Point(originalPoint.x, originalPoint.y - 1)
    rock[pindex] = newPoint

    /* check for conflicts. If conflict, back out and return null. */
    //println("Point $newPoint, shaftBottom $shaftBottom, shaftsize ${shaft.size}")
    if(shaft[(newPoint.y - shaftBottom).toInt()][newPoint.x.toInt()] == '#') {
      for(oldindex in pindex downTo 0) {
        rock[oldindex] = Point(rock[oldindex].x, rock[oldindex].y + 1)
      }
      return null
    }
  }

  return rock
}

/** @brief move the rock one position right or left
    returns the rock at its new position even if it didn't move */
fun rockSideways(rock: Array<Point>,
    shaft: MutableList<Array<Char>>, shaftBottom: Long, right: Boolean):
  Array<Point> {
  val lateralTranslation = if(right) 1 else -1
  val width = shaft[0].size.toLong()

  //println("^^ Rock starting at ${rock[0]}")
  for(pindex in 0 until rock.size) {
    var originalPoint = rock[pindex]
    val newPoint = Point(originalPoint.x + lateralTranslation, originalPoint.y)
    rock[pindex] = newPoint
    //println("^^^ rock[$pindex] translates to $newPoint")

    /* check for conflicts. If conflict, untranslate and return. */
    if(newPoint.x < 0 || newPoint.x >= width ||
        shaft[(newPoint.y - shaftBottom).toInt()][newPoint.x.toInt()] == '#') {
      for(oldindex in pindex downTo 0) {
        rock[oldindex] = Point(
            rock[oldindex].x - lateralTranslation, rock[oldindex].y)
      }
      /*println("^^^ backed out translation")
      print("^^^^")
      for(rockP in rock)
        print(" $rockP")
      println("")*/
      return rock
    }
  }

  //println("^^^ returning translated rock")
  return rock
}

/** @brief When this rock has come to rest, add it to shaft,
    and calculate new top */
fun calculateNewTop(
    rock: Array<Point>, oldTop: Long,
    shaft: MutableList<Array<Char>>, shaftBottom: Long): Long {
  //print("**** adding rock to shaft ")
  //for(rockP in rock)
  //  print(" $rockP")
  //println("")

  var newTop = oldTop
  for(p in rock) {
    shaft[(p.y - shaftBottom).toInt()][p.x.toInt()] = '#'
    if(p.y > newTop)
      newTop = p.y
  }
  return newTop
}

fun printShaft(rock: Array<Point>, shaft: MutableList<Array<Char>>,
    shaftBottom: Long) {
  for(rowIndex in shaft.size - 1 downTo shaft.size - 10) {
    if(rowIndex < 0)
      break
    print("|")
    for(columnIndex in 0 until shaft[0].size) {
      if(Point(columnIndex.toLong(),rowIndex.toLong() + shaftBottom) in rock)
        print('@')
      else
        print(shaft[rowIndex][columnIndex])
    }
    println("|")
  }
}

data class RoundVariables (
    var nmoves: Int,
    var topY: Long,
    var shaftBottom: Long,
    var shaft: MutableList<Array<Char>>,)

fun oneRound(roundVariables: RoundVariables, round: Long,
    goingRight: Array<Boolean>) {
  /* create a new rock at the starting position */
  var oldRock = startRock(roundVariables.topY, round)

  /* add max required number of rows to shaft */
  while(roundVariables.shaft.size <
      (roundVariables.topY - roundVariables.shaftBottom + 4L + maxRockHeight)) {
    roundVariables.shaft.add(arrayOf('.','.','.','.','.','.','.'))
  }
  if(roundVariables.shaft.size > 2000) {
    /* drop first 1000 (arbitrary) rows of shaft */
    roundVariables.shaft = roundVariables.shaft.subList(
        1000, roundVariables.shaft.size)
    roundVariables.shaftBottom += 1000
  }
  //printShaft(oldRock, roundVariables.shaft)

  /* move the rock until it can't fall any more */
  var newRock: Array<Point>? = oldRock
  while(newRock != null) {
    /* move sideways as indicated */
    oldRock = rockSideways(newRock, roundVariables.shaft,
        roundVariables.shaftBottom,
        goingRight[roundVariables.nmoves])
    roundVariables.nmoves = (roundVariables.nmoves + 1) % goingRight.size
    /* move down */
    newRock = rockDown(oldRock, roundVariables.shaft, roundVariables.shaftBottom)
  }

  /* add the rock to the shaft */
  roundVariables.topY = calculateNewTop(oldRock, roundVariables.topY, roundVariables.shaft, roundVariables.shaftBottom)
}


fun dropRocks(input:String, nrounds: Long) : Long {
  /* we create a floor. Looks like the problem uses height indices starting
     at 1, and our floor is at 0, so this works */
  val roundVariables = RoundVariables(0, 0L, 0L,
      mutableListOf(arrayOf('#','#','#','#','#','#','#')))

  /* translate input to true/false for right/left */
  val goingRight = Array<Boolean>(input.trim().length) { index ->
    input.trim()[index] == '>' }

  for(round in 0L until nrounds) {
    if((round % 100000L) == 0L)
      println("Round $round")

    /* never hits */
    if(roundVariables.nmoves == 0 && round % rocks.size == 0L) {
      println("-- modulo 0, topY ${roundVariables.topY}")
    }
    //if(round % rocks.size == 0L)
    //  print(" ${roundVariables.nmoves}")

    oneRound(roundVariables, round, goingRight)
  }

  return roundVariables.topY
}

fun Part1(input:String) : Boolean {
  println("Rock tower is ${dropRocks(input, 2022L)} units tall")
  return true
}

fun Part2(input:String) : Boolean {
  val nrounds = 1000000000000L

  /* find out how many rounds it takes until we're starting at the
     beginning from the shaft position, rock number, and index in the input */

  println("Rock tower is ${dropRocks(input, nrounds)} units tall")
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
