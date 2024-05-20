package creditcards.h2h

import com.mp.javaPaymentSDK.adapters.H2HPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.h2h.H2HPaymentRecurrentInitial
import com.mp.javaPaymentSDK.models.responses.notification.Notification
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import utils.NotificationResponses

class RecurringTest {

    @Test
    fun successResponseRecurringPayment() {

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

        val h2HPaymentRecurrentInitial = H2HPaymentRecurrentInitial()

        h2HPaymentRecurrentInitial.amount = "50"
        h2HPaymentRecurrentInitial.currency = Currency.EUR
        h2HPaymentRecurrentInitial.country = CountryCode.ES
        h2HPaymentRecurrentInitial.cardNumber = "4907270002222227"
        h2HPaymentRecurrentInitial.customerId = "903"
        h2HPaymentRecurrentInitial.chName = "First name Last name"
        h2HPaymentRecurrentInitial.cvnNumber = "123"
        h2HPaymentRecurrentInitial.expDate = "0625"
        h2HPaymentRecurrentInitial.paymentSolution = PaymentSolutions.creditcards
        h2HPaymentRecurrentInitial.statusURL = "https://test.com/status"
        h2HPaymentRecurrentInitial.successURL = "https://test.com/success"
        h2HPaymentRecurrentInitial.errorURL = "https://test.com/error"
        h2HPaymentRecurrentInitial.awaitingURL = "https://test.com/waiting"
        h2HPaymentRecurrentInitial.cancelURL = "https://test.com/cancel"
        h2HPaymentRecurrentInitial.merchantTransactionId = "12345678"
        h2HPaymentRecurrentInitial.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRecurrentInitial(h2HPaymentRecurrentInitial, mockedResponseListener)

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
            "7QDv+7cYdagtQmVfr38p19LXAC7V747KKHceeSdEoxrMJV88ISU1YBv47F+zGTbBQRAxNvsqKgIqQ+GRWcl4SApIA7eK2pihFOfdoDQ9xSAvLB/oY31Bv1OGpToC/L0sJ8mqAOB3vo2cI1Lf0Q34FdD81pE9LpUotPuPKI6oXFBT2pJ7RF2uhhQslEVHnicLmtPCgDmKAg/svckRxZoOrpcE0bteymRpRibu1pIdMDZ7epUk/AB65+kQ6kMuwJNCRCNnlUWvEWize7wc2hKkMLuE1+IvHVOiMJw2WLWeWcQno8UEmhItPJpPJXt/Bk4CG8f867NPAnDqDG2+w/hwqwYVRTiTzLivzVdzZijxyo8DHvFg79K3vMK5Lw1KCG3tRwDCWovWTrIe0F5Q6uFxo3uCf+fuG56LLGQ4svFlxfW/6UnEWmt2NLi9k98LAzcAJjS3Zdg2Xj57OPNPKVh1Ze71LLQy5V4wLhMpJvavy0wzTHIiXY0Px9YltVqoA+ME8zce7B5zjDofW2Oh/Pbhc2Byka4cfvvQucf1yE/BJTcjuEwzQW97eh9fXnypYou8RA2VuWjlhqBkOyJvRzayqLNx6/+5CEfVQopmPhZaaVKVEA3f6fTC2s9w84S5ZfjGqt46QTctEACCFTYuSghkUXsA6KSzDSVDtgnKtdcjy/UzcpMzvOZT69WXnjhcPyljhA/FuD42L+5ghkO0UWItkw==",
            queryParameterSlot.captured["encrypted"]
        )
        Assertions.assertEquals(
            "4d1267ac45f79dbd0de1ed74a49fecaf4572063190f809a8c0f5b10912c651bc",
            queryParameterSlot.captured["integrityCheck"]
        )

        Assertions.assertEquals(Endpoints.H2H_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns NotificationResponses.h2HRecurringSuccessResponse

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
            NotificationResponses.h2HRecurringSuccessResponse,
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
    fun missingFieldRecurringPayment() {
        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HPaymentRecurrentInitial = H2HPaymentRecurrentInitial()

        h2HPaymentRecurrentInitial.amount = "50"
        h2HPaymentRecurrentInitial.currency = Currency.EUR
        h2HPaymentRecurrentInitial.country = CountryCode.ES
        h2HPaymentRecurrentInitial.customerId = "903"
        h2HPaymentRecurrentInitial.chName = "First name Last name"
        h2HPaymentRecurrentInitial.cvnNumber = "123"
        h2HPaymentRecurrentInitial.expDate = "0625"
        h2HPaymentRecurrentInitial.paymentSolution = PaymentSolutions.creditcards
        h2HPaymentRecurrentInitial.statusURL = "https://test.com/status"
        h2HPaymentRecurrentInitial.successURL = "https://test.com/success"
        h2HPaymentRecurrentInitial.errorURL = "https://test.com/error"
        h2HPaymentRecurrentInitial.awaitingURL = "https://test.com/waiting"
        h2HPaymentRecurrentInitial.cancelURL = "https://test.com/cancel"
        h2HPaymentRecurrentInitial.merchantTransactionId = "12345678"
        h2HPaymentRecurrentInitial.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRecurrentInitial(h2HPaymentRecurrentInitial, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        Assertions.assertEquals("Missing cardNumber", errorMessageSlot.captured)
    }

    @Test
    fun failInvalidAmountRecurringPayment() {
        val mockedResponseListener = mockk<ResponseListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val h2HPaymentRecurrentInitial = H2HPaymentRecurrentInitial()

        h2HPaymentRecurrentInitial.amount = "-7895"
        h2HPaymentRecurrentInitial.currency = Currency.EUR
        h2HPaymentRecurrentInitial.country = CountryCode.ES
        h2HPaymentRecurrentInitial.cardNumber = "4907270002222227"
        h2HPaymentRecurrentInitial.customerId = "903"
        h2HPaymentRecurrentInitial.chName = "First name Last name"
        h2HPaymentRecurrentInitial.cvnNumber = "123"
        h2HPaymentRecurrentInitial.expDate = "0625"
        h2HPaymentRecurrentInitial.paymentSolution = PaymentSolutions.creditcards
        h2HPaymentRecurrentInitial.statusURL = "https://test.com/status"
        h2HPaymentRecurrentInitial.successURL = "https://test.com/success"
        h2HPaymentRecurrentInitial.errorURL = "https://test.com/error"
        h2HPaymentRecurrentInitial.awaitingURL = "https://test.com/waiting"
        h2HPaymentRecurrentInitial.cancelURL = "https://test.com/cancel"
        h2HPaymentRecurrentInitial.merchantTransactionId = "12345678"
        h2HPaymentRecurrentInitial.apiVersion = 5

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRecurrentInitial(h2HPaymentRecurrentInitial, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        Assertions.assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }

}