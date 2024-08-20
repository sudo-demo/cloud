package com.example.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.domain.SystemLog;
import com.example.common.service.SystemLogService;
import com.example.common.mapper.SystemLogMapper;
import org.springframework.stereotype.Service;

@Service
public class SystemLogServiceImpl  extends ServiceImpl<SystemLogMapper, SystemLog> implements SystemLogService {
    @Override
    public void saveLog(SystemLog systemLog) {
        this.save(systemLog);
    }
}
