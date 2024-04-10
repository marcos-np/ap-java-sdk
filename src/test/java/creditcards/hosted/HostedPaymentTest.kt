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
            "2BDGM6x+gfSbyWnsjorW6bt9H5s+USzuFPrNh2EGOH9fEcf0GmC0hzuGXJNTKx2mSteJt8m0zt3SD6JuEq6TQGiLKRs+chOVZ3Sq19Iu6DDwgpSAECLs1AikUZPFl0v82o+AIjDJ0+i3QqphdP5qlQe85e7NO52xS8SF2UgIK6xyrjLNfoOIM6EGm+Q1e/n9Np9FGiJvjOK68YAxkNldQRvUvoUsMONXjyVnig1Nre1lrUZRyIeCbESDuR8Iu0RF//Nt/08E/WCgPeY8k376UtFgwB6gGZ6YL0+dRm9d62kaIlmE08gMea+4UZxPJHtVZ7iXseYX0mikhNhCmUFSX2URBYDaEaI8/khxEdO41qouztsgZ6uiEnrWHMkmSTCWfIi77faLe3ZSIVyl69TsFwSdTyvcmlAxF/YircyqVVMJL4UE7nMZ2H7QQYOEvv2p4O4bFViygzhramfRqhNZDxo1CsXpZQdnO6/HghV4yuXUFXd6DB8z2bhAevraIS3WoaeuA9+UyLN8JTjRiwPS2A==",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "7d1f73132ccad63692fd75a6f43a0ad3a0e947042656fa11629968379919fafa",
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
}