package com.smilcool.server.core.dao;

import com.smilcool.server.core.pojo.po.Permission;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface PermissionMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Permission record);

    int insertSelective(Permission record);

    Permission selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Permission record);

    int updateByPrimaryKey(Permission record);

    List<Permission> selectAll();

    Permission selectByName(String name);

}