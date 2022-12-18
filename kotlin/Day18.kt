/* compile with
   kotlinc Grid3D.kt Day18.kt -include-runtime -d Day18.jar && java -jar Day18.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream
import draftyhat.Face
import draftyhat.Box
import draftyhat.Point3D

val DAY=18
val YEAR=2022
val INPUTDIR="../input"


fun readBoxes(input: String): MutableList<Box> {
  val retval = mutableListOf<Box>()
  for(line in input.trim().split('\n')) {
    val point = Point3D(line)
    retval.add(Box(point, Point3D(point.x + 1, point.y + 1, point.z + 1)))
  }
  return retval
}

fun knownExterior(box: Box, minx: Int, miny: Int, minz: Int,
    maxx: Int, maxy: Int, maxz: Int): Boolean {
  return box.a.x < minx || box.a.x > maxx ||
    box.a.y < miny || box.a.y > maxy ||
    box.a.z < minz || box.a.z > maxz
}

fun eliminateInteriorBoxes(boxes: MutableList<Box>): MutableList<Box> {
  /* well, should've left them as 3D points */
  /* diagonals don't count
     so, a box is connected to another box if they share a face
     for each box
       find all boxes connected to every face
       find all boxes connected to those, until we find a known exterior box
       a known exterior box is one with x, y, or z smaller than min or greater
         than max in the set
   */
  /* find min/max x, y, and z (this is min/max of the lower left corner of the
   * box) */
  val boxSet = boxes.toMutableSet()
  val firstbox = boxSet.first()
  var minx = firstbox.a.x
  var miny = firstbox.a.y
  var minz = firstbox.a.z
  var maxx = firstbox.a.x
  var maxy = firstbox.a.y
  var maxz = firstbox.a.z
  for(box in boxes) {
    minx = Math.min(box.a.x, minx)
    miny = Math.min(box.a.y, minx)
    minz = Math.min(box.a.z, minx)
    maxx = Math.max(box.a.x, maxx)
    maxy = Math.max(box.a.y, maxy)
    maxz = Math.max(box.a.z, maxz)
  }

  /* set of boxes not in boxSet that are known to be exterior */
  var knownExterior = setOf<Box>()
  /* set of boxSet to add to this set so that all boxSet will be interior */
  var newInterior = setOf<Box>()

  /* for each box */
  for(box in boxSet) {
    //println("--- box $box ---")
    /* for each box adjacent to this one */
    for(neighbor in box.getNeighbors()) {
      //println("  - neighbor $neighbor -")
      /* if this neighbor is already included, skip it */
      if(neighbor in boxSet || neighbor in newInterior ||
          neighbor in knownExterior)
        continue

      /* find all boxes connected to this one, until we find one that's
         known to be exterior/interior, or can't find any more */
      val connecteds = mutableSetOf<Box>()
      val toSearch = mutableSetOf<Box>(neighbor)
      var interior = true
      while(toSearch.size > 0 && interior) {
        val current = toSearch.first()
        toSearch.remove(current)
        connecteds.add(current)
        if(knownExterior(current, minx, miny, minz, maxx, maxy, maxz)) {
          //println("  current $current calculates exterior")
          interior = false
          break
        }
        for(newNeighbor in current.getNeighbors()) {
          /* ----- debugging -----
          if(current.a == Point3D(2,2,5)) {
            println("looking at ${current.a} neighbor ${newNeighbor.a}")
            println("     in boxSet? ${newNeighbor in boxSet}")
            println("     boxSet.contains? ${boxSet.contains(newNeighbor)}")
          } */

          if(newNeighbor in boxSet)
            continue
          if(newNeighbor in knownExterior) {
            //println("  neighbor $newNeighbor is int knownExterior")
            interior = false
            break
          } else {
            /* don't know what to do with this box. Keep searching. */
            if(!(newNeighbor in connecteds))
              toSearch.add(newNeighbor)
          }
        }
      }

      //println("   Done loop, connecteds $connecteds, interior $interior")

      if(interior) {
        /* add our collection of connected boxSet to those needed to fill in
           the interior */
        newInterior = newInterior.union(connecteds)
        println("Found new interiors $connecteds")
      } else {
        knownExterior = knownExterior.union(connecteds)
      }
    }
  }

  return boxSet.union(newInterior).toMutableList()
}

fun getAllFaces(input: String, eliminateInterior: Boolean): MutableSet<Face> {
  val retval = mutableSetOf<Face>()
  var boxes = readBoxes(input)
  if(eliminateInterior)
    boxes = eliminateInteriorBoxes(boxes)
  for(box in boxes) {
    for(face in box.getFaces()) {
      if(face in retval)
        retval.remove(face)
      else
        retval.add(face)
    }
  }
  return retval
}


fun Part1(input:String) : Boolean {
  val faces = getAllFaces(input, false)
  println("Found ${faces.size} faces")
  return true
}

fun Part2(input:String) : Boolean {
  val faces = getAllFaces(input, true)
  println("Found ${faces.size} faces")
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
