package creditcards.hosted

import com.mp.javaPaymentSDK.adapters.HostedPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.adapters.SocketAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.hosted.HostedPaymentRecurrentInitial
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RecurringTest {

    @Test
    fun successRecurringNotification() {
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

        val hostedPaymentRecurrentInitial = HostedPaymentRecurrentInitial()
        hostedPaymentRecurrentInitial.currency = Currency.EUR
        hostedPaymentRecurrentInitial.paymentSolution = PaymentSolutions.creditcards
        hostedPaymentRecurrentInitial.amount = "50"
        hostedPaymentRecurrentInitial.country = CountryCode.ES
        hostedPaymentRecurrentInitial.customerId = "903"
        hostedPaymentRecurrentInitial.statusURL = "https://test.com/status"
        hostedPaymentRecurrentInitial.successURL = "https://test.com/success"
        hostedPaymentRecurrentInitial.errorURL = "https://test.com/error"
        hostedPaymentRecurrentInitial.awaitingURL = "https://test.com/waiting"
        hostedPaymentRecurrentInitial.cancelURL = "https://test.com/cancel"
        hostedPaymentRecurrentInitial.merchantTransactionId = "12345678"
        hostedPaymentRecurrentInitial.apiVersion = 5

        val hostedPaymentAdapter = HostedPaymentAdapter(credentials)

        hostedPaymentAdapter.sendHostedRecurrentInitial(hostedPaymentRecurrentInitial, mockedResponseListener)

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

        Assertions.assertEquals(3, headersSlot.captured.size)
        Assertions.assertEquals("5", headersSlot.captured["apiVersion"])
        Assertions.assertEquals("CBC", headersSlot.captured["encryptionMode"])
        Assertions.assertEquals("AQEBAQEBAQEBAQEBAQEBAQ==", headersSlot.captured["iv"])

        Assertions.assertEquals(3, queryParameterSlot.captured.size)
        Assertions.assertEquals("111222", queryParameterSlot.captured["merchantId"])
        Assertions.assertEquals(
            "2BDGM6x+gfSbyWnsjorW6YR+jWqn+xq5Rdr/mOk7Fy7y+QqTAA8hUOGSsOksnzXs7LCbpXaom5lwlPb4AFvuTJVaSkyJF97jW1fOHUKySERIT+qCLHesZIboEOYOP1TzFUohvr+FDufNlnqhfFFtkk371jPxKV5g21FB1eq+WuQMBwPTHRHGNrJac0HcEMxcvi7VgqqLJ5I0kopPATShgHohWZfMvdaguvsYQG59Y5+pw2zJM3iEdPU88euZD+0QgiHE8ClU30NyLg25xyVvvGulMmmzGPjktsDBXUFR8XU0g3wCfaidqVLtlVmaCuaO5SArx/bYwW+bIQoGdIa0ABosUiJArZcMCgowOm8QdgBZBuo2EVavz6AzAFA0d4pMfbNE5S2SJJ28f9bXRwSDbMXpp/0+UM+5T+HyoaS82ZT2jP3l9vLuUppnDbOKDjnU0+YcOSs4McXYhp+G2lXzQiZN8KJCRsHJ68gQC3j+sS/ntYZH5L+K1DraDJwYKU+ebJMN4dn14xctnqsSbSIKcWI2hvQn9mGSOgTb55C/CB1Wlvo7R+4pu6Tfm02ueNZanLteegfn5QH7f6RwOLV6vEL7AvpH1IKemo6JMLlEyWI=",
            queryParameterSlot.captured["encrypted"]
        )
        Assertions.assertEquals(
            "f774d7a78c14ccd901520ef5cc378454ababfca51d92b8fcc83fa1f08fe1486d",
            queryParameterSlot.captured["integrityCheck"]
        )

        Assertions.assertEquals(Endpoints.HOSTED_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

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

        Assertions.assertEquals("http://test.com", redirectionUrlSlot.captured)
    }

    @Test
    fun failMissingParameterRecurring() {
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

        val hostedPaymentRecurrentInitial = HostedPaymentRecurrentInitial()
        hostedPaymentRecurrentInitial.currency = Currency.EUR
        hostedPaymentRecurrentInitial.paymentSolution = PaymentSolutions.creditcards
        hostedPaymentRecurrentInitial.amount = "50"
        hostedPaymentRecurrentInitial.country = CountryCode.ES
        hostedPaymentRecurrentInitial.statusURL = "https://test.com/status"
        hostedPaymentRecurrentInitial.successURL = "https://test.com/success"
        hostedPaymentRecurrentInitial.errorURL = "https://test.com/error"
        hostedPaymentRecurrentInitial.awaitingURL = "https://test.com/waiting"
        hostedPaymentRecurrentInitial.cancelURL = "https://test.com/cancel"
        hostedPaymentRecurrentInitial.merchantTransactionId = "12345678"
        hostedPaymentRecurrentInitial.apiVersion = 5

        val hostedPaymentAdapter = HostedPaymentAdapter(credentials)

        hostedPaymentAdapter.sendHostedRecurrentInitial(hostedPaymentRecurrentInitial, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        Assertions.assertEquals("Missing customerId", errorMessageSlot.captured)
    }

    @Test
    fun failInvalidAmountRecurring() {
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

        val hostedPaymentRecurrentInitial = HostedPaymentRecurrentInitial()
        hostedPaymentRecurrentInitial.currency = Currency.EUR
        hostedPaymentRecurrentInitial.paymentSolution = PaymentSolutions.creditcards
        hostedPaymentRecurrentInitial.amount = "50-123"
        hostedPaymentRecurrentInitial.country = CountryCode.ES
        hostedPaymentRecurrentInitial.customerId = "903"
        hostedPaymentRecurrentInitial.statusURL = "https://test.com/status"
        hostedPaymentRecurrentInitial.successURL = "https://test.com/success"
        hostedPaymentRecurrentInitial.errorURL = "https://test.com/error"
        hostedPaymentRecurrentInitial.awaitingURL = "https://test.com/waiting"
        hostedPaymentRecurrentInitial.cancelURL = "https://test.com/cancel"
        hostedPaymentRecurrentInitial.merchantTransactionId = "12345678"
        hostedPaymentRecurrentInitial.apiVersion = 5

        val hostedPaymentAdapter = HostedPaymentAdapter(credentials)

        hostedPaymentAdapter.sendHostedRecurrentInitial(hostedPaymentRecurrentInitial, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        Assertions.assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }
}