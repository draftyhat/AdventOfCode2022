/* compile with
   kotlinc Grid.kt Day15.kt -include-runtime -d Day15.jar && java -jar Day15.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream
import draftyhat.Point
import java.time.LocalTime

val DAY=15
val YEAR=2022
val INPUTDIR="../input"

class Range(val minValue: Int, val maxValue: Int) {
  /* return union of ranges if ranges intersect, else null */
  fun union(other: Range) : Range? {
    if(other.maxValue < (minValue - 1) || other.minValue > (maxValue + 1))
      return null
    return Range(Math.min(minValue, other.minValue), Math.max(maxValue, other.maxValue))
  }

  override operator fun equals(other: Any?): Boolean {
    return other is Range && minValue == other.minValue && maxValue == other.maxValue
  }

  override fun toString(): String {
    return "[" + minValue.toString() + "," + maxValue.toString() + "]"
  }
}

class Sensor(val location: Point, val beacon: Point) {
  val distance = Math.abs(location.x - beacon.x) + Math.abs(location.y - beacon.y)
  val minimumDetectedRow = location.y - distance
  val maximumDetectedRow = location.y + distance
  val minimumDetectedColumn = location.x - distance
  val maximumDetectedColumn = location.x + distance
  init {
    println("Creating sensor at $location, distance $distance rows $minimumDetectedRow - $maximumDetectedRow")
  }
  fun inRange(p: Point): Boolean {
    /* returns True if this point is not a beacon location */
    val newBeaconDistance = Math.abs(p.x - location.x) + Math.abs(p.y - location.y)
    return newBeaconDistance <= distance
  }
  fun getRangeInRow(row: Int): Range? {
    /* get the range this sensor covers in the given row, if any */
    if(row < minimumDetectedRow || row > maximumDetectedRow)
      return null
    val rangeWidth = distance - Math.abs(row - location.y)
    return Range(location.x - rangeWidth, location.x + rangeWidth)
  }

  override fun toString(): String {
    return "sensor$location distance $distance"
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

fun Part2(input:String, test: Boolean) : Boolean {
  val sensorList = readInput(input)
  val rows = if(test) 20 else 4000000
  val columns = if(test) 20 else 4000000

  /* sort the sensors by minimum detected row */
  sensorList.sortBy { it.minimumDetectedRow }

  /* for each row */
  for(row in 0..rows) {
    /* find all sensors that intersect this row. For each of those, record the
     * ranges covered by that sensor */

    val ranges = mutableListOf<Range>()
    for(sensor in sensorList) {
      val range = sensor.getRangeInRow(row)
      //println("$sensor, range in row $row $range")
      if(range != null)
        ranges.add(range)
    }

    //println("---- ranges:")
    //println(ranges)
    ranges.sortBy { it.minValue }
    var merged = true
    while(merged && ranges.size > 1) {
      merged = false
      val toMerge = ranges.removeAt(0)
      for(rangeIndex in 0 until ranges.size) {
        val newRange = toMerge.union(ranges[rangeIndex])
        //println("----  merged $toMerge, ${ranges[rangeIndex]} -> $newRange")
        if(newRange != null) {
          ranges.removeAt(rangeIndex)
          ranges.add(0, newRange)
          merged = true
        }
      }
    }
    //println("----  ranges at end $ranges")
    /* if we were unable to merge the ranges down to one, our missing spot is
       the end of the first range plus one. Oh, whoops, we lost the first
       range. It's 1 before the beginning of the first range. */
    if(ranges[0].minValue > 0) {
      println("Found missing spot! (${ranges[0].minValue - 1},$row), frequency ${(ranges[0].minValue.toLong() - 1L) * 4000000L + row.toLong()}")
      return true
    } else if(ranges[0].maxValue < columns) {
      println("Found missing spot! ($columns,$row), frequency ${columns.toLong() * 4000000L + row.toLong()}")
      return true
    }
    /* otherwise, the ranges cover this entire row. Keep searching. */
  }

  return false
}

fun Part2_old(input:String, test: Boolean) : Boolean {
  val sensorList = readInput(input)
  val rows = if(test) 20 else 4000000
  val columns = if(test) 20 else 4000000

  /* sort the sensors by minimum detected row */
  sensorList.sortBy { it.minimumDetectedRow }

  /* check each spot in the grid */
  var inrange = false
  for(row in 0..rows) {
    /* Only check sensors who can detect something in range of this row. */
    var currentSensorList = mutableListOf<Sensor>()
    //println("-- row $row")
    for(sensor in sensorList) {
      if(sensor.minimumDetectedRow <= row && row <= sensor.maximumDetectedRow) {
        currentSensorList.add(sensor)
        //println("--   adding sensor ${sensor.location} distance ${sensor.distance}")
      }
    }

    if((row % 1000) == 0) {
      println("-- starting row $row at ${LocalTime.now()}")
    }
    for(column in 0..columns) {
      inrange = false
      while(currentSensorList.size > 0 && currentSensorList[0].maximumDetectedColumn < column)
        currentSensorList.removeAt(0)

      for(sensor in currentSensorList) {
        if(Math.abs(column - sensor.location.x) + Math.abs(row - sensor.location.y) <=
            sensor.distance) {
          //println("--  Point ($row,$column) in range of sensor at ${sensor.location} (distance ${Math.abs(column - sensor.location.x) + Math.abs(row-sensor.location.y)}, sensor distance ${sensor.distance})")
          inrange = true
          break
        }
      }
      if(inrange == false) {
        println("Found beacon position! $column $row, frequency ${row.toLong() * 4000000L + column.toLong()}")
        break
      }
    }
    if(inrange == false)
        break
  }
  return inrange == false
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
          success = Part2(testInput, true)
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
      success = Part2(inputText, false)
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
