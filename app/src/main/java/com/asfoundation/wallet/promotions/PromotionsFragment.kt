package com.asfoundation.wallet.promotions

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import com.asf.wallet.R
import com.asfoundation.wallet.repository.SharedPreferencesRepository
import com.asfoundation.wallet.ui.gamification.GamificationMapper
import com.asfoundation.wallet.ui.widget.MarginItemDecoration
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.viewmodel.BasePageViewFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_promotions.*
import kotlinx.android.synthetic.main.gamification_info_bottom_sheet.*
import kotlinx.android.synthetic.main.no_network_retry_only_layout.*
import javax.inject.Inject

class PromotionsFragment : BasePageViewFragment(), PromotionsView {

  @Inject
  lateinit var promotionsInteractor: PromotionsInteractorContract

  @Inject
  lateinit var formatter: CurrencyFormatUtils

  @Inject
  lateinit var mapper: GamificationMapper

  @Inject
  lateinit var preferences: SharedPreferencesRepository

  private lateinit var activityView: PromotionsActivityView
  private lateinit var presenter: PromotionsFragmentPresenter
  private lateinit var adapter: PromotionsAdapter
  private lateinit var detailsBottomSheet: BottomSheetBehavior<View>
  private var clickListener: PublishSubject<PromotionClick>? = null
  private var onBackPressedSubject: PublishSubject<Any>? = null

  companion object {
    fun newInstance() = PromotionsFragment()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    clickListener = PublishSubject.create()
    onBackPressedSubject = PublishSubject.create()
    presenter =
        PromotionsFragmentPresenter(this, activityView, promotionsInteractor, preferences,
            CompositeDisposable(),
            Schedulers.io(), AndroidSchedulers.mainThread())
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    require(
        context is PromotionsActivityView) { PromotionsFragment::class.java.simpleName + " needs to be attached to a " + PromotionsActivityView::class.java.simpleName }
    activityView = context
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_promotions, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    detailsBottomSheet = BottomSheetBehavior.from(bottom_sheet_fragment_container)
    detailsBottomSheet.addBottomSheetCallback(
        object : BottomSheetBehavior.BottomSheetCallback() {
          override fun onStateChanged(bottomSheet: View, newState: Int) = Unit

          override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (slideOffset == 0f) bottomsheet_coordinator_container.visibility = GONE
            bottomsheet_coordinator_container.background.alpha = (255 * slideOffset).toInt()
          }
        })
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }

  override fun showPromotions(promotionsModel: PromotionsModel) {
    adapter = PromotionsAdapter(promotionsModel.promotions, clickListener!!)
    rv_promotions.addItemDecoration(
        MarginItemDecoration(resources.getDimension(R.dimen.promotions_item_margin)
            .toInt()))
    rv_promotions.visibility = VISIBLE
    rv_promotions.adapter = adapter
  }

  override fun showLoading() {
    promotions_progress_bar.visibility = VISIBLE
  }

  override fun retryClick() = RxView.clicks(retry_button)

  override fun getPromotionClicks() = clickListener!!

  override fun showNetworkErrorView() {
    no_promotions.visibility = GONE
    no_network.visibility = VISIBLE
    retry_button.visibility = VISIBLE
    retry_animation.visibility = GONE
  }

  override fun hideNetworkErrorView() {
    no_network.visibility = GONE
  }

  override fun showNoPromotionsScreen() {
    no_network.visibility = GONE
    retry_animation.visibility = GONE
    no_promotions.visibility = VISIBLE
  }

  override fun showRetryAnimation() {
    retry_button.visibility = INVISIBLE
    retry_animation.visibility = VISIBLE
  }

  override fun hideLoading() {
    promotions_progress_bar.visibility = INVISIBLE
  }

  override fun hidePromotions() {
    rv_promotions.visibility = GONE
  }

  override fun getHomeBackPressed() = activityView.backPressed()

  override fun handleBackPressed() {
    // Currently we only call the hide bottom sheet
    // but maybe later additional stuff needs to be handled
    hideBottomSheet()
  }

  override fun getBottomSheetButtonClick() = RxView.clicks(got_it_button)

  override fun getBackPressed() = onBackPressedSubject!!

  override fun hideBottomSheet() {
    detailsBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
    disableBackListener(bottomsheet_coordinator_container)
  }

  override fun showBottomSheet() {
    detailsBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
    bottomsheet_coordinator_container.visibility = VISIBLE
    bottomsheet_coordinator_container.background.alpha = 255
    setBackListener(bottomsheet_coordinator_container)
  }

  override fun getBottomSheetContainerClick() = RxView.clicks(bottomsheet_coordinator_container)

  private fun setBackListener(view: View) {
    activityView.disableBack()
    view.apply {
      isFocusableInTouchMode = true
      requestFocus()
      setOnKeyListener { _, keyCode, keyEvent ->
        if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
          if (detailsBottomSheet.state == BottomSheetBehavior.STATE_EXPANDED)
            onBackPressedSubject?.onNext("")
        }
        true
      }
    }
  }

  private fun disableBackListener(view: View) {
    activityView.enableBack()
    view.apply {
      isFocusableInTouchMode = false
      setOnKeyListener(null)
    }
  }
}
