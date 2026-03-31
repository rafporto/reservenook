package com.reservenook.config

import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

data class LocalizedEmailMessage(
    val subject: String,
    val title: String,
    val intro: String,
    val actionLabel: String,
    val footerNote: String
)

@Component
class LocalizedEmailMessageFactory {

    fun activation(language: String): LocalizedEmailMessage =
        when (normalize(language)) {
            "de" -> LocalizedEmailMessage(
                subject = "Aktivieren Sie Ihr Reservenook-Konto",
                title = "Aktivieren Sie Ihr ReserveNook-Konto",
                intro = "Vielen Dank fuer Ihre Registrierung bei ReserveNook. Bitte bestaetigen Sie Ihre E-Mail-Adresse, um Ihren Firmenbereich zu aktivieren und die Einrichtung abzuschliessen.",
                actionLabel = "Konto aktivieren",
                footerNote = "Sie erhalten diese E-Mail, weil fuer Ihr ReserveNook-Konto eine Registrierung abgeschlossen wurde."
            )
            "pt" -> LocalizedEmailMessage(
                subject = "Ative a sua conta Reservenook",
                title = "Ative a sua conta ReserveNook",
                intro = "Obrigado por criar a sua conta ReserveNook. Confirme o seu endereco de email para ativar o espaco da sua empresa e concluir a configuracao inicial.",
                actionLabel = "Ativar conta",
                footerNote = "Recebeu este email porque foi concluido um registo para a sua conta ReserveNook."
            )
            else -> LocalizedEmailMessage(
                subject = "Activate your Reservenook account",
                title = "Activate your ReserveNook account",
                intro = "Thank you for creating your ReserveNook account. Please confirm your email address to activate your company workspace and complete the setup process.",
                actionLabel = "Activate your account",
                footerNote = "You are receiving this email because a registration was completed for your ReserveNook account."
            )
        }

    fun passwordReset(language: String): LocalizedEmailMessage =
        when (normalize(language)) {
            "de" -> LocalizedEmailMessage(
                subject = "Setzen Sie Ihr Reservenook-Passwort zurueck",
                title = "Setzen Sie Ihr ReserveNook-Passwort zurueck",
                intro = "Wir haben eine Anfrage zum Zuruecksetzen des Passworts fuer Ihr ReserveNook-Konto erhalten. Verwenden Sie den sicheren Link unten, um ein neues Passwort festzulegen.",
                actionLabel = "Neues Passwort festlegen",
                footerNote = "Wenn Sie diese Anfrage nicht gestellt haben, koennen Sie diese E-Mail ignorieren."
            )
            "pt" -> LocalizedEmailMessage(
                subject = "Redefina a sua palavra-passe Reservenook",
                title = "Redefina a sua palavra-passe ReserveNook",
                intro = "Recebemos um pedido para redefinir a palavra-passe da sua conta ReserveNook. Utilize a ligacao segura abaixo para escolher uma nova palavra-passe.",
                actionLabel = "Definir nova palavra-passe",
                footerNote = "Se nao solicitou esta alteracao, pode ignorar esta mensagem."
            )
            else -> LocalizedEmailMessage(
                subject = "Reset your Reservenook password",
                title = "Reset your ReserveNook password",
                intro = "We received a request to reset the password for your ReserveNook account. Use the secure link below to choose a new password.",
                actionLabel = "Choose a new password",
                footerNote = "If you did not request this change, you can safely ignore this email."
            )
        }

    fun inactivity(language: String, companyName: String): LocalizedEmailMessage =
        when (normalize(language)) {
            "de" -> LocalizedEmailMessage(
                subject = "Ihr Reservenook-Unternehmen ist inaktiv",
                title = "Ihr ReserveNook-Unternehmen ist inaktiv",
                intro = "Der Firmenbereich fuer $companyName wurde als inaktiv markiert, weil keine aktuelle Aktivitaet festgestellt wurde. Bitte melden Sie sich an, um den Kontostatus zu pruefen und bei Bedarf zu reagieren.",
                actionLabel = "Konto pruefen",
                footerNote = "Diese Benachrichtigung wurde gesendet, weil Ihr Unternehmen gemaess der aktuellen Plattformrichtlinie in den Inaktivitaetsstatus uebergegangen ist."
            )
            "pt" -> LocalizedEmailMessage(
                subject = "A sua empresa Reservenook esta inativa",
                title = "A sua empresa ReserveNook esta inativa",
                intro = "O espaco da empresa $companyName foi marcado como inativo porque nao foi detetada atividade recente. Inicie sessao para rever o estado da conta e tomar as acoes necessarias.",
                actionLabel = "Rever conta",
                footerNote = "Esta notificacao foi enviada porque a sua empresa entrou no estado de inatividade de acordo com a politica atual da plataforma."
            )
            else -> LocalizedEmailMessage(
                subject = "Your Reservenook company is inactive",
                title = "Your ReserveNook company is inactive",
                intro = "The company workspace for $companyName has been marked as inactive because no recent activity was detected. Please sign in to review the account and take any necessary action.",
                actionLabel = "Review your account",
                footerNote = "This notification was sent because your company has entered the inactive lifecycle state under the current platform policy."
            )
        }

    fun deletionWarning(language: String, companyName: String, deletionScheduledAt: Instant): LocalizedEmailMessage {
        val formattedDate = formatDate(deletionScheduledAt, language)
        return when (normalize(language)) {
            "de" -> LocalizedEmailMessage(
                subject = "Die Loeschung Ihres Reservenook-Unternehmens ist geplant",
                title = "Die Loeschung Ihres ReserveNook-Unternehmens ist geplant",
                intro = "Der Firmenbereich fuer $companyName ist fuer den $formattedDate zur Loeschung vorgesehen. Um die Loeschung zu vermeiden, melden Sie sich bitte vorher an und nehmen Sie die Nutzung wieder auf.",
                actionLabel = "Arbeitsbereich aufrufen",
                footerNote = "Diese Warnung wurde gesendet, weil sich Ihr Unternehmen innerhalb des konfigurierten Vorwarnzeitraums vor der Loeschung befindet."
            )
            "pt" -> LocalizedEmailMessage(
                subject = "A eliminacao da sua empresa Reservenook esta agendada",
                title = "A eliminacao da sua empresa ReserveNook esta agendada",
                intro = "O espaco da empresa $companyName esta agendado para eliminacao em $formattedDate. Para evitar a eliminacao, inicie sessao e retome a atividade antes dessa data.",
                actionLabel = "Aceder ao espaco",
                footerNote = "Este aviso foi enviado porque a sua empresa entrou no periodo de aviso previo para eliminacao definido na politica atual da plataforma."
            )
            else -> LocalizedEmailMessage(
                subject = "Your Reservenook company is scheduled for deletion",
                title = "Your ReserveNook company is scheduled for deletion",
                intro = "The company workspace for $companyName is scheduled for deletion on $formattedDate. To avoid deletion, please sign in and resume activity before that date.",
                actionLabel = "Access your workspace",
                footerNote = "This warning was sent because your company is within the configured deletion warning window under the current platform policy."
            )
        }
    }

    private fun normalize(language: String): String = language.trim().lowercase().ifBlank { "en" }

    private fun formatDate(value: Instant, language: String): String {
        val locale = when (normalize(language)) {
            "de" -> Locale.GERMANY
            "pt" -> Locale.forLanguageTag("pt-PT")
            else -> Locale.UK
        }

        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
            .withLocale(locale)
            .withZone(ZoneOffset.UTC)
            .format(value)
    }
}
