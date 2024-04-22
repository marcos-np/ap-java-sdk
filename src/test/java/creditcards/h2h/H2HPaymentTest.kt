package creditcards.h2h

import com.mp.javaPaymentSDK.adapters.H2HPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.h2h.H2HRedirection
import com.mp.javaPaymentSDK.models.responses.notification.Notification
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import utils.NotificationResponses

class H2HPaymentTest {

    @Test
    fun successResponseH2HPayment() {

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

        val h2HRedirection = H2HRedirection()
        h2HRedirection.amount = "50"
        h2HRedirection.currency = Currency.EUR
        h2HRedirection.country = CountryCode.ES
        h2HRedirection.cardNumber = "4907270002222227"
        h2HRedirection.customerId = "903"
        h2HRedirection.chName = "First name Last name"
        h2HRedirection.cvnNumber = "123"
        h2HRedirection.expDate = "0625"
        h2HRedirection.paymentSolution = PaymentSolutions.creditcards
        h2HRedirection.statusURL = "https://test.com/status"
        h2HRedirection.successURL = "https://test.com/success"
        h2HRedirection.errorURL = "https://test.com/error"
        h2HRedirection.awaitingURL = "https://test.com/waiting"
        h2HRedirection.cancelURL = "https://test.com/cancel"
        h2HRedirection.merchantTransactionId = "12345678"
        h2HRedirection.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRequest(h2HRedirection, mockedResponseListener)

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
            "7QDv+7cYdagtQmVfr38p1+HRNOMcBrftirm7FfE6+GOSF52tAfECBNLpz0a9jfI8Vlr7QWy4vIfNXFdl+saLSXIVvsH8bn31IcWKU3OeMVXo7oK9uHWbv+xCWoUVvCigNVKwnNRhH3hs4+pxifzJH0+t0lnb/P9KViOOqGRL/v7BeDN8BHaCF3rMLeUwE0q0os5MjtPjgmIC+jQVmjHlRb2KE+RhLewh/oazUTgOIMo75uj4DHuvlj9qhfRdU/iLhqgZd7P2jeQ0/zMa0Uv+N34NE4dzfeMNQ+leBbsbCiXKH15RubBngQ3MbT3OVd0+qfQE4bKc3ibITXuexUX/y/AufxXCvhX2hRGUxAaQeD0V1pmYkVAdzzIdQz0feLuZyLSY0QlxRRaME6lE1Re+fiWAik1ryPPY1EsBTDIF1/RgeMfH31KkLa1rgyNAv8G3kO4DNxV1ROmELsILCX4jDApkAlME+3Zx6gc/1BeDiRSwY+yssSBXZ6fJniAX39UzqaEsLeywtjdMadlYyjlvIKkh+x70gpiNIqfoYqVMV4Sr6CHzaMSaivIyWTttcYV+n4rUZDJbfP1NZ3puZ61R7eSBqxKzer23CHYH1un7um6JIrky7hkBbBo0SVCO5SFH",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "c798dd66929a412f5da1346332c0b708da426a550967871beea867b8dd882792",
            queryParameterSlot.captured["integrityCheck"]
        )

        assertEquals(Endpoints.H2H_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns NotificationResponses.h2HRedirectionSuccessResponse

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

        assertEquals(NotificationResponses.h2HRedirectionSuccessResponse, rawResponseSlot.captured)
        assertEquals(2, notificationSlot.captured.operations.size)
        assertNull(notificationSlot.captured.operations.last().paymentSolution)
        assertEquals("TRA", notificationSlot.captured.operations.first().service)
        assertEquals("3DSv2", notificationSlot.captured.operations.last().service)
        assertEquals("SUCCESS", notificationSlot.captured.operations.first().status)
        assertEquals("REDIRECTED", notificationSlot.captured.operations.last().status)
    }

    @Test
    fun missingFieldH2HPayment() {
        val mockedResponseListener = mockk<ResponseListener>();
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any() ) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HRedirection = H2HRedirection()
        h2HRedirection.currency = Currency.EUR
        h2HRedirection.country = CountryCode.ES
        h2HRedirection.cardNumber = "4907270002222227"
        h2HRedirection.customerId = "903"
        h2HRedirection.chName = "First name Last name"
        h2HRedirection.cvnNumber = "123"
        h2HRedirection.expDate = "0625"
        h2HRedirection.paymentSolution = PaymentSolutions.creditcards
        h2HRedirection.statusURL = "https://test.com/status"
        h2HRedirection.successURL = "https://test.com/success"
        h2HRedirection.errorURL = "https://test.com/error"
        h2HRedirection.awaitingURL = "https://test.com/waiting"
        h2HRedirection.cancelURL = "https://test.com/cancel"
        h2HRedirection.merchantTransactionId = "12345678"
        h2HRedirection.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRequest(h2HRedirection, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        assertEquals("Missing amount", errorMessageSlot.captured)
    }

    @Test
    fun failInvalidAmountH2HPayment() {
        val mockedResponseListener = mockk<ResponseListener>();
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any() ) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HRedirection = H2HRedirection()
        h2HRedirection.amount = "50,9"
        h2HRedirection.currency = Currency.EUR
        h2HRedirection.country = CountryCode.ES
        h2HRedirection.cardNumber = "4907270002222227"
        h2HRedirection.customerId = "903"
        h2HRedirection.chName = "First name Last name"
        h2HRedirection.cvnNumber = "123"
        h2HRedirection.expDate = "0625"
        h2HRedirection.paymentSolution = PaymentSolutions.creditcards
        h2HRedirection.statusURL = "https://test.com/status"
        h2HRedirection.successURL = "https://test.com/success"
        h2HRedirection.errorURL = "https://test.com/error"
        h2HRedirection.awaitingURL = "https://test.com/waiting"
        h2HRedirection.cancelURL = "https://test.com/cancel"
        h2HRedirection.merchantTransactionId = "12345678"
        h2HRedirection.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRequest(h2HRedirection, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }
}