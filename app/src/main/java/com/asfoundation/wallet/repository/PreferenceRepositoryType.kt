package com.asfoundation.wallet.repository

interface PreferenceRepositoryType {

  fun hasCompletedOnboarding(): Boolean
  fun setOnboardingComplete()
  fun hasClickedSkipOnboarding(): Boolean
  fun setOnboardingSkipClicked()
  fun getCurrentWalletAddress(): String?
  fun setCurrentWalletAddress(address: String)
  fun isFirstTimeOnTransactionActivity(): Boolean
  fun setFirstTimeOnTransactionActivity()

  fun setPoaNotificationSeenTime(currentTimeInMillis: Long)
  fun getPoaNotifiationSeenTime(): Long
  fun clearPoaNotificationSeenTime()
}
