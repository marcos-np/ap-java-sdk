package creditcards.hosted

import com.mp.javaPaymentSDK.adapters.HostedPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.adapters.SocketAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.hosted.HostedPaymentRedirection
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HostedPaymentTest {

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

        val hostedPaymentRedirection = HostedPaymentRedirection()
        hostedPaymentRedirection.currency = Currency.EUR
        hostedPaymentRedirection.paymentSolution = PaymentSolutions.creditcards
        hostedPaymentRedirection.amount = "50"
        hostedPaymentRedirection.country = CountryCode.ES
        hostedPaymentRedirection.customerId = "903"
        hostedPaymentRedirection.statusURL = "https://test.com/status"
        hostedPaymentRedirection.successURL = "https://test.com/success"
        hostedPaymentRedirection.errorURL = "https://test.com/error"
        hostedPaymentRedirection.awaitingURL = "https://test.com/waiting"
        hostedPaymentRedirection.cancelURL = "https://test.com/cancel"
        hostedPaymentRedirection.merchantTransactionId = "12345678"
        hostedPaymentRedirection.apiVersion = 5

        val hostedPaymentAdapter = HostedPaymentAdapter(credentials)

        hostedPaymentAdapter.sendHostedPaymentRequest(hostedPaymentRedirection, mockedResponseListener)

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
            "2BDGM6x+gfSbyWnsjorW6YR+jWqn+xq5Rdr/mOk7Fy7y+QqTAA8hUOGSsOksnzXs7LCbpXaom5lwlPb4AFvuTJVaSkyJF97jW1fOHUKySERIT+qCLHesZIboEOYOP1TzFUohvr+FDufNlnqhfFFtkk371jPxKV5g21FB1eq+WuQMBwPTHRHGNrJac0HcEMxcvi7VgqqLJ5I0kopPATShgHohWZfMvdaguvsYQG59Y5+pw2zJM3iEdPU88euZD+0QgiHE8ClU30NyLg25xyVvvGulMmmzGPjktsDBXUFR8XU0g3wCfaidqVLtlVmaCuaO5SArx/bYwW+bIQoGdIa0ABosUiJArZcMCgowOm8QdgBZBuo2EVavz6AzAFA0d4pMfbNE5S2SJJ28f9bXRwSDbMXpp/0+UM+5T+HyoaS82ZT2jP3l9vLuUppnDbOKDjnU0+YcOSs4McXYhp+G2lXzQiZN8KJCRsHJ68gQC3j+sS/rkgb89jG0SnMmiRBD8+LHYUeJuPGIDi99mFZCkyCEEYOHHhHSaSBNt2jUBfHcCZbgZTIogBol0vQ7VAbpUGGi5w2Fgsc0ycgnaVspWI6xVQ==",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "9966797579e373792a3f1c59af2bc9d0c09edb1ef3d65bd47595e40cd25485a1",
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

        val hostedPaymentRedirection = HostedPaymentRedirection()
        hostedPaymentRedirection.currency = Currency.EUR
        hostedPaymentRedirection.paymentSolution = PaymentSolutions.creditcards
        hostedPaymentRedirection.amount = "50"
        hostedPaymentRedirection.country = CountryCode.ES
        hostedPaymentRedirection.customerId = "903"
        hostedPaymentRedirection.successURL = "https://test.com/success"
        hostedPaymentRedirection.errorURL = "https://test.com/error"
        hostedPaymentRedirection.awaitingURL = "https://test.com/waiting"
        hostedPaymentRedirection.cancelURL = "https://test.com/cancel"
        hostedPaymentRedirection.merchantTransactionId = "12345678"
        hostedPaymentRedirection.apiVersion = 5

        val hostedPaymentAdapter = HostedPaymentAdapter(credentials)

        hostedPaymentAdapter.sendHostedPaymentRequest(hostedPaymentRedirection, mockedResponseListener)

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

        val hostedPaymentRedirection = HostedPaymentRedirection()
        hostedPaymentRedirection.currency = Currency.EUR
        hostedPaymentRedirection.paymentSolution = PaymentSolutions.creditcards
        hostedPaymentRedirection.amount = "50,11"
        hostedPaymentRedirection.country = CountryCode.ES
        hostedPaymentRedirection.customerId = "903"
        hostedPaymentRedirection.statusURL = "https://test.com/status"
        hostedPaymentRedirection.successURL = "https://test.com/success"
        hostedPaymentRedirection.errorURL = "https://test.com/error"
        hostedPaymentRedirection.awaitingURL = "https://test.com/waiting"
        hostedPaymentRedirection.cancelURL = "https://test.com/cancel"
        hostedPaymentRedirection.merchantTransactionId = "12345678"
        hostedPaymentRedirection.apiVersion = 5

        val hostedPaymentAdapter = HostedPaymentAdapter(credentials)

        hostedPaymentAdapter.sendHostedPaymentRequest(hostedPaymentRedirection, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }
}