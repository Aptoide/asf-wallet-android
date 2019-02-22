package com.appcoins.wallet.appcoins.rewards.repository

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import io.reactivex.Completable
import io.reactivex.Single
import java.math.BigDecimal

class BdsAppcoinsRewardsRepository(private val remoteRepository: RemoteRepository) :
    AppcoinsRewardsRepository {
  override fun sendCredits(toWallet: String, walletAddress: String, signature: String,
                           amount: BigDecimal,
                           origin: String, type: String, packageName: String): Completable {
    return remoteRepository.sendCredits(toWallet, walletAddress, signature, amount, origin, type,
        packageName)
  }

  override fun getBalance(address: String): Single<BigDecimal> {
    return remoteRepository.getBalance(address).map { it.balance }
  }

  override fun pay(walletAddress: String, signature: String,
                   amount: BigDecimal,
                   origin: String?,
                   sku: String?,
                   type: String,
                   developerAddress: String,
                   storeAddress: String,
                   oemAddress: String,
                   packageName: String,
                   payload: String?,
                   callback: String?,
                   orderReference: String?): Single<Transaction> {
    return remoteRepository.pay(walletAddress, signature, amount, origin, sku,
        type, developerAddress, storeAddress, oemAddress, packageName, payload, callback,
        orderReference)
  }
}