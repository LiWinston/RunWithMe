package com.rwm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rwm.entity.Workout;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkoutMapper extends BaseMapper<Workout> {
    // BaseMapper 已经有 insert / selectById / update / delete 等常用方法
}
