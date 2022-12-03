/* compile with
   kotlinc Day01.kt -include-runtime -d Day01.jar && java -jar Day01.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=3
val YEAR=2022
val INPUTDIR="../input"

val priorities = arrayOf('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z')


fun Part1(input:String) : Boolean {
  var total = 0
  input.split("\n").forEachIndexed { index, line ->
    if(line != "")
    {
      /* split the items */
      val compartment0 = line.substring(0, line.length/2)
      val compartment0set: MutableSet<Char> = mutableSetOf()
      for(ch in compartment0.toList())
        compartment0set.add(ch)
      val compartment1 = line.substring(line.length/2)
      /* find the duplicate item */
      var item: Char = '0'
      for(ch in compartment1.toCharArray()) {
        if(compartment0set.contains(ch)) {
          item = ch
          break
        }
      }

      /* add the item's priority to the total */
      if(item == '0')
        throw Exception("ERROR: Could not find item in both compartments," +
           " line $index, compartments $compartment0,$compartment1")
      total += priorities.indexOf(item) + 1
    }
  }
  println("Priority sum: $total")
  return true
}

fun Part2(input:String) : Boolean {
  var total = 0
  /* read 3 lines at a time */
  val groupsize = 3
  val lines = input.strip().split("\n")
  for(index in 0 until lines.size step groupsize) {
    /* for each group of elves */
    val sets = mutableListOf<MutableSet<Char>>()
    for(setindex in index until index + groupsize - 1) {
      val line = lines[setindex]
      /* turn the line into a set */
      val lineset = mutableSetOf<Char>()
      for(ch in line.toList())
        lineset.add(ch)
      sets.add(lineset)
    }
    /* pick up last line */
    val line = lines[index + groupsize - 1]
    /* for each char, find out if it's in the other two sets */
    var badge = '0'
    for(ch in line.toList()) {
      var found = true
      for(set in sets) {
        if(!(ch in set)) {
          found = false
          break
        }
      }
      if(found) {
        badge = ch
        break
      }
    }

    /* add the item's priority to the total */
    if(badge == '0')
      throw Exception("ERROR: Could not find badge among all compartments," +
         " line $index")
    total += priorities.indexOf(badge) + 1
  }
  println("Priority sum: $total")
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
