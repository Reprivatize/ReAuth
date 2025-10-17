/*
 * ReAuth-Backend (ReAuth-Backend.core.main): HmacSha256.kt
 * Copyright (C) 2025 mtctx
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the **GNU General Public License** as published
 * by the Free Software Foundation, either **version 3** of the License, or
 * (at your option) any later version.
 *
 * *This program is distributed WITHOUT ANY WARRANTY;** see the
 * GNU General Public License for more details, which you should have
 * received with this program.
 *
 * SPDX-FileCopyrightText: 2025 mtctx
 * SPDX-License-Identifier: GPL-3.0-only
 */

package reprivatize.reauth.crypto

import mtctx.utilities.crypto.SECURE_RANDOM
import mtctx.utilities.crypto.secureEquals
import org.bouncycastle.crypto.Mac
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.params.KeyParameter

class HmacSha256 {
    companion object {
        private const val KEY_SIZE = 32

        @JvmStatic
        fun generateKey(size: Int = KEY_SIZE): ByteArray {
            val key = ByteArray(size)
            SECURE_RANDOM.nextBytes(key)
            return key
        }

        @JvmStatic
        fun generate(input: String, key: ByteArray = generateKey()): Result {
            val hmac: Mac = HMac(SHA256Digest())
            hmac.init(KeyParameter(key))

            val inputBytes = input.toByteArray(Charsets.UTF_8)
            hmac.update(inputBytes, 0, inputBytes.size)

            val result = ByteArray(hmac.macSize)
            hmac.doFinal(result, 0)

            return Result(result, key)
        }

        @JvmStatic
        fun verify(input: String, tag: ByteArray, key: ByteArray): Boolean =
            generate(input, key).tag.secureEquals(tag)

        fun String.generateMacWithHmacSha256(key: ByteArray = generateKey()): Result = generate(this, key)
        fun String.verifyMacWithHmacSha256(tag: ByteArray, key: ByteArray): Boolean = verify(this, tag, key)
        fun ByteArray.verifyMacWithHmacSha256(unhashed: String, key: ByteArray): Boolean = verify(unhashed, this, key)
    }

    class Result(val tag: ByteArray, val key: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Result

            if (!tag.contentEquals(other.tag)) return false
            if (!key.contentEquals(other.key)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = tag.contentHashCode()
            result = 31 * result + key.contentHashCode()
            return result
        }
    }
}