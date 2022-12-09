/* compile with
   kotlinc Day08.kt Grid.kt -include-runtime -d Day08.jar && java -jar Day08.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream
import draftyhat.Point
import draftyhat.Grid
import draftyhat.readGridItem

val DAY=8
val YEAR=2022
val INPUTDIR="../input"


fun Part1(input:String) : Boolean {
  val inputLines = input.trim().split("\n")
  val grid = Grid(inputLines[0].length, inputLines.size,
      {x,y,z -> readGridItem(x,y,z) }, inputLines)
  val seen = Grid(inputLines[0].length, inputLines.size,
      {_,_,_ -> 0}, 0)

  /* walk each row in the grid from left to right and then right to left.
     Mark all trees we can see in the seen grid. */
  for(row in 0 until grid.height) {
    var column = 0
    var last = -1
    while(column < grid.width) {
      if(last < grid[column, row]) {
        seen[column, row] = 1
        last = grid[column, row]
      }
      column += 1
    }
    column = grid.width - 1
    last = -1
    while(column >= 0) {
      if(last < grid[column, row]) {
        seen[column, row] = 1
        last = grid[column, row]
      }
      column -= 1
    }
  }
  /* walk each column in the grid from top to bottom and then bottom to top.
     Mark all trees we can see in the seen grid. */
  for(column in 0 until grid.width) {
    var row = 0
    var last = -1
    while(row < grid.height) {
      println("Moving right in row $row found ${grid[column, row]} (last $last) at $column,$row")
      if(last < grid[column, row]) {
        seen[column, row] = 1
        last = grid[column, row]
      }
      row += 1
    }
    row = grid.height - 1
    last = -1
    while(row >= 0) {
      println("Moving left in row $row found ${grid[column, row]} (last $last) at $column,$row")
      if(last < grid[column, row]) {
        seen[column, row] = 1
        last = grid[column, row]
      }
      row -= 1
    }
  }

  /* add up all elements in seen to find how many trees we saw */
  println("Found ${seen.sum()} visible trees")
  println(grid)
  println(seen)

  return true
}

fun getScenicScore(grid: Grid, column: Int, row: Int): Int {
  /* look left from this tree */
  var seenLeft = 0
  var tmpcolumn = column - 1
  while(tmpcolumn >= 0) {
    seenLeft++
    /* quit when we see a tree equal to or taller than ours */
    if(grid[tmpcolumn,row] >= grid[column, row])
      break
    tmpcolumn--
  }

  /* look right from this tree */
  var seenRight = 0
  tmpcolumn = column + 1
  while(tmpcolumn < grid.width) {
    seenRight++
    /* quit when we see a tree equal to or taller than ours */
    if(grid[tmpcolumn,row] >= grid[column, row])
      break
    tmpcolumn++
  }

  /* look up from this tree */
  var seenUp = 0
  var tmprow = row - 1
  while(tmprow >= 0) {
    seenUp++
    /* quit when we see a tree equal to or taller than ours */
    if(grid[column,tmprow] >= grid[column, row])
      break
    tmprow--
  }

  /* look down from this tree */
  var seenDown = 0
  tmprow = row + 1
  while(tmprow < grid.height) {
    seenDown++
    /* quit when we see a tree equal to or taller than ours */
    if(grid[column,tmprow] >= grid[column, row])
      break
    tmprow++
  }

  println("Score at ($column,$row): $seenLeft * $seenRight * $seenUp * $seenDown = ${seenLeft * seenRight * seenUp * seenDown}")
  return seenLeft * seenRight * seenUp * seenDown
}

fun Part2(input:String) : Boolean {
  val inputLines = input.trim().split("\n")
  val grid = Grid(inputLines[0].length, inputLines.size,
      {x,y,z -> readGridItem(x,y,z) }, inputLines)

  var maxScore = 0
  for(x in 1 until (grid.width - 1)) {
    for(y in 1 until (grid.height - 1)) {
      val score = getScenicScore(grid, x, y)
      if(score > maxScore)
        maxScore = score
    }
  }

  println("Max scenic score: $maxScore")
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
