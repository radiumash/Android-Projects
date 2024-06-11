package com.rightone.ROChildWater;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class SearchableEditText extends android.support.v7.widget.AppCompatEditText implements View.OnTouchListener, SearchableListDialog.SearchableItem{

    String selectedItem;
    Integer selectedItemPosition;
    //this string above will store the value of selected item.

    public static final int NO_ITEM_SELECTED = -1;
    private Context _context;
    private List _items;
    private SearchableListDialog _searchableListDialog;

    private boolean _isDirty;
    private ArrayAdapter _arrayAdapter;
    private String _strHintText;
    private boolean _isFromInit;

    public SearchableEditText(Context context) {
        super(context);
        this._context = context;
        init();
    }

    public SearchableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this._context = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchableSpinner);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.SearchableSpinner_hintText) {
                _strHintText = a.getString(attr);
            }
        }
        a.recycle();
        init();
    }

    public SearchableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this._context = context;
        init();
    }

    private void init() {
        _items = new ArrayList();
        _searchableListDialog = SearchableListDialog.newInstance
                (_items);
        _searchableListDialog.setOnSearchableItemClickListener(this);
        setOnTouchListener(this);

        _arrayAdapter = (ArrayAdapter) getAdapter();
        if (!TextUtils.isEmpty(_strHintText)) {
            ArrayAdapter arrayAdapter = new ArrayAdapter(_context, android.R.layout
                    .simple_list_item_1, new String[]{_strHintText});
            _isFromInit = true;
            setAdapter(arrayAdapter);
        }
    }

    public ArrayAdapter getAdapter() {
        return _arrayAdapter;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (null != _arrayAdapter) {

                // Refresh content #6
                // Change Start
                // Description: The items were only set initially, not reloading the data in the
                // spinner every time it is loaded with items in the adapter.
                _items.clear();
                for (int i = 0; i < _arrayAdapter.getCount(); i++) {
                    _items.add(_arrayAdapter.getItem(i));
                }
                // Change end.

                _searchableListDialog.show(scanForActivity(_context).getFragmentManager(), "TAG");
            }
        }
        return true;
    }

    public void setAdapter(ArrayAdapter _arrayAdapter) {
        this._arrayAdapter = null;
        this._arrayAdapter = _arrayAdapter;
        setText("");
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);

    }

    @Override
    public void onSearchableItemClicked(Object item, int position) {
        setText(item.toString());

        if (!_isDirty) {
            _isDirty = true;
            setAdapter(_arrayAdapter);
            setText(item.toString());
        }
        selectedItem= getItemAtPosition(position).toString();
        selectedItemPosition = position;

        Toast.makeText(getContext(),"You selected "+item.toString(),Toast.LENGTH_SHORT).show();
    }

    private Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());

        return null;
    }

    public void setSelectedItemPosition(Object item) {
        if (!_isDirty) {
            _isDirty = true;
            setAdapter(_arrayAdapter);
            setText(item.toString());
        }
        selectedItem= item.toString();
        selectedItemPosition = getPositionAtText(item.toString());
    }

    public int getSelectedItemPosition() {
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return NO_ITEM_SELECTED;
        } else {
            return this._arrayAdapter.getPosition(selectedItem);
        }
    }

    public Object getSelectedItem() {
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return null;
        } else {
            return this._arrayAdapter.getItem(selectedItemPosition);
        }
    }

    public Object getItemAtPosition(Integer position){
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return null;
        } else {
            return this._arrayAdapter.getItem(position);
        }
    }

    public Integer getPositionAtText(String text){
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return null;
        } else {
            return this._arrayAdapter.getPosition(text);
        }
    }
}
