package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.thirdparty.S;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Adapter.FaceCarAdapter;
import com.ruoyu.pigroad.myapplication.Adapter.OnItemClickLitener;
import com.ruoyu.pigroad.myapplication.Bean.FaceCarBean;
import com.ruoyu.pigroad.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

public class FaceCarActivity extends AppCompatActivity {

    @BindView(R.id.tv_titile)
    TextView tv_titile;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.tv_order_number)
    TextView tv_order_number;
    @BindView(R.id.tv_order_time)
    TextView tv_order_time;
    @BindView(R.id.tv_order_money)
    TextView tv_order_money;
    @BindView(R.id.tv_order_oilgun)
    TextView tv_order_oilgun;
    @BindView(R.id.tv_oil_type)
    TextView tv_oil_type;
    @BindView(R.id.btn_pay)
    Button btn_pay;

    private int id;
    private String login_token;

    private List<FaceCarBean> datas = new ArrayList<>();
    private FaceCarAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_car_layout);
        ButterKnife.bind(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token = sp.getString("user_token", null);
        id = getIntent().getIntExtra("id", 0);
        tv_titile.setText("订单支付");
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adapter = new FaceCarAdapter(this, datas);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

        getData();

        adapter.setOnItemClickLitener(new OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.setSelection(position);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getSelection() < 0){
                    Toast.makeText(FaceCarActivity.this,"请选择卡进行付款",Toast.LENGTH_SHORT).show();
                }else {
                    Pay(datas.get(adapter.getSelection()).getCar_id());
                }
            }
        });

    }


    /**
     * 获取数据
     */
    private void getData() {
        Log.i(LOG_TIP,API_URL + "?request=private.card.get_pay_face_card_info&platform=app&token=" + login_token + "&id=" + id);
        OkGo.<String>post(API_URL + "?request=private.card.get_pay_face_card_info&platform=app&token=" + login_token + "&id=" + id)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            int code = jsonObject.getInt("code");
                            if (code == 0) {
                                JSONObject order=jsonObject.getJSONObject("order");
                                tv_order_number.setText("订单编号："+order.getString("orderid"));
                                tv_oil_type.setText("油品："+order.getString("oil_name"));
                                tv_order_money.setText("支付金额："+order.getString("need_payment"));
                                tv_order_oilgun.setText("油枪："+order.getString("gun_id")+"号油枪");
                                tv_order_time.setText("下单时间："+order.getString("order_time"));
                                JSONArray card=jsonObject.getJSONArray("card");
                                for (int i=0;i<card.length();i++){
                                    JSONObject temp=card.getJSONObject(i);
                                    FaceCarBean bean=new FaceCarBean();
                                    bean.setCar_name("NO."+temp.getString("card_no")+"  "+temp.getString("type_name"));
                                    bean.setCar_money("余额："+temp.getString("credit"));
                                    bean.setCar_id(temp.getString("card_no"));
                                    datas.add(bean);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 付款操作
     */
    private void Pay(final String card_no){
        OkGo.<String>post(API_URL+"?request=private.card.pay.card.for.face&platform=app&token="+login_token+"&id="+id+"&card_no="+card_no)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONObject order=jsonObject.getJSONObject("data");
                                Intent intent=new Intent(FaceCarActivity.this,PayFinishActivity.class);
                                intent.putExtra("credit",order.getString("credit"));
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(FaceCarActivity.this,jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

}
