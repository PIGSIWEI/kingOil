package com.ruoyu.pigroad.myapplication.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ruoyu.pigroad.myapplication.Bean.OilDetailBean;
import com.ruoyu.pigroad.myapplication.R;

import java.util.List;

/**
 * Created by PIGROAD on 2018/9/19
 * Email:920015363@qq.com
 */
public class OilDetailAdapter extends RecyclerView.Adapter<OilDetailAdapter.ViewHolder>{

    private Context context;
    private List<OilDetailBean> datas;

    public OilDetailAdapter(Context context, List<OilDetailBean> datas){
        this.context=context;
        this.datas=datas;
    }

    @NonNull
    @Override
    public OilDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.oil_detail_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OilDetailAdapter.ViewHolder holder, int position) {
            holder.tv_countryw_oil_price.getPaint().setFlags(Paint. STRIKE_THRU_TEXT_FLAG); //中划线
            holder.tv_oil_type.setText(datas.get(position).getOil_name());
            holder.tv_countryw_oil_price.setText("市场价"+datas.get(position).getCountry_oil_price()+"元");
            holder.tv_merchant_oil_price.setText(datas.get(position).getMerchant_oil_price()+"元");

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_merchant_oil_price,tv_countryw_oil_price,tv_oil_type;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_oil_type=itemView.findViewById(R.id.tv_oil_type);
            tv_countryw_oil_price=itemView.findViewById(R.id.tv_countryw_oil_price);
            tv_merchant_oil_price=itemView.findViewById(R.id.tv_merchant_oil_price);
        }
    }
}
