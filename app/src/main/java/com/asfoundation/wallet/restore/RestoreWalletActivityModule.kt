package com.asfoundation.wallet.restore

import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
class RestoreWalletActivityModule {

  @Provides
  fun providesRestoreWalletActivityPresenter(activity: RestoreWalletActivity,
                                             walletsEventSender: WalletsEventSender,
                                             navigator: RestoreWalletActivityNavigator): RestoreWalletActivityPresenter {
    return RestoreWalletActivityPresenter(activity as RestoreWalletActivityView, walletsEventSender,
        navigator)
  }

  @Provides
  fun providesRestoreWalletActivityNavigator(
      activity: RestoreWalletActivity): RestoreWalletActivityNavigator {
    return RestoreWalletActivityNavigator(activity, activity.supportFragmentManager)
  }
}