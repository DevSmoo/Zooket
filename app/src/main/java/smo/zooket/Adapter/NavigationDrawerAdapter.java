package smo.zooket.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import smo.zooket.Models.DatabaseUser;
import smo.zooket.R;

/**
 * Created by Smo on 5/6/2017.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    DatabaseUser user;
    String mNavTitles[];
    int mIcons[];


    public NavigationDrawerAdapter(DatabaseUser us) {
        this.user=us;
        if (user!=null){
                mNavTitles =new String[] {"صفحه اصلی", "لیست علاقه مندی ها", "درباره ما", "ارتباط با ما","جستجوی پیشرفته","سفارش من","سبد خرید"};
                mIcons = new int[] { R.drawable.home,R.drawable.favorit, R.drawable.about, R.drawable.contact,R.drawable.search, R.drawable.myorder,R.drawable.basket};
        }
        else{
            mNavTitles =new String[] {"صفحه اصلی", "لیست علاقه مندی ها", "درباره ما", "ارتباط با ما","جستجوی پیشرفته"};
            mIcons =new int[] {R.drawable.home, R.drawable.favorit, R.drawable.about, R.drawable.contact,R.drawable.search};
        }
    }

    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigationdrawer_item, parent, false);
            ViewHolder vhItem = new ViewHolder(v, viewType);
            return vhItem;
        } else if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigationdrawer_header, parent, false);
            ViewHolder vhHeader = new ViewHolder(v, viewType);
            return vhHeader;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder holder, int position) {
        if (holder.Holderid == 1) {
            if (position == 4) {
                holder.seperator.setVisibility(View.GONE);
            } else {
                holder.seperator.setVisibility(View.GONE);
            }

            holder.textView.setText(mNavTitles[position - 1]);
            holder.imageView.setImageResource(mIcons[position - 1]);
        } else if (holder.Holderid == 2) {
            if (user == null) {
                holder.textView.setText("ورود به حساب کاربری");
                holder.email.setText("");
                holder.email.setVisibility(View.GONE);
            } else {
                holder.textView.setText(user.getName());
                holder.email.setText(user.getUsername());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mNavTitles.length + 1;
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        int Holderid;
        TextView textView;
        TextView email;
        ImageView imageView;
        View seperator;

        public ViewHolder(View itemView, int ViewType) {
            super(itemView);
            if (ViewType == TYPE_ITEM) {
                textView = (TextView) itemView.findViewById(R.id.rowText);
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                seperator = itemView.findViewById(R.id.Seperator);
                Holderid = 1;
            } else if (ViewType == TYPE_HEADER) {
                textView = (TextView) itemView.findViewById(R.id.Name);
                email = (TextView) itemView.findViewById(R.id.Email);
                Holderid = 2;
            }
        }
    }
}
