package quix.hosted

import com.mp.javaPaymentSDK.adapters.HostedQuixPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.exceptions.InvalidFieldException
import com.mp.javaPaymentSDK.exceptions.MissingFieldException
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixAccommodationPaySolExtendedData
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixArticleAccommodation
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixCartAccommodation
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixItemCartItemAccommodation
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixAccommodation
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HostedPaymentChargeAccommodationTest {

    @Test
    fun successHostedResponse() {

        mockkStatic(SecurityUtils::class)
        every { SecurityUtils.generateIV() } returns ByteArray(16) { 1 }
        every { SecurityUtils.hash256(any()) } answers { callOriginal() }
        every { SecurityUtils.base64Encode(any()) } answers { callOriginal() }
        every { SecurityUtils.applyAESPadding(any()) } answers { callOriginal() }
        every { SecurityUtils.cbcEncryption(any(), any(), any(), any()) } answers { callOriginal() }

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
        credentials.productId = "1112220003"
        credentials.apiVersion = 5

        val hostedQuixAccommodation = HostedQuixAccommodation()
        hostedQuixAccommodation.amount = "99.0"
        hostedQuixAccommodation.customerId = "903"
        hostedQuixAccommodation.statusURL = "https://test.com/paymentNotification"
        hostedQuixAccommodation.cancelURL = "https://test.com/cancel"
        hostedQuixAccommodation.errorURL = "https://test.com/error"
        hostedQuixAccommodation.successURL = "https://test.com/success"
        hostedQuixAccommodation.awaitingURL = "https://test.com/awaiting"
        hostedQuixAccommodation.customerEmail = "test@mail.com"
        hostedQuixAccommodation.dob = "01-12-1999"
        hostedQuixAccommodation.firstName = "Name"
        hostedQuixAccommodation.lastName = "Last Name"
        hostedQuixAccommodation.merchantTransactionId = "12345678"
        hostedQuixAccommodation.ipAddress = "0.0.0.0"

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.streetAddress = "Nombre de la vía y nº"
        quixAddress.postalCode = "28003"

        val quixArticleAccommodation = QuixArticleAccommodation()
        quixArticleAccommodation.name = "Nombre del servicio 2"
        quixArticleAccommodation.reference = "4912345678903"
        quixArticleAccommodation.checkinDate = "2024-10-30T00:00:00+01:00"
        quixArticleAccommodation.checkoutDate = "2024-12-31T23:59:59+01:00"
        quixArticleAccommodation.guests = 1
        quixArticleAccommodation.establishmentName = "Hotel"
        quixArticleAccommodation.address = quixAddress
        quixArticleAccommodation.unitPriceWithTax = 99.0
        quixArticleAccommodation.category = Category.digital

        val quixItemCartItemAccommodation = QuixItemCartItemAccommodation()
        quixItemCartItemAccommodation.article = quixArticleAccommodation
        quixItemCartItemAccommodation.units = 1
        quixItemCartItemAccommodation.isAuto_shipping = true
        quixItemCartItemAccommodation.totalPriceWithTax = 99.0

        val items: MutableList<QuixItemCartItemAccommodation> = java.util.ArrayList()
        items.add(quixItemCartItemAccommodation)

        val quixCartAccommodation = QuixCartAccommodation()
        quixCartAccommodation.currency = Currency.EUR
        quixCartAccommodation.items = items
        quixCartAccommodation.total_price_with_tax = 99.0

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.firstName = "Nombre"
        quixBilling.lastName = "Apellido"

        val quixAccommodationPaySolExtendedData = QuixAccommodationPaySolExtendedData()
        quixAccommodationPaySolExtendedData.cart = quixCartAccommodation
        quixAccommodationPaySolExtendedData.billing = quixBilling
        quixAccommodationPaySolExtendedData.product = "instalments"

        hostedQuixAccommodation.paySolExtendedData = quixAccommodationPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixAccommodationRequest(hostedQuixAccommodation, mockedResponseListener)

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
            "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",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "0646df8e9cfa90ff02ef27a032b46e4a98e0d2249a067dd0bb1509031ccd9db0",
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

        mockkStatic(SecurityUtils::class)
        every { SecurityUtils.generateIV() } returns ByteArray(16) { 1 }
        every { SecurityUtils.hash256(any()) } answers { callOriginal() }
        every { SecurityUtils.base64Encode(any()) } answers { callOriginal() }
        every { SecurityUtils.applyAESPadding(any()) } answers { callOriginal() }
        every { SecurityUtils.cbcEncryption(any(), any(), any(), any()) } answers { callOriginal() }

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
        credentials.apiVersion = 5

        val hostedQuixAccommodation = HostedQuixAccommodation()
        hostedQuixAccommodation.amount = "99.0"
        hostedQuixAccommodation.customerId = "903"
        hostedQuixAccommodation.cancelURL = "https://test.com/cancel"
        hostedQuixAccommodation.errorURL = "https://test.com/error"
        hostedQuixAccommodation.awaitingURL = "https://test.com/awaiting"
        hostedQuixAccommodation.successURL = "https://test.com/success"
        hostedQuixAccommodation.customerEmail = "test@mail.com"
        hostedQuixAccommodation.dob = "01-12-1999"
        hostedQuixAccommodation.firstName = "Name"
        hostedQuixAccommodation.lastName = "Last Name"
        hostedQuixAccommodation.merchantTransactionId = "12345678"

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.streetAddress = "Nombre de la vía y nº"
        quixAddress.postalCode = "28003"

        val quixArticleAccommodation = QuixArticleAccommodation()
        quixArticleAccommodation.name = "Nombre del servicio 2"
        quixArticleAccommodation.reference = "4912345678903"
        quixArticleAccommodation.checkinDate = "2024-10-30T00:00:00+01:00"
        quixArticleAccommodation.checkoutDate = "2024-12-31T23:59:59+01:00"
        quixArticleAccommodation.guests = 1
        quixArticleAccommodation.establishmentName = "Hotel"
        quixArticleAccommodation.address = quixAddress
        quixArticleAccommodation.unitPriceWithTax = 99.0

        val quixItemCartItemAccommodation = QuixItemCartItemAccommodation()
        quixItemCartItemAccommodation.article = quixArticleAccommodation
        quixItemCartItemAccommodation.units = 1
        quixItemCartItemAccommodation.isAuto_shipping = true
        quixItemCartItemAccommodation.totalPriceWithTax = 99.0

        val items: MutableList<QuixItemCartItemAccommodation> = java.util.ArrayList()
        items.add(quixItemCartItemAccommodation)

        val quixCartAccommodation = QuixCartAccommodation()
        quixCartAccommodation.currency = Currency.EUR
        quixCartAccommodation.items = items
        quixCartAccommodation.total_price_with_tax = 99.0

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.firstName = "Nombre"
        quixBilling.lastName = "Apellido"

        val quixAccommodationPaySolExtendedData = QuixAccommodationPaySolExtendedData()
        quixAccommodationPaySolExtendedData.cart = quixCartAccommodation
        quixAccommodationPaySolExtendedData.billing = quixBilling
        quixAccommodationPaySolExtendedData.product = "instalments"

        hostedQuixAccommodation.paySolExtendedData = quixAccommodationPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)

        val exception = assertThrows<MissingFieldException> {
            hostedQuixPaymentAdapter.sendHostedQuixAccommodationRequest(hostedQuixAccommodation, mockedResponseListener)
        }

        assertEquals("Missing category", exception.message)
    }

    @Test
    fun failInvalidAmountHosted() {

        mockkStatic(SecurityUtils::class)
        every { SecurityUtils.generateIV() } returns ByteArray(16) { 1 }
        every { SecurityUtils.hash256(any()) } answers { callOriginal() }
        every { SecurityUtils.base64Encode(any()) } answers { callOriginal() }
        every { SecurityUtils.applyAESPadding(any()) } answers { callOriginal() }
        every { SecurityUtils.cbcEncryption(any(), any(), any(), any()) } answers { callOriginal() }

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
        credentials.productId = "1112220003"
        credentials.apiVersion = 5

        val hostedQuixAccommodation = HostedQuixAccommodation()
        val exception = assertThrows<InvalidFieldException> {
            hostedQuixAccommodation.amount = "99,00"
        }

        assertEquals("amount: Should Follow Format #.#### And Be Between 0 And 1000000", exception.message)
    }
}