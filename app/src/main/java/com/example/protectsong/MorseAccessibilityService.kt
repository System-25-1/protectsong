import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MorseAccessibilityService : AccessibilityService() {

    private val volumePattern = mutableListOf<Char>()
    private var lastKeyTime = 0L
    private val PATTERN_TIMEOUT = 3000L
    private val SOS_PATTERN = listOf('u', 'u', 'u', 'd', 'd', 'd', 'u', 'u', 'u')

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event?.action != KeyEvent.ACTION_DOWN) return false

        val now = System.currentTimeMillis()
        if (now - lastKeyTime > PATTERN_TIMEOUT) {
            volumePattern.clear()
        }
        lastKeyTime = now

        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> volumePattern.add('u')
            KeyEvent.KEYCODE_VOLUME_DOWN -> volumePattern.add('d')
            else -> return false
        }

        if (volumePattern.size >= 9 && volumePattern.takeLast(9) == SOS_PATTERN) {
            volumePattern.clear()
            callEmergencyNumber()
        }

        return super.onKeyEvent(event)
    }

    private fun callEmergencyNumber() {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:112")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "전화 권한이 없어 신고할 수 없습니다.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 이벤트 필요 없음. 비워두기.
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        Toast.makeText(this, "모스부호 접근성 서비스 작동 중", Toast.LENGTH_SHORT).show()
    }
}
