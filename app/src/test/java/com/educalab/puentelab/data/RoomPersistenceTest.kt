package com.educalab.puentelab.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.educalab.puentelab.data.local.PuenteLabDatabase
import com.educalab.puentelab.data.local.entity.*
import com.educalab.puentelab.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Pruebas de persistencia con Room en memoria (Robolectric). No se ejecutaron en este entorno
 * (sin Android SDK/Gradle disponibles, ver docs/BUILD_REPORT.md) pero siguen la API real de
 * Room 2.6.1 / Robolectric 4.13 y deben correr con `./gradlew testDebugUnitTest` en un entorno
 * con SDK de Android.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class RoomPersistenceTest {

    private lateinit var db: PuenteLabDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PuenteLabDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun `guardar y leer un material persiste correctamente`() = runBlocking {
        val material = MaterialEntity("wood", "Madera", "d", 40.0, 3.0, 0.15, setOf(MemberRole.DECK), "#B4783C", "icon", 1)
        db.materialDao().insertAll(listOf(material))
        val fetched = db.materialDao().getById("wood")
        assertEquals("Madera", fetched?.name)
    }

    @Test
    fun `insertar diseno con nodos y barras se puede leer junto`() = runBlocking {
        seedMinimalChallenge()
        val userId = UserProfileEntity.LOCAL_USER_ID
        db.userProfileDao().upsert(UserProfileEntity(userId, "Test", "avatar_casco_naranja", 0L))
        val design = BridgeDesignEntity("d1", "c1", userId, "Mi puente", 0L, 0L, isSaved = false)
        val nodeL = BridgeNodeEntity("n1", "d1", 0.0, 0.0, AnchorSide.LEFT, true, false)
        val nodeR = BridgeNodeEntity("n2", "d1", 6.0, 0.0, AnchorSide.RIGHT, true, false)
        val member = BridgeMemberEntity("m1", "d1", "n1", "n2", "wood", MemberRole.DECK, StructureType.BEAM)

        db.materialDao().insertAll(listOf(MaterialEntity("wood", "Madera", "d", 40.0, 3.0, 0.15, setOf(MemberRole.DECK), "#B4783C", "icon", 1)))
        db.designDao().replaceStructure(design, listOf(nodeL, nodeR), listOf(member))

        val loaded = db.designDao().getDesignWithStructure("d1")
        assertNotNull(loaded)
        assertEquals(2, loaded!!.nodes.size)
        assertEquals(1, loaded.members.size)
    }

    @Test
    fun `replaceStructure reemplaza en vez de acumular filas`() = runBlocking {
        seedMinimalChallenge()
        val userId = UserProfileEntity.LOCAL_USER_ID
        db.userProfileDao().upsert(UserProfileEntity(userId, "Test", "avatar_casco_naranja", 0L))
        db.materialDao().insertAll(listOf(MaterialEntity("wood", "Madera", "d", 40.0, 3.0, 0.15, setOf(MemberRole.DECK), "#B4783C", "icon", 1)))
        val design = BridgeDesignEntity("d1", "c1", userId, "Mi puente", 0L, 0L)
        val nodeL = BridgeNodeEntity("n1", "d1", 0.0, 0.0, AnchorSide.LEFT, true, false)
        val nodeR = BridgeNodeEntity("n2", "d1", 6.0, 0.0, AnchorSide.RIGHT, true, false)
        db.designDao().replaceStructure(design, listOf(nodeL, nodeR), emptyList())
        // segunda escritura con un nodo extra: no debe duplicar n1/n2
        val nodeM = BridgeNodeEntity("n3", "d1", 3.0, 0.0, AnchorSide.NONE, false, false)
        db.designDao().replaceStructure(design, listOf(nodeL, nodeR, nodeM), emptyList())

        val loaded = db.designDao().getDesignWithStructure("d1")
        assertEquals(3, loaded!!.nodes.size)
    }

    @Test
    fun `contador de disenios guardados respeta el cupo`() = runBlocking {
        seedMinimalChallenge()
        val userId = UserProfileEntity.LOCAL_USER_ID
        db.userProfileDao().upsert(UserProfileEntity(userId, "Test", "avatar_casco_naranja", 0L))
        repeat(3) { i ->
            db.designDao().insertDesign(BridgeDesignEntity("saved$i", "c1", userId, "Diseño $i", 0L, 0L, isSaved = true))
        }
        db.designDao().insertDesign(BridgeDesignEntity("draft1", "c1", userId, "Borrador", 0L, 0L, isSaved = false))
        assertEquals(3, db.designDao().countSavedDesigns(userId))
    }

    @Test
    fun `eliminar diseno borra tambien sus nodos por cascada`() = runBlocking {
        seedMinimalChallenge()
        val userId = UserProfileEntity.LOCAL_USER_ID
        db.userProfileDao().upsert(UserProfileEntity(userId, "Test", "avatar_casco_naranja", 0L))
        db.materialDao().insertAll(listOf(MaterialEntity("wood", "Madera", "d", 40.0, 3.0, 0.15, setOf(MemberRole.DECK), "#B4783C", "icon", 1)))
        val design = BridgeDesignEntity("d1", "c1", userId, "Mi puente", 0L, 0L)
        db.designDao().replaceStructure(design, listOf(BridgeNodeEntity("n1", "d1", 0.0, 0.0, AnchorSide.LEFT, true, false)), emptyList())
        db.designDao().deleteDesign("d1")
        val loaded = db.designDao().getDesignWithStructure("d1")
        assertNull(loaded)
    }

    @Test
    fun `progreso se actualiza con upsert sin duplicar filas`() = runBlocking {
        seedMinimalChallenge()
        val userId = UserProfileEntity.LOCAL_USER_ID
        db.userProfileDao().upsert(UserProfileEntity(userId, "Test", "avatar_casco_naranja", 0L))
        val id = "p1"
        db.progressDao().upsert(ProgressEntity(id, userId, "c1", ModuleState.STARTED, 0, 1, null))
        db.progressDao().upsert(ProgressEntity(id, userId, "c1", ModuleState.COMPLETED, 2, 2, 123L))
        val all = db.progressDao().observeAll(userId).first()
        assertEquals(1, all.size)
        assertEquals(ModuleState.COMPLETED, all.first().state)
        assertEquals(2, all.first().bestStars)
    }

    @Test
    fun `insertar insignia de usuario dos veces no duplica (IGNORE)`() = runBlocking {
        val userId = UserProfileEntity.LOCAL_USER_ID
        db.userProfileDao().upsert(UserProfileEntity(userId, "Test", "avatar_casco_naranja", 0L))
        db.badgeDao().insertAll(listOf(BadgeEntity(BadgeId.PRIMER_PUENTE, "Primer Puente", "d", "icon")))
        db.userBadgeDao().insert(UserBadgeEntity("ub1", userId, BadgeId.PRIMER_PUENTE, 1L))
        db.userBadgeDao().insert(UserBadgeEntity("ub1", userId, BadgeId.PRIMER_PUENTE, 2L)) // mismo id -> IGNORE
        val unlocked = db.userBadgeDao().getUnlockedIds(userId)
        assertEquals(1, unlocked.size)
    }

    @Test
    fun `perfil vacio devuelve null antes de crearse`() = runBlocking {
        assertNull(db.userProfileDao().get())
    }

    @Test
    fun `actualizar xp y nivel persiste el valor mas reciente`() = runBlocking {
        val userId = UserProfileEntity.LOCAL_USER_ID
        db.userProfileDao().upsert(UserProfileEntity(userId, "Test", "avatar_casco_naranja", 0L))
        db.userProfileDao().updateXpAndLevel(500, 4)
        val profile = db.userProfileDao().get()
        assertEquals(500, profile?.cachedXp)
        assertEquals(4, profile?.cachedLevel)
    }

    @Test
    fun `consultar desafio inexistente devuelve null sin lanzar excepcion`() = runBlocking {
        assertNull(db.challengeDao().getById("no_existe"))
    }

    private suspend fun seedMinimalChallenge() {
        db.challengeDao().insertAll(
            listOf(
                BridgeChallengeEntity(
                    "c1", ScenarioType.RIVER, 1, "Nivel 1", 6.0, 0.0, 0.0, 6.0, 0.0,
                    emptyList(), 100.0, DemandLevel.LOW, 0.6, 0.1, 0.25, 0.75, StructureType.BEAM, "x", "y"
                )
            )
        )
    }
}
