package com.bepigkiller.dommo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bepigkiller.dommo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDao extends BaseMapper<User> {
}
