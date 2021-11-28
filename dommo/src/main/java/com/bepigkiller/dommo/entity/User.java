package com.bepigkiller.dommo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_user")
public class User {
    @TableId
    private Integer userId;
    private String name;
    private String password;
    private String salt;
    private Date createTime;
}
