package com.example.epsviewer.domain.model

import org.junit.Test
import org.junit.Assert.*

class ResultTest {

    @Test
    fun `Success result returns data`() {
        val result: Result<String> = Result.Success("test")
        assertTrue(result.isSuccess())
        assertEquals("test", result.getOrNull())
        assertNull(result.exceptionOrNull())
    }

    @Test
    fun `Error result contains exception`() {
        val exception = Exception("test error")
        val result: Result<String> = Result.Error(exception, "test error")
        assertTrue(result.isError())
        assertNull(result.getOrNull())
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `Loading result is loading state`() {
        val result: Result<String> = Result.Loading
        assertTrue(result.isLoading())
        assertFalse(result.isSuccess())
        assertFalse(result.isError())
    }
}

