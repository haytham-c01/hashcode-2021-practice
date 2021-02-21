import java.io.File

fun readInput(file: File): Input {
    val lines = file.readLines()

    var input = Input(
        pizzasCount = 0,
        twoPersonTeams = 0,
        threePersonTeams = 0,
        fourPersonTeams = 0,
        pizzas = listOf()
    )

    lines.forEachIndexed { index, string ->
        input = when (index) {
            0 -> input.readData(string)
            else -> input.readPizza(string)
        }
    }

    return input
}

private fun Input.readData(string: String): Input {
    string.splitBySpace().apply {
        return copy(
            pizzasCount = getAsInt(0),
            twoPersonTeams = getAsInt(1),
            threePersonTeams = getAsInt(2),
            fourPersonTeams = getAsInt(3),
        )
    }
}

private fun Input.readPizza(string: String): Input {
    string.splitBySpace().apply {
        val pizza = Pizza(
            id= pizzas.size,
            ingredientsCount = getAsInt(0),
            ingredients = subList(1, size),
        )

        return copy(
            pizzas = pizzas.toMutableList().apply { add(pizza) },
        )
    }
}

data class Input(
    val pizzasCount: Int,
    val twoPersonTeams: Int,
    val threePersonTeams: Int,
    val fourPersonTeams: Int,
    val pizzas: List<Pizza>
){
    override fun toString(): String {
        var str=  "Input(pizzasCount=$pizzasCount, twoPersonTeams=$twoPersonTeams, threePersonTeams=$threePersonTeams, fourPersonTeams=$fourPersonTeams, pizzas="
        pizzas.forEach {
           str += "\n$it"
        }

        return  "$str)"
    }
}

data class Pizza(
    val id: Int,
    val ingredientsCount: Int,
    val ingredients: List<String>,
)