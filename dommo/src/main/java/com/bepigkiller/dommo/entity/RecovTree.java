package com.bepigkiller.dommo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("recov_tree")
public class RecovTree {
    @TableId
    private Integer id;
    private String name;
    private Integer parentId;
}
