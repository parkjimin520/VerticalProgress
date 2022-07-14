package com.example.verticalprogress;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.CustonViewHolder> {

    Context context;

    //item별 클릭 처리
    public interface OnItemClickListener{
        void onItemClick(int pos);
    }
    private OnItemClickListener mListener =null;
    public void setOnItemClickListener(OnItemClickListener listener){
        this.mListener = listener;
    }


    //리스트의 아이템 담을 배열
    private ArrayList<VoiceList> arrayList = null;
    public MainAdapter(ArrayList<VoiceList> list) {
        arrayList = list;
    }

    //아이템 클릭 상태를 저장할 배열
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private int prePosition = -1;


    @NonNull
    @Override
    public MainAdapter.CustonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item,parent,false);
        CustonViewHolder holder = new CustonViewHolder(view);

        return holder;

    }


    //position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시(실제 추가 될 때)
    @Override
    public void onBindViewHolder(@NonNull MainAdapter.CustonViewHolder holder, int position) {
        holder.onBind(arrayList.get(position),position);


    }

    //전체 데이터 개수 리턴
    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    //아이템 뷰를 저장하는 뷰홀더
    public class CustonViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView wordcloud;
        public ImageButton expandButton;
        private int position;


        public CustonViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if(pos != RecyclerView.NO_POSITION){
                        if(mListener != null){
                            mListener.onItemClick(pos);
                        }
                    }
                }
            });

            this.title = (TextView) itemView.findViewById(R.id.title);
            wordcloud = (ImageView) itemView.findViewById(R.id.wordCloud);


            this.expandButton = (ImageButton) itemView.findViewById(R.id.expandButton);
            expandButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (selectedItems.get(position)) {
                        // 펼쳐진 Item을 클릭 시
                        selectedItems.delete(position);
                    } else {
                        // 직전의 클릭됐던 Item의 클릭상태를 지움
                        selectedItems.delete(prePosition);
                        // 클릭한 Item의 position을 저장
                        selectedItems.put(position, true);
                    }
                    if (prePosition != -1) notifyItemChanged(prePosition);
                    notifyItemChanged(position);
                    // 클릭된 position 저장
                    prePosition = position;
                }
            });

        }

        void onBind(VoiceList item, int position){
            this.position = position;

            title.setText(item.getTitle());
            wordcloud.setImageResource(item.getWordcloud());

            changeVisibility(selectedItems.get(position));

        }



        private void changeVisibility(final boolean isExpanded){
            int dpValue = 150;
            float d = context.getResources().getDisplayMetrics().density;
            int height = (int)(dpValue * d);

            ValueAnimator va = isExpanded ? ValueAnimator.ofInt(0,height) : ValueAnimator.ofInt(height,0);
            va.setDuration(600);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int)animation.getAnimatedValue();

                    wordcloud.requestLayout();
                    wordcloud.setVisibility(isExpanded?View.VISIBLE:View.GONE);


                }
            });

            va.start();
        }



    }




}