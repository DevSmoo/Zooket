package smo.zooket.testC;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import smo.zooket.Models.Category;
import smo.zooket.R;

/**
 * Created by Smo on 15/05/2017.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {

    public interface OnItemClickListener {
        void onItemClick(Category item);
    }

    ArrayList<Category> mylist;
    private Context context;
    public static int selected_item = 0;
    private final OnItemClickListener listener;


    //create constructor to initializ context and data sent from main activity.
    public MyAdapter(ArrayList<Category> mylist, Context context,OnItemClickListener listener) {
        this.context=context;
        this.mylist = mylist;
        this.listener=listener;
    }


    @Override
    public MyAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab_text, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyHolder holder, final int position) {

        holder.textData.setText(mylist.get(position).getName().toString());
holder.myBackground.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        selected_item=position;
        notifyDataSetChanged();
        listener.onItemClick(mylist.get(position));
    }
});
        if(position == selected_item)
        {
            holder.textData.setTextColor(context.getResources().getColor(R.color.Red));
        }
        else
        {
            holder.textData.setTextColor(context.getResources().getColor(R.color.White));
        }

//holder.ll.setOnClickListener(new View.OnClickListener() {
  //  @Override
  //  public void onClick(View v) {
         //
    //    int iff=holder.getAdapterPosition();
     //   if(holder.getAdapterPosition()==position){
     ////       holder.textData.setTextColor(context.getResources().getColor(R.color.ColorPrimaryDark));
    //    }else{
      //      holder.textData.setTextColor(context.getResources().getColor(R.color.ColorPrimary));
     //   }
   // }
//});

    }

    @Override
    public int getItemCount() {
        return mylist.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        TextView textData;
        LinearLayout myBackground;
        //contructor for getting reference to the widget
        public MyHolder(View itemView) {
            super(itemView);
            myBackground=(LinearLayout)itemView.findViewById(R.id.holderTxt);
            textData = (TextView) itemView.findViewById(R.id.textdata);
        }
    }
}

