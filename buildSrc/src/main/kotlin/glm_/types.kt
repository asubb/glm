package glm_

val numberTypeInformation = listOf(
    TypeInformation("Byte", "b", "toByte"),
    TypeInformation("Short", "s", "toShort"),
    TypeInformation("Int", "i", "toInt"),
    TypeInformation("Long", "L", "toLong"),
    TypeInformation("UByte", "ub", "toUByte"),
    TypeInformation("UShort", "us", "toUShort"),
    TypeInformation("UInt", "ui", "toUInt"),
    TypeInformation("ULong", "ul", "toULong"),
    TypeInformation("Float", "f", "toFloat"),
    TypeInformation("Double", "d", "toDouble"))
val extensionsToInformation = numberTypeInformation.associateBy {
    it.extension
}
val typeToInformation = numberTypeInformation.associateBy {
    it.type
}
val intPromotedTypes = listOf("Byte", "Short", "UByte", "UShort")
val numberTypes = numberTypeInformation.map {
    it.type
}
val unsignedTypes = listOf("UByte", "UShort", "UInt", "ULong")
val floatingPointTypes = listOf("Float", "Double")

val vectorTypes = numberTypeInformation + TypeInformation("Boolean", "bool", "")
val matrixTypes = numberTypeInformation.filter { it.type in listOf("Float", "Double", "Int"/*, "UInt"*/) }

val String.unsignedToSigned get() = replace("U", "")

val String.`0`
    get() = when (this) {
        "Float" -> "0f"
        "Double" -> "0.0"
        else -> error("invalid type")
    }
val String.`1`
    get() = when (this) {
        "Float" -> "1f"
        "Double" -> "1.0"
        else -> error("invalid type")
    }
val String.`2`
    get() = when (this) {
        "Float" -> "2f"
        "Double" -> "2.0"
        else -> error("invalid type")
    }
val String.`3`
    get() = when (this) {
        "Float" -> "3f"
        "Double" -> "3.0"
        else -> error("invalid type")
    }
val String.promotedExtensionOrThis
    get() = when (this) {
        "Byte" -> "i"
        "Short" -> "i"
        "UByte" -> "ui"
        "UShort" -> "ui"
        else -> "this"
    }
val String.promotedExtensionOrEmpty
    get() = when (this) {
        "Byte" -> ".i"
        "Short" -> ".i"
        "UByte" -> ".ui"
        "UShort" -> ".ui"
        else -> ""
    }
val String.maybePromotedBack
    get() = when (this) {
        "Byte" -> ".b"
        "Short" -> ".s"
        "UByte" -> ".ub"
        "UShort" -> ".us"
        else -> ""
    }
val String.counterpart
    get() = when (this) {
        "Float" -> "Int"
        "Double" -> "Long"
        "Int" -> "Float"
        "Long" -> "Double"
        else -> ""
    }

data class TypeInformation(val type: String, val extension: String, val conversionFunction: String) {
    val id: String
        get() = if (type == "Float") "" else extension

    operator fun component4() = id
}

operator fun Iterable<TypeInformation>.minus(types: Iterable<String>) = types.toSet().let { typeSet -> this.filter { it.type !in typeSet } }


val xyzw = listOf("x", "y", "z", "w")
val wxyz = listOf("w", "x", "y", "z")
val rgba = listOf("r", "g", "b", "a")
val stpq = listOf("s", "t", "p", "q")

fun Generator.xyzwIndexed(ordinal: Int = Generator.Companion.ordinal, block: (Int, String) -> Unit) {
    for (i in 0 until ordinal)
        block(i, xyzw[i])
}

fun Generator.xyzw(ordinal: Int = Generator.Companion.ordinal, block: (String) -> Unit) {
    for (i in 0 until ordinal)
        block(xyzw[i])
}

fun Generator.Xyzw(ordinal: Int = Generator.Companion.ordinal, block: (String) -> Unit) {
    for (i in 0 until ordinal)
        block(xyzw[i].toUpperCase())
}

fun wxyzIndexed(block: (Int, String) -> Unit) {
    for (i in wxyz.indices)
        block(i, wxyz[i])
}

fun wxyz(block: (String) -> Unit) {
    for (i in wxyz)
        block(i)
}

fun Generator.xyzwJointIndexed(ordinal: Int = Generator.Companion.ordinal, separator: String = ", ", block: (Int, String) -> String) =
    (0 until ordinal).joinToString(separator) {
        block(it, xyzw[it])
    }

fun Generator.xyzwJoint(ordinal: Int = Generator.Companion.ordinal, separator: String = ", ", block: (String) -> String) = (0 until ordinal).joinToString(separator) { block(xyzw[it]) }
fun Generator.XyzwJoint(ordinal: Int = Generator.Companion.ordinal, separator: String = ", ", block: (String) -> String) = (0 until ordinal).joinToString(separator) { block(xyzw[it].toUpperCase()) }

fun wxyzJointIndexed(separator: String = ", ", block: (Int, String) -> String) = wxyz.indices.joinToString(separator) { block(it, wxyz[it]) }
fun wxyzJoint(separator: String = ", ", block: (String) -> String) = wxyz.indices.joinToString(separator) { block(wxyz[it]) }


val abcd = listOf("a", "b", "c", "d")

fun abcdN(i: Int, j: Int) = "${abcd[i]}$j"
fun vNN(i: Int, j: Int) = "v$i$j"

fun Generator.abcdIndexed(width: Int = Generator.Companion.width,
                          height: Int = Generator.Companion.height, block: (Int, Int, String) -> Unit) {
    for (i in 0 until width)
        for (j in 0 until height)
            block(i, j, abcdN(i, j))
}

fun Generator.abcd(width: Int = Generator.Companion.width,
                   height: Int = Generator.Companion.height, block: (String) -> Unit) {
    for (i in 0 until width)
        for (j in 0 until height)
            block("${abcd[i]}$j")
}

fun Generator.abcdJointIndexed(width: Int = Generator.Companion.width,
                               height: Int = Generator.Companion.height,
                               rowSeparator: String = ", ", columnSeparator: String = ", ",
                               block: (Int, Int, String) -> String) =
    (0 until width).joinToString(rowSeparator) { i ->
        (0 until height).joinToString(columnSeparator) { j ->
            block(i, j, abcdN(i, j))
        }
    }

fun Generator.abcdJointIndexed(rowSeparator: String = ", ", columnSeparator: String = ", ",
                               block: (Int, Int, String) -> String): String = abcdJointIndexed(Generator.Companion.width,
                                                                                               Generator.Companion.height,
                                                                                               rowSeparator, columnSeparator, block)

fun Generator.abcdJoint(width: Int = Generator.Companion.width,
                        height: Int = Generator.Companion.height,
                        rowSeparator: String = ", ", columnSeparator: String = ", ",
                        block: (String) -> String) =
    (0 until width).joinToString(rowSeparator) { i ->
        (0 until height).joinToString(columnSeparator) { j ->
            block(abcdN(i, j))
        }
    }
fun Generator.abcdJoint(rowSeparator: String = ", ", columnSeparator: String = ", ",
                        block: (String) -> String) = abcdJoint(Generator.Companion.width,
                                                               Generator.Companion.height, rowSeparator, columnSeparator, block)
