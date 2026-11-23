package aggregator.email.dto;

public record EmailStatsDto(
        long total,
        long pending,
        long sending,
        long accepted,
        long failed,
        long uncertain
) {}
