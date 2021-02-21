import java.io.File

fun File.saveOutput(output: Output) {
    this.printWriter().apply {
        // print the output to file here

        println(output.teamsPizza.size)
        output.teamsPizza.forEach {
            println("${it.teamSize}  ${it.pizzasIndexes.spaceSeparatedString()}")
        }

    }.flush()
}

data class Output(
    val teamsPizza: List<TeamPizza>,
)

data class TeamPizza(
    val teamSize: Int,
    val pizzasIndexes: List<Int>,
)
