package utils

import com.mp.javaPaymentSDK.utils.Utils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.util.Locale

class Utils {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            Locale.setDefault(Locale.Builder().setLanguage("es").setRegion("ES").build())
        }
    }

    @Test
    fun parseAmountTest() {
        Assertions.assertEquals("50.1200", Utils.getInstance().parseAmount("50.12"))
        Assertions.assertEquals("50.0000", Utils.getInstance().parseAmount("50"))
        Assertions.assertEquals("50.1234", Utils.getInstance().parseAmount("50.1234111"))
        Assertions.assertEquals("50.1111", Utils.getInstance().parseAmount("50.1111"))
        Assertions.assertEquals("50.1112", Utils.getInstance().parseAmount("50.111167"))
        Assertions.assertNull(Utils.getInstance().parseAmount("50,1111"))
    }

}