package com.asfoundation.wallet.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.asf.wallet.R;

public class AddWalletView extends FrameLayout implements View.OnClickListener {

  public static int ANIMATION_TRANSITIONS = 3;
  private OnNewWalletClickListener onNewWalletClickListener;
  private OnImportWalletClickListener onImportWalletClickListener;

  public AddWalletView(Context context) {
    this(context, R.layout.layout_dialog_add_account);
  }

  public AddWalletView(Context context, @LayoutRes int layoutId) {
    super(context);

    init(layoutId);
  }

  private void init(@LayoutRes int layoutId) {
    View addWalletView = LayoutInflater.from(getContext())
        .inflate(layoutId, this, true);
    if (layoutId == R.layout.layout_dialog_add_account) {
      findViewById(R.id.import_account_action).setOnClickListener(this);
      findViewById(R.id.new_account_action).setOnClickListener(this);
    }
    if (layoutId == R.layout.layout_onboarding) {
      findViewById(R.id.skip_action).setOnClickListener(this);
      findViewById(R.id.ok_action).setOnClickListener(this);

      String termsConditions = getResources().getString(R.string.terms_and_conditions);
      String privacyPolicy = getResources().getString(R.string.privacy_policy);
      String termsPolicyTickBox =
          getResources().getString(R.string.terms_and_conditions_tickbox, termsConditions,
              privacyPolicy);

      SpannableString spannableString = new SpannableString(termsPolicyTickBox);
      setLinkToString(spannableString, termsConditions, "https://appcoins.io/");
      setLinkToString(spannableString, privacyPolicy, "https://appcoins.io/");

      TextView textView = findViewById(R.id.terms_conditions_body);
      textView.setText(spannableString);
      textView.setClickable(true);
      textView.setMovementMethod(LinkMovementMethod.getInstance());

      ViewPager viewPager = findViewById(R.id.intro);
      if (viewPager != null) {
        viewPager.setPageTransformer(false, new DepthPageTransformer());
        viewPager.setAdapter(new IntroPagerAdapter());
        viewPager.addOnPageChangeListener(new PageChangeListener(addWalletView));
      }
    }
  }

  @Override public void onClick(View view) {
    switch (view.getId()) {
      case R.id.new_account_action: {
        if (onNewWalletClickListener != null) {
          onNewWalletClickListener.onNewWallet(view);
        }
      }
      break;
      case R.id.import_account_action: {
        if (onImportWalletClickListener != null) {
          onImportWalletClickListener.onImportWallet(view);
        }
      }
      break;
      case R.id.skip_action: {
        ViewPager viewPager = findViewById(R.id.intro);
        viewPager.setCurrentItem(ANIMATION_TRANSITIONS);
      }
      break;
    }
  }

  private void setLinkToString(SpannableString spannableString, String highlightString,
      String uri) {
    ClickableSpan clickableSpan = new ClickableSpan() {
      @Override public void onClick(@NonNull View widget) {
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        getContext().startActivity(launchBrowser);
      }

      @Override public void updateDrawState(@NonNull TextPaint ds) {
        ds.setColor(getResources().getColor(R.color.grey_8a_alpha));
        ds.setUnderlineText(true);
      }
    };
    int indexHighlightString = spannableString.toString()
        .indexOf(highlightString);
    int highlightStringLength = highlightString.length();
    spannableString.setSpan(clickableSpan, indexHighlightString,
        indexHighlightString + highlightStringLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    spannableString.setSpan(new StyleSpan(Typeface.BOLD), indexHighlightString,
        indexHighlightString + highlightStringLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  public void setOnNewWalletClickListener(OnNewWalletClickListener onNewWalletClickListener) {
    this.onNewWalletClickListener = onNewWalletClickListener;
  }

  public void setOnImportWalletClickListener(
      OnImportWalletClickListener onImportWalletClickListener) {
    this.onImportWalletClickListener = onImportWalletClickListener;
  }

  public interface OnNewWalletClickListener {
    void onNewWallet(View view);
  }

  public interface OnImportWalletClickListener {
    void onImportWallet(View view);
  }

  private static class IntroPagerAdapter extends PagerAdapter {
    private int[] titles = new int[] {
        R.string.intro_title_first_page, R.string.intro_2_title, R.string.intro_3_title,
        R.string.intro_4_title
    };
    private int[] messages = new int[] {
        R.string.intro_1_body, R.string.intro_2_body, R.string.intro_3_body, R.string.intro_4_body
    };

    @Override public int getCount() {
      return titles.length;
    }

    @NonNull @Override public Object instantiateItem(@NonNull ViewGroup container, int position) {
      View view = LayoutInflater.from(container.getContext())
          .inflate(R.layout.layout_page_intro, container, false);
      ((TextView) view.findViewById(R.id.title)).setText(titles[position]);
      ((TextView) view.findViewById(R.id.message)).setText(messages[position]);
      container.addView(view);
      return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
      container.removeView((View) object);
    }

    @Override public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
      return view == object;
    }
  }

  private static class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
      int pageWidth = view.getWidth();

      if (position < -1) { // [-Infinity,-1)
        // This page is way off-screen to the left.
        view.setAlpha(0);
      } else if (position <= 0) { // [-1,0]
        // Use the default slide transition when moving to the left page
        view.setAlpha(1);
        view.setTranslationX(0);
        view.setScaleX(1);
        view.setScaleY(1);
      } else if (position <= 1) { // (0,1]
        // Fade the page out.
        view.setAlpha(1 - position);

        // Counteract the default slide transition
        view.setTranslationX(pageWidth * -position);

        // Scale the page down (between MIN_SCALE and 1)
        float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);
      } else { // (1,+Infinity]
        // This page is way off-screen to the right.
        view.setAlpha(0);
      }
    }
  }

  private static class PageChangeListener implements ViewPager.OnPageChangeListener {

    private View view;
    private LottieAnimationView lottieView;
    private Button skipButton;
    private Button okButton;
    private CheckBox checkBox;
    private TextView warningText;

    PageChangeListener(View view) {
      this.view = view;
      init();
    }

    public void init() {
      lottieView = view.findViewById(R.id.lottie_onboarding);
      skipButton = view.findViewById(R.id.skip_action);
      okButton = view.findViewById(R.id.ok_action);
      checkBox = view.findViewById(R.id.onboarding_checkbox);
      warningText = view.findViewById(R.id.terms_conditions_warning);
    }

    private void showWarningText(int position) {
      if (!checkBox.isChecked() && position == 3) {
        warningText.setVisibility(VISIBLE);
      } else {
        warningText.setVisibility(GONE);
      }
    }

    private void showSkipButton(int position) {
      if (position != 3 && checkBox.isChecked()) {
        skipButton.setVisibility(VISIBLE);
      } else {
        skipButton.setVisibility(GONE);
      }
    }

    private void showOkButton(int position) {
      if (checkBox.isChecked() && position == 3) {
        okButton.setVisibility(VISIBLE);
      } else {
        okButton.setVisibility(GONE);
      }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      lottieView.setProgress((position * (1f / ANIMATION_TRANSITIONS)) + (positionOffset * (1f
          / ANIMATION_TRANSITIONS)));
      checkBox.setOnClickListener(view -> {
        showWarningText(position);
        showSkipButton(position);
        showOkButton(position);
      });
      showWarningText(position);
      showSkipButton(position);
      showOkButton(position);
    }

    @Override public void onPageSelected(int position) {

    }

    @Override public void onPageScrollStateChanged(int state) {

    }
  }
}
