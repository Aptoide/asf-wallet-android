package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import com.asfoundation.wallet.ui.transact.TransferFragmentView.Currency
import com.asfoundation.wallet.ui.transact.TransferFragmentView.TransferData
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.QRUri
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class TransferPresenter(private val view: TransferFragmentView,
                        private val disposables: CompositeDisposable,
                        private val onResumeDisposables: CompositeDisposable,
                        private val interactor: TransferInteractor,
                        private val ioScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val walletInteract: FindDefaultWalletInteract,
                        private val walletBlockedInteract: WalletBlockedInteract,
                        private val packageName: String,
                        private val formatter: CurrencyFormatUtils) {

  fun onResume() {
    handleQrCodeResult()
    handleCurrencyChange()
  }

  fun present() {
    handleButtonClick()
    handleQrCodeButtonClick()
  }

  private fun handleCurrencyChange() {
    onResumeDisposables.add(view.getCurrencyChange()
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .switchMapSingle { currency ->
          getBalance(currency)
              .observeOn(viewScheduler)
              .doOnSuccess {
                val walletCurrency = WalletCurrency.mapToWalletCurrency(currency)
                view.showBalance(formatter.formatCurrency(it, walletCurrency), walletCurrency)
              }
        }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun getBalance(currency: Currency): Single<BigDecimal> {
    return when (currency) {
      Currency.APPC_C -> interactor.getCreditsBalance()
      Currency.APPC -> interactor.getAppcoinsBalance()
      Currency.ETH -> interactor.getEthBalance()
    }
  }

  private fun handleQrCodeResult() {
    onResumeDisposables.add(view.getQrCodeResult()
        .observeOn(ioScheduler)
        .map { QRUri.parse(it.displayValue) }
        .observeOn(viewScheduler)
        .doOnNext { handleQRUri(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleQRUri(qrUri: QRUri) {
    if (qrUri.address != BarcodeCaptureActivity.ERROR_CODE) {
      view.showAddress(qrUri.address)
    } else {
      view.showCameraErrorToast()
    }
  }

  private fun handleQrCodeButtonClick() {
    disposables.add(view.getQrCodeButtonClick()
        .doOnNext { view.showQrCodeScreen() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun shouldBlockTransfer(currency: Currency): Single<Boolean> {
    return if (currency == Currency.APPC_C) {
      walletBlockedInteract.isWalletBlocked()
    } else {
      Single.just(false)
    }
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick()
        .doOnNext { view.showLoading() }
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .flatMapCompletable { data ->
          shouldBlockTransfer(data.currency)
              .flatMapCompletable {
                if (it) {
                  Completable.fromAction { view.showWalletBlocked() }
                      .subscribeOn(viewScheduler)
                } else {
                  makeTransaction(data)
                      .observeOn(viewScheduler)
                      .flatMapCompletable { status ->
                        handleTransferResult(data.currency, status, data.walletAddress,
                            data.amount)
                      }
                      .andThen { view.hideLoading() }
                }
              }
        }
        .doOnError { handleError(it) }
        .retry()
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleError(throwable: Throwable) {
    view.hideLoading()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    } else {
      throwable.printStackTrace()
      view.showUnknownError()
    }
  }

  private fun makeTransaction(data: TransferData): Single<Status> {
    return when (data.currency) {
      Currency.APPC_C -> handleCreditsTransfer(data.walletAddress, data.amount)
      Currency.ETH -> interactor.validateEthTransferData(data.walletAddress, data.amount)
      Currency.APPC -> interactor.validateAppcTransferData(data.walletAddress, data.amount)
    }
  }

  private fun handleTransferResult(currency: Currency, status: Status,
                                   walletAddress: String, amount: BigDecimal): Completable {
    return Single.just(status)
        .subscribeOn(viewScheduler)
        .flatMapCompletable {
          when (status) {
            Status.API_ERROR, Status.UNKNOWN_ERROR, Status.NO_INTERNET -> Completable.fromCallable { view.showUnknownError() }
            Status.SUCCESS -> handleSuccess(currency, walletAddress, amount)
            Status.INVALID_AMOUNT -> Completable.fromCallable { view.showInvalidAmountError() }
            Status.INVALID_WALLET_ADDRESS -> Completable.fromCallable { view.showInvalidWalletAddress() }
            Status.NOT_ENOUGH_FUNDS -> Completable.fromCallable { view.showNotEnoughFunds() }
          }
        }
  }

  private fun handleSuccess(currency: Currency, walletAddress: String,
                            amount: BigDecimal): Completable {
    return when (currency) {
      Currency.APPC_C -> view.openAppcCreditsConfirmationView(walletAddress, amount, currency)
      Currency.APPC -> walletInteract.find()
          .flatMapCompletable { view.openAppcConfirmationView(it.address, walletAddress, amount) }
      Currency.ETH -> walletInteract.find()
          .flatMapCompletable { view.openEthConfirmationView(it.address, walletAddress, amount) }
    }
  }

  private fun handleCreditsTransfer(walletAddress: String,
                                    amount: BigDecimal): Single<Status> {
    return Single.zip(Single.timer(1, TimeUnit.SECONDS),
        interactor.transferCredits(walletAddress, amount, packageName),
        BiFunction { _: Long, status: Status -> status })
  }

  fun clearOnPause() = onResumeDisposables.clear()

  fun stop() = disposables.clear()
}