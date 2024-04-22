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
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixArticleProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixCartProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemCartItemProduct
import com.mp.javaPaymentSDK.models.quix_models.quix_product.QuixItemPaySolExtendedData
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixItem
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HostedPaymentItemsTest {

    @Test
    fun successHostedNotification() {

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
        credentials.productId = "1112220001"

        val hostedQuixItem = HostedQuixItem()
        hostedQuixItem.amount = "99.0"
        hostedQuixItem.customerId = "903"
        hostedQuixItem.statusURL = "https://test.com/paymentNotification"
        hostedQuixItem.cancelURL = "https://test.com/cancel"
        hostedQuixItem.errorURL = "https://test.com/error"
        hostedQuixItem.successURL = "https://test.com/success"
        hostedQuixItem.awaitingURL = "https://test.com/awaiting"
        hostedQuixItem.customerEmail = "test@mail.com"
        hostedQuixItem.dob = "01-12-1999"
        hostedQuixItem.firstName = "Name"
        hostedQuixItem.lastName = "Last Name"
        hostedQuixItem.merchantTransactionId = "12345678"
        hostedQuixItem.apiVersion = 5

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

        hostedQuixItem.paySolExtendedData = quixItemPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixItemRequest(hostedQuixItem, mockedResponseListener)

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
            "2BDGM6x+gfSbyWnsjorW6SehNsfnfDRVlBc2heL9fGfOXyKzcFeArKLvKxO7s3Cz432FV9V9VSgfAfQzbHVr59WgjfTvj+uv9Bs1DxyFcf+hUKY/McQbXpRenl49P0ghITtNiN7Vyxs6TleM/JkjJjz8/ebgqHFTegpiyDbECd9/YURkLT8rvFK8PCIiP218KYAjXsH8990sraRF1T4SJIjBsXEepliqXGHBr1zjNWz19ViO4PQcG4JfsF4YNwi8dtKEsNQ+w9SpuOJ7DpTPVAjjp1iIf/2lb1sEd5SjKkLCB4SnWRsh4GRg0iOay1obfm2oHVwJbJOj6EUvjUrVAorEtFvFMwmC2LqspfOyBTqp3T5gbrwMqRSF+AJ8rFZOUgKzkK7cY2k5SGoCY0FyZS9qiHj70NQRI+VmAT/OPnbQJvz/HXU7AbBVS29vWZ2Tl4S9is5C32YRpyKjsSSa43RO7v74mRN/X4xZn36rldDbcTVRdM/7crGzlx25E169WPUkaxK9U+0soUvUHW5EQ8RJC3RcymRXaRq990B26urzv6VfrooBj7lo74VW55jzB3aJ0hCwr1A+wuVfonalT+73P/sqjKijtXvMEYd/V903e+i/ZXzjVzasXuxU3siuLbJ0pJj+7DUy/BOD2DW1qfxkK9QT3WFea9swAxXZWNsiCz2X4+MiO+YJIXgiArdIxyQqowrz4ZE7CBg0DuYQsm7/x3rs9RvTOY3SeFoA0STMI2xJO7hFtJMWit/ZIDmb+8wGKVYvCrIJtNOsXGucnRSaq9av2sAjQVZAB95AQtGj1Ij86Y7VGz2gk/aUcryFScT8O0cf1aJ+JrSXg9E369sc5A8SFXatR2jvyhit1ztnHdOaFt3uSUJm3DlvEMGgA96DFe5isJ5N3AiS3ANRzJQwhnVgIGXisE5pi30wsaaROgqQj2yrTS2LiF2GT/vSxzEPuNfXRtYKW9naIQ7nbwdUEypOJ6jcjyeAiFZnz+Hx9xsPeGHHm3sPghT6amZJRI2FN9AyLWk8w69esaV8pzxeXs7cYwse6DyrWuRNTn8+W2cKjW4QIofwdz3MesLOJgLmtH9GVEVNf03QIFrWQEwlKGimucyivFmZMTwepO3YLa2JQehiHM+/IxQqXoo/jdQz4xSJDr0XwiTtI4qIOZK1mV8ig1J8u3IIhOBkDX2pQIc5lOZdUNHUDU+HVncA68st3VpE5Lvx6dyiuhEa32OgqPeP7v/zomTYhE8CB8Wx8xw9U+QiBm2OS2zZwo3ZNrUP8HzEWfnk5h7eaUGj00lVrK/YBKEp1uemjecTCIzTri+4oDVnIn+Bm9/F+RFmZIhR2xTshyPuXvFaKuNDdG11DEbDdz8Entb93P5v36qxxAhQl8tuktyW0D6/vSDuxTNtJFEzVAp8XHEO2ouJhjEVkEOkoYEZb8m3IzzEwC8Fx53Pr/hlI/U7OqjzsTLLqvqCv47T4uO2bS+MnaubkbfIMbG4UslXPwt51I4PfcDBt7UYPWgRD006iSQxa6rT1Hkmyxyjkscb8hP+oTEGxU8baNm5DDa1FwXqqlPvmQxAS//PhgPxwQAEk0b2dB06",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "0aec74ee52230459f208246dc6458eba38b0bff17b0d5b1e205fd842705bf665",
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

        val hostedQuixItem = HostedQuixItem()
        hostedQuixItem.amount = "99.0"
        hostedQuixItem.customerId = "903"
        hostedQuixItem.cancelURL = "https://test.com/cancel"
        hostedQuixItem.errorURL = "https://test.com/error"
        hostedQuixItem.successURL = "https://test.com/success"
        hostedQuixItem.customerEmail = "test@mail.com"
        hostedQuixItem.dob = "01-12-1999"
        hostedQuixItem.firstName = "Name"
        hostedQuixItem.lastName = "Last Name"
        hostedQuixItem.merchantTransactionId = "12345678"
        hostedQuixItem.apiVersion = 5

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

        hostedQuixItem.paySolExtendedData = quixItemPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixItemRequest(hostedQuixItem, mockedResponseListener)

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
        credentials.productId = "1112220001"

        val hostedQuixItem = HostedQuixItem()
        hostedQuixItem.amount = "99,01"
        hostedQuixItem.customerId = "903"
        hostedQuixItem.statusURL = "https://test.com/paymentNotification"
        hostedQuixItem.cancelURL = "https://test.com/cancel"
        hostedQuixItem.errorURL = "https://test.com/error"
        hostedQuixItem.successURL = "https://test.com/success"
        hostedQuixItem.awaitingURL = "https://test.com/awaiting"
        hostedQuixItem.customerEmail = "test@mail.com"
        hostedQuixItem.dob = "01-12-1999"
        hostedQuixItem.firstName = "Name"
        hostedQuixItem.lastName = "Last Name"
        hostedQuixItem.merchantTransactionId = "12345678"
        hostedQuixItem.apiVersion = 5

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

        hostedQuixItem.paySolExtendedData = quixItemPaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixItemRequest(hostedQuixItem, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }
}