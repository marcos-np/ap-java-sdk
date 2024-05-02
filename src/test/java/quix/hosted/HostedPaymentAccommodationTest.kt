package quix.hosted

import com.mp.javaPaymentSDK.adapters.HostedQuixPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.adapters.SocketAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixAccommodationPaySolExtendedData
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixArticleAccommodation
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixCartAccommodation
import com.mp.javaPaymentSDK.models.quix_models.quix_accommodation.QuixItemCartItemAccommodation
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixArticleProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixCartProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemCartItemProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemPaySolExtendedData
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixAccommodation
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixItem
import com.mp.javaPaymentSDK.utils.Creds
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HostedPaymentAccommodationTest {

    @Test
    fun successHostedResponse() {

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
        credentials.productId = "1112220003"

        val hostedQuixAccommodation = HostedQuixAccommodation()
        hostedQuixAccommodation.apiVersion = 5
        hostedQuixAccommodation.amount = "99.0"
        hostedQuixAccommodation.customerId = "903"
        hostedQuixAccommodation.statusURL = "https://test.com/paymentNotification"
        hostedQuixAccommodation.cancelURL = "https://test.com/cancel"
        hostedQuixAccommodation.errorURL = "https://test.com/error"
        hostedQuixAccommodation.successURL = "https://test.com/success"
        hostedQuixAccommodation.awaitingURL = "https://test.com/awaiting"
        hostedQuixAccommodation.customerEmail = "test@mail.com"
        hostedQuixAccommodation.dob = "01-12-1999"
        hostedQuixAccommodation.firstName = "Name"
        hostedQuixAccommodation.lastName = "Last Name"
        hostedQuixAccommodation.merchantTransactionId = "12345678"

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixArticleAccommodation = QuixArticleAccommodation()
        quixArticleAccommodation.name = "Nombre del servicio 2"
        quixArticleAccommodation.reference = "4912345678903"
        quixArticleAccommodation.checkinDate = "2024-10-30T00:00:00+01:00"
        quixArticleAccommodation.checkoutDate = "2024-12-31T23:59:59+01:00"
        quixArticleAccommodation.guests = 1
        quixArticleAccommodation.establishmentName = "Hotel"
        quixArticleAccommodation.address = quixAddress
        quixArticleAccommodation.unit_price_with_tax = 99.0

        val quixItemCartItemAccommodation = QuixItemCartItemAccommodation()
        quixItemCartItemAccommodation.article = quixArticleAccommodation
        quixItemCartItemAccommodation.units = 1
        quixItemCartItemAccommodation.isAuto_shipping = true
        quixItemCartItemAccommodation.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemAccommodation> = java.util.ArrayList()
        items.add(quixItemCartItemAccommodation)

        val quixCartAccommodation = QuixCartAccommodation()
        quixCartAccommodation.currency = Currency.EUR
        quixCartAccommodation.items = items
        quixCartAccommodation.total_price_with_tax = 99.0

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixAccommodationPaySolExtendedData = QuixAccommodationPaySolExtendedData()
        quixAccommodationPaySolExtendedData.cart = quixCartAccommodation
        quixAccommodationPaySolExtendedData.billing = quixBilling
        quixAccommodationPaySolExtendedData.product = "instalments"

        hostedQuixAccommodation.paySolExtendedData = quixAccommodationPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixAccommodationRequest(hostedQuixAccommodation, mockedResponseListener)

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
            "2BDGM6x+gfSbyWnsjorW6SehNsfnfDRVlBc2heL9fGfOXyKzcFeArKLvKxO7s3Cz432FV9V9VSgfAfQzbHVr59WgjfTvj+uv9Bs1DxyFcf+hUKY/McQbXpRenl49P0ghITtNiN7Vyxs6TleM/JkjJjz8/ebgqHFTegpiyDbECd9/YURkLT8rvFK8PCIiP218KYAjXsH8990sraRF1T4SJIjBsXEepliqXGHBr1zjNWz19ViO4PQcG4JfsF4YNwi8dtKEsNQ+w9SpuOJ7DpTPVAjjp1iIf/2lb1sEd5SjKkLCB4SnWRsh4GRg0iOay1obfm2oHVwJbJOj6EUvjUrVAorEtFvFMwmC2LqspfOyBTqp3T5gbrwMqRSF+AJ8rFZOUgKzkK7cY2k5SGoCY0FyZS9qiHj70NQRI+VmAT/OPnbQJvz/HXU7AbBVS29vWZ2Tl4S9is5C32YRpyKjsSSa43RO7v74mRN/X4xZn36rldDbcTVRdM/7crGzlx25E169iMQul4nxJODdJ10b0UTQJXF3NmC5SGizD6mpqkOYkfFyG0bqZPsX4JlvvBUmuGb6XJYDI7EI+bo9+jMbbbCan7M2kFnJHXnAxqml+Ig/5AT0Ue+cz4H1HDrJnq1plz3y3dEhitiOtByb0mnG8LQKxLEi8h7p8A3WKgz96GdgTzzIMy9Q8GtL22O9LCedwt5drlDQ716fvMRQ4nZztdCmhQT/V0ICK031Ch/x49z6r9yTBZDljdR8TbpBFib/hEQm8fjY7Sa8JZ9pNSmVsW1c6nhKu1vwB+em5kWcc3SUHr28nwJih4oV6lpdIPhNAoJ3RB9X0teT0cbMPveVfvkWp2EcOZHoT+hMyviYt0bK4HGc/E/BVGoejvpVCNCajG+LuPurj9imv8DFK35W07X0q318FVH0LmX5Tm6hyZKhesux2RfBmYbYt41O14IdRCeZyYSKNSEaZnye3SHhQ9qHmMznXcVgBs+2eRcnveuC0vvvo3qOPIfbgzkzjheFCT2NhaHd3rq25FmyhEuYnm4VcKUQ9zChBppeGSXwVvZgCbXoOdUlYDkOH5vP5yR/YGBBpTl9Kqv2F7it6Qa6+Smo0wh1rSOw+JyeYJDd6yv4CtNNF1tgxZWgl6MxcsMK0XzAOkMnFlCxFOi0FegSSWI3/6GhDVp6zqU5XnapMyCuAPyCH8yrwjEG+TeJbb6tvwFkdPc/ce6bj76+CzEtyHI89f5tSKLGWhbHnXaUJp1oU2FMSKqHGQkOGpvj9FQKGblVYQ7QsuHniMatUkQejtpdp9ynMAPb9PQtE9tow9Plly7zL87hCN91WlVSzv35rnTwgmGvdfI4kN180Tfa/e4z18CuWMi5KCdZ70liktXedKnecjAxaqo1LwoUYMKSBY7sdaycZILnEgMH7jBbCiHoqP3tlnZm2+nCY7pY4WAqIse30d3nMJn3brjt+VemuCH9IxMYNZkYMdITZXfmAEhwZGy5IuIlbJ/40ARVoAbxXpzs2hjKDNQt2Ink4O5EVlNro+Mm5OMoBy86PxhmVyyk2O5s6DlQ7ulql19n/cdxdmwHVIKLhV9md2lYQ3LePqGXbmBhFDQkgtI6nTupgtRcY2wcs7jhXv2cYsLqG9cEkH8AcsTUampcxFXXZnl6aSLLCJOriKeqizOQyP7tExzlgm/PTVVzSdI/dUz+gKbYyuo0SSkhhSvquzS9gPQY+nXGUqOAO9DLfAGxJhrl8SZ1yjyp+mno81dn0uGEA2VjGQDW1D1//udA1p3ed3u/eQMw2BTBupU9dEvM33vh6kffxa2e9QKxb8sl2gty0B8ztE+Nn2RQ0uhWD7FuN7Z6rsbAxfER/Sd4W7+mN8gr9CWF3WX6Suf6xPGqhJXUU4cQ91JJNVSivdHC+VU3AsiCy9P45oYTnIH16jobvrRZNHreel9JwXBzlbIZa09vzJ79pC+ZhF/BpWJCHnUFPSo2PTaY",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "1728f9c6a2ab9610b3f437f44b6a90020e3a59baededf68a2fa8d4286562075a",
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

        val hostedQuixAccommodation = HostedQuixAccommodation()
        hostedQuixAccommodation.amount = "99.0"
        hostedQuixAccommodation.customerId = "903"
        hostedQuixAccommodation.cancelURL = "https://test.com/cancel"
        hostedQuixAccommodation.errorURL = "https://test.com/error"
        hostedQuixAccommodation.awaitingURL = "https://test.com/awaiting"
        hostedQuixAccommodation.successURL = "https://test.com/success"
        hostedQuixAccommodation.customerEmail = "test@mail.com"
        hostedQuixAccommodation.dob = "01-12-1999"
        hostedQuixAccommodation.firstName = "Name"
        hostedQuixAccommodation.lastName = "Last Name"
        hostedQuixAccommodation.merchantTransactionId = "12345678"
        hostedQuixAccommodation.apiVersion = 5

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixArticleAccommodation = QuixArticleAccommodation()
        quixArticleAccommodation.name = "Nombre del servicio 2"
        quixArticleAccommodation.reference = "4912345678903"
        quixArticleAccommodation.checkinDate = "2024-10-30T00:00:00+01:00"
        quixArticleAccommodation.checkoutDate = "2024-12-31T23:59:59+01:00"
        quixArticleAccommodation.guests = 1
        quixArticleAccommodation.establishmentName = "Hotel"
        quixArticleAccommodation.address = quixAddress
        quixArticleAccommodation.unit_price_with_tax = 99.0

        val quixItemCartItemAccommodation = QuixItemCartItemAccommodation()
        quixItemCartItemAccommodation.article = quixArticleAccommodation
        quixItemCartItemAccommodation.units = 1
        quixItemCartItemAccommodation.isAuto_shipping = true
        quixItemCartItemAccommodation.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemAccommodation> = java.util.ArrayList()
        items.add(quixItemCartItemAccommodation)

        val quixCartAccommodation = QuixCartAccommodation()
        quixCartAccommodation.currency = Currency.EUR
        quixCartAccommodation.items = items
        quixCartAccommodation.total_price_with_tax = 99.0

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixAccommodationPaySolExtendedData = QuixAccommodationPaySolExtendedData()
        quixAccommodationPaySolExtendedData.cart = quixCartAccommodation
        quixAccommodationPaySolExtendedData.billing = quixBilling
        quixAccommodationPaySolExtendedData.product = "instalments"

        hostedQuixAccommodation.paySolExtendedData = quixAccommodationPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixAccommodationRequest(hostedQuixAccommodation, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.MISSING_PARAMETER, errorSlot.captured)
        assertEquals("Missing statusURL", errorMessageSlot.captured)
    }

    @Test
    fun failInvalidAmountHosted() {
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
        credentials.productId = "1112220003"

        val hostedQuixAccommodation = HostedQuixAccommodation()
        hostedQuixAccommodation.amount = "99,00"
        hostedQuixAccommodation.customerId = "903"
        hostedQuixAccommodation.statusURL = "https://test.com/paymentNotification"
        hostedQuixAccommodation.cancelURL = "https://test.com/cancel"
        hostedQuixAccommodation.errorURL = "https://test.com/error"
        hostedQuixAccommodation.successURL = "https://test.com/success"
        hostedQuixAccommodation.awaitingURL = "https://test.com/awaiting"
        hostedQuixAccommodation.customerEmail = "test@mail.com"
        hostedQuixAccommodation.dob = "01-12-1999"
        hostedQuixAccommodation.firstName = "Name"
        hostedQuixAccommodation.lastName = "Last Name"
        hostedQuixAccommodation.merchantTransactionId = "12345678"
        hostedQuixAccommodation.apiVersion = 5

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixArticleAccommodation = QuixArticleAccommodation()
        quixArticleAccommodation.name = "Nombre del servicio 2"
        quixArticleAccommodation.reference = "4912345678903"
        quixArticleAccommodation.checkinDate = "2024-10-30T00:00:00+01:00"
        quixArticleAccommodation.checkoutDate = "2024-12-31T23:59:59+01:00"
        quixArticleAccommodation.guests = 1
        quixArticleAccommodation.establishmentName = "Hotel"
        quixArticleAccommodation.address = quixAddress
        quixArticleAccommodation.unit_price_with_tax = 99.0

        val quixItemCartItemAccommodation = QuixItemCartItemAccommodation()
        quixItemCartItemAccommodation.article = quixArticleAccommodation
        quixItemCartItemAccommodation.units = 1
        quixItemCartItemAccommodation.isAuto_shipping = true
        quixItemCartItemAccommodation.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemAccommodation> = java.util.ArrayList()
        items.add(quixItemCartItemAccommodation)

        val quixCartAccommodation = QuixCartAccommodation()
        quixCartAccommodation.currency = Currency.EUR
        quixCartAccommodation.items = items
        quixCartAccommodation.total_price_with_tax = 99.0


        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixAccommodationPaySolExtendedData = QuixAccommodationPaySolExtendedData()
        quixAccommodationPaySolExtendedData.cart = quixCartAccommodation
        quixAccommodationPaySolExtendedData.billing = quixBilling
        quixAccommodationPaySolExtendedData.product = "instalments"

        hostedQuixAccommodation.paySolExtendedData = quixAccommodationPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixAccommodationRequest(hostedQuixAccommodation, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }
}