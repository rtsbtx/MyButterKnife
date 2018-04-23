package com.demo.mybutterknife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.demo.mybutterknife.lib.MyButterKnife;
import com.demo.mybutterknife.lib.annotation.BindView;
import com.demo.mybutterknife.lib.annotation.Unbinder;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.textView)
    TextView textView;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        unbinder = MyButterKnife.bindViews(this);

        textView.setText("wang ba dan");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
