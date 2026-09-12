package com.devshowcase.api.dto;

public class PaginationResponse {
    private int page;
    private int limit;
    private long total;
    private long totalPages;

    public PaginationResponse(int page, int limit, long total, long totalPages) {
        this.page = page;
        this.limit = limit;
        this.total = total;
        this.totalPages = totalPages;
    }

    public int getPage() { return page; }
    public int getLimit() { return limit; }
    public long getTotal() { return total; }
    public long getTotalPages() { return totalPages; }
}
