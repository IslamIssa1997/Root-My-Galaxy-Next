package dev.busung.s25uroot

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdaterTest {

    @Test
    fun aBuildSuffixIsNotAnUpdateOverItsOwnVersion() {
        // Every build carries this suffix so two installs of a version can be told apart; treating
        // it as a difference offered the release already installed as an update, forever.
        assertFalse(
            AppUpdater.isUpdateAvailable(
                latestVersion = "0.2.65",
                currentVersion = "0.2.65+local.4f9a2c1",
            ),
        )
        assertFalse(
            AppUpdater.isUpdateAvailable(
                latestVersion = "0.2.65",
                currentVersion = "0.2.65+ci.42.4f9a2c1",
            ),
        )
    }

    @Test
    fun aNewerReleaseIsAnUpdate() {
        assertTrue(
            AppUpdater.isUpdateAvailable(
                latestVersion = "0.2.66",
                currentVersion = "0.2.65+ci.42.4f9a2c1",
            ),
        )
        assertTrue(
            AppUpdater.isUpdateAvailable(
                latestVersion = "v0.3.0",
                currentVersion = "0.2.65+local.4f9a2c1",
            ),
        )
    }

    @Test
    fun anOlderOrEqualReleaseIsNotAnUpdate() {
        assertFalse(AppUpdater.isUpdateAvailable("0.2.64", "0.2.65+local"))
        assertFalse(AppUpdater.isUpdateAvailable("0.2.65", "0.2.65"))
        assertFalse(AppUpdater.isUpdateAvailable("", "0.2.65"))
    }

    @Test
    fun versionPartsAreComparedAsNumbersNotText() {
        assertTrue(AppUpdater.isUpdateAvailable("0.2.10", "0.2.9"))
        assertTrue(AppUpdater.isUpdateAvailable("0.3", "0.2.9"))
        assertTrue(AppUpdater.compareVersions("1.0.0", "0.9.9") > 0)
        assertEquals(0, AppUpdater.compareVersions("0.2.65", "0.2.65.0"))
        assertTrue(AppUpdater.compareVersions("0.2.64", "0.2.65") < 0)
    }

    @Test
    fun aTagThatIsNotADottedVersionFallsBackToBeingDifferent() {
        assertNull(AppUpdater.versionBase("nightly"))
        assertTrue(AppUpdater.isUpdateAvailable("nightly", "0.2.65+local"))
        assertFalse(AppUpdater.isUpdateAvailable("nightly", "nightly"))
    }

    @Test
    fun versionBaseStripsTheDecoration() {
        assertEquals("0.2.65", AppUpdater.versionBase("0.2.65"))
        assertEquals("0.2.65", AppUpdater.versionBase("v0.2.65"))
        assertEquals("0.2.65", AppUpdater.versionBase("0.2.65+ci.42.4f9a2c1"))
        assertEquals("0.2.65", AppUpdater.versionBase("0.2.65-rc1"))
        assertNull(AppUpdater.versionBase(""))
    }

    @Test
    fun versionBaseExtractsADottedVersionFromACiTag() {
        // The CI workflow tags releases `ci-<base>-<run>`, e.g. `ci-1.0-60`. The older parser
        // split at the first `-` and refused the tag, which left `isUpdateAvailable` on the
        // string-diff fallback and offered an update on every fresh install.
        assertEquals("1.0", AppUpdater.versionBase("ci-1.0-60"))
        assertEquals("1.0.1", AppUpdater.versionBase("ci-1.0.1-3"))
        assertFalse(AppUpdater.isUpdateAvailable("ci-1.0-60", "1.0+ci.60.abc1234"))
        assertFalse(AppUpdater.isUpdateAvailable("ci-1.0.1-3", "1.0.1+ci.3.abc1234"))
    }
}
