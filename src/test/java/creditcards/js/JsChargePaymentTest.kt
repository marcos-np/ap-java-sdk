package creditcards.js

import com.mp.javaPaymentSDK.adapters.JSPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.adapters.ResponseListenerAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.js.JSCharge
import com.mp.javaPaymentSDK.models.responses.notification.Notification
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import utils.NotificationResponses

class JsChargePaymentTest {

    @Test
    fun successHostedNotification() {
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

        val mockedResponseListener = mockk<ResponseListenerAdapter>();
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onRedirectionURLReceived(any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val jsCharge = JSCharge()

        jsCharge.amount = "30"
        jsCharge.prepayToken = "2795f021-f31c-4533-a74d-5d3d887a003b"
        jsCharge.country = CountryCode.ES
        jsCharge.customerId = "55"
        jsCharge.currency = Currency.EUR
        jsCharge.operationType = OperationTypes.DEBIT
        jsCharge.paymentSolution = PaymentSolutions.creditcards
        jsCharge.statusURL = "https://test.com/paymentNotification"
        jsCharge.successURL = "https://test.com/success"
        jsCharge.errorURL = "https://test.com/error"
        jsCharge.awaitingURL = "https://test.com/awaiting"
        jsCharge.cancelURL = "https://test.com/cancel"
        jsCharge.apiVersion = 5

        val jsPaymentAdapter = JSPaymentAdapter(credentials)
        jsPaymentAdapter.sendJSChargeRequest(jsCharge, mockedResponseListener)

        val headersSlot = slot<Map<String, String>>()
        val requestBodySlot = slot<RequestBody>()
        val urlSlot = slot<String>()
        val requestListenerSlot = slot<RequestListener>()

        verify {
            anyConstructed<NetworkAdapter>()["sendRequest"](
                    capture(headersSlot),
                    any <HashMap<String, String>>(),
                    capture(requestBodySlot),
                    capture(urlSlot),
                    capture(requestListenerSlot)
            )
        }

        assertEquals(Endpoints.CHARGE_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)
        assertEquals(2, headersSlot.captured.size)
        assertEquals("2795f021-f31c-4533-a74d-5d3d887a003b", headersSlot.captured["prepayToken"])

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns NotificationResponses.ChargeResponse

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

        assertEquals(
                NotificationResponses.ChargeResponse,
                rawResponseSlot.captured
        )

        assertNotNull(notificationSlot.captured.redirectUrl)
    }

    @Test
    fun failMissingParameterHosted() {
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

        val mockedResponseListener = mockk<ResponseListenerAdapter>();
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onRedirectionURLReceived(any()) } just Runs
        every { mockedResponseListener.onResponseReceived(any(), any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val jsCharge = JSCharge()

        jsCharge.amount = "30"
        jsCharge.country = CountryCode.ES
        jsCharge.customerId = "55"
        jsCharge.currency = Currency.EUR
        jsCharge.operationType = OperationTypes.DEBIT
        jsCharge.paymentSolution = PaymentSolutions.creditcards
        jsCharge.statusURL = "https://test.com/paymentNotification"
        jsCharge.successURL = "https://test.com/success"
        jsCharge.errorURL = "https://test.com/error"
        jsCharge.awaitingURL = "https://test.com/awaiting"
        jsCharge.cancelURL = "https://test.com/cancel"
        jsCharge.apiVersion = 5

        val jsPaymentAdapter = JSPaymentAdapter(credentials)
        jsPaymentAdapter.sendJSChargeRequest(jsCharge, mockedResponseListener)
        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        assertEquals("Missing prepayToken", errorMessageSlot.captured)
    }
}