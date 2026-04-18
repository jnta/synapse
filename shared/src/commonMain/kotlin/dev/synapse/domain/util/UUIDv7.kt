package dev.synapse.domain.util

import kotlinx.datetime.Clock
import kotlin.random.Random

object UUIDv7 {
    private const val TIMESTAMP_MASK = 0xFFFFFFFFFFFFL
    private const val VERSION_V7 = 0x7L
    private const val VARIANT_2 = 0x2L
    private const val TIMESTAMP_SHIFT = 16
    private const val VERSION_SHIFT = 12
    private const val VARIANT_SHIFT = 62
    private const val RAND_A_LIMIT = 4096
    private const val RAND_B_MASK = 0x3FFFFFFFFFFFFFFFL
    private const val HEX_RADIX = 16
    private const val HEX_LENGTH = 16

    fun generate(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        
        val ts48 = timestamp and TIMESTAMP_MASK
        val randA = Random.nextInt(0, RAND_A_LIMIT)
        val randB = Random.nextLong() and RAND_B_MASK
        
        val msb = (ts48 shl TIMESTAMP_SHIFT) or (VERSION_V7 shl VERSION_SHIFT) or randA.toLong()
        val lsb = (VARIANT_2 shl VARIANT_SHIFT) or randB
        
        return formatUuid(msb, lsb)
    }

    private fun formatUuid(msb: Long, lsb: Long): String {
        return buildString {
            append(msb.toHexString(0..7))
            append("-")
            append(msb.toHexString(8..11))
            append("-")
            append(msb.toHexString(12..15))
            append("-")
            append(lsb.toHexString(0..3))
            append("-")
            append(lsb.toHexString(4..15))
        }
    }

    private fun Long.toHexString(range: IntRange): String {
        val hex = this.toString(HEX_RADIX).padStart(HEX_LENGTH, '0')
        return hex.substring(range.first, range.last + 1)
    }
}
