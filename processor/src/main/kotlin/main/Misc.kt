package main


val text = StringBuilder()
var indentation = ""

fun indent(block: () -> Unit) {
    indentation += "\t"
    block()
    indentation = indentation.dropLast(1)
}

operator fun String.unaryPlus() = text.appendLine("$indentation$this")
operator fun String.invoke(block: () -> Unit) {
    +"$this {"
    indent(block)
    +"}"
}

operator fun StringBuilder.plusAssign(text: String) {
    appendLine(text)
}

operator fun String.times(i: Int) = (1..i).joinToString { this }

val operators = listOf("+" to "plus", "-" to "minus", "*" to "times", "/" to "div")