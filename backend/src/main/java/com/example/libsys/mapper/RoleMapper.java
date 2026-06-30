package com.example.libsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.libsys.module.user.entity.RoleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<RoleEntity> {

    @Select("SELECT r.* FROM t_role r INNER JOIN t_user_role ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<RoleEntity> selectRolesByUserId(@Param("userId") Long userId);
}
