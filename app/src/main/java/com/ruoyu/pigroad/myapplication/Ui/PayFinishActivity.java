package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruoyu.pigroad.myapplication.MainActivity2;
import com.ruoyu.pigroad.myapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by PIGROAD on 2018/9/25
 * Email:920015363@qq.com
 */
public class PayFinishActivity extends AppCompatActivity{

    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.btn_return)
    Button btn_return;
    @BindView(R.id.tv_result)
    TextView tv_result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_finish_layout);
        ButterKnife.bind(this);
        this.init();
    }

    private void init() {

        tv_title.setText("付款结果");

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent=new Intent(PayFinishActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });

        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent=new Intent(PayFinishActivity.this, MainActivity2.class);
                startActivity(intent);
            }
        });

        tv_result.setText("当前卡里余额："+getIntent().getStringExtra("credit"));

    }


}
