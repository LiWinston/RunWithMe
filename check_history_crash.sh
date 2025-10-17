#!/bin/bash

# 清理之前的日志
adb logcat -c

echo "==================================="
echo "请手动打开应用并点击 History 页面"
echo "然后按 Ctrl+C 停止日志收集"
echo "==================================="

# 开始收集日志，只显示错误和崩溃信息
adb logcat -s AndroidRuntime:E ActivityManager:I HistoryFragment:D HistoryTodayFragment:D HistoryWeekFragment:D HistoryMonthFragment:D HistoryViewModel:D


