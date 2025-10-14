package com.rwm.controller;

import com.rwm.dto.request.*;
import com.rwm.dto.response.ApplicationItem;
import com.rwm.dto.response.GroupInfoResponse;
import com.rwm.dto.response.Result;
import com.rwm.entity.Group;
import com.rwm.entity.GroupJoinApplication;
import com.rwm.service.GroupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    private Long currentUserId(HttpServletRequest request) { return (Long) request.getAttribute("currentUserId"); }

    @PostMapping("/create")
    public ResponseEntity<Result<Group>> create(HttpServletRequest req, @Valid @RequestBody GroupCreateRequest body) {
        try {
            Group g = groupService.createGroup(currentUserId(req), body);
            return ResponseEntity.ok(Result.ok("Group created", g));
        } catch (Exception e) {
            log.error("Create group failed", e);
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Result<GroupInfoResponse>> myGroup(HttpServletRequest req) {
        GroupInfoResponse info = groupService.getMyGroupInfo(currentUserId(req));
        return ResponseEntity.ok(Result.ok(info));
    }

    @PostMapping("/leave")
    public ResponseEntity<Result<String>> leave(HttpServletRequest req) {
        try {
            groupService.leaveGroup(currentUserId(req));
            return ResponseEntity.ok(Result.ok("Left group"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<Result<Object>> requestJoin(HttpServletRequest req, @Valid @RequestBody GroupJoinRequest body) {
        try {
            GroupJoinApplication app = groupService.requestJoin(currentUserId(req), body);
            if (app == null) {
                return ResponseEntity.ok(Result.ok("Joined directly"));
            }
            return ResponseEntity.ok(Result.ok("Join requested", app));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    @GetMapping("/applications/received")
    public ResponseEntity<Result<List<ApplicationItem>>> received(HttpServletRequest req) {
        List<ApplicationItem> items = groupService.getReceivedApplications(currentUserId(req));
        return ResponseEntity.ok(Result.ok(items));
    }

    @PostMapping("/applications/moderate")
    public ResponseEntity<Result<String>> moderate(HttpServletRequest req, @Valid @RequestBody ModerateApplicationRequest body) {
        try {
            groupService.moderateApplication(currentUserId(req), body.getApplicationId(), body.getApprove(), body.getReason());
            return ResponseEntity.ok(Result.ok("Processed"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    @PostMapping("/members/interact")
    public ResponseEntity<Result<String>> interact(HttpServletRequest req, @Valid @RequestBody MemberInteractRequest body) {
        try {
            groupService.likeOrRemind(currentUserId(req), body.getTargetUserId(), body.getAction());
            return ResponseEntity.ok(Result.ok("Done"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }

    @PostMapping("/weekly/complete")
    public ResponseEntity<Result<String>> completeWeekly(HttpServletRequest req) {
        try {
            groupService.completeWeeklyPlan(currentUserId(req));
            return ResponseEntity.ok(Result.ok("Recorded"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.error(e.getMessage()));
        }
    }
}
