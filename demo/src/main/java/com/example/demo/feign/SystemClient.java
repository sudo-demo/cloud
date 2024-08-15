package com.example.demo.feign;

import com.example.common.domain.SystemUser;
import com.example.common.model.PageDTO;
import com.example.common.model.PageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 
 */
@FeignClient(name = "system",url = "${feign.system.url}")
public interface SystemClient {

    @PostMapping("/user/page")
    PageResult<SystemUser> list(@RequestBody PageDTO pageDTO);
}
