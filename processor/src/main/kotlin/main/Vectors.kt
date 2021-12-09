package main

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import kotlin.math.pow

fun vectors(generator: CodeGenerator) {
    for (i in 1..4) {
        generator.createNewFile(dependencies = Dependencies(false), packageName = "glm.vec$i", fileName = "Vec${i}T").use {
            text.clear()

            vectorsT(i)

            it.write(text.toString().toByteArray())
        }
    }

    for ((type, extension, _, id) in vectorTypes) {
        for (i in 1..4) {
            generator.createNewFile(dependencies = Dependencies(false), packageName = "glm.vec$i", fileName = "Vec$i$id").use {
                text.clear()

                vectors(i, type, extension, id)

                it.write(text.toString().toByteArray())
            }
        }
    }
}

private fun vectorsT(ordinal: Int) {
    +"package glm.vec$ordinal"
    if (ordinal > 1) {
        +"import glm.extensions.swizzle.*"
        +"import glm.extensions.*"
        +"import kotlin.jvm.*"
    }

    "abstract class Vec${ordinal}T<T>" {
        xyzw(ordinal) { c -> +"abstract var $c: T" }
        xyzw(ordinal) { i, c -> +"operator fun component${i + 1}() = $c" }

        +"// -- Aliases --"

        xyzw(ordinal) { i, c ->
            +"var ${rgba[i]}: T"
            indent {
                +"get() = $c"
                "set(value)" {
                    +"$c = value"
                }
            }
        }
        xyzw(ordinal) { i, c ->
            +"var ${stpq[i]}: T"
            indent {
                +"get() = $c"
                "set(value)" {
                    +"$c = value"
                }
            }
        }

        +"// -- Component accesses --"

        "operator fun get(index: Int): T = when (index)" {
            xyzw(ordinal) { i, c -> +"$i -> $c" }
            +"else -> throw IndexOutOfBoundsException()"
        }

        "operator fun set(index: Int, value: T) = when(index)" {
            xyzw(ordinal) { i, c ->
                "$i ->" {
                    +"$c = value"
                }
            }
            +"else -> throw IndexOutOfBoundsException()"
        }


        "companion object" {
            +"const val length = $ordinal"
        }
    }

    if (ordinal > 1) {
        for (unsigned in (unsignedTypes + "out Number")) {
            +"@Suppress(\"UNCHECKED_CAST\")"
            if (unsigned != "out Number") +"@JvmName(\"dot$unsigned\")"
            "infix fun <N> Vec${ordinal}T<N>.dot(v: Vec${ordinal}T<$unsigned>): N = when (this)" {
                for ((_, _, _, id) in numberTypeInformation) {
                    +"is Vec$ordinal$id -> this.dot(v) as N"
                }
                "is Vec${ordinal}Impl -> when (x)" {
                    for ((type, extension) in numberTypeInformation) {
                        +"is $type -> (${xyzwJoint(ordinal, " + ") { c -> "($c as $type) * v.$c.$extension" }}).$extension as N"
                    }
                    +"else -> throw IllegalArgumentException(\"Can't compute dot product of non-number vectors!\")"
                }
                +"else -> throw ArithmeticException(\"Can't compute dot product of non-number vectors!\")"
            }
        }
    }
}

