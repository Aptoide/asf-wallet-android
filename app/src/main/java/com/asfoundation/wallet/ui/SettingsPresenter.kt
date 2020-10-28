package com.asfoundation.wallet.ui

import android.content.Intent
import android.hardware.biometrics.BiometricManager
import com.asfoundation.wallet.ui.wallets.WalletsModel
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

class SettingsPresenter(private val view: SettingsView,
                        private val activityView: SettingsActivityView,
                        private val networkScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val disposables: CompositeDisposable,
                        private val settingsInteractor: SettingsInteractor) {

  fun present() {
    setFingerPrintPreference()
    handleAuthenticationResult()
    onFingerPrintPreferenceChange()
  }

  fun onResume() {
    setFingerPrintPreference(settingsInteractor.retrievePreviousFingerPrintAvailability())
    setupPreferences()
    handleRedeemPreferenceSetup()
  }

  private fun setupPreferences() {
    view.setPermissionPreference()
    view.setSourceCodePreference()
    view.setIssueReportPreference()
    view.setTwitterPreference()
    view.setTelegramPreference()
    view.setFacebookPreference()
    view.setEmailPreference()
    view.setPrivacyPolicyPreference()
    view.setTermsConditionsPreference()
    view.setCreditsPreference()
    view.setVersionPreference()
    view.setRestorePreference()
    view.setBackupPreference()
  }

  private fun setFingerPrintPreference(previousAvailability: Int = -1) {
    val newAvailability = settingsInteractor.retrieveFingerPrintAvailability()
    if (previousAvailability != newAvailability) {
      when (settingsInteractor.retrieveFingerPrintAvailability()) {
        BiometricManager.BIOMETRIC_SUCCESS -> view.setFingerprintPreference(
            settingsInteractor.hasAuthenticationPermission())
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
          settingsInteractor.changeAuthorizationPermission(false)
          view.removeFingerprintPreference()
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
          view.toggleFingerprint(false)
          settingsInteractor.changeAuthorizationPermission(false)
          view.setDisabledFingerPrintPreference()
        }
      }
    }
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.authenticationResult()
        .filter { it }
        .doOnNext {
          view.toggleFingerprint(false)
          settingsInteractor.changeAuthorizationPermission(false)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposables.dispose()

  private fun handleRedeemPreferenceSetup() {
    disposables.add(settingsInteractor.findWallet()
        .doOnSuccess { view.setRedeemCodePreference(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun onBackupPreferenceClick() {
    disposables.add(settingsInteractor.retrieveWallets()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { handleWalletModel(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleWalletModel(walletModel: WalletsModel) {
    when (walletModel.totalWallets) {
      0 -> {
        settingsInteractor.sendCreateErrorEvent()
        view.showError()
      }
      1 -> {
        settingsInteractor.sendCreateSuccessEvent()
        activityView.navigateToBackup(walletModel.walletsBalance[0].walletAddress)
      }
      else -> activityView.showWalletsBottomSheet(walletModel)
    }
  }

  fun onBugReportClicked() = settingsInteractor.displaySupportScreen()

  fun redirectToStore() {
    disposables.add(
        Single.create<Intent> { it.onSuccess(settingsInteractor.retrieveUpdateIntent()) }
            .doOnSuccess { view.navigateToIntent(it) }
            .subscribe({}, { handleError(it) }))
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    view.showError()
  }

  private fun onFingerPrintPreferenceChange() {
    disposables.add(view.switchPreferenceChange()
        .doOnNext {
          if (settingsInteractor.hasAuthenticationPermission()) activityView.showAuthentication()
          else {
            view.toggleFingerprint(true)
            settingsInteractor.changeAuthorizationPermission(true)
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }
}

