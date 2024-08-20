package com.example.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.common.domain.SystemLog;

public interface SystemLogService extends IService<SystemLog> {

    void saveLog(SystemLog systemLog);

}
