package com.bepigkiller.dommo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_role")
public class Role {
    @TableId
    private Integer RoleId;
    private String roleName;
}
