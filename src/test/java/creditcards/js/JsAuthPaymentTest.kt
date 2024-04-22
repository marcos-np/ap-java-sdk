package creditcards.js

import com.mp.javaPaymentSDK.adapters.JSPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.callbacks.JSPaymentListener
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.requests.js.JSAuthorizationRequest
import com.mp.javaPaymentSDK.models.responses.JSAuthorizationResponse
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import utils.NotificationResponses

class JsAuthPaymentTest {

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

        val mockedResponseListener = mockk<JSPaymentListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onAuthorizationResponseReceived(any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val jsAuthorizationRequest = JSAuthorizationRequest()

        jsAuthorizationRequest.country = CountryCode.ES
        jsAuthorizationRequest.customerId = "55"
        jsAuthorizationRequest.currency = Currency.EUR
        jsAuthorizationRequest.operationType = OperationTypes.DEBIT
        jsAuthorizationRequest.apiVersion = 5

        val jsPaymentAdapter = JSPaymentAdapter(credentials)
        jsPaymentAdapter.sendJSAuthorizationRequest(jsAuthorizationRequest, mockedResponseListener)

        val requestBodySlot = slot<RequestBody>()
        val urlSlot = slot<String>()
        val requestListenerSlot = slot<RequestListener>()

        verify {
            anyConstructed<NetworkAdapter>()["sendRequest"](
                    any <HashMap<String, String>>(),
                    any <HashMap<String, String>>(),
                    capture(requestBodySlot),
                    capture(urlSlot),
                    capture(requestListenerSlot)
            )
        }

        assertEquals(Endpoints.AUTH_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns NotificationResponses.AuthResponse

        requestListenerSlot.captured.onResponse(200, mockedResponseBody)

        val rawResponseSlot = slot<String>()
        val jsAuthorizationResponseCapturingSlot = slot<JSAuthorizationResponse>()

        verify {
            mockedResponseListener.onAuthorizationResponseReceived(
                    capture(rawResponseSlot),
                    capture(jsAuthorizationResponseCapturingSlot),

            )
        }

        assertEquals(
                NotificationResponses.AuthResponse,
                rawResponseSlot.captured
        )

        assertEquals("c5f2746c-d4ec-44fa-b7a7-61df2378296d", jsAuthorizationResponseCapturingSlot.captured.authToken)
    }

    @Test
    fun failMissingParameterJS() {
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

        val mockedResponseListener = mockk<JSPaymentListener>()
        every { mockedResponseListener.onError(any(), any()) } just Runs
        every { mockedResponseListener.onAuthorizationResponseReceived(any(), any()) } just Runs

        val credentials = Credentials()
        credentials.merchantPass = "11111111112222222222333333333344"
        credentials.merchantKey = "11111111-1111-1111-1111-111111111111"
        credentials.merchantId = "111222"
        credentials.environment = Environment.STAGING
        credentials.productId = "1112220001"

        val jsAuthorizationRequest = JSAuthorizationRequest()
        jsAuthorizationRequest.country = CountryCode.ES
        jsAuthorizationRequest.currency = Currency.EUR
        jsAuthorizationRequest.operationType = OperationTypes.DEBIT
        jsAuthorizationRequest.apiVersion = 5

        val jsPaymentAdapter = JSPaymentAdapter(credentials)
        jsPaymentAdapter.sendJSAuthorizationRequest(jsAuthorizationRequest, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        assertEquals("Missing customerId", errorMessageSlot.captured)
    }
}