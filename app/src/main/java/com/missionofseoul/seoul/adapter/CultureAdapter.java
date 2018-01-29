package com.missionofseoul.seoul.adapter;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.missionofseoul.seoul.CultureDetailActivity;
import com.missionofseoul.seoul.R;
import com.missionofseoul.seoul.model.CulturePojo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyunho on 2017-10-09.
 */

public class CultureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    LayoutInflater inflater;
    public RequestManager mGlideRequestManager;
    List<CulturePojo.Row> culturePojoList;
    CulturePojo.Row culturePojo;
    private static final int VIEW_TYPE_CULTURE = 501;
    private static final int VIEW_PROG = 211;

    private LoadMoreListener loadMoreListener;
    private GridLayoutManager mGridLayoutManager;

    private int visibleThreshold = 20;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private boolean isMoreLoading = false;
    private int currentPage = 1;

    public interface LoadMoreListener {
        void onMyLoadMore(int currentPage);

    }//end of onLooadMoreListener()

    public void setGridLayoutManager(GridLayoutManager gridLayoutManager) {
        this.mGridLayoutManager = gridLayoutManager;
    }//end of setLinearLayoutManager()

    //
    public void setRecyclerView(RecyclerView mView) {
        mView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //리사이클러 뷰 마지막 위치 알아내기
                int lastVisibleItemPosition = ((GridLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;

                if (!isMoreLoading && lastVisibleItemPosition == itemTotalCount) {

                    if (loadMoreListener != null) {
                        loadMoreListener.onMyLoadMore(currentPage);
                        currentPage++;
                    }
                    isMoreLoading = true;

                }//end of if~else
             /*   visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mGridLayoutManager.getItemCount();
                firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();

                if (!isMoreLoading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {

                    if (LoadMoreListener != null) {
                        LoadMoreListener.onMyLoadMore(currentPage);
                        currentPage++;
                    }

                    isMoreLoading = true;
                }//end of if~ else()*/

            }//end if onScrolled()
        });
    }//end of setRecyclerView()

    //
    public void addAll(List<CulturePojo.Row> list) {
        culturePojoList.clear();
        culturePojoList.addAll(list);
        notifyDataSetChanged();
    }//end of addAll()

    //
    public void addItemMore(List<CulturePojo.Row> list) {
        culturePojoList.addAll(list);
        notifyItemRangeChanged(0, culturePojoList.size());
    }//end of addItemMore()


    public void setMoreLoading(boolean isMoreLoading) {
        this.isMoreLoading = isMoreLoading;

    }//end of setMoreLoading()

    public void setProgressMore(final boolean isProgress) {

        try {
            if (isProgress) {
                new android.os.Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        culturePojoList.add(null);
                        notifyItemInserted(culturePojoList.size() - 1);
                    }
                });
            } else {
                culturePojoList.remove(culturePojoList.size() - 1);
                notifyItemRemoved(culturePojoList.size());
            }
        } catch (ArrayIndexOutOfBoundsException e) {

        }

    }//end of setProgressMore()

    public CultureAdapter(LayoutInflater inflater, RequestManager mGlideRequestManager, CultureAdapter.LoadMoreListener loadMoreListener) {
        this.inflater = inflater;
        this.mGlideRequestManager = mGlideRequestManager;
        this.loadMoreListener = loadMoreListener;

        culturePojoList = new ArrayList<>();
    }//end of 생성자

    @Override
    public int getItemCount() {
        return culturePojoList.size();
    }//end of getItemCoutn()

    @Override
    public int getItemViewType(int position) {
        return culturePojoList.get(position) != null ? VIEW_TYPE_CULTURE : VIEW_PROG;
    }//end go getItemViewType()


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {

            case VIEW_TYPE_CULTURE:
                View view = inflater.inflate(R.layout.item_list_culture, parent, false);
                return new CultureViewHolder(view);

            case VIEW_PROG:

                return new ProgressViewHolder(inflater.inflate(R.layout.item_progress, parent, false));

            default:
                return null;

        }//end of switch()
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_CULTURE:
                ((CultureViewHolder) holder).bind(position);
                break;
        }//end of switch()

    }//end of onBindViewHolder()


    //inner class
    private class CultureViewHolder extends RecyclerView.ViewHolder {

        ImageView mainImg;
        TextView isFree;
        TextView genre;
        TextView title;
        TextView date;
        TextView location;

        public CultureViewHolder(View view) {
            super(view);

            mainImg = (ImageView) view.findViewById(R.id.mainImg);
            isFree = (TextView) view.findViewById(R.id.isFree);
            genre = (TextView) view.findViewById(R.id.genre);
            title = (TextView) view.findViewById(R.id.title);
            date = (TextView) view.findViewById(R.id.date);
            location = (TextView) view.findViewById(R.id.location);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    culturePojo = culturePojoList.get(getLayoutPosition());

                    Intent intent = new Intent(v.getContext(), CultureDetailActivity.class);
                    intent.putExtra("Orglink", culturePojo.getORGLINK());
                    intent.putExtra("MainImg", culturePojo.getMAINIMG());
                    intent.putExtra("Title", culturePojo.getTITLE());
                    intent.putExtra("UseFree", culturePojo.getUSEFEE());
                    intent.putExtra("StartDate", culturePojo.getSTRTDATE());
                    intent.putExtra("EndDate", culturePojo.getENDDATE());
                    intent.putExtra("CodeName", culturePojo.getCODENAME());
                    intent.putExtra("Place", culturePojo.getPLACE());
                    intent.putExtra("UseTrgt", culturePojo.getUSETRGT());
                    intent.putExtra("Sponsor", culturePojo.getSPONSOR());
                    intent.putExtra("Inquiry", culturePojo.getINQUIRY());
                    v.getContext().startActivity(intent);


                   /* int pos = getLayoutPosition();
                    Intent intent = new Intent(v.getContext(), CultureDetailActivity.class);
                    intent.putExtra("CulturePojoList", (Serializable) culturePojoList);
                    intent.putExtra("pos", pos);
                    v.getContext().startActivity(intent);*/

                }//end of onClick()
            });

        }//end of 생성자()

        public void bind(int position) {
            culturePojo = culturePojoList.get(position);

            if (culturePojo.getMAINIMG() != null) {
                String fileName = culturePojo.getMAINIMG();
                int index = fileName.lastIndexOf("/");
                if (index != -1) {
                    String imgUrl = fileName.substring(0, index);
                    String extension = fileName.substring(index + 1);
                    mGlideRequestManager.load(imgUrl.toLowerCase() + "/" + extension).placeholder(R.drawable.bg_no_img).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mainImg);
                }
            }
            if (culturePojo.getISFREE().equals("1")) {
                isFree.setText("무료");
            } else if (culturePojo.getISFREE().equals("0")) {
                isFree.setText("유료");
            }
            genre.setText(culturePojo.getCODENAME());
            title.setText(culturePojo.getTITLE());
            date.setText(culturePojo.getSTRTDATE() + " ~ " + culturePojo.getENDDATE());
            location.setText(culturePojo.getGCODE());


        }//end of bind()

    }//end of innerClass


    //
    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar pBar;

        public ProgressViewHolder(View v) {
            super(v);
            pBar = (ProgressBar) v.findViewById(R.id.pBar);

        }//end of ProgressViewHolder()

    }//end of ProgressViewHolder inner  clsas

}//end of CultureAdapter class
