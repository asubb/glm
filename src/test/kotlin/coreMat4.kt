import io.kotlintest.KTestJUnitRunner
import io.kotlintest.specs.StringSpec
import main.glm
import mat.Mat4
import mat.operators.div
import mat.operators.times
import vec._4.Vec4
import org.junit.runner.RunWith

/**
 * Created by GBarbieri on 12.12.2016.
 */

@RunWith(KTestJUnitRunner::class)
class coreMat4 : StringSpec() {

    init {

        "test operators" {

            val l = Mat4(1f)
            val m = Mat4(1f)
            val u = Vec4(1f)
            val v = Vec4(1f)
            val x = 1f
            val a = m * u
            val b = v * m
            val p = x * m
            val q = m * x
            val R = m != q
            val S = m == l

            (S && !R) shouldBe true
        }

        "test inverse" {

            run {
                val matrix = Mat4(
                        0.6f, 0.2f, 0.3f, 0.4f,
                        0.2f, 0.7f, 0.5f, 0.3f,
                        0.3f, 0.5f, 0.7f, 0.2f,
                        0.4f, 0.3f, 0.2f, 0.6f)
                val inverse = matrix.inverse()
                val identity = matrix * inverse

                glm.all(glm.epsilonEqual(identity[0], Vec4(1.0f, 0.0f, 0.0f, 0.0f), Vec4(0.01f))) shouldBe true
                glm.all(glm.epsilonEqual(identity[1], Vec4(0.0f, 1.0f, 0.0f, 0.0f), Vec4(0.01f))) shouldBe true
                glm.all(glm.epsilonEqual(identity[2], Vec4(0.0f, 0.0f, 1.0f, 0.0f), Vec4(0.01f))) shouldBe true
                glm.all(glm.epsilonEqual(identity[3], Vec4(0.0f, 0.0f, 0.0f, 1.0f), Vec4(0.01f))) shouldBe true
            }
             val matrix = Mat4(
            0.6f, 0.2f, 0.3f, 0.4f,
            0.2f, 0.7f, 0.5f, 0.3f,
            0.3f, 0.5f, 0.7f, 0.2f,
            0.4f, 0.3f, 0.2f, 0.6f)
            val identity = matrix / matrix

            glm.all(glm.epsilonEqual(identity[0], Vec4(1.0f, 0.0f, 0.0f, 0.0f), Vec4(0.01f))) shouldBe true
            glm.all(glm.epsilonEqual(identity[1], Vec4(0.0f, 1.0f, 0.0f, 0.0f), Vec4(0.01f))) shouldBe true
            glm.all(glm.epsilonEqual(identity[2], Vec4(0.0f, 0.0f, 1.0f, 0.0f), Vec4(0.01f))) shouldBe true
            glm.all(glm.epsilonEqual(identity[3], Vec4(0.0f, 0.0f, 0.0f, 1.0f), Vec4(0.01f))) shouldBe true
        }

    }
}