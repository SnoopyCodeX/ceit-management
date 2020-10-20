package com.ceit.management.adapter.pagination;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class PaginationListener extends RecyclerView.OnScrollListener
{
    private static final int PAGE_START = 1;
    private static final int PAGE_SIZE = 10;

    private LinearLayoutManager layoutManager;

    public PaginationListener(LinearLayoutManager layoutManager)
    {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
    {
        super.onScrolled(recyclerView, dx, dy);

        int itemCount = layoutManager.getChildCount();
        int totalCount = layoutManager.getItemCount();
        int visibleItem = layoutManager.findFirstVisibleItemPosition();

        if(!isLoading() && !isLastPage())
            if ((itemCount + visibleItem) >= totalCount && visibleItem >= 0 && totalCount >= PAGE_SIZE)
                loadMoreItems();
    }

    protected abstract void loadMoreItems();
    public abstract boolean isLoading();
    public abstract boolean isLastPage();
}
