package com.rwm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rwm.entity.WorkoutRoute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WorkoutRouteMapper extends BaseMapper<WorkoutRoute> {
    
    /**
     * 根据运动记录ID查询路线轨迹，按顺序排列
     */
    @Select("SELECT * FROM workout_routes WHERE workout_id = #{workoutId} AND deleted = 0 ORDER BY sequence_order ASC")
    List<WorkoutRoute> selectRouteByWorkoutId(Long workoutId);
    
    /**
     * 批量插入路线点
     */
    int insertBatch(List<WorkoutRoute> routes);
}
