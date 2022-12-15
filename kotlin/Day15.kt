/* compile with
   kotlinc Grid.kt Day15.kt -include-runtime -d Day15.jar && java -jar Day15.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream
import draftyhat.Point

val DAY=15
val YEAR=2022
val INPUTDIR="../input"

class Sensor(val location: Point, val beacon: Point, var distance: Int = 0) {
  init {
    distance = Math.abs(location.x - beacon.x) + Math.abs(location.y - beacon.y)
  }
  fun inRange(p: Point): Boolean {
    /* returns True if this point is not a beacon location */
    val newBeaconDistance = Math.abs(p.x - location.x) + Math.abs(p.y - location.y)
    return newBeaconDistance <= distance
  }
}

fun readInput(input: String): MutableList<Sensor> {
  val sensorList = mutableListOf<Sensor>()

  //val linere = Regex("""Sensor at x=\(-??\d+\), y=\(-??\d+\): closest beacon is at x=\(-??\d+\), y=\(-??\d+\)""")
  val linere = Regex("""Sensor at x=(-??\d+), y=(-??\d+): closest beacon is at x=(-??\d+), y=(-??\d+)""")

  for(line in input.trim().split('\n')) {
    val match = linere.matchEntire(line)
    if(match != null) {
      val matchGroupValues = match.groupValues
      val newSensor = Sensor(
          Point(matchGroupValues[1].toInt(), matchGroupValues[2].toInt()),
          Point(matchGroupValues[3].toInt(), matchGroupValues[4].toInt()))
      sensorList.add(newSensor)
    }
  }

  return sensorList
}

fun Part1(input:String, test: Boolean) : Boolean {
  val sensorList = readInput(input)
  val row = if(test) 10 else 2000000
  val positions = mutableSetOf<Int>()
  val beaconOrSensorPositions = mutableSetOf<Int>()

  /* check each spot in the line */
  for(sensor in sensorList) {
    /* first check to see if the sensor is within distance of this line at
       all */
    if(Math.abs(row - sensor.location.y) > sensor.distance) {
      continue
    }

    /* find the x-coordinate of all points within range of this line */
    for(xdiff in 0..sensor.distance - Math.abs(sensor.location.y - row)) {
      positions.add(sensor.location.x - xdiff)
      positions.add(sensor.location.x + xdiff)
    }
    /* if the beacon or sensor is in this row, eliminate that from the list of
     * positions */
    if(sensor.location.y == row)
      beaconOrSensorPositions.add(sensor.location.x)
    if(sensor.beacon.y == row)
      beaconOrSensorPositions.add(sensor.beacon.x)
  }


  println("Found ${positions.size - beaconOrSensorPositions.size} occupied positions")
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
          success = Part1(testInput, true)
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
      success = Part1(inputText, false)
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
