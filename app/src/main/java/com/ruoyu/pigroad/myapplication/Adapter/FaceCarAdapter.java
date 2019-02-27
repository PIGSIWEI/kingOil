package com.ruoyu.pigroad.myapplication.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ruoyu.pigroad.myapplication.Bean.FaceCarBean;
import com.ruoyu.pigroad.myapplication.R;

import java.util.List;

public class FaceCarAdapter extends RecyclerView.Adapter<FaceCarAdapter.ViewHolder> {

    private Context context;
    private List<FaceCarBean> datas;
    private OnItemClickLitener mOnItemClickLitener;
    private int selected = -1;

    public FaceCarAdapter(Context context, List<FaceCarBean> datas){
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

    @NonNull
    @Override
    public FaceCarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(context).inflate(R.layout.face_car_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaceCarAdapter.ViewHolder holder, int position) {
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

            holder.tv_card_money.setText(datas.get(position).getCar_money());
            holder.tv_card_name.setText(datas.get(position).getCar_name());

        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        RadioButton radioButton;
        TextView tv_card_name,tv_card_money;

        public ViewHolder(View itemView) {
            super(itemView);
            radioButton=itemView.findViewById(R.id.rb);
            tv_card_name=itemView.findViewById(R.id.tv_card_name);
            tv_card_money=itemView.findViewById(R.id.tv_card_money);
        }
    }
}
