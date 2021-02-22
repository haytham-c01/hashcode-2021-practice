import java.io.File

private val baseDirectory = "src${File.separator}main${File.separator}resources${File.separator}"
private val inDirectory = "${baseDirectory}in${File.separator}"
private val outDirectory = "${baseDirectory}out${File.separator}"

private val inputFiles = listOf(
    File("${inDirectory}a_example"),
    File("${inDirectory}b_little_bit_of_everything.in"),
    File("${inDirectory}c_many_ingredients.in"),
    File("${inDirectory}d_many_pizzas.in"),
    File("${inDirectory}e_many_teams.in"),
)

private val outFiles = listOf(
    File("${outDirectory}a.out"),
    File("${outDirectory}b.out"),
    File("${outDirectory}c.out"),
    File("${outDirectory}d.out"),
    File("${outDirectory}e.out"),
)

fun main(args: Array<String>) {
    inputFiles.forEachIndexed { index, file ->
        val startTime = System.currentTimeMillis()

        val input = readInput(file)
        val output = Processor().process(input, index == 2)
        outFiles[index].saveOutput(output)

        val endTime = System.currentTimeMillis()
        val elapsedTime = (endTime - startTime) / 1000
        println("${file.name} -> completed in $elapsedTime seconds.")
    }
}

