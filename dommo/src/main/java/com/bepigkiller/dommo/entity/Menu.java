package com.bepigkiller.dommo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_menu")
public class Menu {
    @TableId
    private Integer MenuId;
    private Integer ParentId;
    private String name;
    private String url;
    private String perms;
    private Integer type;
}
