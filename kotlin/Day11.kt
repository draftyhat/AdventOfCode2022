/* compile with
   kotlinc Day01.kt -include-runtime -d Day01.jar && java -jar Day01.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=11
val YEAR=2022
val INPUTDIR="../input"


class Monkey(
  var items: MutableList<Int>,
  var operation: (Int) -> Int,
  var test: Int,
  var trueRecipient: Int,
  var falseRecipient: Int,
  var nItemsInspected: Int = 0,
)

val testMonkeys = arrayOf(
  Monkey(mutableListOf(79,98), { x -> x * 19 }, 23, 2, 3),
  Monkey(mutableListOf(54,65,75,74), { x -> x + 6 }, 19, 2, 0),
  Monkey(mutableListOf(79,60,97), { x -> x * x }, 13, 1, 3),
  Monkey(mutableListOf(74), { x -> x + 3 }, 17, 0, 1),
)
val problemMonkeys = arrayOf(
  Monkey(mutableListOf(97,81,57,57,91,61), { x -> x * 7 }, 11, 5, 6),
  Monkey(mutableListOf(88,62,68,90), { x -> x * 17 }, 19, 4, 2),
  Monkey(mutableListOf(74,87), { x -> x + 2 }, 5, 7, 4),
  Monkey(mutableListOf(53,81, 60, 87, 90, 99, 75), { x -> x + 1 }, 2, 2, 1),
  Monkey(mutableListOf(57), { x -> x + 6 }, 13, 7, 0),
  Monkey(mutableListOf(54,84,91,55,59,72,75,70), { x -> x * x }, 7, 6, 3),
  Monkey(mutableListOf(95,79,79,68,78), { x -> x + 3 }, 3, 1, 3),
  Monkey(mutableListOf(61,97,67), { x -> x + 4 }, 17, 0, 5),
)

fun processMonkey(monkeys: Array<Monkey>, monkeyIndex: Int) {
  val monkey = monkeys[monkeyIndex]

  /* process each item the monkey is carrying */
  val monkeyitems = monkey.items
  monkey.items = mutableListOf<Int>()
  for(item in monkeyitems) {
    //println("Processing monkey $monkeyIndex item $item")
    val newItem = monkey.operation(item) / 3
    val recipient = if(newItem % monkey.test == 0) monkey.trueRecipient else monkey.falseRecipient
    monkeys[recipient].items.add(newItem)
    //println("  final value $newItem for monkey $recipient")
    monkey.nItemsInspected += 1
  }
}

fun Part1(test: Boolean) : Boolean {
  val nrounds = 20
  val monkeys = if(test) testMonkeys else problemMonkeys

  for(round in 0 until nrounds) {
    //println("round $round")
    for(monkeyIndex in 0 until monkeys.size) {
      //println("Processing monkey $monkeyIndex")
      processMonkey(monkeys, monkeyIndex)
    }
  }

  val maxItemsInspected = arrayOf(0, 0)
  for(monkey in monkeys) {
    if(monkey.nItemsInspected > maxItemsInspected[0]) {
      if(monkey.nItemsInspected > maxItemsInspected[1]) {
        maxItemsInspected[0] = maxItemsInspected[1]
        maxItemsInspected[1] = monkey.nItemsInspected
      } else
        maxItemsInspected[0] = monkey.nItemsInspected
    }
  }

  println("Score: ${maxItemsInspected[0]} * ${maxItemsInspected[1]} = ${maxItemsInspected[0] * maxItemsInspected[1]}")

  return true
}

fun Part2(test: Boolean) : Boolean {
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
          //success = Part1(testInput)
          success = Part1(true)
        }
        if(success && "-t2" in args) {
          //success = Part2(testInput)
          success = Part2(true)
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
      //success = Part1(inputText)
      success = Part1(false)
      ranN += 1
    }
    if(success == true && "-2" in args) {
      println("--- running part 2 ---")
      //success = Part2(inputText)
      success = Part2(false)
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
