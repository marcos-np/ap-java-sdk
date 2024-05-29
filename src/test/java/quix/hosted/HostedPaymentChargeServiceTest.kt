package quix.hosted

import com.mp.javaPaymentSDK.adapters.HostedQuixPaymentAdapter
import com.mp.javaPaymentSDK.adapters.NetworkAdapter
import com.mp.javaPaymentSDK.callbacks.RequestListener
import com.mp.javaPaymentSDK.callbacks.ResponseListener
import com.mp.javaPaymentSDK.enums.*
import com.mp.javaPaymentSDK.exceptions.InvalidFieldException
import com.mp.javaPaymentSDK.exceptions.MissingFieldException
import com.mp.javaPaymentSDK.models.Credentials
import com.mp.javaPaymentSDK.models.quix_models.QuixAddress
import com.mp.javaPaymentSDK.models.quix_models.QuixBilling
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixArticleService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixCartService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixItemCartItemService
import com.mp.javaPaymentSDK.models.quix_models.quix_service.QuixServicePaySolExtendedData
import com.mp.javaPaymentSDK.models.requests.quix_hosted.HostedQuixService
import com.mp.javaPaymentSDK.utils.SecurityUtils
import io.mockk.*
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class HostedPaymentChargeServiceTest {

    @Test
    fun successHostedServiceNotification() {

        mockkStatic(SecurityUtils::class)
        every { SecurityUtils.generateIV() } returns ByteArray(16) { 1 }
        every { SecurityUtils.hash256(any()) } answers { callOriginal() }
        every { SecurityUtils.base64Encode(any()) } answers { callOriginal() }
        every { SecurityUtils.applyAESPadding(any()) } answers { callOriginal() }
        every { SecurityUtils.cbcEncryption(any(), any(), any(), any()) } answers { callOriginal() }

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
        credentials.apiVersion = 5

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
        hostedQuixService.ipAddress = "0.0.0.0"

        val quixArticleService = QuixArticleService()
        quixArticleService.name = "Nombre del servicio 2"
        quixArticleService.reference = "4912345678903"
        quixArticleService.endDate = "2024-12-31T23:59:59+01:00"
        quixArticleService.unitPriceWithTax = 99.0
        quixArticleService.category = Category.digital

        val quixItemCartItemService = QuixItemCartItemService()
        quixItemCartItemService.article = quixArticleService
        quixItemCartItemService.units = 1
        quixItemCartItemService.isAutoShipping = true
        quixItemCartItemService.totalPriceWithTax = 99.0

        val items: MutableList<QuixItemCartItemService> = java.util.ArrayList()
        items.add(quixItemCartItemService)

        val quixCartService = QuixCartService()
        quixCartService.currency = Currency.EUR
        quixCartService.items = items
        quixCartService.totalPriceWithTax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.streetAddress = "Nombre de la vía y nº"
        quixAddress.postalCode = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.firstName = "Nombre"
        quixBilling.lastName = "Apellido"

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
            "pDH/U+/gbuzXdYp84aiQKsVwdo0OluLSE7iid4fDTDsOp3Iz5PMaVkId+H/okm/59Slik6eoVuhf9S0X7utcyiYp1zqBuvvjPWiO0Nmne1/ZLwf2liuTEo6jRVTCGjokuW3KnOMHbgeoHjg5TaK6fzocze2OWBs55Luc+A4onL6/qm7Lt8dAhWkUjIcWzIE5KXyKPm4Icm16zGh5wmDou/WEtJVEedu7LsO1HfOvJro6s39Ya+e8RAaNEoQZ64f4J8kDU9KYEm6aQZrOEp/+n1wI2Vc7u/6Y/VGO2ye7649smVWFsPhgGe9L8i5wzQRI4xpVpKLKQKe2Opx7fG7FZVgy1RZ8Ye4t3KZ8qEKlpHMTriACrdB4QxcwctvbRQCWgjCqCEDwD+98eTefF1LRqh2++/xptrxrXxBl+oOiyNbIdG8ZllTmYZIDF/dIESFWQUXqxL3vAYEj4CMctMZSmb2721NohunvlobjzKnbl/LB8o2dxsCrONuhDn8AX4I+5+IErn+6ifP0cgce4p1LwQL3twThfnqWZRIz0D9O54QEVSj/MTHwTFumZHiNdHlw5tHa+rFVK26AbJ/2za7kmZtaIDbJRFzJdCzRD011DY08wqla03v4qBBdNFCBJqjE2NXnmcAELemPMC+CdnfDmyT0Asyn0vLagzVJrWcN5ntxdtojoybPT4MgafMID6scUltph1LrpSLJRYqd39gLMbNv9icUxwpQMWVWFGWH+guE+GOTDcQ3z/DYuhSmS9ApTe/cULsQHjF/J/GHqtNSOupGeMJf54VL7IDeE6dIGoufPFDyO7Zs0wNsflGQh1Ri2OjGeT4cJZRhCchy1a5oxgb4GZekJcFHFvMD8BI5onoEeqbmpT3kEc+noty+nyCB2OGfHCKQ3Pz9Lt6qTIiQARk36KROp5XfSpn5+F3P9nb8dNJEG7UyEpd12AShXkms8vihKrmshExP0ciF5+KLKyysOcJ3l4BZHSqv1xhnMVZAEyTLrYOJpddf7ND1Fq8yQTF71w06gpuJAY161jzMpZiwkzDwngasTOHIeI3KDz5ooKkg7sH/L+KroqenuhPVheN2NAL2F327Ptd6eQf8zWNgJx29UOGxdmx69CeKSS3HeioGPUDAh7py/V/AWVtv5Oob0BMpg68W3AEbEOqG/OhSwHsZ5ckPpjOgJik78w++bsMRcx9lXJ6ofwGWNv0hkgmS17KusF8LC/lcenqHK2nOaZba7ypq8Re3IYBPlDydCErszd/BxFIu2QdXyDPmURo1mlLQ6QDOIZeaNuWr/P6JT7wqe0H+gaVlDPucYLvuPvC3E6f+7BciDYkeUvRthnQrjPNaRoNNgqAFn9NP8u1pChDoHZKtMZS7QKIaCrq53SguUP4GM19ztB56ZBAqiGxm/eYbMrksCUvjZrBO/beIcFT5pcz3oCQ9lFICo9GlvGgSguzQSVnJHqFXdCEBNe7b0aEWwoCmv5hEqX/Dy0rLVNTGNYN/UvzRo8YO72hv28ChYXkOSLbZ0GfH24YF91Z8DbIpkcyfH3m7uwdIi63TAvW8zwqSH+1FVFNUEphdOzkJQA+tlO2CV6J462bIxONatQE4yGLFSDyjnSYDzTbtLXSZ/nVybn3d5ZJ7s/7YPP8+3+FltT8PuvOMACTpWUGZMcT/my78LTzvSWPIsw==",
            queryParameterSlot.captured["encrypted"]
        )
        assertEquals(
            "337825d7d4fc5a302f324ada22f6147214d827acc918b3c5bb2e7171ebac472d",
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

        mockkStatic(SecurityUtils::class)
        every { SecurityUtils.generateIV() } returns ByteArray(16) { 1 }
        every { SecurityUtils.hash256(any()) } answers { callOriginal() }
        every { SecurityUtils.base64Encode(any()) } answers { callOriginal() }
        every { SecurityUtils.applyAESPadding(any()) } answers { callOriginal() }
        every { SecurityUtils.cbcEncryption(any(), any(), any(), any()) } answers { callOriginal() }

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
        credentials.apiVersion = 5

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

        val quixArticleService = QuixArticleService()
        quixArticleService.name = "Nombre del servicio 2"
        quixArticleService.reference = "4912345678903"
        quixArticleService.endDate = "2024-12-31T23:59:59+01:00"
        quixArticleService.unitPriceWithTax = 99.0
        quixArticleService.category = Category.digital

        val quixItemCartItemService = QuixItemCartItemService()
        quixItemCartItemService.article = quixArticleService
        quixItemCartItemService.units = 1
        quixItemCartItemService.isAutoShipping = true
        quixItemCartItemService.totalPriceWithTax = 99.0

        val items: MutableList<QuixItemCartItemService> = java.util.ArrayList()
        items.add(quixItemCartItemService)

        val quixCartService = QuixCartService()
        quixCartService.currency = Currency.EUR
        quixCartService.items = items
        quixCartService.totalPriceWithTax = 99.0

        val quixAddress = QuixAddress()
        quixAddress.city = "Barcelona"
        quixAddress.setCountry(CountryCode.ES)
        quixAddress.streetAddress = "Nombre de la vía y nº"
        quixAddress.postalCode = "28003"

        val quixBilling = QuixBilling()
        quixBilling.address = quixAddress
        quixBilling.firstName = "Nombre"
        quixBilling.lastName = "Apellido"

        val quixServicePaySolExtendedData = QuixServicePaySolExtendedData()
        quixServicePaySolExtendedData.cart = quixCartService
        quixServicePaySolExtendedData.billing = quixBilling
        quixServicePaySolExtendedData.product = "instalments"

        hostedQuixService.paySolExtendedData = quixServicePaySolExtendedData

        val hostedQuixPaymentAdapter = HostedQuixPaymentAdapter(credentials)

        val exception = assertThrows<MissingFieldException> {
            hostedQuixPaymentAdapter.sendHostedQuixServiceRequest(hostedQuixService, mockedResponseListener)
        }

        assertEquals("Missing statusURL", exception.message)
    }

    @Test
    fun failInvalidAmountHosted() {

        mockkStatic(SecurityUtils::class)
        every { SecurityUtils.generateIV() } returns ByteArray(16) { 1 }
        every { SecurityUtils.hash256(any()) } answers { callOriginal() }
        every { SecurityUtils.base64Encode(any()) } answers { callOriginal() }
        every { SecurityUtils.applyAESPadding(any()) } answers { callOriginal() }
        every { SecurityUtils.cbcEncryption(any(), any(), any(), any()) } answers { callOriginal() }

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
        credentials.apiVersion = 5

        val hostedQuixService = HostedQuixService()

        val exception = assertThrows<InvalidFieldException> {
            hostedQuixService.amount = "99,1123"
        }

        assertEquals("amount: Should Follow Format #.#### And Be Between 0 And 1000000", exception.message)
    }
}