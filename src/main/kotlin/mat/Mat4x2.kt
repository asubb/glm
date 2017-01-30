package  mat

import main.BYTES
import vec._2.Vec2

/**
 * Created by GBarbieri on 09.12.2016.
 */

data class Mat4x2(override var value: MutableList<Vec2>) : Mat4x2t<Vec2> {

    // -- Accesses --

    //operator fun get(main.getI: Int) = value[main.getI]

    operator fun set(i: Int, v: Vec2) {
        value[i] = v
    }

    companion object {
        @JvmStatic val SIZE = 4 * 2 * Float.BYTES
    }
}