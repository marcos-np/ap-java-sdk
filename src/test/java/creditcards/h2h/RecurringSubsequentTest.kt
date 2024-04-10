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
            "8R7mQRffpbBlV84oSuBAXcqmS6xvKcOR/C3dSOSpZt/KRbA+jBGxcTMIMybhY/q9WxCPo1Ber/fVwAuHYYUv2WLrOhrtQLZYdzgMj+gzcXS0GzlGVb0yiWSI2jwzTN34SfTc20vME3WGYFjAJfmQveW+ijmD5UFyGJUmjN6a3lvn0ywN4I/cmpQSXY2BOaxOyyJA5LjUvb0Ta9EMBb6+RF2S68e3B4Z2mVp9U0HRpVnN9B5yKiPzQiDjYDxSOUDvGXlTuAh1DM1Ic3Nv03ySejgj1SjENm4xduPzy2Z1S3D627wKP4FF3UZ3EkGbI6fvI+UFJsqe3TsoO0by5YEnDbKoVHPzGu/HS9d/G3wx2hgvchusWn1hxUUxY20AOh3NhIx3aD6ViOOwaBdoWLzBQuSMwzSUXeztzcIWvAJZ7Hoo6maq2KhIhH9YWujtYasOVE9Xv0e/HoSJIkK7F8xpYzJsSEhs4W9DJL4YPIIgwZkJsH2eSGAR/3e0rPC1gdZLPqB9DPrr0wEBCH1EioUUt3Hh59tDOW78XaQIlIhPPDznxge7EF643pWIKhfkK2RrDLvsbmdSQqaCuc/M+rD1W7VMr7XVzM/3SrrlxZ795jI6EpzBDl/8bEn+uPWuScZYOOBF8LnqUlNRyNxLSi7nAQ==",
            queryParameterSlot.captured["encrypted"]
        )
        Assertions.assertEquals(
            "d339232ede5f640717b6f4045dbbb37a45c2a613edb71e870e46e9e6dd588dbc",
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

        val h2HPaymentAdapter = H2HPaymentAdapter(credentials)

        h2HPaymentAdapter.sendH2hPaymentRecurrentSuccessive(h2HPaymentRecurrentSuccessive, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        Assertions.assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        Assertions.assertEquals("Missing cardNumberToken", errorMessageSlot.captured)
    }

}