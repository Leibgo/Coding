package com.bepigkiller.dommo.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bepigkiller.dommo.entity.RecovTree;
import com.bepigkiller.dommo.mapper.RecovTreeDao;
import com.bepigkiller.dommo.service.RecovTreeService;
import com.bepigkiller.dommo.vo.RecovTreeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecovTreeServiceImpl extends ServiceImpl<RecovTreeDao, RecovTree> implements RecovTreeService {

    @Override
    public List<RecovTreeVo> getTree() {
        LambdaQueryWrapper<RecovTree> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RecovTree::getParentId,0);
        List<RecovTree> rootNodes = this.list(wrapper);
        return entity2Vo(rootNodes);
    }

    private List<RecovTreeVo> entity2Vo(List<RecovTree> rootNodes) {
        if(rootNodes.size() == 0){
            return null;
        }
        List<RecovTreeVo> treeVos = new ArrayList<>();
        for (RecovTree rootNode : rootNodes) {
            //属性复制到VO
            RecovTreeVo vo = new RecovTreeVo();
            BeanUtils.copyProperties(rootNode, vo);
            //查找相同父类ID的子节点,给VO注入children
            LambdaQueryWrapper<RecovTree> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(RecovTree::getParentId, rootNode.getId());
            List<RecovTree> children = this.list(childWrapper);
            vo.setChildren(entity2Vo(children));
            treeVos.add(vo);
        }
        return treeVos;
    }
}
