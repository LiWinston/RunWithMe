package com.rwm.schedule;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rwm.entity.GroupMember;
import com.rwm.mapper.GroupMemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class WeeklyCountersResetTask {

    private final GroupMemberMapper groupMemberMapper;

    // 每周一 00:05 清零
    @Scheduled(cron = "0 5 0 ? * MON")
    public void resetWeeklyCounters() {
        try {
            UpdateWrapper<GroupMember> uw = new UpdateWrapper<>();
            uw.set("weekly_like_count", 0).set("weekly_remind_count", 0).eq("deleted", false);
            int updated = groupMemberMapper.update(null, uw);
            log.info("Weekly counters reset for {} members", updated);
        } catch (Exception ex) {
            log.error("Failed to reset weekly counters: {}", ex.getMessage());
        }
    }
}
