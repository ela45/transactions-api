package com.app.dto;

import java.io.Serializable;

public class ResponseDTO<T> implements Serializable {
    private T content;

    public ResponseDTO(){

    }

    public ResponseDTO(T content){
        this.content=content;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}
