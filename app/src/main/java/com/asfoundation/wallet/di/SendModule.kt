package com.asfoundation.wallet.di

import com.asfoundation.wallet.interact.FetchGasSettingsInteract
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.router.ConfirmationRouter
import com.asfoundation.wallet.router.TransactionsRouter
import com.asfoundation.wallet.util.TransferParser
import com.asfoundation.wallet.viewmodel.SendViewModelFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import io.reactivex.subjects.PublishSubject

@InstallIn(ActivityComponent::class)
@Module
class SendModule {
  @Provides
  fun provideSendViewModelFactory(findDefaultWalletInteract: FindDefaultWalletInteract,
                                  confirmationRouter: ConfirmationRouter,
                                  fetchGasSettingsInteract: FetchGasSettingsInteract,
                                  transferParser: TransferParser,
                                  transactionsRouter: TransactionsRouter): SendViewModelFactory {
    return SendViewModelFactory(findDefaultWalletInteract, fetchGasSettingsInteract,
        confirmationRouter, transferParser, transactionsRouter)
  }

  @Provides
  fun provideConfirmationRouter() = ConfirmationRouter(PublishSubject.create())
}