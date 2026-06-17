package com.home.tools.util;

import com.home.tools.dto.StatusTransitionResult;
import com.home.tools.entity.ToolStatus;

import java.util.*;

public class StatusTransitionUtil {

    private static final Map<ToolStatus, Set<ToolStatus>> ALLOWED_TRANSITIONS = new HashMap<>();

    private static final Map<String, String> TRANSITION_REASONS = new HashMap<>();

    static {
        ALLOWED_TRANSITIONS.put(ToolStatus.AVAILABLE, new HashSet<>(Arrays.asList(
            ToolStatus.IN_USE,
            ToolStatus.MAINTENANCE,
            ToolStatus.LOANED,
            ToolStatus.LOST
        )));

        ALLOWED_TRANSITIONS.put(ToolStatus.IN_USE, new HashSet<>(Arrays.asList(
            ToolStatus.AVAILABLE,
            ToolStatus.MAINTENANCE,
            ToolStatus.LOST
        )));

        ALLOWED_TRANSITIONS.put(ToolStatus.MAINTENANCE, new HashSet<>(Arrays.asList(
            ToolStatus.AVAILABLE,
            ToolStatus.LOST
        )));

        ALLOWED_TRANSITIONS.put(ToolStatus.LOANED, new HashSet<>(Arrays.asList(
            ToolStatus.AVAILABLE,
            ToolStatus.LOST
        )));

        ALLOWED_TRANSITIONS.put(ToolStatus.LOST, new HashSet<>(Arrays.asList(
            ToolStatus.AVAILABLE
        )));

        TRANSITION_REASONS.put("AVAILABLE->IN_USE", "使用工具");
        TRANSITION_REASONS.put("AVAILABLE->MAINTENANCE", "开始保养");
        TRANSITION_REASONS.put("AVAILABLE->LOANED", "借出工具");
        TRANSITION_REASONS.put("AVAILABLE->LOST", "确认丢失");

        TRANSITION_REASONS.put("IN_USE->AVAILABLE", "使用完毕归还");
        TRANSITION_REASONS.put("IN_USE->MAINTENANCE", "使用后需保养");
        TRANSITION_REASONS.put("IN_USE->LOST", "使用中丢失");

        TRANSITION_REASONS.put("MAINTENANCE->AVAILABLE", "保养完成");
        TRANSITION_REASONS.put("MAINTENANCE->LOST", "保养中发现丢失");

        TRANSITION_REASONS.put("LOANED->AVAILABLE", "借出归还");
        TRANSITION_REASONS.put("LOANED->LOST", "借出后丢失");

        TRANSITION_REASONS.put("LOST->AVAILABLE", "找回工具");
    }

    public static StatusTransitionResult validate(ToolStatus from, ToolStatus to) {
        if (from == null || to == null) {
            return StatusTransitionResult.fail("状态不能为空", "状态参数无效");
        }

        if (from == to) {
            return StatusTransitionResult.success();
        }

        Set<ToolStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        if (allowed != null && allowed.contains(to)) {
            return StatusTransitionResult.success();
        }

        String reason = getInvalidReason(from, to);
        return StatusTransitionResult.fail(
            "不允许从 " + getStatusName(from) + " 变更为 " + getStatusName(to),
            reason
        );
    }

    public static boolean canTransition(ToolStatus from, ToolStatus to) {
        if (from == null || to == null) return false;
        if (from == to) return true;
        Set<ToolStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    public static List<ToolStatus> getAllowedTargets(ToolStatus from) {
        if (from == null) return Collections.emptyList();
        Set<ToolStatus> allowed = ALLOWED_TRANSITIONS.get(from);
        return allowed != null ? new ArrayList<>(allowed) : Collections.emptyList();
    }

    public static String getTransitionReason(ToolStatus from, ToolStatus to) {
        String key = from.name() + "->" + to.name();
        return TRANSITION_REASONS.getOrDefault(key, "状态变更");
    }

    private static String getInvalidReason(ToolStatus from, ToolStatus to) {
        return switch (from) {
            case AVAILABLE -> switch (to) {
                default -> "可用工具可以直接使用、送去保养、借出或登记丢失，但无法从可用状态直接变为 " + getStatusName(to);
            };
            case IN_USE -> switch (to) {
                case LOANED -> "正在使用的工具无法直接借出，请先归还后再借出";
                default -> "使用中的工具可以归还、送去保养或登记丢失";
            };
            case MAINTENANCE -> switch (to) {
                case IN_USE -> "保养中的工具无法直接使用，请先完成保养";
                case LOANED -> "保养中的工具无法借出，请先完成保养";
                default -> "保养中的工具只能完成保养或登记丢失";
            };
            case LOANED -> switch (to) {
                case IN_USE -> "借出的工具无法直接使用，请先归还";
                case MAINTENANCE -> "借出的工具无法直接保养，请先归还";
                default -> "借出的工具只能归还或登记丢失";
            };
            case LOST -> switch (to) {
                case IN_USE -> "已丢失的工具无法使用";
                case MAINTENANCE -> "已丢失的工具无法保养";
                case LOANED -> "已丢失的工具无法借出";
                default -> "已丢失的工具只能找回后变为可用状态";
            };
        };
    }

    public static String getStatusName(ToolStatus status) {
        if (status == null) return "未知";
        return switch (status) {
            case AVAILABLE -> "可用";
            case IN_USE -> "使用中";
            case MAINTENANCE -> "保养中";
            case LOANED -> "借出";
            case LOST -> "丢失";
        };
    }
}
