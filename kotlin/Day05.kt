/* compile with
   kotlinc Day01.kt -include-runtime -d Day01.jar && java -jar Day01.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=5
val YEAR=2022
val INPUTDIR="../input"

fun printCrates(crates:Array<MutableList<Char>>) {
  crates.forEachIndexed { index, crateStack ->
    print("${index + 1}: ")
    for(crate in crateStack)
      print(crate)
    println("")
  }
}

/* read a single line of crate contents */
fun stackCrates(line: String, crates:Array<MutableList<Char>>) {
  for(crateIndex in 0 until crates.size) {
    val stringIndex = crateIndex * 4 + 1
    if(stringIndex >= line.length)
      break
    val ch: Char = line[stringIndex]
    /* if this is a crate, put it on the top of the indicated crate stack */
    if(ch.code >= 'A'.code && ch.code <= 'Z'.code)
      crates[crateIndex].add(ch)
  }
}

fun readCrates(input: String,
    crateMover9001: Boolean = false): Array<MutableList<Char>> {
  val lines : List<String> = input.split("\n")

  /* read lines until we get to the line indicating the crates
     see sample data */
  var crateBaseIndex = 0
  while(lines[crateBaseIndex].trim()[0] == '[')
    crateBaseIndex++

  /* count number of crates */
  val nCrates = lines[crateBaseIndex].length / 4 + 1

  /* initialize crate stacks */
  val crates = Array<MutableList<Char>>(nCrates) { mutableListOf<Char>() }

  /* read initial stacks */
  for(index in crateBaseIndex - 1 downTo 0)
    stackCrates(lines[index], crates)

  moveCrates(lines, crateBaseIndex + 1, crates, crateMover9001)

  return crates
}

/* read each move command line, move items between stacks */
fun moveCrates(inputLines: List<String>, inputIndex: Int,
    crates:Array<MutableList<Char>>, crateMover9001: Boolean = false) {
  for(index in inputIndex until inputLines.size) {
    val line = inputLines[index]
    if(line.trim() != "") {
      /* line looks like "move n from stack# to stack#"
         assume stack#s are legal! Note they're 1-based, though */
      /* hmm, no group functionality in Kotlin regexp? Doing this
         the hard way... */
      val moveFromLine = line.split(" from ")
      /* strip "move " from the start to get number of crates to move */
      val sstr = moveFromLine[0].substring("move ".length)
      val nCratesToMove = moveFromLine[0].substring("move ".length).toInt()
      /* split out " to " to get stacks to move between */
      val stackNumbers = moveFromLine[1].split(" to ")
      val fromStack = stackNumbers[0].toInt() - 1
      val toStack = stackNumbers[1].toInt() - 1

      /* move crates */
      //println("---- moving $nCratesToMove crates from stack $fromStack to $toStack")
      //println("----  fromStack size ${crates[fromStack].size}")
      //printCrates(crates)
      if(crateMover9001) {
        printCrates(crates)
        for(i in nCratesToMove downTo 1) {
          crates[toStack].add(
              crates[fromStack].removeAt(crates[fromStack].size - i))
        }
      } else { /* part 1 */
        for(i in 0 until nCratesToMove)
          crates[toStack].add(
              crates[fromStack].removeAt(crates[fromStack].size - 1))
      }
    }
  }
}

fun Part1(input:String) : Boolean {
  val crates = readCrates(input)

  for(crate in crates) {
    print(crate[crate.size - 1])
  }
  println("")
  return true
}

fun Part2(input:String) : Boolean {
  val crates = readCrates(input, true)

  for(crate in crates) {
    print(crate[crate.size - 1])
  }
  println("")
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
