package com.ruoyu.pigroad.myapplication.Ui;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.ruoyu.pigroad.myapplication.Adapter.OnItemClickLitener;
import com.ruoyu.pigroad.myapplication.Adapter.PersonalBillAdapter;
import com.ruoyu.pigroad.myapplication.Bean.BillBean;
import com.ruoyu.pigroad.myapplication.Login.LoginActivity;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Util.ActivityManager;
import com.ruoyu.pigroad.myapplication.Util.Toastutil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.ruoyu.pigroad.myapplication.Config.Config_API.API_URL;
import static com.ruoyu.pigroad.myapplication.Config.Config_API.LOG_TIP;

/**
 * Created by PIGROAD on 2018/9/18
 * Email:920015363@qq.com
 */
public class PersonalBillManager extends AppCompatActivity{

    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.tv_titile)
    TextView tv_title;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.ll_bill)
    LinearLayout ll_bill;
    @BindView(R.id.ll_add_bill)
    LinearLayout ll_add_bill;

    private PersonalBillAdapter adapter;
    private List<BillBean> datas=new ArrayList<>();
    private static final int REQUEST_CODE_CHEAT = 0;
    private String login_token;
    private ActivityManager activityManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_bill_manager_layout);
        ButterKnife.bind(this);
        activityManager=ActivityManager.getInstance();
        activityManager.addActivity(this);
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_title.setText("发票抬头管理");

        //拿token
        SharedPreferences sp = getSharedPreferences("USER", MODE_PRIVATE);
        login_token=sp.getString("user_token",null);

        adapter=new PersonalBillAdapter(datas,this);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        //适配器  点击事件
        adapter.setOnItemClickLitener(new OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                ShowWindow(datas.get(position).getId(),position);
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        //添加发票
        ll_add_bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(PersonalBillManager.this,AddBillActivity.class);
                startActivityForResult(intent,REQUEST_CODE_CHEAT);
            }
        });

    }


    /**
     * 点击长按   弹出 窗口 事件
     */
    public void ShowWindow(final int id, final int postion){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] items={getString(R.string.address_edit),getString(R.string.address_delete),"设为默认发票"};
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Intent intent=new Intent(PersonalBillManager.this,EditBillActivity.class);
                        intent.putExtra("bill_id",id+"");
                        intent.putExtra("bill_title",datas.get(postion).getBill_title());
                        intent.putExtra("bill_number",datas.get(postion).getBill_number());
                        intent.putExtra("is_select",datas.get(postion).getIs_select()+"");
                        startActivity(intent);
                        break;
                    case 1:
                        DeleteBill(id);
                        break;
                    case 2:
                        ChangeBill(datas.get(postion).getId(),datas.get(postion).getBill_title(),datas.get(postion).getBill_number());
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * 删除发票操作
     */
    private void DeleteBill(int id){
        OkGo.<String>post(API_URL+"?request=private.invoice.action.del.invoice&platform=app&id="+id+"&token="+login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            String msg=jsonObject.getString("msg");
                            if (code ==0){
                                Toastutil.show(PersonalBillManager.this,msg);
                                CheckBill();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(PersonalBillManager.this, LoginActivity.class);
                                startActivity(intent4);
                            }else {
                                Toastutil.show(PersonalBillManager.this,msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * 查询发票
     */
    private void CheckBill(){
        datas.clear();
        OkGo.<String>post(API_URL+"?request=private.invoice.get.invoice.list&platform=app&token="+login_token)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            if (code == 0){
                                JSONArray data=jsonObject.getJSONArray("data");
                                if (data.length() == 0){
                                    ll_bill.setVisibility(View.GONE);
                                }else {
                                    ll_bill.setVisibility(View.VISIBLE);
                                }
                                for (int i=0;i<data.length();i++){
                                    JSONObject temp=data.getJSONObject(i);
                                    BillBean bean=new BillBean();
                                    bean.setBill_number(temp.getString("tax_number"));
                                    bean.setBill_title(temp.getString("company_name"));
                                    bean.setIs_select(temp.getInt("is_select"));
                                    bean.setId(temp.getInt("id"));
                                    if (temp.getInt("is_select") == 1){
                                        adapter.setSelection(i);
                                    }
                                    datas.add(bean);
                                }
                                adapter.notifyDataSetChanged();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(PersonalBillManager.this, LoginActivity.class);
                                startActivity(intent4);
                            }else {
                                ll_bill.setVisibility(View.GONE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 返回重新获取数据
     */
    @Override
    protected void onResume() {
        super.onResume();
        CheckBill();
    }

    //编辑 发票 保存 操作
    private void ChangeBill(int id,String company_name,String tax_number){
        OkGo.<String>post(API_URL+"?request=private.invoice.action.edit.invoice&token="+login_token+"&platform=app&id="+id+"&company_name="+company_name+"&tax_number="+tax_number+"&is_select=1")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i(LOG_TIP,response.body());
                        try {
                            JSONObject jsonObject=new JSONObject(response.body());
                            int code =jsonObject.getInt("code");
                            String msg=jsonObject.getString("msg");
                            if (code == 0){
                                Toastutil.show(PersonalBillManager.this,"已设为默认发票抬头");
                                CheckBill();
                            }else if (code == 999){
                                activityManager.exit();
                                //clear token
                                SharedPreferences preferences = getSharedPreferences("USER", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                //返回登录界面
                                Intent intent4=new Intent(PersonalBillManager.this, LoginActivity.class);
                                startActivity(intent4);
                            }else {
                                Toastutil.show(PersonalBillManager.this,msg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
