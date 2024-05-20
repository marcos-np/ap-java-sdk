package creditcards.h2h

import com.mp.javaPaymentSDK.adapters.H2HPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPaymentRecurrentInitial
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPaymentRecurrentSuccessive
import com.mp.javaPaymentSDK.models.responses.notification.Notification
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import utils.NotificationResponses

class RecurringSubsequentTest {

    @Test
    fun successResponseRecurringSubsequentPayment() {

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

        val h2HPaymentRecurrentSuccessive = H2HPaymentRecurrentSuccessive()

        h2HPaymentRecurrentSuccessive.amount = "50"
        h2HPaymentRecurrentSuccessive.currency = Currency.EUR
        h2HPaymentRecurrentSuccessive.country = CountryCode.ES
        h2HPaymentRecurrentSuccessive.customerId = "903"
        h2HPaymentRecurrentSuccessive.chName = "First name Last name"
        h2HPaymentRecurrentSuccessive.paymentSolution = PaymentSolutions.creditcards
        h2HPaymentRecurrentSuccessive.cardNumberToken = "6537275043632227"
        h2HPaymentRecurrentSuccessive.subscriptionPlan = "511845609608301"
        h2HPaymentRecurrentSuccessive.statusURL = "https://test.com/status"
        h2HPaymentRecurrentSuccessive.successURL = "https://test.com/success"
        h2HPaymentRecurrentSuccessive.errorURL = "https://test.com/error"
        h2HPaymentRecurrentSuccessive.awaitingURL = "https://test.com/waiting"
        h2HPaymentRecurrentSuccessive.cancelURL = "https://test.com/cancel"
        h2HPaymentRecurrentSuccessive.merchantTransactionId = "12345678"
        h2HPaymentRecurrentSuccessive.apiVersion = 5
        h2HPaymentRecurrentSuccessive.merchantExemptionsSca = MerchantExemptionsSca.MIT

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRecurrentSuccessive(h2HPaymentRecurrentSuccessive, mockedResponseListener)

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
            "7QDv+7cYdagtQmVfr38p1+HRNOMcBrftirm7FfE6+GOSF52tAfECBNLpz0a9jfI8Vlr7QWy4vIfNXFdl+saLSXIVvsH8bn31IcWKU3OeMVXo7oK9uHWbv+xCWoUVvCigNVKwnNRhH3hs4+pxifzJH0+t0lnb/P9KViOOqGRL/v7BeDN8BHaCF3rMLeUwE0q0os5MjtPjgmIC+jQVmjHlRaLBeYMcksCGGLiitoEbe2SoKnsjHZT8cs9buWpyVyLU9ggoFLvmDP+p+Q47dZMImQm4B3vbjK491m6f8XRLk3ZMzmd1ngtJeujUA0lVmoyAm/8PtsWx0VW6uYMG9HzD5bWr28YpGg1p6/QUPTxBqonmn31ZE9aWoEZ0rwUkYu2C9HjWatRi7IHwnvXIuYKW5IMK86ScCWn4Wlz21b3fTnQG9l3fpT7v84z4payy5kD4oNhEIxaQqoLwWCA/ITJEA5VMsgioL4w19FCLZtTGKZ7hUHpJEK6NrVCkJ528Hw9CMkLl6nblE6hFRC+0NkC2m29lAf0tWfOqf9Sl31DEWTz/YB0HabILapWxhh1p2dq8WhpEC3733l63vabG5H1aBJqHrX3vb7+Bv3UNtjabB4IJKyVHdY/UIyK45BmmmG9ViQjVA4nu99I0LBoEDgHviORHT1oBRFqpfM0utLXZDRafyYCZkswZpiQsUejj7NAMXherNSMD3Pz/p1WB7ZadVA==",
            queryParameterSlot.captured["encrypted"]
        )
        Assertions.assertEquals(
            "d8b0b6f5c828b472b893d5ad9da0efae0fe35e9fae1b238c5f511d17dad0fea9",
            queryParameterSlot.captured["integrityCheck"]
        )

        Assertions.assertEquals(Endpoints.H2H_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns NotificationResponses.h2HRecurringSubsequentResponse

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
            NotificationResponses.h2HRecurringSubsequentResponse,
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
    fun missingFieldRecurringSubsequentPayment() {
        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HPaymentRecurrentSuccessive = H2HPaymentRecurrentSuccessive()

        h2HPaymentRecurrentSuccessive.amount = "50"
        h2HPaymentRecurrentSuccessive.currency = Currency.EUR
        h2HPaymentRecurrentSuccessive.country = CountryCode.ES
        h2HPaymentRecurrentSuccessive.customerId = "903"
        h2HPaymentRecurrentSuccessive.chName = "First name Last name"
        h2HPaymentRecurrentSuccessive.paymentSolution = PaymentSolutions.creditcards
        h2HPaymentRecurrentSuccessive.subscriptionPlan = "511845609608301"
        h2HPaymentRecurrentSuccessive.statusURL = "https://test.com/status"
        h2HPaymentRecurrentSuccessive.successURL = "https://test.com/success"
        h2HPaymentRecurrentSuccessive.errorURL = "https://test.com/error"
        h2HPaymentRecurrentSuccessive.awaitingURL = "https://test.com/waiting"
        h2HPaymentRecurrentSuccessive.cancelURL = "https://test.com/cancel"
        h2HPaymentRecurrentSuccessive.merchantTransactionId = "12345678"
        h2HPaymentRecurrentSuccessive.apiVersion = 5
        h2HPaymentRecurrentSuccessive.merchantExemptionsSca = MerchantExemptionsSca.MIT

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRecurrentSuccessive(h2HPaymentRecurrentSuccessive, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        Assertions.assertEquals("Missing cardNumberToken", errorMessageSlot.captured)
    }

    @Test
    fun failInvalidAmountRecurringSubsequentPayment() {
        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HPaymentRecurrentSuccessive = H2HPaymentRecurrentSuccessive()

        h2HPaymentRecurrentSuccessive.amount = "50-12"
        h2HPaymentRecurrentSuccessive.currency = Currency.EUR
        h2HPaymentRecurrentSuccessive.country = CountryCode.ES
        h2HPaymentRecurrentSuccessive.customerId = "903"
        h2HPaymentRecurrentSuccessive.chName = "First name Last name"
        h2HPaymentRecurrentSuccessive.paymentSolution = PaymentSolutions.creditcards
        h2HPaymentRecurrentSuccessive.cardNumberToken = "6537275043632227"
        h2HPaymentRecurrentSuccessive.subscriptionPlan = "511845609608301"
        h2HPaymentRecurrentSuccessive.statusURL = "https://test.com/status"
        h2HPaymentRecurrentSuccessive.successURL = "https://test.com/success"
        h2HPaymentRecurrentSuccessive.errorURL = "https://test.com/error"
        h2HPaymentRecurrentSuccessive.awaitingURL = "https://test.com/waiting"
        h2HPaymentRecurrentSuccessive.cancelURL = "https://test.com/cancel"
        h2HPaymentRecurrentSuccessive.merchantTransactionId = "12345678"
        h2HPaymentRecurrentSuccessive.apiVersion = 5
        h2HPaymentRecurrentSuccessive.merchantExemptionsSca = MerchantExemptionsSca.MIT

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRecurrentSuccessive(h2HPaymentRecurrentSuccessive, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        Assertions.assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }

}