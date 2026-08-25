package com.educalab.puentelab.domain.model

data class MemberAnalysis(
    val memberId: String,
    val length: Double,
    val cost: Double,
    val capacity: Double,
    val demand: Double,
    val stressRatio: Double,
    val role: MemberRole
)

data class SimulationResult(
    val passed: Boolean,
    val failureReasons: List<FailureReason>,
    val totalCost: Double,
    val budget: Double,
    val budgetRemaining: Double,
    val maxStressRatio: Double,
    val weakestMemberId: String?,
    val memberAnalyses: List<MemberAnalysis>,
    val routeNodeIds: List<String>,
    val stars: Int,
    val feedback: List<String>
) {
    val budgetUsedRatio: Double get() = if (budget <= 0.0) 0.0 else totalCost / budget
}
