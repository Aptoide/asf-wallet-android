package com.asfoundation.wallet.ui.widget.adapter;

import android.os.Bundle;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.asf.wallet.R;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.ui.widget.holder.BinderViewHolder;
import com.asfoundation.wallet.ui.widget.holder.WalletHolder;

public class WalletsAdapter extends RecyclerView.Adapter<BinderViewHolder> {

  private final OnSetWalletDefaultListener onSetWalletDefaultListener;
  private final OnWalletDeleteListener onWalletDeleteListener;
  private final OnExportWalletListener onExportWalletListener;

  private Wallet[] wallets = new Wallet[0];

  private Wallet defaultWallet = null;

  public WalletsAdapter(OnSetWalletDefaultListener onSetWalletDefaultListener,
      OnWalletDeleteListener onWalletDeleteListener,
      OnExportWalletListener onExportWalletListener) {
    this.onSetWalletDefaultListener = onSetWalletDefaultListener;
    this.onWalletDeleteListener = onWalletDeleteListener;
    this.onExportWalletListener = onExportWalletListener;
  }

  @Override public BinderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    BinderViewHolder binderViewHolder = null;
    if (viewType == WalletHolder.VIEW_TYPE) {
      WalletHolder h = new WalletHolder(R.layout.item_wallet_manage, parent);
      h.setOnSetWalletDefaultListener(onSetWalletDefaultListener);
      h.setOnWalletDeleteListener(onWalletDeleteListener);
      h.setOnExportWalletListener(onExportWalletListener);
      binderViewHolder = h;
    }
    return binderViewHolder;
  }

  @Override public void onBindViewHolder(BinderViewHolder holder, int position) {
    if (getItemViewType(position) == WalletHolder.VIEW_TYPE) {
      Wallet wallet = wallets[position];
      Bundle bundle = new Bundle();
      bundle.putBoolean(WalletHolder.IS_DEFAULT_ADDITION,
          defaultWallet != null && defaultWallet.sameAddress(wallet.address));
      bundle.putBoolean(WalletHolder.IS_LAST_ITEM, getItemCount() == 1);
      holder.bind(wallet, bundle);
    }
  }

  @Override public int getItemViewType(int position) {
    return WalletHolder.VIEW_TYPE;
  }

  @Override public int getItemCount() {
    return wallets.length;
  }

  public void setWallets(Wallet[] wallets) {
    this.wallets = wallets == null ? new Wallet[0] : wallets;
    notifyDataSetChanged();
  }

  public Wallet getDefaultWallet() {
    return defaultWallet;
  }

  public void setDefaultWallet(Wallet wallet) {
    this.defaultWallet = wallet;
    notifyDataSetChanged();
  }

  public interface OnSetWalletDefaultListener {
    void onSetDefault(Wallet wallet);
  }

  public interface OnWalletDeleteListener {
    void onDelete(Wallet delete);
  }

  public interface OnExportWalletListener {
    void onExport(Wallet wallet);
  }
}
