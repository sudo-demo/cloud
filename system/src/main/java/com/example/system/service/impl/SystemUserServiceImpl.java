package com.example.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.common.annotation.DataScope;
import com.example.common.config.Security.PermissionService;
import com.example.common.model.Permission;
import com.example.common.util.JwtUtil;
import com.example.system.domain.SystemRole;
import com.example.system.domain.SystemUser;
import com.example.system.domain.dto.LoginBody;
import com.example.system.domain.dto.SystemUserDto;
import com.example.system.domain.excel.SystemUserExcelDto;
import com.example.system.service.SystemUserService;
import com.example.system.mapper.SystemUserMapper;
import com.example.common.domain.LoginUser;
import com.example.common.exception.validateException;
import com.example.common.model.PageDTO;
import com.sun.deploy.net.URLEncoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @description 针对表【system_user(用户信息表)】的数据库操作Service实现
 * @createDate 2023-09-01 10:40:37
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser>
        implements SystemUserService {

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    PermissionService permissionService;

    //    @DataScope(clazz = SystemUserServiceImpl.class, callMethod = "setUserDataScope")
    @Override
    public IPage<SystemUser> getUserPage(PageDTO pageDTO) {
        Page<SystemUser> page = pageDTO.toPage();
        permissionService.getContext().setAfterFunction((vRoleApi, conditions) -> {
            // 弹出最后一个元素
            if(vRoleApi.getRoleId().equals(1L)){
                if (!conditions.isEmpty()) {
                    List<String> where;
                    where = conditions.remove(conditions.size() - 1);
                    where.add("role_id = '1'");
                    conditions.add(where);
                }
            }
            return conditions;
        });
        //        LambdaQueryWrapper<SystemUser> wrapper = new LambdaQueryWrapper<>();
//
//        IPage<SystemUser> systemUserPage = systemUserMapper.selectPage(page, wrapper);
        return baseMapper.getUserPage(page);
    }

    public String setUserDataScope() {
        return "2 = 1";
    }

    @Override
    public SystemUser getInfo(Integer userId) {
        return systemUserMapper.selectById(userId);
    }

    @Override
    public void created(SystemUserDto systemUserDto) {
        boolean exists = lambdaQuery()
                .select(SystemUser::getUserName)
                .eq(SystemUser::getLoginId, systemUserDto.getLoginId())
                .exists();
        if (exists) {
            throw new validateException("登录账号已存在");
        }
        systemUserDto.setUserId(null);
        SystemUser systemUser = BeanUtil.copyProperties(systemUserDto, SystemUser.class);
        systemUser.setUserId(null);
        systemUserMapper.insert(systemUser);
        System.out.println(systemUser.getUserId());
    }

    @Override
    public void updated(SystemUserDto systemUserDto) {
        systemUserDto.setLoginId(null);
        SystemUser systemUser = BeanUtil.copyProperties(systemUserDto, SystemUser.class);
        systemUserMapper.updateById(systemUser);
    }

    @Override
    public void remove(Integer userId) {
        systemUserMapper.deleteById(userId);
    }

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public String login(LoginBody loginBody) {
        // 创建Authentication对象
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginBody.getLoginId(), loginBody.getPassword());
        // 调用AuthenticationManager的authenticate方法进行认证
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        if (authentication == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        return jwtUtil.createToken(loginUser.getUser());
    }

    public void getAct(String data) {
        System.out.printf("到了：" + data);
    }

    /**
     *
     * https://easyexcel.opensource.alibaba.com
     * @param response
     * @throws IOException
     */
    @Override
    public void exportExcel(HttpServletResponse response) throws IOException {
        QueryWrapper<SystemUser> queryWrapper = new QueryWrapper<>();
        List<SystemUser> systemUsers = baseMapper.selectList(queryWrapper);
        // 转换为 SystemUserExcelDto 列表
        List<SystemUserExcelDto> dtoList = systemUsers.stream().map(user -> {
            SystemUserExcelDto dto = new SystemUserExcelDto();
            BeanUtils.copyProperties(user, dto);
            return dto;
        }).collect(Collectors.toList());


        // 设置响应头，告诉浏览器这是个Excel文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里文件名需要URL编码，避免中文乱码
        String fileName = URLEncoder.encode("系统用户列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写出Excel，自动关闭流
        EasyExcel.write(response.getOutputStream(), SystemUserExcelDto.class)
                .sheet("用户数据")
                .doWrite(dtoList);


    }
}




