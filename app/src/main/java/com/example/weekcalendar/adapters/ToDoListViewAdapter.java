package com.example.weekcalendar.adapters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import android.annotation.SuppressLint;
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
import com.example.weekcalendar.activities.ActivityToDoListPage;
import com.example.weekcalendar.customclasses.CustomDay;
import com.example.weekcalendar.customclasses.CustomToDo;

public class ToDoListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ActivityToDoListPage a;
    private List<CustomDay> expandableListTitle;
    private Map<CustomDay, List<CustomToDo>> expandableListDetail;
    private final Set<Pair<Long, Long>> toggledItems = new HashSet<>();
    private Set<CheckBox> myCheckBoxes = new HashSet<>();

    private final Set<Pair<Long, Long>> itemsToDelete = new HashSet<>();
    private Set<CheckBox> myDeletedCheckBoxes = new HashSet<>();

    public ToDoListViewAdapter(Context context, List<CustomDay> expandableListTitle,
                               Map<CustomDay, List<CustomToDo>> expandableListDetail) {
        this.context = context;
        this.a = (ActivityToDoListPage) context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
    }

    @Override
    public CustomToDo getChild(int listPosition, int expandedListPosition) {
        return Objects.requireNonNull(this.expandableListDetail
                .get(this.expandableListTitle.get(listPosition)))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        CustomToDo toDo = getChild(listPosition, expandedListPosition);
        final String expandedListText = toDo.getTitle();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = Objects.requireNonNull(layoutInflater).inflate(R.layout.list_item, null);
        }
        CheckBox expandedListTextView = convertView.findViewById(R.id.list_child);
        expandedListTextView.setText(expandedListText);
        expandedListTextView.setChecked(toDo.getCompleted());

        expandedListTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!a.canDelete) {
                    Intent i = new Intent(context, ActivityCreateToDoPage.class);
                    i.putExtra("todo", toDo);
                    context.startActivity(i);
                    return true;
                } else {
                    return false;
                }
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
                if (a.canDelete) {
                    expandedListTextView.setChecked(toDo.getCompleted());
                    expandedListTextView.jumpDrawablesToCurrentState();
                    if (itemsToDelete.contains(tag)) {
                        expandedListTextView.setSelected(false);
                        itemsToDelete.remove(tag);
                        myDeletedCheckBoxes.remove(expandedListTextView);
                    } else {
                        expandedListTextView.setSelected(true);
                        itemsToDelete.add(tag);
                        myDeletedCheckBoxes.add(expandedListTextView);
                    }
                } else {
                    if (toggledItems.contains(tag)) {
                        toggledItems.remove(tag);
                    } else {
                        toggledItems.add(tag);
                    }

                    if (cb.isChecked()) {
                        myCheckBoxes.add(cb);
                    } else {
                        myCheckBoxes.remove(cb);
                    }
                    toDo.toggleComplete();
                }
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return Objects.requireNonNull(this.expandableListDetail
                .get(this.expandableListTitle.get(listPosition)))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle == null ? 0 : this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = getGroup(listPosition).toString();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = Objects.requireNonNull(layoutInflater).inflate(R.layout.list_group, null);
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

    public Set<Pair<Long, Long>> getToggledItems() {
        return this.toggledItems;
    }

    public Set<Pair<Long, Long>> getItemsToDelete() {
        return this.itemsToDelete;
    }

    public void remove(int groupPos, int childPos) {
        CustomDay group = this.expandableListTitle.get(groupPos);
        List<CustomToDo> toDos = this.expandableListDetail.get(group);
        Objects.requireNonNull(toDos).remove(childPos);
        if (this.getChildrenCount(groupPos) == 0) {
            this.expandableListTitle.remove(groupPos);
        }
        if (this.expandableListTitle.size() == 0) {
            setNewItems(new ArrayList<>());
        } else {
            setNewItems(this.expandableListTitle);
        }
    }

    public void setNewItems(List<CustomDay> listOfCat) {
        this.expandableListTitle = listOfCat;
        notifyDataSetChanged();
    }

    public void resetCheckBoxes() {
        for (CheckBox box : this.myCheckBoxes) {
            box.setChecked(false);
        }
        for (CheckBox box : this.myDeletedCheckBoxes) {
            box.setSelected(false);
        }
        this.myCheckBoxes.clear();
        this.myDeletedCheckBoxes.clear();
    }
}