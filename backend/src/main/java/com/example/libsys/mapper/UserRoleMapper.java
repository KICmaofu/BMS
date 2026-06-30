package com.example.libsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.libsys.module.user.entity.UserRoleEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRoleEntity> {

    @Delete("DELETE FROM t_user_role WHERE user_id = #{userId}")
    void deleteByUserId(@Param("userId") Long userId);

    @Insert("<script>" +
            "INSERT INTO t_user_role (user_id, role_id) VALUES " +
            "<foreach collection='roleIds' item='roleId' separator=','>" +
            "(#{userId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("userId") Long userId, @Param("roleIds") Long[] roleIds);
}