private fun vectors(ordinal: Int, type: String, extension: String, id: String) {
    +"package glm.vec$ordinal"

    +"import glm.*"
    +"import glm.extensions.*"
    +"import kotlin.jvm.*"
    +"import kotlin.math.abs"
    repeat(4) { +"import glm.vec${it + 1}.*" }
    abcd(3, 3) { c, r, _ -> +"import glm.mat${matrixSizeString(c + 2, r + 2)}.*" }

    val vec = "Vec$ordinal"
    "open class $vec$id(var array: ${type}Array, var ofs: Int = 0) : ${vec}T<$type>()" {
        xyzw(ordinal) { i, c ->
            val delta = if (i == 0) "" else " + $i"

            +"override var $c: $type"
            indent {
                +"get() = array[ofs$delta]"
                "set(value)" {
                    +"array[ofs$delta] = value"
                }
            }
        }

        +"// Implicit basic constructors"
        +"constructor() : this(${type}Array($ordinal))"
        +"constructor(v: $vec$id) : this(${xyzwJoint(ordinal) { c -> "v.$c" }})"

        +"// Explicit basic constructors"
        val arrayOf = "${type.lowercase()}ArrayOf"
        val a = "x" * ordinal
        +"constructor(x: $type) : this(${if (ordinal == 1) "$arrayOf($a)" else a})"
        +"constructor(x: Number) : this(x.$extension)"
        if (type == "UByte" || type == "UShort") {
            +"constructor(x: UInt) : this(x.$extension)"
            +"constructor(x: ULong) : this(x.$extension)"
        }

        if (ordinal != 1) {
            +"constructor(${xyzwJoint(ordinal) { c -> "$c: $type" }}) : this($arrayOf(${xyzwJoint(ordinal) { c -> c }}))"
            if (type == "UByte" || type == "UShort") {
                +"constructor(${xyzwJoint(ordinal) { c -> "$c: UInt" }}) : this(${xyzwJoint(ordinal) { c -> "$c.$extension" }})"
                +"constructor(${xyzwJoint(ordinal) { c -> "$c: ULong" }}) : this(${xyzwJoint(ordinal) { c -> "$c.$extension" }})"
            }

            +"// Conversion scalar constructors"
            +"constructor(v: Vec1T<out Number>) : this(v.x.$extension)"

            +"// Explicit conversions (From section 5.4.1 Conversion and scalar constructors of GLSL 1.30.08 specification)"
            fun oneTypes(comp: String) = listOf("$comp: Number", "$comp: Vec1T<out Number>")
            fun value(p: String) = if (p.last() == '>') "${p[0]}.x" else p[0]
            var postfix = ".$extension"
            fun constructor(vararg arguments: String) {
                +"constructor(${arguments.joinToString()}) : this(${arguments.joinToString { "${value(it)}$postfix" }})"
                postfix = ""
            }
            for (x in oneTypes("x")) {
                for (y in oneTypes("y")) {
                    if (ordinal > 2) {
                        for (z in oneTypes("z")) {
                            if (ordinal > 3) {
                                for (w in oneTypes("w")) {
                                    constructor(x, y, z, w)
                                }
                            } else {
                                constructor(x, y, z)
                            }
                        }
                    } else {
                        constructor(x, y)
                    }
                }
            }
        }

        +"// Conversion vector constructors"
        +"// Explicit conversions (From section 5.4.1 Conversion and scalar constructors of GLSL 1.30.08 specification)"

        infix fun String.args(b: String) = +"constructor($this) : this($b)"
        val V1 = "Vec1T<out Number>"
        val V2 = "Vec2T<out Number>"
        val V3 = "Vec3T<out Number>"
        val V4 = "Vec4T<out Number>"
        val N = "Number"
        when (ordinal) {
            1 -> {
                "v: $V1" args "v.x"
                "v: $V2" args "v.x"
                "v: $V3" args "v.x"
                "v: $V4" args "v.x"
            }
            2 -> {
                "v: $V2" args "v.x, v.y"
                "v: $V3" args "v.x, v.y"
                "v: $V4" args "v.x, v.y"
            }
            3 -> {
                "xy: $V2, z: $N" args "xy.x, xy.y, z"
                "xy: $V2, z: $V1" args "xy.x, xy.y, z.x"
                "x: $N, yz: $V2" args "x, yz.x, yz.y"
                "x: $V1, yz: $V2" args "x.x, yz.x, yz.y"
                "v: $V3" args "v.x, v.y, v.z"
                "v: $V4" args "v.x, v.y, v.z"
            }
            4 -> {
                "xy: $V2, z: $N, w: $N" args "xy.x, xy.y, z, w"
                "xy: $V2, z: $V1, w: $N" args "xy.x, xy.y, z.x, w"
                "xy: $V2, z: $N, w: $V1" args "xy.x, xy.y, z, w.x"
                "xy: $V2, z: $V1, w: $V1" args "xy.x, xy.y, z.x, w.x"
                "x: $N, yz: $V2, w: $N" args "x, yz.x, yz.y, w"
                "x: $V1, yz: $V2, w: $N" args "x.x, yz.x, yz.y, w"
                "x: $N, yz: $V2, w: $V1" args "x, yz.x, yz.y, w.x"
                "x: $V1, yz: $V2, w: $V1" args "x.x, yz.x, yz.y, w.x"
                "x: $N, y: $N, zw: $V2" args "x, y, zw.x, zw.y"
                "x: $V1, y: $N, zw: $V2" args "x.x, y, zw.x, zw.y"
                "x: $N, y: $V1, zw: $V2" args "x, y.x, zw.x, zw.y"
                "x: $V1, y: $V1, zw: $V2" args "x.x, y.x, zw.x, zw.y"
                "xyz: $V3, w: $N" args "xyz.x, xyz.y, xyz.z, w"
                "xyz: $V3, w: $V1" args "xyz.x, xyz.y, xyz.z, w.x"
                "x: $N, yzw: $V3" args "x, yzw.x, yzw.y, yzw.z"
                "x: $V1, yzw: $V3" args "x.x, yzw.x, yzw.y, yzw.z"
                "xy: $V2, zw: $V2" args "xy.x, xy.y, zw.x, zw.y"
                "v: $V4" args "v.x, v.y, v.z, v.w"
            }
        }

        if (type in numberTypes) {
            +"// Unary arithmetic operators"
            for ((operatorChar, operatorName) in operators) {
                if ("Byte" in type || "Short" in type) {
                    for (scalar in listOf(N, type)) {
                        +"operator fun ${operatorName}Assign(scalar: $scalar) = ${operatorName}Assign(scalar.${if (type in unsignedTypes) "u" else ""}i)"
                    }
                } else {
                    +"operator fun ${operatorName}Assign(scalar: $N) = ${operatorName}Assign(scalar.$extension)"
                    "operator fun ${operatorName}Assign(scalar: $type)" {
                        xyzw(ordinal) { c ->
                            when {
                                "Byte" in type || "Short" in type -> +"$c = ($c $operatorChar scalar).$extension"
                                else -> +"$c $operatorChar= scalar"
                            }
                        }
                    }
                }
                if ("Byte" in type || "Short" in type) {
                    "operator fun ${operatorName}Assign(scalar: ${if (type[0] == 'U') "UInt" else "Int"})" {
                        xyzw(ordinal) { c -> +"$c = ($c $operatorChar scalar).$extension" }
                    }
                    if (type in unsignedTypes)
                        +"operator fun ${operatorName}Assign(scalar: ULong) = ${operatorName}Assign(scalar.$extension)"
                } else if (type in unsignedTypes)
                    if (type == "UInt")
                        +"operator fun ${operatorName}Assign(scalar: ULong) = ${operatorName}Assign(scalar.ui)"
                    else +"operator fun ${operatorName}Assign(scalar: UInt) = ${operatorName}Assign(scalar.ul)"
                "operator fun ${operatorName}Assign(v: $V1)" {
                    xyzw(ordinal) { c ->
                        if ("Byte" in type || "Short" in type)
                            +"$c = ($c $operatorChar v.x.$extension).$extension"
                        else +"$c $operatorChar= v.x.$extension"
                    }
                }
                "operator fun ${operatorName}Assign(v: Vec1$id)" {
                    xyzw(ordinal) { c ->
                        if ("Byte" in type || "Short" in type)
                            +"$c = ($c $operatorChar v.x).$extension"
                        else +"$c $operatorChar= v.x"
                    }
                }
                if (ordinal != 1) {
                    "operator fun ${operatorName}Assign(v: Vec${ordinal}T<out Number>)" {
                        xyzw(ordinal) { c ->
                            if ("Byte" in type || "Short" in type)
                                +"$c = ($c $operatorChar v.$c.$extension).$extension"
                            else +"$c $operatorChar= v.$c.$extension"
                        }
                    }
                    "operator fun ${operatorName}Assign(v: Vec$ordinal$id)" {
                        xyzw(ordinal) { c ->
                            if ("Byte" in type || "Short" in type)
                                +"$c = ($c $operatorChar v.$c).$extension"
                            else +"$c $operatorChar= v.$c"
                        }
                    }
                }
            }

            +"// Increment and decrement operators"
            "operator fun inc(): Vec$ordinal$id" {
                xyzw(ordinal) { c -> +"$c++" }
                +"return this"
            }
            "operator fun dec(): Vec$ordinal$id" {
                xyzw(ordinal) { c -> +"$c--" }
                +"return this"
            }

            "operator fun invoke(${xyzwJoint(ordinal) { c -> "$c: $type" }}): $vec$id" {
                xyzw(ordinal) { c -> +"this.$c = $c" }
                +"return this"
            }

            +"// Unary bit operators TODO"
            +"// Unary operators"
            +"operator fun unaryPlus(): Vec$ordinal$id = this"
            if (type !in unsignedTypes)
                +"operator fun unaryMinus(): Vec$ordinal$id = Vec$ordinal$id(${xyzwJoint(ordinal) { c -> "-$c" }})"

            +"// Binary operators"
            for ((operatorChar, operatorName) in operators) {
                +"operator fun $operatorName(scalar: $type): $vec$id = $operatorName(${xyzwJoint(ordinal) { _, _ -> "scalar" }}, $vec$id())"
                +"fun $operatorName(scalar: $type, res: $vec$id): $vec$id = $operatorName(${xyzwJoint(ordinal) { _, _ -> "scalar" }}, res)"
                +"operator fun $operatorName(v: Vec1$id): $vec$id = $operatorName(${xyzwJoint(ordinal) { _ -> "v.x" }}, $vec$id())"
                +"fun $operatorName(v: Vec1$id, res: $vec$id): $vec$id = $operatorName(${xyzwJoint(ordinal) { _ -> "v.x" }}, res)"
                if (ordinal != 1) {
                    +"operator fun $operatorName(v: Vec$ordinal$id): $vec$id = $operatorName(${xyzwJoint(ordinal) { c -> "v.$c" }}, $vec$id())"
                    +"fun $operatorName(v: Vec$ordinal$id, res: $vec$id): $vec$id = $operatorName(${xyzwJoint(ordinal) { c -> "v.$c" }}, res)"
                    if (operatorChar == "*" && type in matrixTypes.map { it.type })
                        for (i in 2..4) {
                            val args = xyzwJoint(i, ",\n\t\t\t\t\t\t\t\t\t\t") { j, _ ->
                                (0 until ordinal).joinToString(" + ") { "${xyzw[it]} * m.${abcd[j]}$it" }
                            }
                            +"operator fun times(m: Mat${matrixSizeString(i, ordinal)}$id) = times(m, Vec$i$id())"
                            +"fun times(m: Mat${matrixSizeString(i, ordinal)}$id, res: Vec$i$id) = res($args)"
                        }
                }
                val args = xyzwJoint(ordinal) { c -> "this.$c $operatorChar $c" }
                +"fun $operatorName(${xyzwJoint(ordinal) { c -> "$c: $type" }}, res: $vec$id): $vec$id = res($args)"
            }
        }

        if (ordinal > 1 && type in numberTypes) {
            +"// Dot products"
            for (unsigned in (unsignedTypes + "out Number")) {
                if (unsigned != "out Number") +"@JvmName(\"dot$unsigned\")"
                +"fun dot(v: Vec${ordinal}T<$unsigned>) = (${xyzwJoint(ordinal, " + ") { c -> "$c * v.$c.$extension" }}).$extension"
            }
        }

        +"override fun equals(other: Any?) = other is Vec$ordinal$id && ${xyzwJoint(ordinal, " && ") { c -> "$c == other.$c" }}"
        +"override fun hashCode() = ${xyzwJoint(ordinal, " + ") { i, c -> "${31f.pow(i).toInt()} * $c.hashCode()" }}"

        if (type == "Float" || type == "Double") {
            +"fun equal(v: $vec$id, epsilon: $type = $type.MIN_VALUE) = BooleanArray(length) { abs(array[it] - v.array[it]) <= epsilon }"
            +"fun notEqual(v: $vec$id, epsilon: $type = $type.MIN_VALUE) = BooleanArray(length) { abs(array[it] - v.array[it]) > epsilon }"
            "fun allEqual(v: $vec$id, epsilon: $type = $type.MIN_VALUE): Boolean" {
                +"for (i in 0 until length)"
                +"\tif(abs(array[i] - v.array[i]) > epsilon)"
                +"\t\treturn false"
                +"return true"
            }
            "fun anyNotEqual(v: $vec$id, epsilon: $type = $type.MIN_VALUE): Boolean" {
                +"for (i in 0 until length)"
                +"\tif(abs(array[i] - v.array[i]) > epsilon)"
                +"\t\treturn true"
                +"return false"
            }
        } else {
            +"infix fun equal(v: $vec$id) = BooleanArray(length) { array[it] == m.array[it] }"
            +"infix fun notEqual(v: $vec$id) = BooleanArray(length) { array[it] != m.array[it] }"
            +"fun allEqual(v: $vec$id): Boolean = array.contentEquals(m.array)"
            +"fun anyNotEqual(v: $vec$id): Boolean = !array.contentEquals(m.array)"
        }

        if (type == "Boolean") {
            +"// Boolean operators"
            val vec_ = "Vec$ordinal$extension"
            +"infix fun and(v: $vec_) = $vec_(${xyzwJoint(ordinal) { c -> "$c && v.$c" }})"
            +"infix fun or(v: $vec_) = $vec_(${xyzwJoint(ordinal) { c -> "$c || v.$c" }})"
        }

        "companion object" {
            +"const val length = Vec${ordinal}T.length"
            if (type in numberTypes)
                +"const val size = length * $type.SIZE_BYTES"
        }
    }

    +"// Binary operators"
    if (type in numberTypes) {
        for ((operatorChar, operatorName) in operators) {
            +"operator fun $type.$operatorName(v: Vec$ordinal$id) = Vec$ordinal$id(${xyzwJoint(ordinal) { c -> "this $operatorChar v.$c" }})"
            if (ordinal != 1)
                +"operator fun Vec1$id.$operatorName(v: Vec$ordinal$id) = Vec$ordinal$id(${xyzwJoint(ordinal) { c -> "x $operatorChar v.$c" }})"
        }
    }
}