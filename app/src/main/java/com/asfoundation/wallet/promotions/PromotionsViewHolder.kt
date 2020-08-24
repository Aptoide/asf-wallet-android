package com.asfoundation.wallet.promotions

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.GlideApp
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_promotions_default.view.*
import kotlinx.android.synthetic.main.item_promotions_gamification.view.*
import kotlinx.android.synthetic.main.item_promotions_progress.view.*
import kotlinx.android.synthetic.main.item_promotions_referrals.view.*
import kotlinx.android.synthetic.main.item_promotions_title.view.*
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

abstract class PromotionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  abstract fun bind(promotion: Promotion)

  protected fun handleExpiryDate(textView: TextView, containerDate: LinearLayout, endDate: Long) {
    val currentTime = TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    val diff: Long = endDate - currentTime
    val days = TimeUnit.DAYS.convert(diff, TimeUnit.SECONDS)

    if (days > 3) {
      containerDate.visibility = View.GONE
    } else {
      containerDate.visibility = View.VISIBLE
      textView.text = itemView.context.getString(R.string.perks_end_in_days, days.toString())
    }
  }

}

class TitleViewHolder(itemView: View) : PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val titleItem = promotion as TitleItem

    val title = if (titleItem.isGamificationTitle) {
      itemView.context.getString(titleItem.title, titleItem.bonus)
    } else itemView.context.getString(titleItem.title)
    itemView.promotions_title.text = title
    itemView.promotions_subtitle.setText(titleItem.subtitle)
  }

}

class ProgressViewHolder(itemView: View,
                         private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val progressItem = promotion as ProgressItem

    itemView.setOnClickListener {
      clickListener.onNext(PromotionClick((promotion.id)))
    }

    GlideApp.with(itemView.context)
        .load(progressItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.progress_icon)

    itemView.progress_title.text = progressItem.title
    itemView.progress_current.progress = progressItem.current.toInt()
    itemView.progress_current.max = progressItem.objective.toInt()

    handleExpiryDate(itemView.progress_expiry_date, itemView.progress_container_date,
        progressItem.endDate)
  }

}

class DefaultViewHolder(itemView: View,
                        private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val defaultItem = promotion as DefaultItem

    itemView.setOnClickListener {
      clickListener.onNext(PromotionClick((promotion.id)))
    }

    GlideApp.with(itemView.context)
        .load(defaultItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.default_icon)

    itemView.default_title.text = defaultItem.title
    handleExpiryDate(itemView.default_expiry_date, itemView.default_container_date,
        defaultItem.endDate)
  }

}

class FutureViewHolder(itemView: View,
                       private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val futureItem = promotion as FutureItem

    itemView.setOnClickListener {
      clickListener.onNext(PromotionClick((promotion.id)))
    }

    GlideApp.with(itemView.context)
        .load(futureItem.icon)
        .error(R.drawable.ic_promotions_default)
        .circleCrop()
        .into(itemView.default_icon)

    itemView.default_title.text = futureItem.title
  }

}

class ReferralViewHolder(itemView: View,
                         private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  override fun bind(promotion: Promotion) {
    val referralItem = promotion as ReferralItem

    itemView.setOnClickListener {
      clickListener.onNext(PromotionClick((promotion.id)))
    }

    val formatter = CurrencyFormatUtils.create()
    val bonus = formatter.formatCurrency(referralItem.bonus, WalletCurrency.FIAT)

    val subtitle = itemView.context.getString(R.string.promotions_referral_card_title,
        referralItem.currency + bonus)

    itemView.referral_subtitle.text = subtitle
  }

}

class GamificationViewHolder(itemView: View,
                             private val clickListener: PublishSubject<PromotionClick>) :
    PromotionsViewHolder(itemView) {

  private var mapper = GamificationMapper(itemView.context)

  override fun bind(promotion: Promotion) {
    val gamificationItem = promotion as GamificationItem
    val df = DecimalFormat("###.#")

    itemView.setOnClickListener {
      clickListener.onNext(PromotionClick((promotion.id)))
    }

    itemView.planet.setImageDrawable(gamificationItem.planet)
    itemView.current_level_bonus.background = mapper.getOvalBackground(gamificationItem.level)
    itemView.current_level_bonus.text =
        itemView.context?.getString(R.string.gamif_bonus, df.format(gamificationItem.bonus))
    itemView.planet_title.text = gamificationItem.title
    itemView.planet_subtitle.text = gamificationItem.phrase//TODO replace with

    handleLinks(gamificationItem.links, itemView)
  }

  private fun handleLinks(links: List<GamificationLinkItem>, itemView: View) {
    if (links.isEmpty()) {
      itemView.linked_perks.visibility = View.GONE
    } else {
      itemView.linked_perks.visibility = View.VISIBLE
      val adapter = PromotionsGamificationAdapter(links)
      itemView.linked_perks.adapter = adapter
    }
  }
}