package com.bepigkiller.dommo.vo;

import com.bepigkiller.dommo.entity.RecovTree;
import lombok.Data;

import java.util.List;
@Data
public class RecovTreeVo {
    private Integer id;
    private String name;
    private List<RecovTreeVo> children;
}
