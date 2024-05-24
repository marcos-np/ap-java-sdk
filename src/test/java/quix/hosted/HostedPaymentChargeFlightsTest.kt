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
import com.mp.javaPaymentSDK.models.quix_models.quix_flight.*
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixFlight
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HostedPaymentChargeFlightsTest {

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
        credentials.productId = "1112220001"
        credentials.apiVersion = 5

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
        hostedQuixFlight.ipAddress = "0.0.0.0"

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
        quixArticleFlight.departureDate = "2024-12-31T23:59:59+01:00"
        quixArticleFlight.passengers = passangers
        quixArticleFlight.segments = segments
        quixArticleFlight.unitPriceWithTax = 99.0
        quixArticleFlight.category = Category.digital

        val quixItemCartItemFlight = QuixItemCartItemFlight()
        quixItemCartItemFlight.article = quixArticleFlight
        quixItemCartItemFlight.units = 1
        quixItemCartItemFlight.isAutoShipping = true
        quixItemCartItemFlight.totalPriceWithTax = 99.0

        val items: MutableList<QuixItemCartItemFlight> = ArrayList()
        items.add(quixItemCartItemFlight)

        val quixCartFlight = QuixCartFlight()
        quixCartFlight.currency = Currency.EUR
        quixCartFlight.items = items
        quixCartFlight.totalPriceWithTax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.streetAddress = "Nombre de la vía y nº"
        quixAddress.postalCode = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.firstName = "Nombre"
        quixBilling.lastName = "Apellido"

        val quixFlightPaySolExtendedData = QuixFlightPaySolExtendedData()
        quixFlightPaySolExtendedData.cart = quixCartFlight
        quixFlightPaySolExtendedData.billing = quixBilling
        quixFlightPaySolExtendedData.product = "instalments"

        hostedQuixFlight.paySolExtendedData = quixFlightPaySolExtendedData

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
            "pDH/U+/gbuzXdYp84aiQKsVwdo0OluLSE7iid4fDTDsOp3Iz5PMaVkId+H/okm/59Slik6eoVuhf9S0X7utcyiYp1zqBuvvjPWiO0Nmne1/ZLwf2liuTEo6jRVTCGjokuW3KnOMHbgeoHjg5TaK6fzocze2OWBs55Luc+A4onL6/qm7Lt8dAhWkUjIcWzIE5KXyKPm4Icm16zGh5wmDou/WEtJVEedu7LsO1HfOvJro6s39Ya+e8RAaNEoQZ64f4J8kDU9KYEm6aQZrOEp/+n1wI2Vc7u/6Y/VGO2ye7649smVWFsPhgGe9L8i5wzQRI4xpVpKLKQKe2Opx7fG7FZVgy1RZ8Ye4t3KZ8qEKlpHMTriACrdB4QxcwctvbRQCWgjCqCEDwD+98eTefF1LRqh2++/xptrxrXxBl+oOiyNbIdG8ZllTmYZIDF/dIESFWQUXqxL3vAYEj4CMctMZSmb2721NohunvlobjzKnbl/LB8o2dxsCrONuhDn8AX4I+5+IErn+6ifP0cgce4p1LwQL3twThfnqWZRIz0D9O54QEVSj/MTHwTFumZHiNdHlw5tHa+rFVK26AbJ/2za7kmZtaIDbJRFzJdCzRD011DY08wqla03v4qBBdNFCBJqjE2NXnmcAELemPMC+CdnfDmyT0Asyn0vLagzVJrWcN5ntxdtojoybPT4MgafMID6scUltph1LrpSLJRYqd39gLMbNv9icUxwpQMWVWFGWH+guE+GOTDcQ3z/DYuhSmS9ApTe/cULsQHjF/J/GHqtNSOupGeMJf54VL7IDeE6dIGoufPFDyO7Zs0wNsflGQh1Ri2OjGeT4cJZRhCchy1a5oxgb4GZekJcFHFvMD8BI5onoEeqbmpT3kEc+noty+nyCB2OGfHCKQ3Pz9Lt6qTIiQARk36KROp5XfSpn5+F3P9nb8dNJEG7UyEpd12AShXkms8vihKrmshExP0ciF5+KLKyysOcJ3l4BZHSqv1xhnMVZAEyTLrYOJpddf7ND1Fq8yQTF71w06gpuJAY161jzMpZiwkzDwngasTOHIeI3KDz5ooKkg7sH/L+KroqenuhPVheN2NAL2F327Ptd6eQf8zdAdk7Bk6454+mf/UR9ZebntXlKTQGrRJWowcPsJEhkIbw82YIJQbGunOP8xvVXxtycXmSMSFTBaxizIQjKenyf0JO2W9DdfYndUl6JG1ayfIGzgXM+Vqo7+I4xyKUrnRiuuGtMs6Ir574D9Ut0BD8ezBVO9vSEJ9EZ+ZhsnBNl07BRR7CvDeE4CIO9DEJpktUteK7RTiVI70uogrf1BStFG+nvpDnHMAvHTtP8gZVn0AwhhawjB8wYokGg0db1AsmGN+vhYQbaEQgg+2aWh9EXAcNbMiL3jqbQijqwlmkfx0oVzsAbyEm3HJWvck4NIker63CV0umPkPlqwzp3YcRvpNVl4CzSVjNCtIGg648glV6Sb/CyOIZzbpF9/pP3OmDmeDvmFSdlGyaLWYKLfuQGGJ40gUFGx/Gv09r+tZU9E1+HAoyO/P87h/3HNIhHvuN6rkVN6Q0hXMObWNQvh4sDxowiTsOSK9V370gKI1qZRH55nlEbHMTNk53idU9yQQOfecj0eQYVh1lR2hxau0Wh7pDb0FCdOJldUi5g7Gyyw1OcW8sS43Uuc1lROJ3VPkdtbKbgRT4d0l/JLxGtG+TrMPSfusKsLKzAv/XLD+qqoWFHJ519H4lwm3uzd7cr5g+lqbtVQKKxS1dh+/paLxXxb62cQrYqiWeiUIUJ/0Qi/0bNrR6Ac/RB678t12WJmE555zOdeGd72qREag9Uyk3MBtbuQPIpCbgQrJ9f5VU4+yOxVTyCz8Mq6SUUkdD5gZD+WdvPLHTKjsumhBIibmec=",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "f2dfd78451135a036594d7e0541cacad0b049bd1c16a354f49d5a3d126dd120f",
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
        quixArticleFlight.departureDate = "2024-12-31T23:59:59+01:00"
        quixArticleFlight.passengers = passangers
        quixArticleFlight.segments = segments
        quixArticleFlight.unitPriceWithTax = 99.0
        quixArticleFlight.category = Category.digital

        val quixItemCartItemFlight = QuixItemCartItemFlight()
        quixItemCartItemFlight.article = quixArticleFlight
        quixItemCartItemFlight.units = 1
        quixItemCartItemFlight.isAutoShipping = true
        quixItemCartItemFlight.totalPriceWithTax = 99.0

        val items: MutableList<QuixItemCartItemFlight> = ArrayList()
        items.add(quixItemCartItemFlight)

        val quixCartFlight = QuixCartFlight()
        quixCartFlight.currency = Currency.EUR
        quixCartFlight.items = items
        quixCartFlight.totalPriceWithTax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.streetAddress = "Nombre de la vía y nº"
        quixAddress.postalCode = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.firstName = "Nombre"
        quixBilling.lastName = "Apellido"

        val quixFlightPaySolExtendedData = QuixFlightPaySolExtendedData()
        quixFlightPaySolExtendedData.cart = quixCartFlight
        quixFlightPaySolExtendedData.billing = quixBilling
        quixFlightPaySolExtendedData.product = "instalments"

        hostedQuixFlight.paySolExtendedData = quixFlightPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)

        val exception = assertThrows<MissingFieldException> {
            hostedQuixPaymentAdapter.sendHostedQuixFlightRequest(hostedQuixFlight, mockedResponseListener)
        }

        assertEquals("Missing statusURL", exception.message)
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
        credentials.productId = "1112220001"
        credentials.apiVersion = 5

        val hostedQuixFlight = HostedQuixFlight()

        val exception = assertThrows<InvalidFieldException> {
            hostedQuixFlight.amount = "99,0"
        }

        assertEquals("amount: Should Follow Format #.#### And Be Between 0 And 1000000", exception.message)
    }
}