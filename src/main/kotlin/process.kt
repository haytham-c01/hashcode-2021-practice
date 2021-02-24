import java.util.*
import kotlin.math.sqrt

class Processor {
    private val ingredientsMap = mutableMapOf<String, LinkedList<Pizza>>()
    private val waitingTeamsCount = mutableMapOf<Int, Int>()
    private val pizzaDeliveries = mutableListOf<MutableList<Pizza>>()
    private lateinit var unusedPizza: MutableList<Pizza>
    private var greedy = false

    fun process(input: Input, greedy: Boolean): Output {
        initData(input, greedy)

        // 1. find solution
        while (weCanServeMore()) {
            val pizzaDelivery = findPizzaDelivery()

            if (pizzaDelivery.size.isValidPizzaCount()) {
                waitingTeamsCount[pizzaDelivery.size] = waitingTeamsCount[pizzaDelivery.size]!! - 1
                pizzaDeliveries.add(pizzaDelivery.toMutableList())
            }
        }

        // 2. optimize results
        val teamsPizza = pizzaDeliveries.groupBy { it.size }

        // find if converting 2,4 deliveries into a 3,3 would improve score
        teamsPizza[4]?.forEach { fourSizeDelivery ->
            if (teamsPizza[2].isNullOrEmpty() || waitingTeamsCount[3]!! <= 0) return@forEach
            val fourSizedScore = fourSizeDelivery.deliveryScore()
            var bestPizza: Pair<Int, Pizza>? = null
            var bestDeliveryIndex = -1

            teamsPizza[2]?.forEachIndexed { innerIndex, twoSizedDelivery ->
                val twoSizedScore = fourSizeDelivery.deliveryScore()
                val optimizationPizza =
                    findOptimizationPizza(fourSizeDelivery, twoSizedDelivery, fourSizedScore + twoSizedScore)

                if (optimizationPizza != null) {
                    if ((bestPizza == null && bestDeliveryIndex == -1) || optimizationPizza.first > bestPizza!!.first) {
                        bestPizza = optimizationPizza
                        bestDeliveryIndex = innerIndex
                    }
                }
            }

            if (bestPizza != null && bestDeliveryIndex != -1) {
                teamsPizza[2]!![bestDeliveryIndex].add(bestPizza!!.second)
                fourSizeDelivery.remove(bestPizza!!.second)
            }
        }


        return getOutput()
    }

    private fun findOptimizationPizza(
        fourSizeDelivery: List<Pizza>,
        twoSizedDelivery: List<Pizza>,
        combinedScore: Int
    ): Pair<Int, Pizza>? {
        var bestScore = combinedScore
        var selectedPizza: Pizza? = null

        fourSizeDelivery.forEach { pizza ->
            val newFourSized = fourSizeDelivery.toMutableList().apply { remove(pizza) }
            val newTwoSized = twoSizedDelivery.toMutableList().apply { add(pizza) }

            val newScore = newFourSized.deliveryScore() + newTwoSized.deliveryScore()

            if (newScore > bestScore) {
                bestScore = newScore
                selectedPizza = pizza
            }
        }

        return if (selectedPizza != null) Pair(bestScore, selectedPizza!!) else null
    }


    fun List<Pizza>.deliveryScore() = sqrt(map { it.ingredients }.flatten().distinct().size.toDouble()).toInt()

    private fun weCanServeMore() = (4 downTo 2).sumBy { waitingTeamsCount[it]!! } > 0 && unusedPizza.size > 0

    private fun getOutput() = Output(
        teamsPizza = pizzaDeliveries
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

    private fun findPizzaDelivery(
        usedIngredients: MutableSet<String> = mutableSetOf(),
        pizzaDelivery: MutableSet<Pizza> = mutableSetOf(),
        pizzaUsedIngredientsCount: MutableMap<Pizza, Int> = mutableMapOf(),
    ): Set<Pizza> {
        if (pizzaDelivery.size == maxRequiredPizzaCount()) return pizzaDelivery
        if (unusedPizza.isEmpty()) return pizzaDelivery

        val pizza = if (greedy) selectPizzaGreedy(pizzaUsedIngredientsCount)
        else selectPizza(pizzaUsedIngredientsCount)
        if (pizzaWasted(pizza, pizzaUsedIngredientsCount, pizzaDelivery)) return pizzaDelivery

        val selectedPizza = pizza.usePizza()
        pizzaUsedIngredientsCount.remove(selectedPizza)
        pizzaDelivery.add(selectedPizza)

        pizza.ingredients
            .filter { !usedIngredients.contains(it) }
            .mapNotNull { ingredientsMap[it] }
            .flattenLinkedList()
            .forEach {
                pizzaUsedIngredientsCount[it] = (pizzaUsedIngredientsCount[it] ?: 0) + 1
            }

        usedIngredients.addAll(selectedPizza.ingredients)
        return findPizzaDelivery(usedIngredients, pizzaDelivery, pizzaUsedIngredientsCount)
    }


    private fun selectPizzaGreedy(pizzaUsedIngredientsCount: MutableMap<Pizza, Int>): Pizza {
        var selectedPizza: Pizza = unusedPizza.first()
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

    private fun Pizza.getValue(usedIngredients: Int) = ingredientsCount - usedIngredients * 4.5

    private fun selectPizza(
        pizzaWithUsedIngredients: MutableMap<Pizza, Int>
    ) = if (pizzaWithUsedIngredients.size < unusedPizza.size) {
        unusedPizza.first {
            !pizzaWithUsedIngredients.contains(it)
        }
    } else {
        // select the pizza with minimum waste
        pizzaWithUsedIngredients.minByOrNull { it.value }?.key!!

    }

    private fun pizzaWasted(
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


    private fun initData(input: Input, greedy: Boolean) {
        this.greedy = greedy

        waitingTeamsCount[2] = input.twoPersonTeams
        waitingTeamsCount[3] = input.threePersonTeams
        waitingTeamsCount[4] = input.fourPersonTeams

        unusedPizza = input.pizzas
            .sortedByDescending { it.ingredientsCount }
            .toMutableList()

//        unusedPizza= input.pizzas.toSortedSet { p1, p2 ->
//            when{
//                p1.ingredientsCount > p2.ingredientsCount -> -1
//                p1.ingredientsCount < p2.ingredientsCount -> 1
//                else -> if(p1.id==p2.id) 0 else 1
//            }
//        }

        input.pizzas.forEach { pizza ->
            pizza.ingredients.forEach { ingredient ->
                val ingredientPizzas = ingredientsMap.getOrDefault(ingredient, LinkedList())
                ingredientsMap[ingredient] = ingredientPizzas.apply { add(pizza) }
            }
        }
    }
}


