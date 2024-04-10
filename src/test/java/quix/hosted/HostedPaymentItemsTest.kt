package quix.hosted

import com.mp.javaPaymentSDK.adapters.HostedQuixPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.adapters.SocketAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixArticleProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixCartProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemCartItemProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemPaySolExtendedData
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixItem
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HostedPaymentItemsTest {

    @Test
    fun successHostedNotification() {

        val mockedSecurityUtils = mockk<SecurityUtils>()
        every { mockedSecurityUtils.generateIV() } returns ByteArray(16) { 1 }
        every { mockedSecurityUtils.hash256(any()) } answers { callOriginal() }
        every { mockedSecurityUtils.base64Encode(any()) } answers { callOriginal() }
        every { mockedSecurityUtils.applyAESPadding(any()) } answers { callOriginal() }
        every { mockedSecurityUtils.cbcEncryption(any(), any(), any(), any()) } answers { callOriginal() }

        mockkStatic(SecurityUtils::class)
        every { SecurityUtils.getInstance() } returns mockedSecurityUtils

        mockkConstructor(NetworkAdapter::class, recordPrivateCalls = true)
        every {
            anyConstructed<NetworkAdapter>()["sendRequest"](
                any<HashMap<String, String>>(),
                any<HashMap<String, String>>(),
                any<RequestBody>(),
                any<String>(),
                any<RequestListener>()
            )
        } answers { }

        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onRedirectionURLReceived(any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs


        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val hostedQuixItem = HostedQuixItem()
        hostedQuixItem.amount = 99.0
        hostedQuixItem.customerId = "903"
        hostedQuixItem.statusURL = "https://test.com/paymentNotification"
        hostedQuixItem.cancelURL = "https://test.com/cancel"
        hostedQuixItem.errorURL = "https://test.com/error"
        hostedQuixItem.successURL = "https://test.com/success"
        hostedQuixItem.awaitingURL = "https://test.com/awaiting"
        hostedQuixItem.customerEmail = "test@mail.com"
        hostedQuixItem.dob = "01-12-1999"
        hostedQuixItem.firstName = "Name"
        hostedQuixItem.lastName = "Last Name"
        hostedQuixItem.merchantTransactionId = "12345678"
        hostedQuixItem.apiVersion = 5

        val quixArticleProduct = QuixArticleProduct()
        quixArticleProduct.name = "Nombre del servicio 2"
        quixArticleProduct.reference = "4912345678903"
        quixArticleProduct.unit_price_with_tax = 99.0

        val quixItemCartItemProduct = QuixItemCartItemProduct()
        quixItemCartItemProduct.article = quixArticleProduct
        quixItemCartItemProduct.units = 1
        quixItemCartItemProduct.isAuto_shipping = true
        quixItemCartItemProduct.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemProduct> = ArrayList()
        items.add(quixItemCartItemProduct)

        val quixCartProduct = QuixCartProduct()
        quixCartProduct.currency = Currency.EUR
        quixCartProduct.items = items
        quixCartProduct.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "08003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixItemPaySolExtendedData = QuixItemPaySolExtendedData()
        quixItemPaySolExtendedData.cart = quixCartProduct
        quixItemPaySolExtendedData.billing = quixBilling
        quixItemPaySolExtendedData.product = "instalments"

        hostedQuixItem.paySolExtendedData = quixItemPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixItemRequest(hostedQuixItem, mockedResponseListener)

        val headersSlot = slot<Map<String, String>>()
        val queryParameterSlot = slot<Map<String, String>>()
        val requestBodySlot = slot<RequestBody>()
        val urlSlot = slot<String>()
        val requestListenerSlot = slot<RequestListener>()

        verify {
            anyConstructed<NetworkAdapter>()["sendRequest"](
                capture(headersSlot),
                capture(queryParameterSlot),
                capture(requestBodySlot),
                capture(urlSlot),
                capture(requestListenerSlot)
            )
        }

        assertEquals(3, headersSlot.captured.size)
        assertEquals("5", headersSlot.captured["apiVersion"])
        assertEquals("CBC", headersSlot.captured["encryptionMode"])
        assertEquals("AQEBAQEBAQEBAQEBAQEBAQ==", headersSlot.captured["iv"])

        assertEquals(3, queryParameterSlot.captured.size)
        assertEquals("111222", queryParameterSlot.captured["merchantId"])
        assertEquals(
            "2BDGM6x+gfSbyWnsjorW6b0Wh5TTXozicFV50i79ERjoBYqoDeBdLcajlW0uweHUpvyt/spU/m+Hlg7onB2Iaw6ZF0GhG7N/22Qa9aZRSmpFV1HW2hciZPwSgX9voTk5DjULmRTMdfmczdSZd7FfmfK9xXgSZwl+lDf20sfwGDJsix4ilrCeIQ0qBNYxAMxcMNiDA9G7gcVZdTG6HkAf9HadcVyUdsD6xmJHHkH84F1rud1qtNUdvqwS/xX2H7xT0lI43yhD6WWAOBo6fIs15SILG302wArHl7HofWJXyT9YUM2FV3lAZzuxbkIayX4jNzh27i7fc+WX3tVaFr5fjQzcHLfXAA0ZdhWFGMMg0LYuTs6idSkV4pzNZtofNRYD5t7hhrxvc3GxU8BnIbitsp8zL0cCG2Zps0n0J4Y7AqWWKSegT4VNy9oCfy64cLiaUMT4v1vdvAAh5iocuhl3BOEc750XcKLDu+6U3cjvKC7sbuJQ9eRY2LY86gqBmthGPurFtOecHT9lfLlrvC/KYWqKtBKJCTMq4WVBgRXr4zYJM4Anp0lPSTxlYcb6G5Hbyl1ALQyBrNaSsyFU0vjzV2XTPEIXQIk2cgqnguV//AZYH7JAXnOLeZMJBlf2mG22RnULhyB7XnuEOG2stMXKTbwCoPe4hlpXdCTkEH2r+QFVglIanU/B5D1ZemsvoNQYpvJSC4728daodbgRTy9wjcxOHSOwwSEEdbVJti3f1XBLVv/DdmIABA02IGvNA4Ep4BNyVu5n2h4Zd6+47YYOm5BJXZ8IjtEaQPTHSEM08ePGbWpVSsg5S0zn/hAl1+VwzfdMtUF92lYwPeslR8FX0434vTEb4GevhaEgOLo420Z8o/624yIjLlYo3+NKYiy3PpsJ0zmDiPJKRqIwwfAhuMEWuTlPtV7IQip1J3vIhboP8VDUbODo8vo4y09XdHSznsRKxo8E8+qz8Vo9pI5gIwdmdM3xNZ16zXsh6b9xWAqvE9hAFKRMgcC68ssyl1+OCRS1TNTSxeUI7ozld61oS379XT2TAenjmrn0dzVFG5T3n/qpWV4egzMiNIbLagv+0xL7dvdzNa3y2a7qunu9UOtC19zJ+bFo4PpxsiP+x6DmQuALzjFbhNuh3/duo7sqQ0RjwvukVW4/nBN8uQcsUEGs8Biyrxzxy3WRk9OIX9Q+516hVPMurfcJNiEfsrz7til/hx0Znd3e3oNEf8PD1iiRbQPlHipz9wmx/Mjk/iepnQl0Hw5HHTlo32R1vyOaQoeOEeJL1IFNbfr1hXL+4HfvWEF7WLRE2HxHy3v5tMp+ZpVxI246gNxNUP8u4HcAY+KqBKKZT2JH5LZF3CVNcd1Pnd0bFeF4QNZeVajA6tMUjK+RuxuwAsr4qAN678vqKwUJXCUtSSJMO9yB6Vhcm4f1O/mMzVMbn4YKVrOxv2XlADiLarJZ/BVt7Sa3yBBWrF9KsSVCD0jwqkXwV5izNlX1CfIDzizPyOOaW1Ou8rOu9W05OJM5IIy6XxuA3DdI0Jufe08mRTfXngfJ5KL4tY2p59pn8iq9gjcfYG7AjHm8XZOy2LxuaQoUV00th+slbZIam1u7BGzloLDmkkM7HEmL9PVwm5xxjMB5d1ZAlcv4MzZXwMcADPGcLUR9KXRs",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "7e94aac0b3f8bf92fdd60830a37ed9872c7317ae8cb80303d97abb13a5c1db51",
            queryParameterSlot.captured["integrityCheck"]
        )

        assertEquals(Endpoints.HOSTED_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns "http://test.com"

        requestListenerSlot.captured.onResponse(200, mockedResponseBody)

        val redirectionUrlSlot = slot<String>()

        verify {
            mockedResponseListener.onRedirectionURLReceived(
                capture(redirectionUrlSlot)
            )
        }

        verify(exactly = 0) {
            mockedResponseListener.onResponseReceived(any(), any(), any())
            mockedResponseListener.onError(any(), any())
        }

        assertEquals("http://test.com", redirectionUrlSlot.captured)
    }

    @Test
    fun failMissingParameterHosted() {
        mockkConstructor(SocketAdapter::class, recordPrivateCalls = true)
        every { anyConstructed<SocketAdapter>().connect(any(), any()) } just Runs

        val mockedSecurityUtils = mockk<SecurityUtils>()
        every { mockedSecurityUtils.generateIV() } returns ByteArray(16) { 1 }
        every { mockedSecurityUtils.hash256(any()) } answers { callOriginal() }
        every { mockedSecurityUtils.base64Encode(any()) } answers { callOriginal() }
        every { mockedSecurityUtils.applyAESPadding(any()) } answers { callOriginal() }
        every { mockedSecurityUtils.cbcEncryption(any(), any(), any(), any()) } answers { callOriginal() }

        mockkStatic(SecurityUtils::class)
        every { SecurityUtils.getInstance() } returns mockedSecurityUtils

        mockkConstructor(NetworkAdapter::class, recordPrivateCalls = true)
        every {
            anyConstructed<NetworkAdapter>()["sendRequest"](
                any<HashMap<String, String>>(),
                any<HashMap<String, String>>(),
                any<RequestBody>(),
                any<String>(),
                any<RequestListener>()
            )
        } answers { }

        val mockedResponseListener = mockk<ResponseListener>();
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onRedirectionURLReceived(any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs


        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val hostedQuixItem = HostedQuixItem()
        hostedQuixItem.amount = 99.0
        hostedQuixItem.customerId = "903"
        hostedQuixItem.cancelURL = "https://test.com/cancel"
        hostedQuixItem.errorURL = "https://test.com/error"
        hostedQuixItem.successURL = "https://test.com/success"
        hostedQuixItem.customerEmail = "test@mail.com"
        hostedQuixItem.dob = "01-12-1999"
        hostedQuixItem.firstName = "Name"
        hostedQuixItem.lastName = "Last Name"
        hostedQuixItem.merchantTransactionId = "12345678"
        hostedQuixItem.apiVersion = 5

        val quixArticleProduct = QuixArticleProduct()
        quixArticleProduct.name = "Nombre del servicio 2"
        quixArticleProduct.reference = "4912345678903"
        quixArticleProduct.unit_price_with_tax = 99.0

        val quixItemCartItemProduct = QuixItemCartItemProduct()
        quixItemCartItemProduct.article = quixArticleProduct
        quixItemCartItemProduct.units = 1
        quixItemCartItemProduct.isAuto_shipping = true
        quixItemCartItemProduct.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemProduct> = ArrayList()
        items.add(quixItemCartItemProduct)

        val quixCartProduct = QuixCartProduct()
        quixCartProduct.currency = Currency.EUR
        quixCartProduct.items = items
        quixCartProduct.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "08003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixItemPaySolExtendedData = QuixItemPaySolExtendedData()
        quixItemPaySolExtendedData.cart = quixCartProduct
        quixItemPaySolExtendedData.billing = quixBilling
        quixItemPaySolExtendedData.product = "instalments"

        hostedQuixItem.paySolExtendedData = quixItemPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixItemRequest(hostedQuixItem, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        assertEquals("Missing statusURL", errorMessageSlot.captured)
    }
}