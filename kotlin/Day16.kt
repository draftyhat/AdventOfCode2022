/* compile with
   kotlinc Day16.kt -include-runtime -d Day16.jar && java -jar Day16.jar -1

   plan: try all paths. Record least time with most flow rate arrival
   at each valve; discard paths under that.
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=16
val YEAR=2022
val INPUTDIR="../input"

class Valve(val name: String, val flowRate: Int,
  val connectedTo: List<String>, var open: Boolean = false) {
  /* time it takes to get to any other valve */
  val timeMap = mutableMapOf<String, Int>()

  override fun toString(): String {
    val openstring = if(open) "O" else "|"
    return "Valve<$name> $flowRate $openstring"
  }
}

fun readValves(input: String) : MutableMap<String, Valve> {
  val linere = Regex("""Valve ([A-Z][A-Z]) has flow rate=(\d+); tunnels?+ leads?+ to valves?+ (.*)""")

  val valves = mutableMapOf<String, Valve>()
  /* parse valves */
  for(line in input.trim().split('\n')) {
    val match = linere.matchEntire(line)
    val matchgroup = match?.groupValues
    if(matchgroup != null) {
      valves[matchgroup[1]] = Valve(matchgroup[1], matchgroup[2].toInt(),
          matchgroup[3].split(", "))
    } else {
      println("WARNING: could not read line \"$line\"")
    }
  }

  return valves
}

class Path(val flowRate: Int, val flowed: Int, val atValve: String,
    val justOpened: Boolean,
    val open: MutableSet<String> = mutableSetOf<String>())

fun buildTimeMap(valves: MutableMap<String, Valve>) {
  /* map all valves at increasing depth */
  valves.forEach { entry -> 
    val valve = entry.value
    var depth = 1
    var connectedThisDepth = mutableListOf<String>()
    for(connected in valve.connectedTo)
      connectedThisDepth.add(connected)
    while(valve.timeMap.size < (valves.size - 1)) {
      val connectedNextDepth = mutableListOf<String>()
      for(connected in connectedThisDepth) {
        if(!(connected in valve.timeMap) && connected != valve.name) {
          val connectedValve = valves[connected]
          if(connectedValve != null) {
            valve.timeMap[connected] = depth
            for(nextConnected in connectedValve.connectedTo) {
              if(!(nextConnected in valve.timeMap))
                connectedNextDepth.add(nextConnected)
            }
          }
        }
      }
      connectedThisDepth = connectedNextDepth
      depth += 1
    }
  }
}

fun Part1(input:String) : Boolean {
  val valves = readValves(input)
  val totalTime = 30
  
  /* calculate the time it takes to get from any valve to any other */
  buildTimeMap(valves)

  /* for each step, go open the valve that offers the most flow */
  var totalFlow = 0
  val open = mutableSetOf<String>()
  var currentValve = valves["AA"]
  var timeLeft = totalTime
  while(timeLeft > 0) {
    var maxNextValve = currentValve
    var maxNextFlow = 0
    var timeTaken = timeLeft
    /* find valve to open */
    println("Currently at ${currentValve!!.name}, timeMap ${currentValve!!.timeMap}")
    currentValve!!.timeMap.forEach { entry ->
      if(!(entry.key in open)) {
        val nextValve = valves[entry.key]
        /* note, it takes 1 time to open the valve */
        val flow = nextValve!!.flowRate * ((timeLeft - 1) - entry.value)
        if(flow > maxNextFlow) {
          maxNextFlow = flow
          /*  +1 for 1 time to open the valve */
          timeTaken = entry.value + 1
          maxNextValve = nextValve
        }
      }
    }
    println("Using $timeTaken steps to move to ${maxNextValve!!.name}, total flow contributed $maxNextFlow")

    /* open the valve we found */
    open.add(maxNextValve!!.name)
    totalFlow += maxNextFlow
    timeLeft -= timeTaken
    currentValve = maxNextValve
  }

  println("Final flow: $totalFlow")
  return true
}


fun Part1_bruteForce(input:String) : Boolean {
  val valves = readValves(input)
  var paths = mutableListOf(Path(0, 0, "AA", false))
  val timeLeft = 30

  /* start at first valve. Calculate max flow achieved by going straight
     to any other valve. Always take the largest? */

  for(time in 0 until timeLeft) {
    println("Time $time, ${paths.size} paths")
    val newPaths = mutableListOf<Path>()
    for(path in paths) {
      /* 2 options: either open valve, or move to next valve */
      val valveAt = valves[path.atValve]
      if(valveAt != null) {
        /*  don't bother opening open valves, or valves with flow rate 0 */
        if(!(valveAt.name in path.open) || valveAt.flowRate > 0) {
          /* open valve */
          newPaths.add(Path(path.flowRate + valveAt.flowRate,
                path.flowed + path.flowRate, path.atValve,
                true))
        }
        /* move to each of the connected valves */
        for(nextValve in valveAt.connectedTo) {
          newPaths.add(Path(path.flowRate, path.flowed + path.flowRate,
              nextValve, false))
        }
      }
    }
    paths = newPaths
  }

  /* find greatest amount flowed */
  var greatestFlowed = 0
  for(path in paths)
    greatestFlowed = Math.max(greatestFlowed, path.flowed)

  println("Found greatest flowed $greatestFlowed")
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
