package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletAnalytics
import com.asfoundation.wallet.billing.analytics.WalletEventSender
import com.asfoundation.wallet.interact.WalletModel
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class RestoreWalletPasswordPresenter(private val view: RestoreWalletPasswordView,
                                     private val activityView: RestoreWalletActivityView,
                                     private val interactor: RestoreWalletPasswordInteractor,
                                     private val walletEventSender: WalletEventSender,
                                     private val disposable: CompositeDisposable,
                                     private val viewScheduler: Scheduler,
                                     private val networkScheduler: Scheduler,
                                     private val computationScheduler: Scheduler) {

  fun present(keystore: String) {
    populateUi(keystore)
    handleRestoreWalletButtonClicked(keystore)
  }

  private fun populateUi(keystore: String) {
    disposable.add(interactor.extractWalletAddress(keystore)
        .subscribeOn(networkScheduler)
        .flatMap { address ->
          interactor.getOverallBalance(address)
              .observeOn(viewScheduler)
              .doOnSuccess { fiatValue -> view.updateUi(address, fiatValue) }
        }
        .subscribe())
  }

  private fun handleRestoreWalletButtonClicked(keystore: String) {
    disposable.add(view.restoreWalletButtonClick()
        .doOnNext {
          activityView.hideKeyboard()
          view.showWalletRestoreAnimation()
        }
        .doOnNext {
          walletEventSender.sendWalletPasswordRestoreEvent(WalletAnalytics.ACTION_IMPORT,
              WalletAnalytics.STATUS_SUCCESS)
        }
        .doOnError { t ->
          walletEventSender.sendWalletPasswordRestoreEvent(WalletAnalytics.ACTION_IMPORT,
              WalletAnalytics.STATUS_FAIL, t.message)
        }
        .observeOn(computationScheduler)
        .flatMapSingle { interactor.restoreWallet(keystore, it) }
        .observeOn(viewScheduler)
        .doOnNext { handleWalletModel(it) }
        .doOnNext {
          walletEventSender.sendWalletCompleteRestoreEvent(WalletAnalytics.STATUS_SUCCESS)
        }
        .doOnError { t ->
          walletEventSender.sendWalletCompleteRestoreEvent(WalletAnalytics.STATUS_FAIL, t.message)
        }
        .subscribe())
  }

  private fun setDefaultWallet(address: String) {
    disposable.add(interactor.setDefaultWallet(address)
        .doOnComplete { view.showWalletRestoredAnimation() }
        .subscribe())
  }

  private fun handleWalletModel(walletModel: WalletModel) {
    if (walletModel.error.hasError) {
      view.hideAnimation()
      view.showError(walletModel.error.type)
    } else {
      setDefaultWallet(walletModel.address)
    }
  }

  fun stop() {
    disposable.clear()
  }
}