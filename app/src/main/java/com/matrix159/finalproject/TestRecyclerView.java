
package com.matrix159.finalproject;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/** RECYCLER VIEW TO MESS AROUND WITH */
public class TestRecyclerView extends RecyclerView.Adapter<TestRecyclerView.ViewHolder>
{

    private List<MyItem> myItems;
    private ItemListener myListener;

    public TestRecyclerView(List<MyItem> items, ItemListener listener)
    {
        myItems = items;
        myListener = listener;
    }

    public void setListener(ItemListener listener)
    {
        myListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        //return new ViewHolder(LayoutInflater.from(parent.getContext())
         //       .inflate(R.layout.LAYOUT, parent, false)); // TODO
        return null;
    }

    @Override
    public int getItemCount()
    {
        return myItems.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        holder.setData(myItems.get(position));
    }

    public interface ItemListener
    {
        void onItemClick(MyItem item);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {

        // TODO - Your view members
        public MyItem item;

        public ViewHolder(View itemView)
        {
            super(itemView);
            itemView.setOnClickListener(this);
            // TODO instantiate/assign view members
        }

        public void setData(MyItem item)
        {
            this.item = item;
            // TODO set data to view
        }

        @Override
        public void onClick(View v)
        {
            if (myListener != null)
            {
                myListener.onItemClick(item);
            }
        }
    }


}
                                