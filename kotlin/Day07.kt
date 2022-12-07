/* compile with
   kotlinc Day01.kt -include-runtime -d Day01.jar && java -jar Day01.jar -1
*/
import kotlin.io.NoSuchFileException
import java.io.File
import java.io.InputStream

val DAY=7
val YEAR=2022
val INPUTDIR="../input"

val FILETYPE_DIRECTORY='d'
val FILETYPE_FILE='f'
data class Inode(
  val type: Char,
  val name: String,
  var size: Long,
  val contents: MutableList<Inode>,
  var parent: Inode?,
)

fun lineToInode( line: String ): Inode {
  /* lines look like:
        dir <name>
        <size> <name>
   */
  var size: Long
  var type: Char = FILETYPE_FILE
  var name: String

  val splitline = line.split(" ")
  try {
    /* try to parse it as a file */
    size = splitline[0].toLong()
    name = splitline[1]
  } catch(e: Exception) {
    size = 0L
    type = FILETYPE_DIRECTORY
    name = splitline[1]
  }

  return Inode(type, name, size, mutableListOf<Inode>(), null)
}


fun readFiles(input:String): Inode {
  /* returns root file */
  val root = Inode(FILETYPE_DIRECTORY, "/", 0, mutableListOf<Inode>(), null)
  var currentDir = root

  /* first line must be the command to cd into the root directory */
  /* all other commands are:
       cd <dirname>
       cd ..
       ls */
  val lines = input.split('\n')
  for(lineindex in 1 until lines.size) {
    val line = lines[lineindex]
    if(line == "")
      continue

    val linesplit = line.split(' ')
    if(linesplit[0] == "$") {
      /* this is a command */
      if(linesplit[1] == "cd") {
        /* are we cd'ing up? */
        if(linesplit[2] == "..") {
          val tmp = currentDir.parent
          if(tmp != null)
            currentDir = tmp
        } else {
          /* find the subdir we're cd'ing into */
          var tmp : Inode? = null
          for(tmpContent in currentDir.contents) {
            if(tmpContent.name == linesplit[2]) {
              tmp = tmpContent
              break
            }
          }
          /* if we didn't find the dir, add it (is this necessary?) */ 
          if(tmp == null) {
            tmp = Inode(FILETYPE_DIRECTORY, linesplit[2], 0,
                mutableListOf<Inode>(), currentDir)
          }
          currentDir = tmp
        }
      }
      /* ignore "ls" commands, we'll just read any non-file line as the
         result of an "ls" */
    } else {
      val newInode = lineToInode(line)
      currentDir.contents.add(newInode)
      newInode.parent = currentDir
      println("Parsed file \"${newInode.name}\" (${newInode.type}), size ${newInode.size}, parent \"${currentDir.name}\"")
      /* adjust sizes all the way up the stack */
      if(newInode.type == FILETYPE_FILE) {
        var tmpDir : Inode? = currentDir
        while(tmpDir != null) {
          tmpDir.size += newInode.size
          tmpDir = tmpDir.parent
        }
      }
    }
  }

  return root
}

fun Part1(input:String) : Boolean {
  val threshold = 100000L
  val root = readFiles(input)
  println("Total size of directories smaller than threshold: ${root.size}")
  return true
}

fun Part2(input:String) : Boolean {
  val root = readFiles(input)
  val threshold = 30000000L - (70000000L - root.size)
  val toVisit = mutableListOf<Inode>(root)
  var target = root
  while(toVisit.size > 0) {
    val currentDir = toVisit.removeAt(0)
    //println("Visiting dir \"${currentDir.name}\", size ${currentDir.size}")
    if(currentDir.size >= threshold) {
      if(currentDir.size < target.size)
        target = currentDir
      /* search subdirs */
      for(toSearch in currentDir.contents) {
        if(toSearch.type == FILETYPE_DIRECTORY)
          toVisit.add(toSearch)
      }
    }
  }
  println("Directory to delete: ${target.name} (size ${target.size})")
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
