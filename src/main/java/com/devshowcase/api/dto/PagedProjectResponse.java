package com.devshowcase.api.dto;

import java.util.List;

public class PagedProjectResponse {
    private List<ProjectResponse> data;
    private PaginationResponse pagination;

    public PagedProjectResponse(List<ProjectResponse> data, PaginationResponse pagination) {
        this.data = data;
        this.pagination = pagination;
    }

    public List<ProjectResponse> getData() { return data; }
    public PaginationResponse getPagination() { return pagination; }
}
