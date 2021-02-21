//import java.util.*
//
//class PizzaFinder(private val ingredientsMap: Map<String, LinkedList<Pizza>>) {
//
//    // map of list of ingredients (as keys) and the corresponding pizza that uses it and how many ingredients does it us as value
//    private val cachedResults = sortedMapOf<List<String>, Map<Pizza, Int>>(comparator = { o1, o2 ->
//        when {
//            o1.size > o2.size -> 1
//            o1.size < o2.size -> -1
//            else -> 0
//        }
//    }
//    )
//
//    fun findPizzas(ingredients: List<String>): Map<Pizza, Int> {
//        if (cachedResults.contains(ingredients)) return cachedResults[ingredients]!!
//
//        val ingredientsSet = ingredients.toMutableSet()
//        val pizzaWithUsedIngredients = mutableMapOf<Pizza, Int>()
//
//        val resultKey = cachedResults.tailMap(ingredients).keys.firstOrNull { ingredientsSet.containsAll(it) }
//        if (resultKey != null) {
//            ingredientsSet.removeAll(resultKey)
//        }
//
//
//        ingredientsSet
//            .mapNotNull { ingredientsMap[it] }
//            .flattenLinkedList()
//            .forEach {
//                pizzaWithUsedIngredients[it] = (pizzaWithUsedIngredients[it] ?: 0) + 1
//            }
//
//        if (resultKey != null) {
//            cachedResults[ingredientsSet.toList()] = pizzaWithUsedIngredients
//            pizzaWithUsedIngredients.addResult(cachedResults[resultKey]!!)
//        }
//        cachedResults[ingredients] = pizzaWithUsedIngredients
//
//        return pizzaWithUsedIngredients
//    }
//
//
//}
//
//fun MutableMap<Pizza, Int>.addResult(result: Map<Pizza, Int>) {
//    result.forEach {
//        this[it.key] = (this[it.key] ?: 0) + it.value
//    }
//}