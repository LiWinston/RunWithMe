# Group Social API (backend)

Base path: `/api/group` (JWT required)

- POST `/create` { name, memberLimit? } -> Group
- GET `/me` -> GroupInfoResponse
- POST `/leave`
- POST `/join` { groupId, inviterUserId? } -> if inviter==owner then direct join, else application created
- GET `/applications/received` -> [ApplicationItem]
- POST `/applications/moderate` { applicationId, approve, reason? }
- POST `/members/interact` { targetUserId, action: LIKE|REMIND }
- POST `/weekly/complete` -> mark user weekly plan completed, add points, coupon awarding logic

Result wrapper: `{ code: 0|!=0, message, data }`.
