package dev.nick.tiles.tile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import dev.nick.tiles.R;

public class DropDownTileView extends TileView {

    Spinner mSpinner;

    public DropDownTileView(Context context) {
        super(context);
    }

    public DropDownTileView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initDropdown() {
        mSpinner = new Spinner(getContext());
        mSpinner.setVisibility(INVISIBLE);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                onDropdownItemSelected(position, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // noop
            }
        });

    }

    public void onDropdownItemSelected(int position, boolean fromSpinner) {

    }


    @Override
    protected void onBindActionView(RelativeLayout container) {
        initDropdown();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, onCreateDropDownList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        int dropDownWidth = getResources().getDimensionPixelSize(R.dimen.drop_down_width);
        container.addView(mSpinner, generateCenterParams(dropDownWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    protected List<String> onCreateDropDownList() {
        List<String> list = new ArrayList<String>();
        list.add("Android");
        list.add("Blackberry");
        list.add("Cherry");
        list.add("Duck");
        list.add("Female");
        return list;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        mSpinner.performClick();
    }
}
