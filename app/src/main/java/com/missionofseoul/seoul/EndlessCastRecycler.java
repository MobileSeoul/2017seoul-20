package com.missionofseoul.seoul;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by hyunho on 2017-02-06.
 */

public abstract class EndlessCastRecycler extends RecyclerView.OnScrollListener {

    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 10; // The minimum amount of items to have below your current scroll position before loading more. 보여줄 개수만큼 넣기
    int firstVisibleItem, visibleItemCount, totalItemCount;

    private int current_page = 1;

    private GridLayoutManager mGridLayoutManager;

    public EndlessCastRecycler(GridLayoutManager mGridLayoutManager) {
        this.mGridLayoutManager = mGridLayoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mGridLayoutManager.getItemCount();
        firstVisibleItem = mGridLayoutManager.findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached

            // Do something
            onLoadMore(current_page);
            current_page++;

            loading = true;
        }

    }//end of onScrolled()

    public abstract void onLoadMore(int current_page);

}
