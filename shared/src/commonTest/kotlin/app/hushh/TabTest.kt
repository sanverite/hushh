package app.hushh

import kotlin.test.Test
import kotlin.test.assertEquals

class TabTest {
    @Test
    fun bottomBarIsNearbyPeopleSettingsWithNearbyFirst() {
        assertEquals(listOf(Tab.Nearby, Tab.People, Tab.Settings), Tab.entries)
    }
}
