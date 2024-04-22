package creditcards.h2h

import com.mp.javaPaymentSDK.adapters.H2HPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPreAuthorization
import com.mp.javaPaymentSDK.models.responses.notification.Notification
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import utils.NotificationResponses

class PreAuthorizationTest {

    @Test
    fun successResponsePreAuthorizationPayment() {
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
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HPreAuthorization = H2HPreAuthorization()

        h2HPreAuthorization.amount = "50"
        h2HPreAuthorization.currency = Currency.EUR
        h2HPreAuthorization.country = CountryCode.ES
        h2HPreAuthorization.customerId = "903"
        h2HPreAuthorization.chName = "First name Last name"
        h2HPreAuthorization.paymentSolution = PaymentSolutions.creditcards
        h2HPreAuthorization.cardNumber = "4907270002222227"
        h2HPreAuthorization.cvnNumber = "123"
        h2HPreAuthorization.expDate = "0625"
        h2HPreAuthorization.statusURL = "https://test.com/status"
        h2HPreAuthorization.successURL = "https://test.com/success"
        h2HPreAuthorization.errorURL = "https://test.com/error"
        h2HPreAuthorization.awaitingURL = "https://test.com/waiting"
        h2HPreAuthorization.cancelURL = "https://test.com/cancel"
        h2HPreAuthorization.merchantTransactionId = "12345678"
        h2HPreAuthorization.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPreAuthorizationRequest(h2HPreAuthorization, mockedResponseListener)

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
            "7QDv+7cYdagtQmVfr38p1+HRNOMcBrftirm7FfE6+GOSF52tAfECBNLpz0a9jfI8Vlr7QWy4vIfNXFdl+saLSXIVvsH8bn31IcWKU3OeMVXo7oK9uHWbv+xCWoUVvCigNVKwnNRhH3hs4+pxifzJH0+t0lnb/P9KViOOqGRL/v7BeDN8BHaCF3rMLeUwE0q0llyYxUOdhDGW6RlC5J2CqfovVXd5aK8SEAkS3roO74Vr2Ab/iK1vJbdn2hlN7ybb8PFD2eBOHMcdGzv2+Kij/f+ijk5GrnXvcx3LU1q5qUUlcUT+iKpiBdxANrRc5D1fhJXehVUrT6PkwNAlDgDEhcnkUSSz5ehCN6DgjoEhHgRa0K49EIpYX/hU5t5eeQOjtCdRqvZ8vlIxTd2lw8S38beGooQ5ub0CiSAk41Q4gvnxKq/CYqMPhAXGA9lLNfM2bO49N91nJKLMB8KmteAauFwKhU8HTwZRxlA3/BNl3PsWd428UwtCq4CNJLezCo5/GYpqzjONEZD0bbXDwt8a90wzUzykcl6kPYg0lRF04VYKl7sf4qw+4cKyH726VIKNZV7Dv/2iuyISYJ2OcYCP3Ty7SkuaIBPChWldIsNysa+W0hdAkYsQh7CQR60OtjGp1xeRXjCeDfT9SNKGgxdX3g==",
            queryParameterSlot.captured["encrypted"]
        )
        Assertions.assertEquals(
            "1099521ebd8a703be8f2a2762f672479cbe1759d20bed38c3191d4a28d33d616",
            queryParameterSlot.captured["integrityCheck"]
        )

        Assertions.assertEquals(Endpoints.H2H_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns NotificationResponses.h2HPreAuthorizationResponse

        requestListenerSlot.captured.onResponse(200, mockedResponseBody)

        val rawResponseSlot = slot<String>()
        val notificationSlot = slot<Notification>()
        val transactionResult = slot<TransactionResult>()

        verify {
            mockedResponseListener.onResponseReceived(
                    capture(rawResponseSlot),
                    capture(notificationSlot),
                    capture(transactionResult)
            )
        }

        Assertions.assertEquals(
            NotificationResponses.h2HPreAuthorizationResponse,
            rawResponseSlot.captured
        )
        Assertions.assertEquals(2, notificationSlot.captured.operations.size)
        Assertions.assertNull(notificationSlot.captured.operations.last().paymentSolution)
        Assertions.assertEquals("TRA", notificationSlot.captured.operations.first().service)
        Assertions.assertEquals("3DSv2", notificationSlot.captured.operations.last().service)
        Assertions.assertEquals("SUCCESS", notificationSlot.captured.operations.first().status)
        Assertions.assertEquals("REDIRECTED", notificationSlot.captured.operations.last().status)
    }

    @Test
    fun missingFieldPreAuthorizationPayment() {
        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HPreAuthorization = H2HPreAuthorization()

        h2HPreAuthorization.amount = "50"
        h2HPreAuthorization.currency = Currency.EUR
        h2HPreAuthorization.country = CountryCode.ES
        h2HPreAuthorization.customerId = "903"
        h2HPreAuthorization.chName = "First name Last name"
        h2HPreAuthorization.paymentSolution = PaymentSolutions.creditcards
        h2HPreAuthorization.cvnNumber = "123"
        h2HPreAuthorization.expDate = "0625"
        h2HPreAuthorization.statusURL = "https://test.com/status"
        h2HPreAuthorization.successURL = "https://test.com/success"
        h2HPreAuthorization.errorURL = "https://test.com/error"
        h2HPreAuthorization.awaitingURL = "https://test.com/waiting"
        h2HPreAuthorization.cancelURL = "https://test.com/cancel"
        h2HPreAuthorization.merchantTransactionId = "12345678"
        h2HPreAuthorization.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPreAuthorizationRequest(h2HPreAuthorization, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        Assertions.assertEquals("Missing cardNumber", errorMessageSlot.captured)
    }

    @Test
    fun failInvalidAmountPreAuthorizationPayment() {
        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HPreAuthorization = H2HPreAuthorization()
        h2HPreAuthorization.amount = "50.123.22"
        h2HPreAuthorization.currency = Currency.EUR
        h2HPreAuthorization.country = CountryCode.ES
        h2HPreAuthorization.customerId = "903"
        h2HPreAuthorization.chName = "First name Last name"
        h2HPreAuthorization.paymentSolution = PaymentSolutions.creditcards
        h2HPreAuthorization.cardNumber = "4907270002222227"
        h2HPreAuthorization.cvnNumber = "123"
        h2HPreAuthorization.expDate = "0625"
        h2HPreAuthorization.statusURL = "https://test.com/status"
        h2HPreAuthorization.successURL = "https://test.com/success"
        h2HPreAuthorization.errorURL = "https://test.com/error"
        h2HPreAuthorization.awaitingURL = "https://test.com/waiting"
        h2HPreAuthorization.cancelURL = "https://test.com/cancel"
        h2HPreAuthorization.merchantTransactionId = "12345678"
        h2HPreAuthorization.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPreAuthorizationRequest(h2HPreAuthorization, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        Assertions.assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }

}