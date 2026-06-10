package com.noteapp.shared.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class ExportMarkdownUseCaseTest {

    private val useCase = ExportMarkdownUseCase()

    @Test
    fun `converts h1 to markdown heading`() {
        assertEquals("# Title", useCase.execute("<h1>Title</h1>").trim())
    }

    @Test
    fun `converts h2 to markdown heading`() {
        assertEquals("## Title", useCase.execute("<h2>Title</h2>").trim())
    }

    @Test
    fun `converts bold to markdown bold`() {
        assertEquals("**hello**", useCase.execute("<strong>hello</strong>").trim())
    }

    @Test
    fun `converts italic to markdown italic`() {
        assertEquals("_hello_", useCase.execute("<em>hello</em>").trim())
    }

    @Test
    fun `converts inline code to markdown code`() {
        assertEquals("`code`", useCase.execute("<code>code</code>").trim())
    }

    @Test
    fun `converts list items to markdown bullets`() {
        val result = useCase.execute("<ul><li>Item 1</li><li>Item 2</li></ul>")
        assert(result.contains("- Item 1")) { "Expected '- Item 1' in: $result" }
        assert(result.contains("- Item 2")) { "Expected '- Item 2' in: $result" }
    }

    @Test
    fun `strips remaining html tags`() {
        assertEquals("plain text", useCase.execute("<p>plain text</p>").trim())
    }

    @Test
    fun `decodes html entities`() {
        assertEquals("a & b < c > d", useCase.execute("a &amp; b &lt; c &gt; d").trim())
    }
}
