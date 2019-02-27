package com.ruoyu.pigroad.myapplication.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ruoyu.pigroad.myapplication.Bean.RecordBean;
import com.ruoyu.pigroad.myapplication.R;

import java.util.List;

/**
 * Created by PIGROAD on 2018/9/11
 * Email:920015363@qq.com
 */
public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder>{

    private Context context;
    private List<RecordBean> dadtas;

    public RecordAdapter(Context context,List<RecordBean> dadtas){
        this.context=context;
        this.dadtas=dadtas;
    }

    @Override
    public RecordAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.record_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecordAdapter.ViewHolder holder, int position) {
        holder.tv_oil_coupon.setText("￥优惠"+dadtas.get(position).getDiscount_money());
        holder.tv_oil_name.setText(dadtas.get(position).getName());
        holder.tv_oil_real_pay.setText("实付"+dadtas.get(position).getPay_money());
        holder.tv_oil_time.setText(dadtas.get(position).getPay_time());
    }

    @Override
    public int getItemCount() {
        return dadtas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_oil_name,tv_oil_time,tv_oil_coupon,tv_oil_real_pay;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_oil_name=itemView.findViewById(R.id.tv_oil_name);
            tv_oil_time=itemView.findViewById(R.id.tv_oil_time);
            tv_oil_coupon=itemView.findViewById(R.id.tv_oil_coupon);
            tv_oil_real_pay=itemView.findViewById(R.id.tv_oil_real_pay);
        }
    }
}
