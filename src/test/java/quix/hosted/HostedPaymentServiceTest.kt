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
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixArticleService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixCartService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixItemCartItemService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixServicePaySolExtendedData
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixItem
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixService
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HostedPaymentServiceTest {

    @Test
    fun successHostedServiceNotification() {
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

        val hostedQuixService = HostedQuixService()
        hostedQuixService.amount = "99.0"
        hostedQuixService.customerId = "903"
        hostedQuixService.statusURL = "https://test.com/paymentNotification"
        hostedQuixService.cancelURL = "https://test.com/cancel"
        hostedQuixService.errorURL = "https://test.com/error"
        hostedQuixService.successURL = "https://test.com/success"
        hostedQuixService.awaitingURL = "https://test.com/awaiting"
        hostedQuixService.customerEmail = "test@mail.com"
        hostedQuixService.dob = "01-12-1999"
        hostedQuixService.firstName = "Name"
        hostedQuixService.lastName = "Last Name"
        hostedQuixService.merchantTransactionId = "12345678"
        hostedQuixService.apiVersion = 5

        val quixArticleService = QuixArticleService()
        quixArticleService.name = "Nombre del servicio 2"
        quixArticleService.reference = "4912345678903"
        quixArticleService.startDate = "2024-10-30T00:00:00+01:00"
        quixArticleService.endDate = "2024-12-31T23:59:59+01:00"
        quixArticleService.unit_price_with_tax = 99.0

        val quixItemCartItemService = QuixItemCartItemService()
        quixItemCartItemService.article = quixArticleService
        quixItemCartItemService.units = 1
        quixItemCartItemService.isAuto_shipping = true
        quixItemCartItemService.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemService> = java.util.ArrayList()
        items.add(quixItemCartItemService)

        val quixCartService = QuixCartService()
        quixCartService.currency = Currency.EUR
        quixCartService.items = items
        quixCartService.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixServicePaySolExtendedData = QuixServicePaySolExtendedData()
        quixServicePaySolExtendedData.cart = quixCartService
        quixServicePaySolExtendedData.billing = quixBilling
        quixServicePaySolExtendedData.product = "instalments"

        hostedQuixService.paySolExtendedData = quixServicePaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixServiceRequest(hostedQuixService, mockedResponseListener)

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
            "2BDGM6x+gfSbyWnsjorW6SehNsfnfDRVlBc2heL9fGfOXyKzcFeArKLvKxO7s3Cz432FV9V9VSgfAfQzbHVr59WgjfTvj+uv9Bs1DxyFcf+hUKY/McQbXpRenl49P0ghITtNiN7Vyxs6TleM/JkjJjz8/ebgqHFTegpiyDbECd9/YURkLT8rvFK8PCIiP218KYAjXsH8990sraRF1T4SJIjBsXEepliqXGHBr1zjNWz19ViO4PQcG4JfsF4YNwi8dtKEsNQ+w9SpuOJ7DpTPVAjjp1iIf/2lb1sEd5SjKkLCB4SnWRsh4GRg0iOay1obfm2oHVwJbJOj6EUvjUrVAorEtFvFMwmC2LqspfOyBTqp3T5gbrwMqRSF+AJ8rFZOUgKzkK7cY2k5SGoCY0FyZS9qiHj70NQRI+VmAT/OPnbQJvz/HXU7AbBVS29vWZ2Tl4S9is5C32YRpyKjsSSa43RO7v74mRN/X4xZn36rldDbcTVRdM/7crGzlx25E169WPUkaxK9U+0soUvUHW5EQ8RJC3RcymRXaRq990B26urzv6VfrooBj7lo74VW55jzB3aJ0hCwr1A+wuVfonalT+73P/sqjKijtXvMEYd/V903e+i/ZXzjVzasXuxU3siuLbJ0pJj+7DUy/BOD2DW1qfxkK9QT3WFea9swAxXZWNsiCz2X4+MiO+YJIXgiArdIxyQqowrz4ZE7CBg0DuYQsm7/x3rs9RvTOY3SeFoA0STMI2xJO7hFtJMWit/ZIDmb+8wGKVYvCrIJtNOsXGucnRSaq9av2sAjQVZAB95AQtGj1Ij86Y7VGz2gk/aUcryFScT8O0cf1aJ+JrSXg9E369sc5A8SFXatR2jvyhit1ztnHdOaFt3uSUJm3DlvEMGgA96DFe5isJ5N3AiS3ANRzJQwhnVgIGXisE5pi30wsaZOPDusGPLtbpqHc161cvfXTLabSxmdD2AChUJIWP6zcSjaZ7UGc5EI4jFkKuQDivF7uSZrwqidloQysaoJwibbCGpBr6xc7Ph2fmASG7kWYF2/eMUeMli94DNlon7nmhV0CLEt3VLqP/mFfH+l0F7x+AXL3q0iA4g7EbMcaYT+QNeKUmPiH07L0GGYD6lEZ++3tzXCtAng1bZKDBcvV6tMgpxKAmR6RjBJ0Kock8i3z5PFo6OJBURNM0brbhjtTwLXZf/vn71IeemhNqMf9Xg8t+u4w/PFpN0G2P7s1zmxVdxij2MzQdxfi1f4Lq5ykYU3BeeUVcKcFxxeaaJ3ea9txNi3+dlHwG4FGtW58SgEgD2RQmGvMaDKc3kdJQj403wlNIvAn1nxOk31jmesLGlRpN6QdmCZfB77mptTfJxco0fzNbChqa8V1METILW2pldl2Z4i5gY549HEHgr7sR/iMD6n+H4mK65U/Z+SugawOKO+VPQ0mmrE8KlPfeJzpvPbRrt7MBADQjKmj79aD7DXqN29O1tjQyipg6DyXOxSlLlV0jCH7OKzqWUOwzsonO6uSJpNuunzFGZZ5qgrFVn+o9FvnrdhgLxk2BBbTM36seNHEBcJJXc85zzPBS1DQtTduYGJmJC8aSGj+CB0I7qXCdi6PA1eMf08knOBW/t8fQHikW9ud+8Glx6hsDFX8drTC45lQBhZ2aizsOTgK4yJ+mfb+YwLD/wXBm9TcuHVjwVn46lXa8u4q3hgFqdTZwJbMYF/TChOdswd7eA08PdM",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "7f8eab74bffacc38c22accff798101040ad2767584eb8c7a70dc4d9c60caf94e",
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

        val hostedQuixService = HostedQuixService()
        hostedQuixService.amount = "99.0"
        hostedQuixService.customerId = "903"
        hostedQuixService.cancelURL = "https://test.com/cancel"
        hostedQuixService.errorURL = "https://test.com/error"
        hostedQuixService.successURL = "https://test.com/success"
        hostedQuixService.awaitingURL = "https://test.com/awaiting"
        hostedQuixService.customerEmail = "test@mail.com"
        hostedQuixService.dob = "01-12-1999"
        hostedQuixService.firstName = "Name"
        hostedQuixService.lastName = "Last Name"
        hostedQuixService.merchantTransactionId = "12345678"
        hostedQuixService.apiVersion = 5

        val quixArticleService = QuixArticleService()
        quixArticleService.name = "Nombre del servicio 2"
        quixArticleService.reference = "4912345678903"
        quixArticleService.startDate = "2024-10-30T00:00:00+01:00"
        quixArticleService.endDate = "2024-12-31T23:59:59+01:00"
        quixArticleService.unit_price_with_tax = 99.0

        val quixItemCartItemService = QuixItemCartItemService()
        quixItemCartItemService.article = quixArticleService
        quixItemCartItemService.units = 1
        quixItemCartItemService.isAuto_shipping = true
        quixItemCartItemService.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemService> = java.util.ArrayList()
        items.add(quixItemCartItemService)

        val quixCartService = QuixCartService()
        quixCartService.currency = Currency.EUR
        quixCartService.items = items
        quixCartService.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixServicePaySolExtendedData = QuixServicePaySolExtendedData()
        quixServicePaySolExtendedData.cart = quixCartService
        quixServicePaySolExtendedData.billing = quixBilling
        quixServicePaySolExtendedData.product = "instalments"

        hostedQuixService.paySolExtendedData = quixServicePaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixServiceRequest(hostedQuixService, mockedResponseListener)

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

        val hostedQuixService = HostedQuixService()
        hostedQuixService.amount = "99,1123"
        hostedQuixService.customerId = "903"
        hostedQuixService.statusURL = "https://test.com/paymentNotification"
        hostedQuixService.cancelURL = "https://test.com/cancel"
        hostedQuixService.errorURL = "https://test.com/error"
        hostedQuixService.successURL = "https://test.com/success"
        hostedQuixService.awaitingURL = "https://test.com/awaiting"
        hostedQuixService.customerEmail = "test@mail.com"
        hostedQuixService.dob = "01-12-1999"
        hostedQuixService.firstName = "Name"
        hostedQuixService.lastName = "Last Name"
        hostedQuixService.merchantTransactionId = "12345678"
        hostedQuixService.apiVersion = 5

        val quixArticleService = QuixArticleService()
        quixArticleService.name = "Nombre del servicio 2"
        quixArticleService.reference = "4912345678903"
        quixArticleService.startDate = "2024-10-30T00:00:00+01:00"
        quixArticleService.endDate = "2024-12-31T23:59:59+01:00"
        quixArticleService.unit_price_with_tax = 99.0

        val quixItemCartItemService = QuixItemCartItemService()
        quixItemCartItemService.article = quixArticleService
        quixItemCartItemService.units = 1
        quixItemCartItemService.isAuto_shipping = true
        quixItemCartItemService.total_price_with_tax = 99.0

        val items: MutableList<QuixItemCartItemService> = java.util.ArrayList()
        items.add(quixItemCartItemService)

        val quixCartService = QuixCartService()
        quixCartService.currency = Currency.EUR
        quixCartService.items = items
        quixCartService.total_price_with_tax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.street_address = "Nombre de la vía y nº"
        quixAddress.postal_code = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.first_name = "Nombre"
        quixBilling.last_name = "Apellido"

        val quixServicePaySolExtendedData = QuixServicePaySolExtendedData()
        quixServicePaySolExtendedData.cart = quixCartService
        quixServicePaySolExtendedData.billing = quixBilling
        quixServicePaySolExtendedData.product = "instalments"

        hostedQuixService.paySolExtendedData = quixServicePaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)
        hostedQuixPaymentAdapter.sendHostedQuixServiceRequest(hostedQuixService, mockedResponseListener)

        val errorSlot = slot<Error>()
        val errorMessageSlot = slot<String>()

        verify { mockedResponseListener.onError(capture(errorSlot), capture(errorMessageSlot)) }

        assertEquals(Error.INVALID_AMOUNT, errorSlot.captured)
        assertEquals(Error.INVALID_AMOUNT.message, errorMessageSlot.captured)
    }
}