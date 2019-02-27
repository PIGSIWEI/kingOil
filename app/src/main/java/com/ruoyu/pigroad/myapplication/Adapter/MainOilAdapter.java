package com.ruoyu.pigroad.myapplication.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.florent37.fiftyshadesof.FiftyShadesOf;
import com.ruoyu.pigroad.myapplication.Bean.MainOilBean;
import com.ruoyu.pigroad.myapplication.R;
import com.ruoyu.pigroad.myapplication.Ui.MainOilDetailActivity;

import java.util.Collections;
import java.util.List;

/**
 * Created by PIGROAD on 2018/9/18
 * Email:920015363@qq.com
 */
public class MainOilAdapter extends RecyclerView.Adapter<MainOilAdapter.ViewHolder>{

    private List<MainOilBean> datas;
    private Context context;

    public MainOilAdapter( List<MainOilBean> datas,Context context){

        this.datas=datas;
        this.context=context;
    }

    @NonNull
    @Override
    public MainOilAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.main_recyle_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MainOilAdapter.ViewHolder holder, final int position) {

        final FiftyShadesOf fiftyShadesOf=FiftyShadesOf.with(context)
                .on(holder.tv_oil_name,holder.tv_oil_address,holder.tv_distance,holder.iv_ali,holder.iv_wx,holder.tv_jln)
                .start();

        final double distance=Math.round(datas.get(position).getDistance()/100d)/10d;
        holder.tv_distance.setText(distance+"公里");
        holder.tv_oil_name.setText(datas.get(position).getName());
        holder.tv_oil_address.setText(datas.get(position).getAddress());
        Glide.with(context).load(datas.get(position).getPic_url()).into(holder.iv_oil_img);

        new Handler().postDelayed(new Runnable(){
            public void run(){
                //execute the task
                fiftyShadesOf.stop();
            }
        },1000);

        holder.rl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, MainOilDetailActivity.class);
                intent.putExtra("oil_img",datas.get(position).getPic_url());
                intent.putExtra("oil_name",datas.get(position).getName());
                intent.putExtra("oil_adress",datas.get(position).getAddress());
                intent.putExtra("oil_distance",distance+"");
                intent.putExtra("store_id",datas.get(position).getId()+"");
                intent.putExtra("oil_lat",datas.get(position).getLat());
                intent.putExtra("oil_lng",datas.get(position).getLng());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView iv_oil_img,iv_wx,iv_ali;
        TextView tv_oil_name,tv_oil_address,tv_distance,tv_jln;
        RelativeLayout rl_btn;
        public ViewHolder(View itemView) {
            super(itemView);
            tv_distance=itemView.findViewById(R.id.tv_distance);
            tv_oil_address=itemView.findViewById(R.id.tv_oil_address);
            tv_jln=itemView.findViewById(R.id.tv_jln);
            tv_oil_name=itemView.findViewById(R.id.tv_oil_name);
            iv_oil_img=itemView.findViewById(R.id.iv_oil_img);
            iv_ali=itemView.findViewById(R.id.iv_ali);
            iv_wx=itemView.findViewById(R.id.iv_wx);
            rl_btn=itemView.findViewById(R.id.rl_btn);
        }
    }

}
