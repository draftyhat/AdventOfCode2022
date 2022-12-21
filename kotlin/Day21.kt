/* compile with
   kotlinc Day21.kt -include-runtime -d Day21.jar && java -jar Day21.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=21
val YEAR=2022
val INPUTDIR="../input"

fun addDependency(dependencies: MutableMap<String, MutableList<String>>,
    depender: String, dependsOn: String) {
  /* store that dependsOn provides info about depender */
  val found = dependencies[dependsOn]
  if(found != null)
    found.add(depender)
  else
    dependencies[dependsOn] = mutableListOf<String>(depender)
}

fun resolveEquation(firstValue: Long?, operation: String,
    secondValue: Long?): Long? {
  if(firstValue == null || secondValue == null)
    return null
  return when(operation) {
    "+" -> firstValue + secondValue
    "-" -> firstValue - secondValue
    "*" -> firstValue * secondValue
    "/" -> firstValue / secondValue
    "=" -> firstValue
    else -> null
  }
}

fun readAndSolve(input: String): Long {
  /* key: monkey, value: key's dependencies */
  val dependencies = mutableMapOf<String,MutableList<String>>()
  /* known values */
  val values = mutableMapOf<String, Long>()
  /* unparsed lines, keyed by lhs monkey */
  val lines = mutableMapOf<String, List<String>>()
  /* values to cascade */
  val toResolve = mutableListOf<String>()

  /* first attempt to parse each line */
  for(line in input.trim().split('\n')) {
     val linesplit = line.split(": ")
     try {
      values[linesplit[0]] = linesplit[1].toLong()
      toResolve.add(linesplit[0])
     } catch(e: Exception) {
       /* not a value line. Put it in unparsed. Also, parse out dependencies
          for this monkey. */
       val depsplit = linesplit[1].split(' ')
       addDependency(dependencies, linesplit[0], depsplit[0])
       addDependency(dependencies, linesplit[0], depsplit[2])
       lines[linesplit[0]] = depsplit
     }
  }

  /* now resolve dependencies until we're done */
  while(toResolve.size > 0) {
    val monkey = toResolve.removeAt(0)
    dependencies[monkey]?.forEach { depender ->
      /* check to see if this depender is completely resolved */
      val equation = lines[depender]!!
      val equationValue = resolveEquation(values[equation[0]],
          equation[1], values[equation[2]])
      if(equationValue != null) {
        values[depender] = equationValue
        toResolve.add(depender)
      } else {
        /* else couldn't resolve this monkey, add it to the end of the list to
         * try later */
        toResolve.add(monkey)
      }
    }
  }

  return values["root"]!!
}

fun addLine(lines: MutableMap<String, MutableList<List<String>>>,
    lhs: String, rhs: List<String>) {
  val lineEntry = lines[lhs]
  if(lineEntry != null)
    lineEntry.add(rhs)
  else
    lines[lhs] = mutableListOf<List<String>>(rhs)
}

fun addReverseDependencies(
    lines: MutableMap<String, MutableList<List<String>>>,
    dependencies: MutableMap<String, MutableList<String>>,
    lhs: String, equation: List<String>) {
  val firstOperand = equation[0]
  val operator = equation[1]
  val secondOperand = equation[2]

  /* plus converts to minus, multiplication converts to division, etc. */
  when(operator) {
    "+" -> {
      addLine(lines, firstOperand, listOf(lhs, "-", secondOperand))
      addLine(lines, secondOperand, listOf(lhs, "-", firstOperand))
    }
    "-" -> {
      addLine(lines, firstOperand, listOf(lhs, "+", secondOperand))
      addLine(lines, secondOperand, listOf(firstOperand, "-", lhs))
    }
    "*" -> {
      addLine(lines, firstOperand, listOf(lhs, "/", secondOperand))
      addLine(lines, secondOperand, listOf(lhs, "/", firstOperand))
    }
    "/" -> {
      addLine(lines, firstOperand, listOf(lhs, "*", secondOperand))
      addLine(lines, secondOperand, listOf(firstOperand, "/", lhs))
    }
  }

  /* now each of the operands may also be resolved by the lhs and the other
   * operator */
  addDependency(dependencies, firstOperand, lhs)
  addDependency(dependencies, firstOperand, secondOperand)
  addDependency(dependencies, secondOperand, lhs)
  addDependency(dependencies, secondOperand, firstOperand)
}

fun readAndSolve2(input: String): Long {
  /* starting from the part1 code base, add the part2 twists */

  /* key: monkey, value: key's dependencies */
  val dependencies = mutableMapOf<String,MutableList<String>>()
  /* known values */
  val values = mutableMapOf<String, Long>()
  /* unparsed lines, keyed by lhs monkey */
  val lines = mutableMapOf<String, MutableList<List<String>>>()
  /* values to cascade */
  val toResolve = mutableListOf<String>()

  /* first attempt to parse each line */
  for(line in input.trim().split('\n')) {
     val linesplit = line.split(": ")
     if(linesplit[0] == "humn")
       continue
     try {
      values[linesplit[0]] = linesplit[1].toLong()
      toResolve.add(linesplit[0])
     } catch(e: Exception) {
       /* not a value line. Put it in unparsed. Also, parse out dependencies
          for this monkey. */
       val depsplit = linesplit[1].split(' ')
       if(linesplit[0] == "root") {
         /* root is special */
         addDependency(dependencies, depsplit[0], depsplit[2])
         addDependency(dependencies, depsplit[2], depsplit[0])
         /* add a fake equation: value1: value2 = value2
            which resolveEquation will know how to deal with */
         addLine(lines, depsplit[0],
             listOf<String>(depsplit[2], "=", depsplit[2]))
         addLine(lines, depsplit[2], listOf<String>(depsplit[0], "=",
             depsplit[0]))
       } else {
         addDependency(dependencies, linesplit[0], depsplit[0])
         addDependency(dependencies, linesplit[0], depsplit[2])
         addLine(lines, linesplit[0], depsplit)
         /* now because we may be working backwards, we add equations
            that will resolve any one of the operands too */
         addReverseDependencies(lines, dependencies, linesplit[0], depsplit)
       }
     }
  }

  /* now resolve dependencies until we're done */
  while(toResolve.size > 0) {
    val monkey = toResolve.removeAt(0)
    dependencies[monkey]?.forEach { depender ->
      if(values[depender] == null) {
        /* check to see if this depender is completely resolved */
        val equations = lines[depender]!!
        var solved = false
        for(equation in equations) {
          val equationValue = resolveEquation(values[equation[0]],
              equation[1], values[equation[2]])
          if(equationValue != null) {
            values[depender] = equationValue
            solved = true
            break
          } else {
            /* else couldn't resolve this monkey, add it to the end of the list to
             * try later */
          }
        }
        if(solved)
          toResolve.add(monkey)
        else
          toResolve.add(depender)
      }
    }
  }

  return values["humn"]!!
}

fun Part1(input:String) : Boolean {
  val answer = readAndSolve(input)
  println("Root yells $answer!")
  return true
}

fun Part2(input:String) : Boolean {
  val answer = readAndSolve2(input)
  println("humn yells $answer!")
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
