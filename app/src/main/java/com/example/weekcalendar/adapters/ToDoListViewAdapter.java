package com.example.weekcalendar.adapters;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.weekcalendar.R;
import com.example.weekcalendar.activities.ActivityCreateToDoPage;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.customclasses.CustomToDo;

public class ToDoListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<CustomDay> expandableListTitle;
    private Map<CustomDay, List<CustomToDo>> expandableListDetail;
    private final Set<Pair<Long, Long>> checkedItems = new HashSet<>();

    private Set<CheckBox> myCheckBoxes = new HashSet<>();

    public ToDoListViewAdapter(Context context, List<CustomDay> expandableListTitle,
                               Map<CustomDay, List<CustomToDo>> expandableListDetail) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public CustomToDo getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        CustomToDo toDo = getChild(listPosition, expandedListPosition);
        final String expandedListText = toDo.getDetails();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        TextView expandedListTextView = (CheckBox) convertView.findViewById(R.id.list_child);
        expandedListTextView.setText(expandedListText);

        expandedListTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(context, ActivityCreateToDoPage.class);
                i.putExtra("todo", toDo);
                context.startActivity(i);
                return true;
            }
        });

        Pair<Long, Long> tag = new Pair<>(getGroupId(listPosition),
                getChildId(listPosition, expandedListPosition));
        expandedListTextView.setTag(tag);
        expandedListTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                final Pair<Long, Long> tag = (Pair<Long, Long>) v.getTag();
                if (cb.isChecked()) {
                    checkedItems.add(tag);
                    myCheckBoxes.add(cb);
                } else {
                    checkedItems.remove(tag);
                    myCheckBoxes.remove(cb);
                }
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail
                .get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = getGroup(listPosition).toString();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = convertView.findViewById(R.id.list_parent);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }

    public Set<Pair<Long, Long>> getCheckedItems() {
        return checkedItems;
    }

    public void remove(int groupPos, int childPos) {
        CustomDay group = this.expandableListTitle.get(groupPos);
        List<CustomToDo> toDos = this.expandableListDetail.get(group);
        toDos.remove(childPos);
        if (this.getChildrenCount(groupPos) == 0) {
            this.expandableListTitle.remove(groupPos);
        }
        setNewItems(this.expandableListTitle);
    }

    public void setNewItems(List<CustomDay> listOfCat) {
        this.expandableListTitle = listOfCat;
        notifyDataSetChanged();
    }

    public void resetCheckBoxes() {
        for (CheckBox box : this.myCheckBoxes) {
            box.setChecked(false);
        }
        this.myCheckBoxes.clear();
    }
}