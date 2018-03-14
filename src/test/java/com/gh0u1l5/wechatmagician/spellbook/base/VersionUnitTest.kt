package com.gh0u1l5.wechatmagician.spellbook.base

import org.junit.Test as Test
import org.junit.Assert.*

class VersionUnitTest {
    @Test fun testVersionCompare() {
        assertTrue(Version("1") < Version("2"))
        assertTrue(Version("1") == Version("1"))
        assertTrue(Version("1.2") < Version("1.2.1"))
        assertTrue(Version("1.12") > Version("1.1"))

        assertTrue(Version("6.5.8") > Version("6.5.4"))
        assertTrue(Version("6.5.8") >= Version("6.5.4"))
        assertTrue(Version("6.5.4") >= Version("6.5.4"))
        assertTrue(Version("6.5.4") == Version("6.5.4"))
    }
}