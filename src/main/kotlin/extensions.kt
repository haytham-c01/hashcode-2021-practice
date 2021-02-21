import java.util.*


fun String.splitBySpace(): List<String> = split(" ")
fun <T> List<T>.spaceSeparatedString():String = joinToString(separator = " ")
fun List<String>.getAsInt(index: Int):Int = get(index).toInt()

fun <T> Iterable<LinkedList<T>>.flattenLinkedList(): LinkedList<T> {
    val result = LinkedList<T>()
    for (element in this) {
        result.addAll(element)
    }
    return result
}