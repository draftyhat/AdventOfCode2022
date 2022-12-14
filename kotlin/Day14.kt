/* compile with
   kotlinc Day14.kt -include-runtime -d Day14.jar && java -jar Day14.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream
import draftyhat.Point

val DAY=14
val YEAR=2022
val INPUTDIR="../input"


/* store lines of rock in a dictionary */

fun addLine(rocks: MutableSet<Point>, lastPoint: Point, newPoint: Point) {
  /* add the line backwards, because it shouldn't matter which direction we go.
   * lastPoint is already in the set, but newPoint is not. */
  var xstep = 0
  if(lastPoint.x != newPoint.x) {
    xstep = if(lastPoint.x > newPoint.x) 1 else -1
  }
  var ystep = 0
  if(lastPoint.y != newPoint.y) {
    ystep = if(lastPoint.y > newPoint.y) 1 else -1
  }

  var thisPoint = newPoint
  while(thisPoint != lastPoint) {
    rocks.add(thisPoint)
    thisPoint = Point(thisPoint.x + xstep, thisPoint.y + ystep)
  }
}

fun readRocks(input: String) : MutableSet<Point> {
  val rocks = mutableSetOf<Point>()

  for(line in input.trim().split('\n')) {
    var lastPoint : Point? = null
    for(pointString in line.split(' ')) {
      if(pointString == "->")
        continue

      /* read first point */
      if(lastPoint == null) {
        /* read first point */
        lastPoint = Point(pointString)
        /* put this point in our dictionary */
        rocks.add(lastPoint)
      }

      /* read non-first point. Add new point and line from last point to the
       * dictionary. */
      val newPoint = Point(pointString)
      addLine(rocks, lastPoint, newPoint)
      lastPoint = newPoint
    }
  }

  return rocks
}

fun dropSand(startPoint: Point, rocks: MutableSet<Point>): Int {
  /* returns the number of grains of sand that were trapped */

  /* could find minimum y from rocks, but it's easier just to use 500 */
  val maxY = 500

  /* trapped sand */
  val sand = mutableSetOf<Point>()
  var done = false

  /* two ways we can finish: sand builds up around the start point, or
     sand falls down to the bottom */
  while(!done) {
    var nextSand = startPoint
    var newSand = startPoint
    
    while(!(nextSand in rocks || nextSand in sand) &&
        nextSand.y <= maxY) {
      newSand = nextSand
      nextSand = Point(newSand.x, newSand.y + 1)
      /* if we can't fall straight down, try to the left */
      if(nextSand in rocks || nextSand in sand) {
        nextSand = Point(newSand.x - 1, newSand.y + 1)
        if(nextSand in rocks || nextSand in sand) {
          /* then try to the right */
          nextSand = Point(newSand.x + 1, newSand.y + 1)
        }
      }
    }

    //println("Found newSand $newSand")
    if(newSand in rocks || newSand in sand || newSand.y >= maxY)
      done = true
    else
      sand.add(newSand)
  }

  return sand.size
}

fun Part1(input:String) : Boolean {
  val rocks = readRocks(input)
  val nTrappedSand = dropSand(Point(500, 0), rocks)
  println("Found $nTrappedSand trapped sand grains")
  return true
}

fun Part2(input:String) : Boolean {
  val rocks = readRocks(input)

  /* add floor at max y component */
  val startPoint = Point(500, 0)
  var maxY = rocks.first().y
  for(rock in rocks) {
    if(rock.y > maxY)
      maxY = rock.y
  }
  maxY += 2
  for(x in startPoint.x - maxY until (startPoint.x + maxY + 1))
    rocks.add(Point(x, maxY))

  val nTrappedSand = dropSand(startPoint, rocks)
  println("Found $nTrappedSand trapped sand grains")
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
