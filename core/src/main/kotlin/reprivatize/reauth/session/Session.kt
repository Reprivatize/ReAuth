/*
 * ReAuth-Backend (ReAuth-Backend.core.main): Session.kt
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

package reprivatize.reauth.session

import kotlinx.serialization.Serializable
import reprivatize.reauth.data.RASSession
import reprivatize.reauth.reAuthServer
import reprivatize.reauth.serializer.UUIDSerializer
import java.util.*
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Serializable
@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class Session(
    @Serializable(with = UUIDSerializer::class)
    override val uuid: UUID,
    override val macKey: ByteArray,
    override val createdAt: Long,
    override val validFor: Duration = reAuthServer.config.session.validForDuration(),
) : RASSession {
    override fun expired(): Boolean =
        Instant.fromEpochMilliseconds(createdAt) + validFor < Clock.System.now()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Session

        if (createdAt != other.createdAt) return false
        if (uuid != other.uuid) return false
        if (!macKey.contentEquals(other.macKey)) return false
        if (validFor != other.validFor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = createdAt.hashCode()
        result = 31 * result + uuid.hashCode()
        result = 31 * result + macKey.contentHashCode()
        result = 31 * result + validFor.hashCode()
        return result
    }
}