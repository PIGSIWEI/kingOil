package com.ruoyu.pigroad.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ruoyu.pigroad.myapplication.Bean.CouponManageBean;
import com.ruoyu.pigroad.myapplication.Bean.CouponManageBean2;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Ui.CouponDetailActivity;

import java.util.List;

/**
 * Created by PIGROAD on 2018/9/13
 * Email:920015363@qq.com
 */
public class CouponManageUsedAdapter extends RecyclerView.Adapter<CouponManageUsedAdapter.ViewHolder>{

    private Context context;
    private List<CouponManageBean2> datas;

    public CouponManageUsedAdapter(Context context, List<CouponManageBean2> datas){
        this.context=context;
        this.datas=datas;
    }

    @NonNull
    @Override
    public CouponManageUsedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.coupon_used_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponManageUsedAdapter.ViewHolder holder, final int position) {
        holder.tv_coupon_time.setText(datas.get(position).getStart_time()+"至"+datas.get(position).getExpire_time());
        holder.tv_coupon_title.setText(datas.get(position).getCoupon_name());
        holder.tv_min_money.setText("满"+datas.get(position).getMin_money());
        holder.tv_title_info.setText("满"+datas.get(position).getMin_money()+"元可用，不含团购");

        holder.ll_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, CouponDetailActivity.class);
                intent.putExtra("id",datas.get(position).getId()+"");
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_min_money,tv_coupon_title,tv_title_info,tv_coupon_time;
        LinearLayout ll_btn;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_min_money=itemView.findViewById(R.id.tv_min_money);
            tv_coupon_title=itemView.findViewById(R.id.tv_coupon_title);
            tv_title_info=itemView.findViewById(R.id.tv_title_info);
            tv_coupon_time=itemView.findViewById(R.id.tv_coupon_time);
            ll_btn=itemView.findViewById(R.id.ll_btn);

        }
    }
}
