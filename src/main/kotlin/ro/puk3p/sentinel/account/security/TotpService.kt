package ro.puk3p.sentinel.account.security

import dev.samstevens.totp.code.DefaultCodeGenerator
import dev.samstevens.totp.code.DefaultCodeVerifier
import dev.samstevens.totp.code.HashingAlgorithm
import dev.samstevens.totp.qr.QrData
import dev.samstevens.totp.qr.ZxingPngQrGenerator
import dev.samstevens.totp.secret.DefaultSecretGenerator
import dev.samstevens.totp.time.SystemTimeProvider
import dev.samstevens.totp.util.Utils
import org.springframework.stereotype.Service

/** TOTP (RFC 6238) helpers: secret generation, otpauth/QR rendering, code checks. */
@Service
class TotpService {
    private val secretGenerator = DefaultSecretGenerator()
    private val qrGenerator = ZxingPngQrGenerator()
    private val verifier =
        DefaultCodeVerifier(DefaultCodeGenerator(), SystemTimeProvider()).apply {
            // Accept the adjacent time step on each side to tolerate small clock drift.
            setAllowedTimePeriodDiscrepancy(1)
        }

    fun newSecret(): String = secretGenerator.generate()

    private fun qrData(
        username: String,
        secret: String,
    ): QrData =
        QrData.Builder()
            .label("Sentinel ($username)")
            .secret(secret)
            .issuer("Sentinel")
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build()

    fun otpauthUri(
        username: String,
        secret: String,
    ): String = qrData(username, secret).uri

    /** A `data:image/png;base64,...` QR encoding the otpauth URI, for inline display. */
    fun qrDataUri(
        username: String,
        secret: String,
    ): String {
        val data = qrData(username, secret)
        val image = qrGenerator.generate(data)
        return Utils.getDataUriForImage(image, qrGenerator.imageMimeType)
    }

    fun verify(
        secret: String,
        code: String,
    ): Boolean = verifier.isValidCode(secret, code)
}
