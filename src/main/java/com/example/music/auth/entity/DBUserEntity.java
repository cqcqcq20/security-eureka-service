package com.example.music.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.common.users.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@TableName("user")
public class DBUserEntity extends UserEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @JsonIgnore
    @TableField(whereStrategy = FieldStrategy.NOT_EMPTY)
    private String password;
    
}
