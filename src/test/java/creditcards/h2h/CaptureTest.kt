package creditcards.h2h

import com.mp.javaPaymentSDK.adapters.H2HPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPaymentRecurrentSuccessive
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPreAuthorizationCapture
import com.mp.javaPaymentSDK.models.responses.notification.Notification
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import utils.NotificationResponses

class CaptureTest {

    @Test
    fun successNotificationCapture() {

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

        val h2HPreAuthorizationCapture = H2HPreAuthorizationCapture()

        h2HPreAuthorizationCapture.paymentSolution = PaymentSolutions.creditcards
        h2HPreAuthorizationCapture.merchantTransactionId = "46604547"
        h2HPreAuthorizationCapture.transactionId = "7817556"
        h2HPreAuthorizationCapture.merchantTransactionId = "12345678"
        h2HPreAuthorizationCapture.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPreAuthorizationCapture(h2HPreAuthorizationCapture, mockedResponseListener)

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
            "pDH/U+/gbuzXdYp84aiQKmAB1WoG3rPuYdEo0WU+k+I6Po6qoGum82RamnYAjsafVDBx2jk7xKlzE5alJVodXAOqiuCV3paKIRxM8ao64TsaSgiWG/8QQ8K2YYlrg3TPDUs+ECdEqQxGCg58SDUWaQ==",
            queryParameterSlot.captured["encrypted"]
        )
        Assertions.assertEquals(
            "50374804a5b7ec7c63339d60ce57a7da9bc6c14c78ac5a0b81866fa26f8cebff",
            queryParameterSlot.captured["integrityCheck"]
        )

        Assertions.assertEquals(Endpoints.CAPTURE_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns NotificationResponses.CaptureResponse

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
            NotificationResponses.CaptureResponse,
            rawResponseSlot.captured
        )
        Assertions.assertEquals(1, notificationSlot.captured.operations.size)
        Assertions.assertEquals(PaymentSolutions.caixapucpuce, notificationSlot.captured.operations.last().paymentSolution)
        Assertions.assertEquals("SUCCESS", notificationSlot.captured.operations.last().status)
    }

    @Test
    fun missingFieldCapture() {
        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING

        val h2HPreAuthorizationCapture = H2HPreAuthorizationCapture()

        h2HPreAuthorizationCapture.paymentSolution = PaymentSolutions.creditcards
        h2HPreAuthorizationCapture.merchantTransactionId = "46604547"
        h2HPreAuthorizationCapture.merchantTransactionId = "12345678"
        h2HPreAuthorizationCapture.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPreAuthorizationCapture(h2HPreAuthorizationCapture, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        Assertions.assertEquals("Missing transactionId", errorMessageSlot.captured)
    }

    @Test
    fun missingApiVersionFieldCapture() {
        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING

        val h2HPreAuthorizationCapture = H2HPreAuthorizationCapture()

        h2HPreAuthorizationCapture.paymentSolution = PaymentSolutions.creditcards
        h2HPreAuthorizationCapture.merchantTransactionId = "46604547"
        h2HPreAuthorizationCapture.merchantTransactionId = "12345678"
        h2HPreAuthorizationCapture.transactionId = "1234567"

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPreAuthorizationCapture(h2HPreAuthorizationCapture, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        Assertions.assertEquals("Invalid apiVersion", errorMessageSlot.captured)
    }

}