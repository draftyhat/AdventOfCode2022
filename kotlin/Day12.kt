/* compile with
   kotlinc Day12.kt -include-runtime -d Day12.jar && java -jar Day12.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream
import java.util.PriorityQueue

val DAY=12
val YEAR=2022
val INPUTDIR="../input"


class Square (
    val row: Int,
    val column: Int,
    val height: Char,
    var pathlength: Int = -1) {
  override fun toString(): String {
    if(height == '0' || height == '1')
      return height.toString()
    if(pathlength != -1)
      return (height + ('A' - 'a')).toString()
    return height.toString()
  }
}

/* grid is read from top left to bottom right */
class Point(
    val x: Int,
    val y: Int) {
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

class Grid (
    var width: Int,
    var height: Int,
    init: (column: Int, row: Int, opaque:Any) -> Square,
    opaqueInitArgument: Any) {
  private var gridData = Array<Square>(width * height,
      {index -> init(index % width, index / width, opaqueInitArgument)})

  operator fun get(x: Int, y: Int) : Square {
    return gridData[y * width + x]
  }
  operator fun get(p: Point) : Square { return get(p.x, p.y) }
  operator fun set(x: Int, y: Int, value: Square) : Square {
    val tmp = gridData[y * width + x]
    gridData[y * width + x] = value
    return tmp
  }
  operator fun set(p: Point, z: Square) : Square { return set(p.x, p.y, z) }

  fun neighbors(x: Int, y: Int, diagonals: Boolean = false):MutableList<Point> {
    val retval = mutableListOf<Point>()
    if((x - 1) >= 0) {
      retval.add(Point(x - 1, y))
      if(diagonals) {
        if((y - 1) >= 0)
          retval.add(Point(x - 1, y - 1))
        if((y + 1) < height)
          retval.add(Point(x - 1, y + 1))
      }
    }
    if((y - 1) >= 0) {
      retval.add(Point(x, y - 1))
    }
    if((x + 1) < width) {
      retval.add(Point(x + 1, y))
      if(diagonals) {
        if((y - 1) >= 0)
          retval.add(Point(x + 1, y - 1))
        if((y + 1) < height)
          retval.add(Point(x + 1, y + 1))
      }
    }
    if((y + 1) < height) {
      retval.add(Point(x, y + 1))
    }
    return retval
  }
  fun neighbors(p: Point, diagonals: Boolean = false): MutableList<Point> {
    return neighbors(p.x, p.y, diagonals)
  }
  override fun toString(): String {
    /* this is awful */
    var retval = ""
    for(index in 0 until gridData.size) {
      if((index % width) == 0 && index != 0)
        retval = retval + "\n"
      retval = retval + gridData[index].toString()
    }
    return retval
  }
}


fun readGridItem(column: Int, row: Int, opaque:Any): Square {
  @Suppress("UNCHECKED_CAST")
  val inputLines = opaque as List<String>
  var ch = inputLines[row][column]
  if(ch == 'S')
    ch = '0'
  else if(ch == 'E')
    ch = '1'
  return Square(row, column, ch)
}


fun hike(input:String, start: Char = '0') : Int {
  val inputLines = input.trim().split('\n')
  val grid = Grid(inputLines[0].length, inputLines.size,
      { column, row, opaque -> readGridItem(column, row, opaque) },
      inputLines)

  /* initlialize minHeap */
  var pathlength = -1
  val minheap = PriorityQueue<Square>( compareBy<Square> { it.pathlength } )

  /* Find the start square(s) */
  for(column in 0 until grid.width) {
    for(row in 0 until grid.height) {
      val element = grid[column, row]
      if(element.height == start) {
        element.pathlength = 0
        minheap.add(element)
      }
    }
  }

  /* Djikstra-style traversal */
  while(minheap.size > 0 && pathlength == -1) {
    val here = minheap.remove()
    //println("${here.pathlength}] at ${here.row},${here.column} ${here.height}")
    /* check here's neighbors */
    for(neighborPoint in grid.neighbors(here.column, here.row)) {
      val neighbor = grid[neighborPoint]
      if(neighbor.height == '1' && here.height == 'z') {
        //println("Found 'E' at ${here.pathlength + 1}")
        pathlength = here.pathlength + 1
        break;
      }
      if(neighbor.pathlength == -1) {
        val pathdiff = neighbor.height - here.height
        if(here.height != '1' &&
            ((pathdiff <= 1) || (here.height == '0' && neighbor.height == 'a'))) {
          neighbor.pathlength = here.pathlength + 1
          minheap.add(neighbor)
        }
      }
    }
  }

  println("Path length: ${pathlength} steps")
  return pathlength
}


fun Part1(input:String) : Boolean {
  hike(input, '0')
  return true
}

fun Part2(input:String) : Boolean {
  hike(input, 'a')
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
