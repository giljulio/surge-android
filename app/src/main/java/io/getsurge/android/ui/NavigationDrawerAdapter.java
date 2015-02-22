package io.getsurge.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import io.getsurge.android.R;

import java.util.List;

import io.getsurge.android.model.DrawerHeader;
import io.getsurge.android.model.DrawerItem;
import io.getsurge.android.model.NavigationItem;
import io.getsurge.android.net.Volley;
import io.getsurge.android.utils.IntentUtils;

/**
 * Created by poliveira on 24/10/2014.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DrawerItem> mData;
    private NavigationDrawerCallbacks mNavigationDrawerCallbacks;
    private int mSelectedPosition;
    private int mSelectedColor = Color.BLACK;
    private Activity mActivity;

    public NavigationDrawerAdapter(Activity activity, List<DrawerItem> data) {
        this.mActivity = activity;
        mData = data;
    }

    public NavigationDrawerCallbacks getNavigationDrawerCallbacks() {
        return mNavigationDrawerCallbacks;
    }

    public void setNavigationDrawerCallbacks(NavigationDrawerCallbacks navigationDrawerCallbacks) {
        mNavigationDrawerCallbacks = navigationDrawerCallbacks;
    }

    public int getSelectedColor() {
        return mSelectedColor;
    }

    public void setSelectedColor(int mSelectedColor) {
        this.mSelectedColor = mSelectedColor;
    }

    public List<DrawerItem> getData() {
        return mData;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if(viewType == 0) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_row, viewGroup, false);
            return new RowViewHolder(v);
        } else if(viewType == 1){
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_header, viewGroup, false);
            return new HeaderViewHolder(v);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0)
            return 1;
        return 0;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int i) {
        if(viewHolder instanceof RowViewHolder) {
            NavigationItem navigationItem = (NavigationItem) mData.get(i);
            RowViewHolder rowViewHolder = (RowViewHolder)viewHolder;
            rowViewHolder.textView.setText(navigationItem.getText());
            rowViewHolder.imageView.setImageResource(navigationItem.getThumbnail());
            rowViewHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNavigationDrawerCallbacks != null)
                        mNavigationDrawerCallbacks.onNavigationDrawerItemSelected(i);
                }
            });

            if (mSelectedPosition == i) {
                viewHolder.itemView.setBackgroundColor(mActivity.getResources().getColor(R.color.light_grey));
                rowViewHolder.imageView.getDrawable().setColorFilter(mSelectedColor, PorterDuff.Mode.SRC_ATOP);
                rowViewHolder.textView.setTextColor(mSelectedColor);
                rowViewHolder.textView.setTypeface(null, Typeface.BOLD);
            } else {
                rowViewHolder.imageView.setColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY);
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
                rowViewHolder.textView.setTextColor(Color.BLACK);
                rowViewHolder.textView.setTypeface(null, Typeface.NORMAL);
            }
        } else if(viewHolder instanceof HeaderViewHolder){
            DrawerHeader drawerHeader = (DrawerHeader) mData.get(i);
            HeaderViewHolder headerViewHolder = (HeaderViewHolder)viewHolder;
            headerViewHolder.background.setImageUrl("http://puu.sh/g6dYn/2135074573.png", Volley.getInstance(mActivity).getImageLoader());
            if(drawerHeader.isSignedIn()) {
                headerViewHolder.accountLabelView.setText(drawerHeader.getUsername());
            } else {
                headerViewHolder.accountLabelView.setText("Sign in");
            }
            headerViewHolder.background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Account[] accounts = AccountManager.get(mActivity).getAccounts();
                    if(accounts.length > 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setMessage("Are you sure you want to logout?")
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        AccountManager.get(mActivity).removeAccount(accounts[0], null, null);
                                    }
                                })
                                .show();
                    } else {
                        IntentUtils.openSignUpAndSignInActivity(mActivity);
                    }
                }
            });
        }
    }

    public void selectPosition(int position) {
        int lastPosition = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(lastPosition);
        notifyItemChanged(position);
    }

    public int getSelectedPosition(){
        return mSelectedPosition;
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public static class RowViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;

        public RowViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_name);
            imageView = (ImageView) itemView.findViewById(R.id.item_thumbnail);
        }

        public void setOnClickListener(View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }
    }
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {

        View accountContainer;
        TextView accountLabelView;
        NetworkImageView background;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            accountContainer = itemView.findViewById(R.id.account_container);
            accountLabelView = (TextView) itemView.findViewById(R.id.account_label);
            background = (NetworkImageView) itemView.findViewById(R.id.background);
        }

    }
}
