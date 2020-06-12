package com.asfoundation.wallet.topup

import com.asfoundation.wallet.topup.paymentMethods.PaymentMethodData
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import java.math.BigDecimal

interface TopUpFragmentView {

  fun getChangeCurrencyClick(): Observable<Any>
  fun getEditTextChanges(): Observable<TopUpData>
  fun getPaymentMethodClick(): Observable<String>
  fun getNextClick(): Observable<TopUpData>
  fun setupPaymentMethods(paymentMethods: List<PaymentMethodData>)
  fun setupCurrency(localCurrency: LocalCurrency)
  fun setConversionValue(topUpData: TopUpData)
  fun switchCurrencyData()
  fun setNextButtonState(enabled: Boolean)
  fun hideLoadingButton()
  fun showLoadingButton()
  fun rotateChangeCurrencyButton()
  fun toggleSwitchCurrencyOn()
  fun toggleSwitchCurrencyOff()
  fun hideBonus()
  fun setBonus(bonus: BigDecimal, currency: String)
  fun showBonus()
  fun showMaxValueWarning(value: String)
  fun showMinValueWarning(value: String)
  fun hideValueInputWarning()
  fun changeMainValueColor(isValid: Boolean)
  fun changeMainValueText(value: String)
  fun getSelectedCurrency(): String
  fun paymentMethodsFocusRequest()
  fun disableSwapCurrencyButton()
  fun enableSwapCurrencyButton()
  fun showNoNetworkError()
  fun showRetryAnimation()
  fun retryClick(): Observable<Any>
  fun getValuesClicks(): Observable<FiatValue>
  fun setValuesAdapter(values: List<FiatValue>)
  fun showValuesAdapter()
  fun hideValuesAdapter()
  fun getKeyboardEvents(): Observable<Boolean>
  fun setDefaultAmountValue(amount: String)
  fun removeBonus()
}
