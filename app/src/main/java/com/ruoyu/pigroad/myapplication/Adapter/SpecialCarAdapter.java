package com.ruoyu.pigroad.myapplication.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ruoyu.pigroad.myapplication.Bean.SpecialBean;
import com.ruoyu.pigroad.myapplication.R;

import java.util.List;

/**
 * Created by PIGROAD on 2018/9/12
 * Email:920015363@qq.com
 */
public class SpecialCarAdapter extends RecyclerView.Adapter<SpecialCarAdapter.ViewHolder>{

    private Context context;
    private List<SpecialBean> datas;
    private OnItemClickLitener mOnItemClickLitener;
    private int selected = -1;

    public SpecialCarAdapter(Context context,List<SpecialBean> datas){
        this.context=context;
        this.datas=datas;
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void setSelection(int position){
        this.selected = position;
        notifyDataSetChanged();
    }

    public boolean isSelection(){
        if (selected > 0){
            return  true;
        }else {
            return false;
        }
    }

    public int getSelection(){
        return selected;
    }

    @Override
    public SpecialCarAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       View view = LayoutInflater.from(context).inflate(R.layout.special_car_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SpecialCarAdapter.ViewHolder holder, int position) {
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

        Glide.with(context).load(datas.get(position).getCar_icon()).into(holder.iv_car_img);
        holder.tv_car_name.setText(datas.get(position).getCar_name());

        int state=datas.get(position).getAuth_status();
        //auth_status是专车认证状态 0-可申请  1-申请中  2-已认证
        if (state == 0){
            holder.tv_car_state.setText("可申请认证");
        }else if (state == 1){
            holder.tv_car_state.setText("申请认证中");
        }else if (state ==2){
            holder.tv_car_state.setText("已认证");
        }

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_car_img;
        TextView tv_car_name,tv_car_state;
        RadioButton radioButton;
        public ViewHolder(View itemView) {
            super(itemView);
            iv_car_img=itemView.findViewById(R.id.iv_car_img);
            tv_car_name=itemView.findViewById(R.id.tv_car_name);
            tv_car_state=itemView.findViewById(R.id.tv_car_state);
            radioButton=itemView.findViewById(R.id.rb);
        }
    }
}
