package com.ruoyu.pigroad.myapplication.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ruoyu.pigroad.myapplication.Bean.BillBean;
import com.ruoyu.pigroad.myapplication.R;

import java.util.List;

/**
 * Created by PIGROAD on 2018/9/18
 * Email:920015363@qq.com
 */
public class PersonalBillAdapter extends RecyclerView.Adapter<PersonalBillAdapter.ViewHolder>{
    private int selected = -1;

    private Context context;
    private List<BillBean> datas;
    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
    {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public void setSelection(int position){
        this.selected = position;
        notifyDataSetChanged();
    }

    public PersonalBillAdapter(List<BillBean> datas,Context context){
        this.context=context;
        this.datas=datas;
    }

    @Override
    public PersonalBillAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bill_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PersonalBillAdapter.ViewHolder holder, int position) {
        if(holder instanceof ViewHolder){
            final ViewHolder viewHolder = holder;

            if(selected == position){
                viewHolder.radioButton.setChecked(true);
                viewHolder.itemView.setSelected(true);
            } else {
                viewHolder.radioButton.setChecked(false);
                viewHolder.itemView.setSelected(false);
            }

            if (mOnItemClickLitener != null)
            {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        mOnItemClickLitener.onItemClick(viewHolder.itemView, viewHolder.getAdapterPosition());
                    }
                });
            }

            holder.tv_bill_number.setText(datas.get(position).getBill_number());
            holder.tv_bill_title.setText(datas.get(position).getBill_title());


        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        RadioButton radioButton;
        TextView tv_bill_title,tv_bill_number;
        public ViewHolder(View itemView) {
            super(itemView);
            radioButton=itemView.findViewById(R.id.rb);
            tv_bill_title=itemView.findViewById(R.id.tv_bill_title);
            tv_bill_number=itemView.findViewById(R.id.tv_bill_number);
        }

    }
}
