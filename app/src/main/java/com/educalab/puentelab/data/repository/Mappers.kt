package com.educalab.puentelab.data.repository

import com.educalab.puentelab.data.local.entity.*
import com.educalab.puentelab.domain.model.*

fun MaterialEntity.toDomain(): MaterialSpec = MaterialSpec(
    id = id, name = name, description = description, strength = strength,
    costPerUnit = costPerUnit, weightFactor = weightFactor, allowedRoles = allowedRoles,
    unlockLevel = unlockLevel
)

fun BridgeChallengeEntity.toDomain(): BridgeChallengeSpec = BridgeChallengeSpec(
    id = id, scenario = scenario, orderIndex = orderIndex, name = name, spanUnits = spanUnits,
    leftBank = GridPoint(leftBankX, leftBankY), rightBank = GridPoint(rightBankX, rightBankY),
    fixedSupports = fixedSupports, budget = budget, demand = demand, maxSlope = maxSlope,
    starThresholds = StarThresholds(budgetMarginFor2Stars, budgetMarginFor3Stars, maxStressFor3Stars),
    recommendedStructure = recommendedStructure, narrativeIntro = narrativeIntro, narrativeSuccess = narrativeSuccess
)

fun BridgeNodeEntity.toDomain(): BridgeNode = BridgeNode(id, GridPoint(x, y), anchorSide, isFixedByLevel, isUserPier)

fun BridgeNode.toEntity(designId: String): BridgeNodeEntity = BridgeNodeEntity(
    id = id, designId = designId, x = point.x, y = point.y,
    anchorSide = anchorSide, isFixedByLevel = isFixedByLevel, isUserPier = isUserPier
)

fun BridgeMemberEntity.toDomain(): BridgeMember = BridgeMember(id, nodeAId, nodeBId, materialId, role, structureType)

fun BridgeMember.toEntity(designId: String): BridgeMemberEntity = BridgeMemberEntity(
    id = id, designId = designId, nodeAId = nodeAId, nodeBId = nodeBId,
    materialId = materialId, role = role, structureType = structureType
)

fun DesignWithStructure.toDomain(): BridgeDesignSpec = BridgeDesignSpec(
    id = design.id, challengeId = design.challengeId, name = design.name,
    nodes = nodes.map { it.toDomain() }, members = members.map { it.toDomain() }
)
