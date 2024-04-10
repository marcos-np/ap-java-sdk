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
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixArticleService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixCartService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixItemCartItemService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixServicePaySolExtendedData
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixItem
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixService
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HostedPaymentServiceTest {

    @Test
    fun successHostedServiceNotification() {
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

        val hostedQuixService = HostedQuixService()
        hostedQuixService.amount = 99.0
        hostedQuixService.customerId = "903"
        hostedQuixService.statusURL = "https://test.com/paymentNotification"
        hostedQuixService.cancelURL = "https://test.com/cancel"
        hostedQuixService.errorURL = "https://test.com/error"
        hostedQuixService.successURL = "https://test.com/success"
        hostedQuixService.awaitingURL = "https://test.com/awaiting"
        hostedQuixService.customerEmail = "test@mail.com"
        hostedQuixService.dob = "01-12-1999"
        hostedQuixService.firstName = "Name"
        hostedQuixService.lastName = "Last Name"
        hostedQuixService.merchantTransactionId = "12345678"
        hostedQuixService.apiVersion = 5

        val quixArticleService = QuixArticleService()
        quixArticleService.name = "Nombre del servicio 2"
        quixArticleService.reference = "4912345678903"
        quixArticleService.startDate = "2024-10-30T00:00:00+01:00"
        quixArticleService.endDate = "2024-12-31T23:59:59+01:00"
        quixArticleService.unit_price_with_tax = 99.0

        val quixItemCartItemService = QuixItemCartItemService()
        quixItemCartItemService.article = quixArticleService
        quixItemCartItemService.units = 1
        quixItemCartItemService.isAuto_shipping = true
        quixItemCartItemService.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemService> = java.util.ArrayList()
        items.add(quixItemCartItemService)

        val quixCartService = QuixCartService()
        quixCartService.currency = Currency.EUR
        quixCartService.items = items
        quixCartService.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixServicePaySolExtendedData = QuixServicePaySolExtendedData()
        quixServicePaySolExtendedData.cart = quixCartService
        quixServicePaySolExtendedData.billing = quixBilling
        quixServicePaySolExtendedData.product = "instalments"

        hostedQuixService.paySolExtendedData = quixServicePaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixServiceRequest(hostedQuixService, mockedResponseListener)

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
            "2BDGM6x+gfSbyWnsjorW6b0Wh5TTXozicFV50i79ERjoBYqoDeBdLcajlW0uweHUpvyt/spU/m+Hlg7onB2Iaw6ZF0GhG7N/22Qa9aZRSmpFV1HW2hciZPwSgX9voTk5DjULmRTMdfmczdSZd7FfmfK9xXgSZwl+lDf20sfwGDJsix4ilrCeIQ0qBNYxAMxcMNiDA9G7gcVZdTG6HkAf9HadcVyUdsD6xmJHHkH84F1rud1qtNUdvqwS/xX2H7xT0lI43yhD6WWAOBo6fIs15SILG302wArHl7HofWJXyT9YUM2FV3lAZzuxbkIayX4jNzh27i7fc+WX3tVaFr5fjQzcHLfXAA0ZdhWFGMMg0LYuTs6idSkV4pzNZtofNRYD5t7hhrxvc3GxU8BnIbitsp8zL0cCG2Zps0n0J4Y7AqWWKSegT4VNy9oCfy64cLiaUMT4v1vdvAAh5iocuhl3BOEc750XcKLDu+6U3cjvKC7sbuJQ9eRY2LY86gqBmthGPurFtOecHT9lfLlrvC/KYWqKtBKJCTMq4WVBgRXr4zYJM4Anp0lPSTxlYcb6G5Hbyl1ALQyBrNaSsyFU0vjzV2XTPEIXQIk2cgqnguV//AZYH7JAXnOLeZMJBlf2mG22RnULhyB7XnuEOG2stMXKTbwCoPe4hlpXdCTkEH2r+QFVglIanU/B5D1ZemsvoNQYpvJSC4728daodbgRTy9wjcxOHSOwwSEEdbVJti3f1XBLVv/DdmIABA02IGvNA4Ep4BNyVu5n2h4Zd6+47YYOm5BJXZ8IjtEaQPTHSEM08ePGbWpVSsg5S0zn/hAl1+VwzfdMtUF92lYwPeslR8FX0434vTEb4GevhaEgOLo420Z8o/624yIjLlYo3+NKYiy3PpsJ0zmDiPJKRqIwwfAhuMEWuTlPtV7IQip1J3vIhboP8VDUbODo8vo4y09XdHSzNkrYBOiJL67nsJX0YTO+ohIdOBz4Zh7SgE8m6DULYtZYXzM0jLWSOy8zWb0Zl+sExejpjWb2upFQjTzXBanwQ4uiq5PTx2sacthLRwkFORukbY5YMQYTfojiIvfZJrFbniy15AYYRtP92dprAezqEt/ZRhsQBEFakrEHNWkeCK6Okpc3cCtBZUBrnluUEAske6gac7GZOHfR2ULGp5tChCgQUEx2c63mrMUbxmJFdJpLekVUvQYZa2M07HIvw5sc7pGAffLRNjTWw7ZZ7cD/AY0iUse3kaSVeVJC038eu/4n5ofKOS3ZQcSN8QRIg5akBTnZifPkI3mcczbg1jvvPH3YJhYvz52tMAqvYuacVLUSlkEcIqcZ5lQOT57KfxOLeGAM/uMhlWK/5V/mDcyugo8j4qdDF4Bxqh6xaFq2my+vAQyR5+R7HgsPooKWbrqrXiXbREhOByV1VkExEBeL/G992pBJuX1WTaARt9W1A2ugM1XDYKZ6JrFJCMAOQGz6UrVoGKOJ46mp3N7pbsuvmgWXTLkzXIZOpeI6a7u2jfMpADtO/fAU2CW8ohPeNxkht9WPHlpoigMKmjWo64Eyo87DvG4EQOLTwodEmHR8Ciy70Hi232BTZMZ/8Nl0XCUs0cE7EwfaQf4VPQCSieK46OJzWvys1tWG7AQvvDfwNww0Qhnv9hKGT9N3/DtKlbo2CfMyoFxrUC0GNlu6LzOV7Uw9sKs8VrMzBbJfIcvAeGK7f1nKJrkBumDrVFgo+BVlym/Ev9I+bW0magD9Civv/9ovKsH5v/PRFuDOhsBjfmF0KZjcfedn4DAFsmy4OItMnh/kp6jZo1QWAMSIVc3lGQ==",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "e68ab283ce8f81acd393b852aca81695ce575538f4f41b67833ac2197237f578",
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