package com.collectto.api_collectto.domain.shared;

import java.util.List;

public record DomainSlice<T>(
    List<T> content, 
    boolean hasNext
) {}
