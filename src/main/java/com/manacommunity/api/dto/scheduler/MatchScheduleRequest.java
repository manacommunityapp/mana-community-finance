package com.manacommunity.api.dto.scheduler;

public record MatchScheduleRequest(Long homeTeamId, Long awayTeamId, String matchType, String stage, String startTime) {}
