package se.simulator.findmycar_gpstracker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SpinAdapter extends ArrayAdapter<ListItem>{
    private ListItem[] items;

    public SpinAdapter(Context context, int textViewResourceId, ListItem[] items){
        super(context,textViewResourceId,items);
        this.items = items;
    }

    public int getCount(){
        return items.length;
    }

    public ListItem getItem(int position){
        return items[position];
    }

    public long getItemId(int position){
        return position;
    }
}
