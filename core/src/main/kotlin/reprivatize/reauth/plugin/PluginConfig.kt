/*
 *     ReAuth-Backend: PluginConfig.kt
 *     Copyright (C) 2025 mtctx
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
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