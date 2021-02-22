import java.util.*

class Processor {
    private val ingredientsMap = mutableMapOf<String, LinkedList<Pizza>>()
    private val waitingTeamsCount = mutableMapOf<Int, Int>()
    private val servedTeamsPizza = mutableListOf<List<Pizza>>()
    private val usedPizza = mutableSetOf<Pizza>()
    private val unusedPizza = mutableSetOf<Pizza>()
    private var greedy = false

    private fun weCanServeMore() = (4 downTo 2).sumBy { waitingTeamsCount[it]!! } > 0 && unusedPizza.size > 0

    fun process(input: Input, greedy: Boolean): Output {
        this.greedy = greedy
        setIngredientsMap(input)
        setUnusedPizzaSet(input)
        setWaitingTeamsCount(input)

        while (weCanServeMore()) {

            val pizzaDelivery = findPizzaDelivery().onEach { it.usePizza() }

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


    private val deliveryGroups: TreeSet<Set<Pizza>> = sortedSetOf(
        comparator = { o1, o2 ->
            val firstDeliveryValue = deliveryValueOf(o1)
            val secondDeliveryValue = deliveryValueOf(o1)
            when {
                firstDeliveryValue > secondDeliveryValue -> 1
                firstDeliveryValue < secondDeliveryValue -> -1
                else -> if (o1 == o2) 0 else 1
            }
        }
    )

    private fun findPizzaDelivery(): Set<Pizza> {
        if (deliveryGroups.isEmpty()) {
            unusedPizza.forEach {
                // println(it.id)
                deliveryGroups.add(findPizzaDeliveryGroup(it))
            }
        }

        return if (isValidDelivery(deliveryGroups.first())) deliveryGroups.first()
        else {
            // update all invalid deliveries until first valid one
            val iterator = deliveryGroups.iterator()
            val newDeliveryGroups= mutableListOf<Set<Pizza>>()
            while (iterator.hasNext()) {
                val delivery = iterator.next()
                if (isValidDelivery(delivery)) break
                iterator.remove()

                if (usedPizza.contains(delivery.first())) continue
                newDeliveryGroups.add(findPizzaDeliveryGroup(delivery.first()))
            }
            deliveryGroups.addAll(newDeliveryGroups)

            deliveryGroups.first()
        }
    }


    private fun deliveryValueOf(pizzaDelivery: Set<Pizza>): Int =
        pizzaDelivery.map { it.ingredients }.flatten().distinct().size

    private fun isValidDelivery(pizzaDelivery: Set<Pizza>): Boolean =
        pizzaDelivery.firstOrNull { usedPizza.contains(it) } == null

    private fun findPizzaDeliveryGroup(firstPizza: Pizza): Set<Pizza> {
        // initialize used ingredients
        val usedIngredients = mutableSetOf<String>()
        // initialize pizza delivery
        val pizzaDelivery = mutableSetOf<Pizza>()
        //var pizzaDeliveryWaste= 0

        val pizzaUsedIngredientsCount = mutableMapOf<Pizza, Int>()
        val currentUnusedPizzas = unusedPizza.toMutableSet()

        while (pizzaDelivery.size < maxRequiredPizzaCount() && currentUnusedPizzas.isNotEmpty()) {

            val selectedPizza = when {
                pizzaDelivery.size == 0 -> firstPizza
                greedy -> selectPizzaGreedy(pizzaUsedIngredientsCount, currentUnusedPizzas)
                else -> selectPizza(pizzaUsedIngredientsCount, currentUnusedPizzas)
            }
            if (zeroValuePizza(selectedPizza, pizzaUsedIngredientsCount, pizzaDelivery)) break

            pizzaDelivery.add(selectedPizza)
            pizzaUsedIngredientsCount.remove(selectedPizza)
            currentUnusedPizzas.remove(selectedPizza)

            selectedPizza.ingredients
                .filter { !usedIngredients.contains(it) }
                .mapNotNull { ingredientsMap[it] }
                .flattenLinkedList()
                .forEach {
                    if (!pizzaDelivery.contains(it)) {
                        pizzaUsedIngredientsCount[it] = (pizzaUsedIngredientsCount[it] ?: 0) + 1
                    }
                }

            usedIngredients.addAll(selectedPizza.ingredients)
        }


        return pizzaDelivery
    }


    private fun selectPizzaGreedy(
        pizzaUsedIngredientsCount: MutableMap<Pizza, Int>,
        currentUnusedPizzas: Set<Pizza>
    ): Pizza {
        var selectedPizza = currentUnusedPizzas.first()
        if (pizzaUsedIngredientsCount[selectedPizza] ?: 0 == 0) return selectedPizza

        currentUnusedPizzas.forEach { pizza ->
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
        pizzaWithUsedIngredients: MutableMap<Pizza, Int>, currentUnusedPizzas: Set<Pizza>
    ): Pizza {
        return if (pizzaWithUsedIngredients.size < currentUnusedPizzas.size) {
            currentUnusedPizzas.first {
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
    }

    private fun zeroValuePizza(
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


