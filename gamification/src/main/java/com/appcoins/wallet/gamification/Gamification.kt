package com.appcoins.wallet.gamification

import com.appcoins.wallet.gamification.repository.ForecastBonus
import com.appcoins.wallet.gamification.repository.GamificationRepository
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal

class Gamification(private val repository: GamificationRepository) {
  private var forecastBonus: ForecastBonus? = null
  fun getUserStatus(wallet: String): Single<UserStats> {
    return repository.getUserStatus(wallet)
  }

  fun getLevels(): Single<Levels> {
    return repository.getLevels()
  }

  fun getEarningBonus(wallet: String, packageName: String,
                      amount: BigDecimal): Single<ForecastBonus> {
    if (forecastBonus == null) {
      return repository.getForecastBonus(wallet, packageName, amount)
          .doOnSuccess { this.forecastBonus = it }
    }
    return Single.just(forecastBonus)
  }

  fun hasNewLevel(wallet: String): Single<Boolean> {
    return Single.zip(repository.getLastShownLevel(wallet), getUserStatus(wallet),
        BiFunction { lastShownLevel: Int, userStats: UserStats ->
          userStats.status == UserStats.Status.OK && lastShownLevel < userStats.level
        })
  }

  fun levelShown(wallet: String, level: Int): Completable {
    return repository.shownLevel(wallet, level)
  }
}
