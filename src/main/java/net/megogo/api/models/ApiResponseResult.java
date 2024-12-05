package net.megogo.api.models;

public record ApiResponseResult<T> (
    String result,
    T data
){}
