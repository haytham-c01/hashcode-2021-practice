import java.util.*

class Processor {
    private val ingredientsMap = mutableMapOf<String, LinkedList<Pizza>>()
    private val waitingTeamsCount = mutableMapOf<Int, Int>()
    private val servedTeamsPizza = mutableListOf<List<Pizza>>()
    private val usedPizza = mutableSetOf<Pizza>()
    private val unusedPizza = mutableSetOf<Pizza>()
    private var greedy = false

    private fun weCanServeMore() = (4 downTo 2).sumBy { waitingTeamsCount[it]!! } > 0 && unusedPizza.size > 0

    fun process(input: Input, greedy:Boolean): Output {
        this.greedy= greedy
        setIngredientsMap(input)
        setUnusedPizzaSet(input)
        setWaitingTeamsCount(input)

        while (weCanServeMore()) {
            val pizzaDelivery = findPizzaDeliveryGreedy()

            if (pizzaDelivery.size.isValidPizzaCount()) {
                waitingTeamsCount[pizzaDelivery.size] = waitingTeamsCount[pizzaDelivery.size]!! - 1
                servedTeamsPizza.add(pizzaDelivery.toList())
            }
        }

        return getOutput()
    }

    private fun getOutput() = Output(
        teamsPizza = servedTeamsPizza
            .filter {
                !it.isNullOrEmpty()
            }
            .map {
                TeamPizza(
                    teamSize = it.size,
                    pizzasIndexes = it.map { pizza -> pizza.id }
                )
            }
    )

    private fun findPizzaDeliveryGreedy(): Set<Pizza> {
        // initialize used ingredients
        val usedIngredients = mutableSetOf<String>()
        // initialize pizza delivery
        val pizzaDelivery = mutableSetOf<Pizza>()
        //var pizzaDeliveryWaste= 0

        val pizzaUsedIngredientsCount = mutableMapOf<Pizza, Int>()

        while (pizzaDelivery.size < maxRequiredPizzaCount() && unusedPizza.isNotEmpty()) {
            val pizza = if (greedy) selectPizzaGreedy(pizzaUsedIngredientsCount)
            else selectPizza(pizzaUsedIngredientsCount)

            if (itIsZeroValuePizza(pizza, pizzaUsedIngredientsCount, pizzaDelivery)) break

            val selectedPizza = pizza.usePizza()
            pizzaUsedIngredientsCount.remove(selectedPizza)

            pizza.ingredients
                .filter { !usedIngredients.contains(it) }
                .mapNotNull { ingredientsMap[it] }
                .flattenLinkedList()
                .forEach {
                        pizzaUsedIngredientsCount[it] = (pizzaUsedIngredientsCount[it]?:0) + 1
                }

            pizzaDelivery.add(selectedPizza)
            usedIngredients.addAll(selectedPizza.ingredients)
        }


        return pizzaDelivery
    }

    private fun selectPizzaGreedy(pizzaUsedIngredientsCount: MutableMap<Pizza, Int>): Pizza {
        var selectedPizza = unusedPizza.first()
        if (pizzaUsedIngredientsCount[selectedPizza] ?: 0 == 0) return selectedPizza

        unusedPizza.forEach { pizza ->
            val selectedPizzaUsedIngredients = pizzaUsedIngredientsCount[selectedPizza] ?: 0
            val selectedPizzaValue = selectedPizza.getValue(selectedPizzaUsedIngredients)

            if (selectedPizza != pizza) {
                val pizzaUsedIngredients = pizzaUsedIngredientsCount[pizza] ?: 0
                val pizzaValue = pizza.getValue(pizzaUsedIngredients)
                if (pizzaUsedIngredients == 0 && selectedPizzaValue >= pizzaValue) return selectedPizza
                else if (pizzaValue > selectedPizzaValue) selectedPizza = pizza
            }
        }

        return selectedPizza
    }

    private fun Pizza.getValue(usedIngredients: Int) = ingredientsCount - usedIngredients * 2

    private fun selectPizza(
        pizzaWithUsedIngredients: MutableMap<Pizza, Int>
    ) = if (pizzaWithUsedIngredients.size < unusedPizza.size) {
        unusedPizza.first {
            !pizzaWithUsedIngredients.contains(it)
        }
    } else {

        // select the pizza with minimum waste
        pizzaWithUsedIngredients.minByOrNull {
            it.value
        }?.key!!

        // alternative solution select the pizza with most value
        // most value: most unused ingredients
        //                val selectedPizza= pizzaUsedIngredientsCount.toList().maxByOrNull {
        //                    it.first.ingredientsCount - it.second
        //                }?.apply {
        //                    println("${first} - ${second} ")
        //                }!!.first

    }

    private fun itIsZeroValuePizza(
        selectedPizza: Pizza,
        pizzaUsedIngredientsCount: MutableMap<Pizza, Int>,
        pizzaDelivery: MutableSet<Pizza>
    ) =
        selectedPizza.ingredientsCount == pizzaUsedIngredientsCount[selectedPizza] && waitingTeamsCount.getOrDefault(
            pizzaDelivery.size,
            0
        ) > 0


    private fun Int.isValidPizzaCount(): Boolean {
        val count = this
        return count in 2..4 && waitingTeamsCount[count]!! > 0
    }

    private fun maxRequiredPizzaCount() = (4 downTo 2).firstOrNull { waitingTeamsCount[it]!! > 0 } ?: 0

    private fun Pizza.usePizza(): Pizza {
        unusedPizza.remove(this)
        usedPizza.add(this)

        // remove the pizza from ingredientsMap
        // also remove the ingredient key if its empty

        ingredients.forEach {

            ingredientsMap[it]?.remove(this)
            if (ingredientsMap[it].isNullOrEmpty()) {
                ingredientsMap.remove(it)
            }
        }


        return this
    }

    private fun setWaitingTeamsCount(input: Input) {
        waitingTeamsCount[2] = input.twoPersonTeams
        waitingTeamsCount[3] = input.threePersonTeams
        waitingTeamsCount[4] = input.fourPersonTeams
    }


    private fun setUnusedPizzaSet(input: Input) {
        unusedPizza.addAll(input.pizzas.sortedByDescending { it.ingredients.size })
    }


    private fun setIngredientsMap(input: Input) {
        input.pizzas.forEach { pizza ->
            pizza.ingredients.forEach { ingredient ->
                val ingredientPizzas = ingredientsMap.getOrDefault(ingredient, LinkedList())
                ingredientsMap[ingredient] = ingredientPizzas.apply { add(pizza) }
            }
        }
    }

}


