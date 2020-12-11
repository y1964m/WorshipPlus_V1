package com.hanarae.administrator.worshipplus;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class Help extends AppCompatActivity {

    ViewPager viewPager;
    TextView skip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        if(MainActivity.currentMode == AppCompatDelegate.MODE_NIGHT_NO) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        viewPager = findViewById(R.id.viewpager_help);
        final HelpAdapter helpAdapter = new HelpAdapter(getApplicationContext());
        viewPager.setAdapter(helpAdapter);
        Toast.makeText(getApplicationContext(),"옆으로 넘기기",Toast.LENGTH_SHORT).show();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0: Toast.makeText(getApplicationContext(),"보기 화면",Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        break;
                    case 2: Toast.makeText(getApplicationContext(),"콘티 작성",Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6: Toast.makeText(getApplicationContext(),"악보 화면",Toast.LENGTH_SHORT).show();
                        break;
                    case 7: Toast.makeText(getApplicationContext(),"검색 화면",Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        skip = findViewById(R.id.textView_help);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public class HelpAdapter extends PagerAdapter {

        private Context mContext = null ;

        public HelpAdapter(Context context){
            mContext = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = null ;

            if (mContext != null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.help_imageview, container, false);
                ImageView imageView = view.findViewById(R.id.imageview_help);
                switch (position){
                    case 0: imageView.setImageResource(R.drawable.help1);
                        break;
                    case 1: imageView.setImageResource(R.drawable.help2);
                        break;
                    case 2: imageView.setImageResource(R.drawable.help3);
                        break;
                    case 3: imageView.setImageResource(R.drawable.help4);
                        break;
                    case 4: imageView.setImageResource(R.drawable.help5);
                        break;
                    case 5: imageView.setImageResource(R.drawable.help6);
                        break;
                    case 6: imageView.setImageResource(R.drawable.help7);
                        break;
                    case 7: imageView.setImageResource(R.drawable.help8);
                        break;
                }
            }

            // 뷰페이저에 추가.
            container.addView(view);
            return view ;
        }

        @Override
        public int getCount() {
            return 8;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            // 뷰페이저에서 삭제.
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return (view == object);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
