package com.noteapp.shared.domain.usecase

class ExportMarkdownUseCase {
    fun execute(htmlContent: String): String = htmlContent
        .replace(Regex("<h1[^>]*>(.*?)</h1>", RegexOption.DOT_MATCHES_ALL)) { "# ${it.groupValues[1]}\n" }
        .replace(Regex("<h2[^>]*>(.*?)</h2>", RegexOption.DOT_MATCHES_ALL)) { "## ${it.groupValues[1]}\n" }
        .replace(Regex("<strong[^>]*>(.*?)</strong>", RegexOption.DOT_MATCHES_ALL)) { "**${it.groupValues[1]}**" }
        .replace(Regex("<b[^>]*>(.*?)</b>", RegexOption.DOT_MATCHES_ALL)) { "**${it.groupValues[1]}**" }
        .replace(Regex("<em[^>]*>(.*?)</em>", RegexOption.DOT_MATCHES_ALL)) { "_${it.groupValues[1]}_" }
        .replace(Regex("<i[^>]*>(.*?)</i>", RegexOption.DOT_MATCHES_ALL)) { "_${it.groupValues[1]}_" }
        .replace(Regex("<code[^>]*>(.*?)</code>", RegexOption.DOT_MATCHES_ALL)) { "`${it.groupValues[1]}`" }
        .replace(Regex("<li[^>]*>(.*?)</li>", RegexOption.DOT_MATCHES_ALL)) { "- ${it.groupValues[1]}\n" }
        .replace(Regex("<[^>]+>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&nbsp;", " ")
        .trim()
}
