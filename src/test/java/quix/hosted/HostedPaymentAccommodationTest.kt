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
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixAccommodationPaySolExtendedData
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixArticleAccommodation
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixCartAccommodation
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixItemCartItemAccommodation
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixArticleProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixCartProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemCartItemProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemPaySolExtendedData
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixAccommodation
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixItem
import com.mp.javaPaymentSDK.utils.Creds
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HostedPaymentAccommodationTest {

    @Test
    fun successHostedResponse() {

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
        credentials.productId = "1112220003"

        val hostedQuixAccommodation = HostedQuixAccommodation()
        hostedQuixAccommodation.amount = 99.0
        hostedQuixAccommodation.apiVersion = 5
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

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixArticleAccommodation = QuixArticleAccommodation()
        quixArticleAccommodation.name = "Nombre del servicio 2"
        quixArticleAccommodation.reference = "4912345678903"
        quixArticleAccommodation.checkinDate = "2024-10-30T00:00:00+01:00"
        quixArticleAccommodation.checkoutDate = "2024-12-31T23:59:59+01:00"
        quixArticleAccommodation.guests = 1
        quixArticleAccommodation.establishmentName = "Hotel"
        quixArticleAccommodation.address = quixAddress
        quixArticleAccommodation.unit_price_with_tax = 99.0

        val quixItemCartItemAccommodation = QuixItemCartItemAccommodation()
        quixItemCartItemAccommodation.article = quixArticleAccommodation
        quixItemCartItemAccommodation.units = 1
        quixItemCartItemAccommodation.isAuto_shipping = true
        quixItemCartItemAccommodation.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemAccommodation> = java.util.ArrayList()
        items.add(quixItemCartItemAccommodation)

        val quixCartAccommodation = QuixCartAccommodation()
        quixCartAccommodation.currency = Currency.EUR
        quixCartAccommodation.items = items
        quixCartAccommodation.total_price_with_tax = 99.0

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

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
            "fbc543b4c2e0c8a76bdb03ba07af936d93aa7351a0231d980f5dd9b0c6113c01",
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

        val hostedQuixAccommodation = HostedQuixAccommodation()
        hostedQuixAccommodation.amount = 99.0
        hostedQuixAccommodation.customerId = "903"
        hostedQuixAccommodation.cancelURL = "https://test.com/cancel"
        hostedQuixAccommodation.errorURL = "https://test.com/error"
        hostedQuixAccommodation.successURL = "https://test.com/success"
        hostedQuixAccommodation.customerEmail = "test@mail.com"
        hostedQuixAccommodation.dob = "01-12-1999"
        hostedQuixAccommodation.firstName = "Name"
        hostedQuixAccommodation.lastName = "Last Name"
        hostedQuixAccommodation.merchantTransactionId = "12345678"
        hostedQuixAccommodation.apiVersion = 5

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixArticleAccommodation = QuixArticleAccommodation()
        quixArticleAccommodation.name = "Nombre del servicio 2"
        quixArticleAccommodation.reference = "4912345678903"
        quixArticleAccommodation.checkinDate = "2024-10-30T00:00:00+01:00"
        quixArticleAccommodation.checkoutDate = "2024-12-31T23:59:59+01:00"
        quixArticleAccommodation.guests = 1
        quixArticleAccommodation.establishmentName = "Hotel"
        quixArticleAccommodation.address = quixAddress
        quixArticleAccommodation.unit_price_with_tax = 99.0

        val quixItemCartItemAccommodation = QuixItemCartItemAccommodation()
        quixItemCartItemAccommodation.article = quixArticleAccommodation
        quixItemCartItemAccommodation.units = 1
        quixItemCartItemAccommodation.isAuto_shipping = true
        quixItemCartItemAccommodation.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemAccommodation> = java.util.ArrayList()
        items.add(quixItemCartItemAccommodation)

        val quixCartAccommodation = QuixCartAccommodation()
        quixCartAccommodation.currency = Currency.EUR
        quixCartAccommodation.items = items
        quixCartAccommodation.total_price_with_tax = 99.0


        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixAccommodationPaySolExtendedData = QuixAccommodationPaySolExtendedData()
        quixAccommodationPaySolExtendedData.cart = quixCartAccommodation
        quixAccommodationPaySolExtendedData.billing = quixBilling
        quixAccommodationPaySolExtendedData.product = "instalments"

        hostedQuixAccommodation.paySolExtendedData = quixAccommodationPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixAccommodationRequest(hostedQuixAccommodation, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        assertEquals("Missing statusURL", errorMessageSlot.captured)
    }
}