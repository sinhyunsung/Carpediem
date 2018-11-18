package edu.dongyang.cs.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class itemAdapter extends BaseAdapter {
    public ArrayList<ItemList> itemLists = new ArrayList<>();
    DBHelper dbHelper;

    String [] send = new String[30];
    @Override
    public int getCount() {
        return itemLists.size();
    }

    @Override
    public ItemList getItem(int i) {
        return itemLists.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class ViewHolder {
        TextView name;
        TextView phone;
        CheckBox check;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();
        ViewHolder holder = null;
        dbHelper = new DBHelper(context);

        Log.v("ConvertView", String.valueOf(i));
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.contact_row, parent, false);

            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.name);
            holder.phone = convertView.findViewById(R.id.phone);
            holder.check = convertView.findViewById(R.id.check);
            convertView.setTag(holder);

            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        CheckBox cb = (CheckBox) v;
                        ItemList itemList = (ItemList) cb.getTag();
                        itemList.setSelected(cb.isChecked());
                        if (cb.isChecked())  {
                            itemLists.get(i).setSelected(true);
                            dbHelper.insert(itemList.getName(), itemList.getPhone(), true);
                            Toast.makeText(context, itemList.getName() + " 등록되었습니다.", Toast.LENGTH_SHORT).show();
                        }else {
                            itemLists.get(i).setSelected(false);
                            dbHelper.delete(itemList.getPhone());
                            Toast.makeText(context, itemList.getName() + " 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "다시 시도해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ItemList itemList = itemLists.get(i);
        holder.name.setText(itemList.getName());
        holder.phone.setText(itemList.getPhone());
        holder.check.setChecked(itemList.isSelected());
        holder.check.setTag(itemList);
        return convertView;
    }

    public void addItem(String name, String phone, boolean check) {

        ItemList itemList = new ItemList(name, phone, check);

        /* MyItem에 아이템을 setting한다. */
        itemList.setName(name);
        itemList.setPhone(phone);
        itemList.setSelected(check);

        /* mItems에 MyItem을 추가한다. */
        itemLists.add(itemList);
    }
}

