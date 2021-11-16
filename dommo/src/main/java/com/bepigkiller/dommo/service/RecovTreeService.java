package com.bepigkiller.dommo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bepigkiller.dommo.entity.RecovTree;
import com.bepigkiller.dommo.vo.RecovTreeVo;
import org.springframework.stereotype.Service;

import java.util.List;


public interface RecovTreeService extends IService<RecovTree> {
    List<RecovTreeVo> getTree();
}
