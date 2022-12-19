/* compile with
   kotlinc Day19.kt -include-runtime -d Day19.jar && java -jar Day19.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=19
val YEAR=2022
val INPUTDIR="../input"

/*
  products are ore robots, ore, clay robots, clay, obsidian robots, obsidian,
  geode robots, and geodes
  algorithm: prioritize the most valuable product. If you will gain
  enough resources to create both the lesser and the greater products before
  you will have enough to create the greater product, go ahead and create the
  lesser product

  working backwards, find min time it takes to create a geode robot, and how many
  of each robot will be required to achieve that?

  ore robot: 4 ore
  clay robot: 2 clay
  obsidian robot: 3 ore, 14 clay
  geode robot: 2 ore, 7 obsidian
*/

class Status(
  val products: MutableMap<String, Int> = mutableMapOf(
    "ore" to 0, "clay" to 0, "obsidian" to 0, "geode" to 0),
  val robots: MutableMap<String, Int> = mutableMapOf(
    "ore" to 1, "clay" to 0, "obsidian" to 0, "geode" to 0)) {
  override fun toString(): String {
    return "Status products: " + products.entries.joinToString(",") +
        "   robots: " + robots.entries.joinToString(",")
  }
}
class Blueprint(val number: Int,
    val robotCost: MutableMap<String, MutableMap<String, Int>> = mutableMapOf<String, MutableMap<String, Int>>()) {
  override fun toString(): String {
    var retval = "Blueprint $number:"
    for(robot in robotCost.keys) {
      retval = retval + " Each $robot robot costs " +
          robotCost[robot]!!.entries.joinToString(" and ") + "."
    }
    return retval
  }
}

fun readBlueprints(input: String): MutableList<Blueprint> {
  val blueprintre = Regex("""Blueprint (\d+):(.*)$""")
  val robotre = Regex("""Each ([a-z]*) robot costs (.*$)""")
  val retval = mutableListOf<Blueprint>()

  for(line in input.trim().split('\n')) {
    val blueprintgroups = blueprintre.matchEntire(line)!!.groupValues
    val blueprint = Blueprint(blueprintgroups[1].toInt())

    for(robot in blueprintgroups[2].trim().trim('.').split('.')) {
      val robotCost = mutableMapOf<String, Int>()
      val robotgroups = robotre.matchEntire(robot.trim())!!.groupValues
      for(cost in robotgroups[2].trim().split(" and ")) {
        val costsplit = cost.split(' ')
        robotCost[costsplit[1]] = costsplit[0].toInt()
      }
      blueprint.robotCost[robotgroups[1]] = robotCost
    }
    
    retval.add(blueprint)
  }
  return retval
}


fun findMaxGeodes(blueprint: Blueprint, nminutes: Int): Int {
  var statuses = mutableListOf<Status>(Status())

  /* find the maximum time at which we must have at least one robot of each
     type in order to create at least one geode. This will allow us to
     prune paths. */
  /* the cost of one clay is A ores to make a robot plus 1 day, so A+1 units
     the cost of one obsidian is B clays plus C ores, or A+

     in the example, Blueprint 1:
        Each ore robot costs 4 ore.
        Each clay robot costs 2 ore.
        Each obsidian robot costs 3 ore and 14 clay.
        Each geode robot costs 2 ore and 7 obsidian.
     working backwards, in order to create at least one geode, must have a
       geode robot by nminutes-1
     must have at least one obsidian robot by nminutes - 7, or
        must have 2 obsidian robots by nminutes - 3
     earliest a clay robot can appear: day 2
   */
  val minDaysToCreateRobot = mutableMapOf<String, Int>("ore" -> 0)
  val timeLimits = mutableMapOf<String, Int>()
  for(product in blueprint.robotCost.keys) {
    val robotCost = blueprint.robotCost[product]

  }

  for(time in 1..nminutes) {
    println("--Time $time-- ${statuses.size} statuses")
    val newStatuses = mutableListOf<Status>()
    for(status in statuses) {
      /* this status survives until next round */
      newStatuses.add(status)

      /* can we make a new robot? If so, choose to both make one and not by
         adding a status where we make one. The original status will never make
         a robot.
         We prune further by making a robot only if this is the first day we
         could have made the robot; doesn't make sense to make a robot on the
         second day, it could have been producing on the first day
         Can only make one robot per turn, thank goodness
       */
      for(newRobot in status.robots.keys) {
        var foundThisStep = false
        var canMake = true
        val robotCost = blueprint.robotCost[newRobot]
        for(requiredProduct in robotCost!!.keys) {
          val cost: Int = robotCost[requiredProduct]!!
          val have: Int = status.products[requiredProduct] ?: 0
          if(have < cost) {
            canMake = false
            break
          } else if(have < cost + status.robots[requiredProduct]!!) {
            /* only make a new robot if we just generated enough products to
             * create the robot. Else we could have created it last time, and
             * collected one more product. */
            foundThisStep = true
          }
        }
        if(canMake && foundThisStep) {
          /* make this robot! */
          val newStatus = Status(status.products.toMutableMap(),
              status.robots.toMutableMap())

          /* first have all existing robots produce one product */ 
          for(product in newStatus.robots.keys)
            newStatus.products[product] = (newStatus.products[product] ?: 0) +
              (newStatus.robots[product] ?: 0)

          /* add our new robot */
          newStatus.robots[newRobot] = (newStatus.robots[newRobot] ?: 0) + 1
          /* subtract the cost */
          for(requiredProduct in robotCost.keys) {
            val cost: Int = robotCost[requiredProduct]!!
            val have: Int = newStatus.products[requiredProduct] ?: 0
            newStatus.products[requiredProduct] = have - cost
          }
          newStatuses.add(newStatus)
        }
      }

      /* @TODO throw this status away if it can't make any geodes */
      /* all robots produce one product. Already did this for new statuses. */
      for(product in status.robots.keys)
        status.products[product] = (status.products[product] ?: 0) +
          (status.robots[product] ?: 0)
    }
    statuses = newStatuses

  }

  var maxGeodes = 0
  for(status in statuses)
    maxGeodes = Math.max(status.products["geode"] ?: 0, maxGeodes)

  println("****** blueprint ${blueprint.number} maxGeodes $maxGeodes")
  return maxGeodes
}

fun Part1(input:String) : Boolean {
  val nminutes = 24
  val blueprints = readBlueprints(input)
  var retval = 0
  for(blueprint in blueprints) {
    val maxGeodes = findMaxGeodes(blueprint, nminutes)
    retval += maxGeodes * blueprint.number
  }
  println("Answer: $retval")
  return false
}

fun Part2(input:String) : Boolean {
  val nminutes = 32
  /* elephants!!!!! */
  var blueprints = readBlueprints(input)
  if(blueprints.size > 3)
    blueprints = blueprints.subList(0, 3)

  var retval = 1
  for(blueprint in blueprints) {
    val maxGeodes = findMaxGeodes(blueprint, nminutes)
    retval *= maxGeodes
  }
  println("Answer: $retval")

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
