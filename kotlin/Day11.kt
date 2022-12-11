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
  var items: MutableList<Long>,
  var operation: (Long) -> Long,
  var test: Long,
  var trueRecipient: Int,
  var falseRecipient: Int,
  var nItemsInspected: Long = 0,
)

val testMonkeys = arrayOf(
  Monkey(mutableListOf(79L,98L), { x -> x * 19L }, 23L, 2, 3),
  Monkey(mutableListOf(54L,65L,75L,74L), { x -> x + 6L }, 19L, 2, 0),
  Monkey(mutableListOf(79L,60L,97L), { x -> x * x }, 13L, 1, 3),
  Monkey(mutableListOf(74L), { x -> x + 3L }, 17L, 0, 1),
)
val problemMonkeys = arrayOf(
  Monkey(mutableListOf(97L,81L,57L,57L,91L,61L), { x -> x * 7L }, 11L, 5, 6),
  Monkey(mutableListOf(88L,62L,68L,90L), { x -> x * 17L }, 19L, 4, 2),
  Monkey(mutableListOf(74L,87L), { x -> x + 2L }, 5L, 7, 4),
  Monkey(mutableListOf(53L,81L,60L,87,90,99,75L), { x -> x + 1L }, 2L, 2, 1),
  Monkey(mutableListOf(57L), { x -> x + 6 }, 13L, 7, 0),
  Monkey(mutableListOf(54L,84L,91L,55L,59L,72L,75L,70L), { x -> x * x }, 7L, 6, 3),
  Monkey(mutableListOf(95L,79L,79L,68L,78L), { x -> x + 3L }, 3L, 1, 3),
  Monkey(mutableListOf(61L,97L,67L), { x -> x + 4L }, 17L, 0, 5),
)

fun processMonkey(monkeys: Array<Monkey>, monkeyIndex: Int,
    worryDivisor: Long, testModulus: Long) {
  val monkey = monkeys[monkeyIndex]

  /* process each item the monkey is carrying */
  val monkeyitems = monkey.items
  monkey.items = mutableListOf<Long>()
  for(item in monkeyitems) {
    //println("Processing monkey $monkeyIndex item $item")
    val newItem = (monkey.operation(item) / worryDivisor) % testModulus
    val recipient = if(newItem % monkey.test == 0L) monkey.trueRecipient else monkey.falseRecipient
    monkeys[recipient].items.add(newItem)
    //println("  final value $newItem for monkey $recipient")
    monkey.nItemsInspected += 1L
  }
}

fun doProblem(nrounds: Long, test: Boolean, worryDivisor: Long) {
  val monkeys = if(test) testMonkeys else problemMonkeys

  /* compute test modulus by taking product of all monkeys' tests */
  var testModulus = 1L
  for(monkey in monkeys) {
    testModulus *= monkey.test
  }

  for(round in 0 until nrounds) {
    //println("round $round")
    for(monkeyIndex in 0 until monkeys.size) {
      //println("Processing monkey $monkeyIndex")
      processMonkey(monkeys, monkeyIndex, worryDivisor, testModulus)
    }
  }

  val maxItemsInspected = arrayOf(0L, 0L)
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
}

fun Part1(test: Boolean) : Boolean {
  doProblem(20L, test, 3L)
  return true
}

fun Part2(test: Boolean) : Boolean {
  doProblem(10000L, test, 1L)
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
