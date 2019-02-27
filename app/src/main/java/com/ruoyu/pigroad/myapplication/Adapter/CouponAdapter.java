package com.ruoyu.pigroad.myapplication.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ruoyu.pigroad.myapplication.Bean.CouponBean;
import com.ruoyu.pigroad.myapplication.R;

import java.util.List;

/**
 * Created by PIGROAD on 2018/9/10
 * Email:920015363@qq.com
 */
public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.ViewHolder>{

    private List<CouponBean> datas;
    private Context context;
    private OnItemClickLitener mOnItemClickLitener;
    private int selected = -1;

    public CouponAdapter(Context context,List<CouponBean> datas){
        this.datas=datas;
        this.context=context;
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void setSelection(int position){
        this.selected = position;
        notifyDataSetChanged();
    }

    @Override
    public CouponAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.coupon_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CouponAdapter.ViewHolder holder, int position) {
        if(holder instanceof ViewHolder) {
            final ViewHolder viewHolder = holder;

            if (selected == position) {
                viewHolder.radioButton.setChecked(true);
                viewHolder.itemView.setSelected(true);
            } else {
                viewHolder.radioButton.setChecked(false);
                viewHolder.itemView.setSelected(false);
            }

            if (mOnItemClickLitener != null) {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickLitener.onItemClick(viewHolder.itemView, viewHolder.getAdapterPosition());
                    }
                });
            }
        }

        holder.tv_coupon_name.setText(datas.get(position).getCoupon_name());
        holder.tv_coupon_money.setText("￥"+datas.get(position).getCoupon_money());
        holder.tv_coupon_time.setText(datas.get(position).getStart_time()+"至"+datas.get(position).getExpire_time());

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_coupon_money,tv_coupon_name,tv_coupon_time;
        RadioButton radioButton;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_coupon_money=itemView.findViewById(R.id.tv_coupon_money);
            tv_coupon_name=itemView.findViewById(R.id.tv_coupon_name);
            tv_coupon_time=itemView.findViewById(R.id.tv_coupon_time);
            radioButton=itemView.findViewById(R.id.rb);
        }
    }
}
