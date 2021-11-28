package com.bepigkiller.dommo.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bepigkiller.dommo.entity.User;
import com.bepigkiller.dommo.mapper.UserDao;
import com.bepigkiller.dommo.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService  {
}
