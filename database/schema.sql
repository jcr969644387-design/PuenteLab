-- PuenteLab — schema.sql
-- Espejo en SQL puro del esquema Room (versión 1). Room genera su propio SQL internamente
-- desde las anotaciones @Entity; este archivo es una referencia legible y versionable,
-- pensada para lectura humana y para herramientas externas de inspección.

PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS user_profile (
    id TEXT PRIMARY KEY NOT NULL,
    alias TEXT NOT NULL,
    avatarId TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    soundEnabled INTEGER NOT NULL DEFAULT 1,
    hapticEnabled INTEGER NOT NULL DEFAULT 1,
    onboardingCompleted INTEGER NOT NULL DEFAULT 0,
    cachedXp INTEGER NOT NULL DEFAULT 0,
    cachedLevel INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS materials (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    strength REAL NOT NULL,
    costPerUnit REAL NOT NULL,
    weightFactor REAL NOT NULL,
    allowedRoles TEXT NOT NULL,       -- CSV: "DECK,BRACE,..."
    colorHex TEXT NOT NULL,
    iconKey TEXT NOT NULL,
    unlockLevel INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS bridge_challenges (
    id TEXT PRIMARY KEY NOT NULL,
    scenario TEXT NOT NULL,
    orderIndex INTEGER NOT NULL,
    name TEXT NOT NULL,
    spanUnits REAL NOT NULL,
    leftBankX REAL NOT NULL,
    leftBankY REAL NOT NULL,
    rightBankX REAL NOT NULL,
    rightBankY REAL NOT NULL,
    fixedSupports TEXT NOT NULL,      -- "x:y;x:y"
    budget REAL NOT NULL,
    demand TEXT NOT NULL,
    maxSlope REAL NOT NULL,
    budgetMarginFor2Stars REAL NOT NULL,
    budgetMarginFor3Stars REAL NOT NULL,
    maxStressFor3Stars REAL NOT NULL,
    recommendedStructure TEXT,
    narrativeIntro TEXT NOT NULL,
    narrativeSuccess TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS bridge_designs (
    id TEXT PRIMARY KEY NOT NULL,
    challengeId TEXT NOT NULL REFERENCES bridge_challenges(id) ON DELETE CASCADE,
    userProfileId TEXT NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    isSaved INTEGER NOT NULL DEFAULT 0,
    duplicatedFromId TEXT
);
CREATE INDEX IF NOT EXISTS idx_designs_challenge ON bridge_designs(challengeId);
CREATE INDEX IF NOT EXISTS idx_designs_user ON bridge_designs(userProfileId);
CREATE INDEX IF NOT EXISTS idx_designs_saved ON bridge_designs(isSaved);

CREATE TABLE IF NOT EXISTS bridge_nodes (
    id TEXT PRIMARY KEY NOT NULL,
    designId TEXT NOT NULL REFERENCES bridge_designs(id) ON DELETE CASCADE,
    x REAL NOT NULL,
    y REAL NOT NULL,
    anchorSide TEXT NOT NULL,
    isFixedByLevel INTEGER NOT NULL,
    isUserPier INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_nodes_design ON bridge_nodes(designId);

CREATE TABLE IF NOT EXISTS bridge_members (
    id TEXT PRIMARY KEY NOT NULL,
    designId TEXT NOT NULL REFERENCES bridge_designs(id) ON DELETE CASCADE,
    nodeAId TEXT NOT NULL,
    nodeBId TEXT NOT NULL,
    materialId TEXT NOT NULL REFERENCES materials(id) ON DELETE RESTRICT,
    role TEXT NOT NULL,
    structureType TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_members_design ON bridge_members(designId);
CREATE INDEX IF NOT EXISTS idx_members_material ON bridge_members(materialId);

CREATE TABLE IF NOT EXISTS vehicles (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    iconKey TEXT NOT NULL,
    themeScenario TEXT NOT NULL,
    weightMultiplier REAL NOT NULL,
    unlockLevel INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS simulation_runs (
    id TEXT PRIMARY KEY NOT NULL,
    designId TEXT NOT NULL REFERENCES bridge_designs(id) ON DELETE CASCADE,
    challengeId TEXT NOT NULL REFERENCES bridge_challenges(id) ON DELETE CASCADE,
    vehicleId TEXT NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    ranAt INTEGER NOT NULL,
    attemptNumber INTEGER NOT NULL,
    passed INTEGER NOT NULL,
    totalCost REAL NOT NULL,
    budget REAL NOT NULL,
    budgetRemaining REAL NOT NULL,
    maxStressRatio REAL NOT NULL,
    weakestMemberId TEXT,
    stars INTEGER NOT NULL,
    structureTypesUsed TEXT NOT NULL,
    feedback TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_runs_design ON simulation_runs(designId);
CREATE INDEX IF NOT EXISTS idx_runs_challenge ON simulation_runs(challengeId);
CREATE INDEX IF NOT EXISTS idx_runs_vehicle ON simulation_runs(vehicleId);
CREATE INDEX IF NOT EXISTS idx_runs_ranat ON simulation_runs(ranAt);

CREATE TABLE IF NOT EXISTS simulation_results (
    id TEXT PRIMARY KEY NOT NULL,
    simulationRunId TEXT NOT NULL REFERENCES simulation_runs(id) ON DELETE CASCADE,
    memberId TEXT NOT NULL,
    length REAL NOT NULL,
    cost REAL NOT NULL,
    capacity REAL NOT NULL,
    demand REAL NOT NULL,
    stressRatio REAL NOT NULL,
    role TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_results_run ON simulation_results(simulationRunId);

CREATE TABLE IF NOT EXISTS builder_stamps (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    scenario TEXT NOT NULL,
    iconKey TEXT NOT NULL,
    unlockChallengeId TEXT,
    unlockBadgeId TEXT
);

CREATE TABLE IF NOT EXISTS user_stamps (
    id TEXT PRIMARY KEY NOT NULL,
    userProfileId TEXT NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    stampId TEXT NOT NULL REFERENCES builder_stamps(id) ON DELETE CASCADE,
    unlockedAt INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_userstamps_user ON user_stamps(userProfileId);
CREATE INDEX IF NOT EXISTS idx_userstamps_stamp ON user_stamps(stampId);

CREATE TABLE IF NOT EXISTS progress (
    id TEXT PRIMARY KEY NOT NULL,
    userProfileId TEXT NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    challengeId TEXT NOT NULL REFERENCES bridge_challenges(id) ON DELETE CASCADE,
    state TEXT NOT NULL,
    bestStars INTEGER NOT NULL,
    attemptsCount INTEGER NOT NULL,
    firstPassedAt INTEGER
);
CREATE INDEX IF NOT EXISTS idx_progress_user ON progress(userProfileId);
CREATE INDEX IF NOT EXISTS idx_progress_challenge ON progress(challengeId);

CREATE TABLE IF NOT EXISTS badges (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    iconKey TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS user_badges (
    id TEXT PRIMARY KEY NOT NULL,
    userProfileId TEXT NOT NULL REFERENCES user_profile(id) ON DELETE CASCADE,
    badgeId TEXT NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
    unlockedAt INTEGER NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_userbadges_user ON user_badges(userProfileId);
CREATE INDEX IF NOT EXISTS idx_userbadges_badge ON user_badges(badgeId);
