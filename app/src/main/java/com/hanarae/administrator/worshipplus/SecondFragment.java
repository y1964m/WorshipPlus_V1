package com.hanarae.administrator.worshipplus;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;


public class SecondFragment extends Fragment {
    // Store instance variables
    static EditText editText_bible;
    static EditText editText_title1;
    static EditText editText_title2;
    LinearLayout linearLayout;
    InputMethodManager imm;

    // newInstance constructor for creating fragment with arguments
    public static SecondFragment newInstance(int page, String title) {
        SecondFragment fragment = new SecondFragment();
        Bundle args = MainActivity.args;
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragment.setArguments(args);
        return fragment;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_input_second, container, false);
        editText_bible= (EditText)view.findViewById(R.id.editText_bible);
        editText_title1= (EditText)view.findViewById(R.id.editText_title1);
        editText_title2= (EditText)view.findViewById(R.id.editText_title2);

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        linearLayout = view.findViewById(R.id.linear_layout_second);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(v.getWindowToken(),0);
            }
        });



//실시간으로 정보 반영하는 리스너
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                getArguments().putString("someBible", editText_bible.getText().toString());
                getArguments().putString("someTitle1", editText_title1.getText().toString());
                getArguments().putString("someTitle2", editText_title2.getText().toString());
                if(ThirdFragment.conti_info!=null) {
                    ThirdFragment.state_code = 0;
                    ThirdFragment.conti_info.setVisibility(View.GONE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        editText_bible.addTextChangedListener(watcher);
        editText_title1.addTextChangedListener(watcher);
        editText_title2.addTextChangedListener(watcher);

        return view;
    }
}

