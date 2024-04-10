package quix.js

import com.mp.javaPaymentSDK.adapters.JSQuixPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.adapters.SocketAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixArticleProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixCartProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemCartItemProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemPaySolExtendedData
import com.mp.javaPaymentSDK.models.requests.quix_js.JSQuixItem
import com.mp.javaPaymentSDK.models.responses.notification.Notification
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import utils.NotificationResponses

class JsPaymentItemsTest {

    @Test
    fun successJsNotification() {
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

        val jsQuixItem = JSQuixItem()
        jsQuixItem.amount = 99.0
        jsQuixItem.customerId = "903"
        jsQuixItem.prepayToken = "2795f021-f31c-4533-a74d-5d3d887a003b"
        jsQuixItem.statusURL = "https://test.com/paymentNotification"
        jsQuixItem.cancelURL = "https://test.com/cancel"
        jsQuixItem.errorURL = "https://test.com/error"
        jsQuixItem.successURL = "https://test.com/success"
        jsQuixItem.awaitingURL = "https://test.com/awaiting"
        jsQuixItem.customerEmail = "test@mail.com"
        jsQuixItem.dob = "01-12-1999"
        jsQuixItem.firstName = "Name"
        jsQuixItem.lastName = "Last Name"
        jsQuixItem.merchantTransactionId = "12345678"
        jsQuixItem.apiVersion = 5

        val quixArticleProduct = QuixArticleProduct()
        quixArticleProduct.name = "Nombre del servicio 2"
        quixArticleProduct.reference = "4912345678903"
        quixArticleProduct.unit_price_with_tax = 99.0

        val quixItemCartItemProduct = QuixItemCartItemProduct()
        quixItemCartItemProduct.article = quixArticleProduct
        quixItemCartItemProduct.units = 1
        quixItemCartItemProduct.isAuto_shipping = true
        quixItemCartItemProduct.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemProduct> = ArrayList()
        items.add(quixItemCartItemProduct)

        val quixCartProduct = QuixCartProduct()
        quixCartProduct.currency = Currency.EUR
        quixCartProduct.items = items
        quixCartProduct.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "08003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixItemPaySolExtendedData = QuixItemPaySolExtendedData()
        quixItemPaySolExtendedData.cart = quixCartProduct
        quixItemPaySolExtendedData.billing = quixBilling
        quixItemPaySolExtendedData.product = "instalments"

        jsQuixItem.paySolExtendedData = quixItemPaySolExtendedData

        val jsQuixPaymentAdapter = JSQuixPaymentAdapter(credentials)
        jsQuixPaymentAdapter.sendJSQuixItemRequest(jsQuixItem, mockedResponseListener)

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

        assertEquals(2, headersSlot.captured.size)
        assertEquals("2795f021-f31c-4533-a74d-5d3d887a003b", headersSlot.captured["prepayToken"])
        assertEquals(Endpoints.CHARGE_ENDPOINT.getEndpoint(Environment.STAGING), urlSlot.captured)

        val mockedResponseBody = mockk<ResponseBody>()
        every { mockedResponseBody.string() } returns NotificationResponses.JsQuixItemsResponse

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
                NotificationResponses.JsQuixItemsResponse,
                rawResponseSlot.captured
        )

       assertEquals(1, notificationSlot.captured.operations.size)
       assertEquals("af24252b-e8c9-4fb2-9da2-7a476b2d8cd4", notificationSlot.captured.nemuruCartHash)
       assertEquals("62WBmZM44eDS2gZfVbgvEg5Cydea7IcY", notificationSlot.captured.nemuruAuthToken)
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

        val jsQuixItem = JSQuixItem()
        jsQuixItem.amount = 99.0
        jsQuixItem.customerId = "903"
        jsQuixItem.prepayToken = "2795f021-f31c-4533-a74d-5d3d887a003b"
        jsQuixItem.statusURL = "https://test.com/paymentNotification"
        jsQuixItem.cancelURL = "https://test.com/cancel"
        jsQuixItem.errorURL = "https://test.com/error"
        jsQuixItem.successURL = "https://test.com/success"
        jsQuixItem.customerEmail = "test@mail.com"
        jsQuixItem.dob = "01-12-1999"
        jsQuixItem.firstName = "Name"
        jsQuixItem.lastName = "Last Name"
        jsQuixItem.merchantTransactionId = "12345678"
        jsQuixItem.apiVersion = 5

        val quixArticleProduct = QuixArticleProduct()
        quixArticleProduct.name = "Nombre del servicio 2"
        quixArticleProduct.reference = "4912345678903"
        quixArticleProduct.unit_price_with_tax = 99.0

        val quixItemCartItemProduct = QuixItemCartItemProduct()
        quixItemCartItemProduct.article = quixArticleProduct
        quixItemCartItemProduct.isAuto_shipping = true
        quixItemCartItemProduct.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemProduct> = ArrayList()
        items.add(quixItemCartItemProduct)

        val quixCartProduct = QuixCartProduct()
        quixCartProduct.currency = Currency.EUR
        quixCartProduct.items = items
        quixCartProduct.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "08003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixItemPaySolExtendedData = QuixItemPaySolExtendedData()
        quixItemPaySolExtendedData.cart = quixCartProduct
        quixItemPaySolExtendedData.billing = quixBilling
        quixItemPaySolExtendedData.product = "instalments"

        jsQuixItem.paySolExtendedData = quixItemPaySolExtendedData

        val jsQuixPaymentAdapter = JSQuixPaymentAdapter(credentials)
        jsQuixPaymentAdapter.sendJSQuixItemRequest(jsQuixItem, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        assertEquals("Missing units", errorMessageSlot.captured)
    }
}