package ru.edu.pr03;

/** Неизменяемая заявка */
public final class ApprovalRequest {
    private final long amount;
    private final boolean urgent;
    private final RiskLevel risk;
    private final boolean containsPersonalData;

    public ApprovalRequest(long amount, boolean urgent, RiskLevel risk,
                           boolean containsPersonalData) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        if (risk == null) {
            throw new IllegalArgumentException("risk is required");
        }
        this.amount = amount;
        this.urgent = urgent;
        this.risk = risk;
        this.containsPersonalData = containsPersonalData;
    }

    public long getAmount() { return amount; }
    public boolean isUrgent() { return urgent; }
    public RiskLevel getRisk() { return risk; }
    public boolean containsPersonalData() { return containsPersonalData; }
}
