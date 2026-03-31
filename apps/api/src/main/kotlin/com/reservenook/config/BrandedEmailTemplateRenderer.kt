package com.reservenook.config

import com.reservenook.registration.application.RegistrationProperties
import org.springframework.stereotype.Component

data class BrandedEmailContent(
    val plainText: String,
    val html: String
)

@Component
class BrandedEmailTemplateRenderer(
    private val registrationProperties: RegistrationProperties
) {

    val publicBaseUrl: String
        get() = registrationProperties.publicBaseUrl.trimEnd('/')

    fun render(
        title: String,
        intro: String,
        actionLabel: String? = null,
        actionUrl: String? = null,
        footerNote: String = "You are receiving this email because of activity in your ReserveNook account."
    ): BrandedEmailContent {
        val appBaseUrl = publicBaseUrl
        val escapedTitle = escapeHtml(title)
        val escapedIntro = escapeHtml(intro)
        val escapedFooterNote = escapeHtml(footerNote)
        val actionSectionHtml = if (actionLabel != null && actionUrl != null) {
            """
            <tr>
              <td style="padding:0 32px 32px 32px;">
                <a href="$actionUrl" style="display:inline-block;padding:14px 22px;border-radius:999px;background:#b45a38;color:#fff8f2;text-decoration:none;font-weight:700;">
                  ${escapeHtml(actionLabel)}
                </a>
              </td>
            </tr>
            <tr>
              <td style="padding:0 32px 32px 32px;color:#6d6258;font-size:14px;line-height:1.5;">
                If the button does not work, use this link:<br />
                <a href="$actionUrl" style="color:#b45a38;text-decoration:none;">${escapeHtml(actionUrl)}</a>
              </td>
            </tr>
            """.trimIndent()
        } else {
            ""
        }
        val html = """
        <html lang="en">
          <body style="margin:0;padding:24px;background:#f4f1ea;font-family:Segoe UI,Helvetica Neue,Arial,sans-serif;color:#1f1b16;">
            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
              <tr>
                <td align="center">
                  <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="max-width:640px;border-collapse:collapse;">
                    <tr>
                      <td style="padding:0 0 16px 0;">
                        <a href="$appBaseUrl" style="display:inline-block;text-decoration:none;">
                          <img src="$appBaseUrl/reservenook-logo.svg" alt="ReserveNook" width="220" style="display:block;border:0;height:auto;" />
                        </a>
                      </td>
                    </tr>
                    <tr>
                      <td style="background:#fffaf2;border:1px solid #d9cfc1;border-radius:24px;overflow:hidden;">
                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="border-collapse:collapse;">
                          <tr>
                            <td style="padding:32px 32px 16px 32px;font-size:28px;font-weight:700;color:#241a16;">
                              $escapedTitle
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:0 32px 28px 32px;color:#6d6258;font-size:16px;line-height:1.7;">
                              $escapedIntro
                            </td>
                          </tr>
                          $actionSectionHtml
                        </table>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:18px 8px 0 8px;color:#6d6258;font-size:13px;line-height:1.6;">
                        $escapedFooterNote<br />
                        <a href="$appBaseUrl" style="color:#b45a38;text-decoration:none;">$appBaseUrl</a>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </body>
        </html>
        """.trimIndent()

        val plainText = buildString {
            appendLine(title)
            appendLine()
            appendLine(intro)
            if (actionLabel != null && actionUrl != null) {
                appendLine()
                appendLine("$actionLabel: $actionUrl")
            }
            appendLine()
            appendLine(footerNote)
            appendLine(appBaseUrl)
        }.trim()

        return BrandedEmailContent(
            plainText = plainText,
            html = html
        )
    }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
}
