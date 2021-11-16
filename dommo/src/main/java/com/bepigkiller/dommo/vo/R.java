package com.bepigkiller.dommo.vo;

import lombok.Data;

@Data
public class R {
    private int code;
    private String msg;
    private Object data;

    public R (){
        this.code = 200;
        this.msg  = "操作成功";
    }

    public static R success(Object data){
        R r = new R();
        r.setData(data);
        return r;
    }
}
