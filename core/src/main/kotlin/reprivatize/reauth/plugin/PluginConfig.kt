/*
 * ReAuth-Backend (ReAuth-Backend.core.main): PluginConfig.kt
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

package reprivatize.reauth.plugin

import kotlinx.serialization.Serializable
import mtctx.pluggable.Config

@Serializable
data class PluginConfig(
    val name: String,
    val version: String,
    val author: String,
    val description: String = "",
    val website: String = "https://example.com/",
    override val mainClass: String = "com.example.reauth_plugin.MainKt",
) : Config