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
        hostedQuixFlight.amount = "99.0"
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
            "bd821f01ebbeed9d50ae1bccfbfbf52564eabd6ff14dea0d4905576c52d4d6ca",
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
        hostedQuixFlight.amount = "99.0"
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

    @Test
    fun failInvalidAmountHosted() {
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
        hostedQuixFlight.amount = "99,0"
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

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }
}