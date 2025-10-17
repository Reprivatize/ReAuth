/*
 * ReAuth-Backend (ReAuth-Backend.plugin.api.main): RASSessionService.kt
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

package reprivatize.reauth.service

import reprivatize.reauth.data.RASSession
import java.util.*

interface RASSessionService {

    suspend fun create(): String

    suspend fun read(uuid: UUID): RASSession?

    suspend fun delete(uuid: UUID)

    suspend fun isValid(uuid: UUID, clientMacTagBase64: String): Boolean
    suspend fun isValid(uuid: UUID, clientMacTag: ByteArray): Boolean
    suspend fun isExpired(uuid: UUID): Boolean

    suspend fun session(uuid: UUID): RASSession?
}