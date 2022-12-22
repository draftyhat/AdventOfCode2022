/* compile with
   kotlinc Day22.kt -include-runtime -d Day22.jar && java -jar Day22.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=22
val YEAR=2022
val INPUTDIR="../input"

class Grid(val width: Int, val height: Int) {
  val data: Array<Array<Char>>
  init {
    data = Array(height) { Array(width) { ' ' }}
  }

  operator fun get(x: Int, y: Int): Char {
    return data[y][x]
  }
  operator fun set(x: Int, y: Int, value: Char) {
    data[y][x] = value
  }
  
  override fun toString(): String {
    var retval = ""
    data.forEach() { row -> 
      retval = retval + "|" + row.joinToString("") + "|\n"
    }
    return retval
  }
}

fun readGrid(lines: List<String>): Grid {
  /* find width (length of longest line) and height (number of lines
     until we get to the blank one) */
  var width = 0
  var height = 0

  /* find grid width/height */
  for(line in lines) {
    if(line.length == 0)
      break
    height += 1
    width = width.coerceAtLeast(line.length)
  }
  val grid = Grid(width, height)

  /* input grid characters */

  for(row in 0 until height) {
    val line = lines[row]
    for(column in 0 until width) {
      if(line.length > column)
        grid.data[row][column] = line[column]
      else
        break
    }
  }

  return grid
}

class Vector(var x: Int, var y: Int, var direction: Char)
{
  override fun toString(): String {
    return "($x,$y)->$direction"
  }
}
val directionCode: Map<Char, Int> = mapOf(
  'R' to 0, 'D' to 1, 'L' to 2, 'U' to 3
)
val directionChar: Map<Int, Char> = mapOf(
  0 to 'R', 1 to 'D', 2 to 'L', 3 to 'U'
)

fun findStartPosition(grid: Grid): Vector {
  val vector = Vector(0,0,'R')
  while(grid[vector.x, vector.y] == ' ')
    vector.x += 1
  return vector
}

fun moveN(vector: Vector, grid: Grid, n: Int) {
  when(vector.direction) {
    'R' -> {
      for(move in 0 until n) {
        vector.x = (vector.x + 1).mod(grid.width)
        var actuallyMoved = 1
        while(grid[vector.x, vector.y] == ' ') {
          vector.x = (vector.x + 1).mod(grid.width)
          actuallyMoved++
        }
        if(grid[vector.x, vector.y] == '#') {
          vector.x = (vector.x - actuallyMoved).mod(grid.width)
          break
        }
      }
    }
    'D' -> {
      for(move in 0 until n) {
        vector.y = (vector.y + 1).mod(grid.height)
        var actuallyMoved = 1
        while(grid[vector.x, vector.y] == ' ') {
          vector.y = (vector.y + 1).mod(grid.height)
          actuallyMoved++
        }
        if(grid[vector.x, vector.y] == '#') {
          vector.y = (vector.y - actuallyMoved).mod(grid.height)
          break
        }
      }
    }
    'L' -> {
      for(move in 0 until n) {
        vector.x = (vector.x - 1).mod(grid.width)
        var actuallyMoved = 1
        while(grid[vector.x, vector.y] == ' ') {
          vector.x = (vector.x - 1).mod(grid.width)
          actuallyMoved++
        }
        if(grid[vector.x, vector.y] == '#') {
          vector.x = (vector.x + actuallyMoved).mod(grid.width)
          break
        }
      }
    }
    'U' -> {
      for(move in 0 until n) {
        vector.y = (vector.y - 1).mod(grid.height)
        var actuallyMoved = 1
        while(grid[vector.x, vector.y] == ' ') {
          vector.y = (vector.y - 1).mod(grid.height)
          actuallyMoved++
        }
        if(grid[vector.x, vector.y] == '#') {
          vector.y = (vector.y + actuallyMoved).mod(grid.height)
          break
        }
      }
    }
    else -> {
      throw Exception("vector going unknown direction ${vector.direction}")
    }
  }
}

fun moveNPart2(vector: Vector, grid: Grid, n: Int) {
  when(vector.direction) {
    'R' -> {
      for(move in 0 until n) {
        vector.x = (vector.x + 1).mod(grid.width)
        var actuallyMoved = 1
        while(grid[vector.x, vector.y] == ' ') {
          vector.x = (vector.x + 1).mod(grid.width)
          actuallyMoved++
        }
        if(grid[vector.x, vector.y] == '#') {
          vector.x = (vector.x - actuallyMoved).mod(grid.width)
          break
        }
      }
    }
    'D' -> {
      for(move in 0 until n) {
        vector.y = (vector.y + 1).mod(grid.height)
        var actuallyMoved = 1
        while(grid[vector.x, vector.y] == ' ') {
          vector.y = (vector.y + 1).mod(grid.height)
          actuallyMoved++
        }
        if(grid[vector.x, vector.y] == '#') {
          vector.y = (vector.y - actuallyMoved).mod(grid.height)
          break
        }
      }
    }
    'L' -> {
      for(move in 0 until n) {
        vector.x = (vector.x - 1).mod(grid.width)
        var actuallyMoved = 1
        while(grid[vector.x, vector.y] == ' ') {
          vector.x = (vector.x - 1).mod(grid.width)
          actuallyMoved++
        }
        if(grid[vector.x, vector.y] == '#') {
          vector.x = (vector.x + actuallyMoved).mod(grid.width)
          break
        }
      }
    }
    'U' -> {
      for(move in 0 until n) {
        vector.y = (vector.y - 1).mod(grid.height)
        var actuallyMoved = 1
        while(grid[vector.x, vector.y] == ' ') {
          vector.y = (vector.y - 1).mod(grid.height)
          actuallyMoved++
        }
        if(grid[vector.x, vector.y] == '#') {
          vector.y = (vector.y + actuallyMoved).mod(grid.height)
          break
        }
      }
    }
    else -> {
      throw Exception("vector going unknown direction ${vector.direction}")
    }
  }
}

fun walkGrid(grid: Grid, instructions: String, part2: Boolean = false): Vector {
  val vector = findStartPosition(grid)

  var sIndex = 0
  while(sIndex < instructions.length) {
    /* read next number */
    var n = 0
    while(sIndex < instructions.length && instructions[sIndex] != 'R'
        && instructions[sIndex] != 'L') {
      n = n * 10 + instructions[sIndex].toString().toInt()
      sIndex += 1
    }

    /* move n steps */
    if(part2)
      moveNPart2(vector, grid, n)
    else
      moveN(vector, grid, n)

    /* turn, if so indicated */
    if(sIndex < instructions.length) {
      if(instructions[sIndex] == 'L') {
        vector.direction = directionChar[
          (directionCode[vector.direction]!! - 1).mod(4)]!!
      }
      else {
        vector.direction = directionChar[
          (directionCode[vector.direction]!! + 1).mod(4)]!!
      }
      sIndex += 1
    }
  }

  return vector
}

fun Part1(input:String) : Boolean {
  val lines = input.trimEnd().split("\n")
  val grid = readGrid(lines)
  println(grid)
  val vector = walkGrid(grid, lines[lines.size - 1])
  println("Finish at ${vector.x+1},${vector.y+1} facing" +
     " ${vector.direction} (${directionCode[vector.direction]}):" +
     " ${1000*(vector.y + 1) + 4*(vector.x + 1) + directionCode[vector.direction]!!}")
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
