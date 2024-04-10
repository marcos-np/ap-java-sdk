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
import com.mp.javaPaymentSDK.models.quix_models.quix_flight.*
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixFlight
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HostedPaymentFlightsTest {

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
        credentials.productId = "1112220001"

        val hostedQuixFlight = HostedQuixFlight()
        hostedQuixFlight.amount = 99.0
        hostedQuixFlight.customerId = "903"
        hostedQuixFlight.statusURL = "https://test.com/paymentNotification"
        hostedQuixFlight.cancelURL = "https://test.com/cancel"
        hostedQuixFlight.errorURL = "https://test.com/error"
        hostedQuixFlight.successURL = "https://test.com/success"
        hostedQuixFlight.awaitingURL = "https://test.com/awaiting"
        hostedQuixFlight.customerEmail = "test@mail.com"
        hostedQuixFlight.dob = "01-12-1999"
        hostedQuixFlight.firstName = "Name"
        hostedQuixFlight.lastName = "Last Name"
        hostedQuixFlight.merchantTransactionId = "12345678"
        hostedQuixFlight.apiVersion = 5

        val quixPassengerFlight = QuixPassengerFlight()
        quixPassengerFlight.firstName = "Pablo"
        quixPassengerFlight.lastName = "Navvaro"

        val passangers: MutableList<QuixPassengerFlight> = ArrayList()
        passangers.add(quixPassengerFlight)

        val quixSegmentFlight = QuixSegmentFlight()
        quixSegmentFlight.iataDepartureCode = "MAD"
        quixSegmentFlight.iataDestinationCode = "BCN"

        val segments: MutableList<QuixSegmentFlight> = ArrayList()
        segments.add(quixSegmentFlight)

        val quixArticleFlight = QuixArticleFlight()
        quixArticleFlight.name = "Nombre del servicio 2"
        quixArticleFlight.reference = "4912345678903"
        quixArticleFlight.customerMemberSince = "2023-10-30T00:00:00+01:00"
        quixArticleFlight.departureDate = "2024-12-31T23:59:59+01:00"
        quixArticleFlight.passengers = passangers
        quixArticleFlight.segments = segments
        quixArticleFlight.unit_price_with_tax = 99.0

        val quixItemCartItemFlight = QuixItemCartItemFlight()
        quixItemCartItemFlight.article = quixArticleFlight
        quixItemCartItemFlight.units = 1
        quixItemCartItemFlight.isAuto_shipping = true
        quixItemCartItemFlight.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemFlight> = ArrayList()
        items.add(quixItemCartItemFlight)

        val quixCartFlight = QuixCartFlight()
        quixCartFlight.currency = Currency.EUR
        quixCartFlight.items = items
        quixCartFlight.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixFlightPaySolExtendedData = QuixFlightPaySolExtendedData()
        quixFlightPaySolExtendedData.cart = quixCartFlight
        quixFlightPaySolExtendedData.billing = quixBilling
        quixFlightPaySolExtendedData.product = "instalments"

        hostedQuixFlight.paysolExtendedData = quixFlightPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixFlightRequest(hostedQuixFlight, mockedResponseListener)

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
            "3ad1c13519953959d202135e95d4d1f8905d8d786504bd6f533cfe3db9d54d42",
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

        val hostedQuixFlight = HostedQuixFlight()
        hostedQuixFlight.amount = 99.0
        hostedQuixFlight.customerId = "903"
        hostedQuixFlight.cancelURL = "https://test.com/cancel"
        hostedQuixFlight.errorURL = "https://test.com/error"
        hostedQuixFlight.successURL = "https://test.com/success"
        hostedQuixFlight.customerEmail = "test@mail.com"
        hostedQuixFlight.dob = "01-12-1999"
        hostedQuixFlight.firstName = "Name"
        hostedQuixFlight.lastName = "Last Name"
        hostedQuixFlight.merchantTransactionId = "12345678"
        hostedQuixFlight.apiVersion = 5

        val quixPassengerFlight = QuixPassengerFlight()
        quixPassengerFlight.firstName = "Pablo"
        quixPassengerFlight.lastName = "Navvaro"

        val passangers: MutableList<QuixPassengerFlight> = ArrayList()
        passangers.add(quixPassengerFlight)

        val quixSegmentFlight = QuixSegmentFlight()
        quixSegmentFlight.iataDepartureCode = "MAD"
        quixSegmentFlight.iataDestinationCode = "BCN"

        val segments: MutableList<QuixSegmentFlight> = ArrayList()
        segments.add(quixSegmentFlight)

        val quixArticleFlight = QuixArticleFlight()
        quixArticleFlight.name = "Nombre del servicio 2"
        quixArticleFlight.reference = "4912345678903"
        quixArticleFlight.customerMemberSince = "2023-10-30T00:00:00+01:00"
        quixArticleFlight.departureDate = "2024-12-31T23:59:59+01:00"
        quixArticleFlight.passengers = passangers
        quixArticleFlight.segments = segments
        quixArticleFlight.unit_price_with_tax = 99.0

        val quixItemCartItemFlight = QuixItemCartItemFlight()
        quixItemCartItemFlight.article = quixArticleFlight
        quixItemCartItemFlight.units = 1
        quixItemCartItemFlight.isAuto_shipping = true
        quixItemCartItemFlight.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemFlight> = ArrayList()
        items.add(quixItemCartItemFlight)

        val quixCartFlight = QuixCartFlight()
        quixCartFlight.currency = Currency.EUR
        quixCartFlight.items = items
        quixCartFlight.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixFlightPaySolExtendedData = QuixFlightPaySolExtendedData()
        quixFlightPaySolExtendedData.cart = quixCartFlight
        quixFlightPaySolExtendedData.billing = quixBilling
        quixFlightPaySolExtendedData.product = "instalments"

        hostedQuixFlight.paysolExtendedData = quixFlightPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixFlightRequest(hostedQuixFlight, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        assertEquals("Missing statusURL", errorMessageSlot.captured)
    }
}