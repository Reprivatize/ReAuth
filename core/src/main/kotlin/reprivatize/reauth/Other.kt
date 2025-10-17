/*
 *     ReAuth-Backend: Other.kt
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

package reprivatize.reauth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import mtctx.utilities.crypto.secureEquals

val localIps =
    listOf("127.0.0.1", "0.0.0.0", "::1", "0:0:0:0:0:0:0:1", "localhost", "127.0.1.1")

@Deprecated(
    "Use isInternalSecretBased() instead as its more secure!",
    ReplaceWith("isInternalSecretBased()"),
    DeprecationLevel.WARNING
)
fun ApplicationCall.isInternalIPBased(): Boolean = localIps.contains(request.origin.remoteHost)
fun ApplicationCall.isInternalSecretBased(): Boolean {
    var header = request.header(HttpHeaders.Authorization) ?: return false
    if (!header.startsWith("REAUTH:")) header = "REAUTH:$header"
    return header.encodeToByteArray() secureEquals "REAUTH:${reAuthServer.config.internalHostsSecretKey}".encodeToByteArray()
}


/**
 * ████  ████████████  ██████     ████  ████████████   ████████████   ██████████████
 * ████  ████████████  ██████     ████  ████████████   ████████████   ██████████████
 * ████ ████           ████████   ████  ████    ████   ████     ████  ████
 * ████ ████           ████████   ████  ████    ████   ████     ████  ████
 * ████ ████   ██████  ████ ████  ████  ████    ████   ███████████    ██████████
 * ████ ████   ██████  ████ ████  ████  ████    ████   ███████████    ██████████
 * ████ ████    ████   ████  ████ ████  ████    ████   ████    ████   ████
 * ████ ████    ████   ████  ████ ████  ████    ████   ████    ████   ████
 * ████  ████████████  ████   ████████  ████████████   ████    ████   ██████████████
 * ████  ████████████  ████   ████████  ████████████   ████    ████   ██████████████
 */

enum class RASHttpHeaders(val ktor: String) {
    Accept(HttpHeaders.Accept),
    AcceptCharset(HttpHeaders.AcceptCharset),
    AcceptEncoding(HttpHeaders.AcceptEncoding),
    AcceptLanguage(HttpHeaders.AcceptLanguage),
    AcceptRanges(HttpHeaders.AcceptRanges),
    Age(HttpHeaders.Age),
    Allow(HttpHeaders.Allow),
    ALPN(HttpHeaders.ALPN),
    AuthenticationInfo(HttpHeaders.AuthenticationInfo),
    Authorization(HttpHeaders.Authorization),
    CacheControl(HttpHeaders.CacheControl),
    Connection(HttpHeaders.Connection),
    ContentDisposition(HttpHeaders.ContentDisposition),
    ContentEncoding(HttpHeaders.ContentEncoding),
    ContentLanguage(HttpHeaders.ContentLanguage),
    ContentLength(HttpHeaders.ContentLength),
    ContentLocation(HttpHeaders.ContentLocation),
    ContentRange(HttpHeaders.ContentRange),
    ContentType(HttpHeaders.ContentType),
    Cookie(HttpHeaders.Cookie),
    DASL(HttpHeaders.DASL),
    Date(HttpHeaders.Date),
    DAV(HttpHeaders.DAV),
    Depth(HttpHeaders.Depth),
    Destination(HttpHeaders.Destination),
    ETag(HttpHeaders.ETag),
    Expect(HttpHeaders.Expect),
    Expires(HttpHeaders.Expires),
    From(HttpHeaders.From),
    Forwarded(HttpHeaders.Forwarded),
    Host(HttpHeaders.Host),
    HTTP2Settings(HttpHeaders.HTTP2Settings),
    If(HttpHeaders.If),
    IfMatch(HttpHeaders.IfMatch),
    IfModifiedSince(HttpHeaders.IfModifiedSince),
    IfNoneMatch(HttpHeaders.IfNoneMatch),
    IfRange(HttpHeaders.IfRange),
    IfScheduleTagMatch(HttpHeaders.IfScheduleTagMatch),
    IfUnmodifiedSince(HttpHeaders.IfUnmodifiedSince),
    LastModified(HttpHeaders.LastModified),
    Location(HttpHeaders.Location),
    LockToken(HttpHeaders.LockToken),
    Link(HttpHeaders.Link),
    MaxForwards(HttpHeaders.MaxForwards),
    MIMEVersion(HttpHeaders.MIMEVersion),
    OrderingType(HttpHeaders.OrderingType),
    Origin(HttpHeaders.Origin),
    Overwrite(HttpHeaders.Overwrite),
    Position(HttpHeaders.Position),
    Pragma(HttpHeaders.Pragma),
    Prefer(HttpHeaders.Prefer),
    PreferenceApplied(HttpHeaders.PreferenceApplied),
    ProxyAuthenticate(HttpHeaders.ProxyAuthenticate),
    ProxyAuthenticationInfo(HttpHeaders.ProxyAuthenticationInfo),
    ProxyAuthorization(HttpHeaders.ProxyAuthorization),
    PublicKeyPins(HttpHeaders.PublicKeyPins),
    PublicKeyPinsReportOnly(HttpHeaders.PublicKeyPinsReportOnly),
    Range(HttpHeaders.Range),
    Referrer(HttpHeaders.Referrer),
    RetryAfter(HttpHeaders.RetryAfter),
    ScheduleReply(HttpHeaders.ScheduleReply),
    ScheduleTag(HttpHeaders.ScheduleTag),
    SecWebSocketAccept(HttpHeaders.SecWebSocketAccept),
    SecWebSocketExtensions(HttpHeaders.SecWebSocketExtensions),
    SecWebSocketKey(HttpHeaders.SecWebSocketKey),
    SecWebSocketProtocol(HttpHeaders.SecWebSocketProtocol),
    SecWebSocketVersion(HttpHeaders.SecWebSocketVersion),
    Server(HttpHeaders.Server),
    SetCookie(HttpHeaders.SetCookie),
    SLUG(HttpHeaders.SLUG),
    StrictTransportSecurity(HttpHeaders.StrictTransportSecurity),
    TE(HttpHeaders.TE),
    Timeout(HttpHeaders.Timeout),
    Trailer(HttpHeaders.Trailer),
    TransferEncoding(HttpHeaders.TransferEncoding),
    Upgrade(HttpHeaders.Upgrade),
    UserAgent(HttpHeaders.UserAgent),
    Vary(HttpHeaders.Vary),
    Via(HttpHeaders.Via),
    Warning(HttpHeaders.Warning),
    WWWAuthenticate(HttpHeaders.WWWAuthenticate),
    AccessControlAllowOrigin(HttpHeaders.AccessControlAllowOrigin),
    AccessControlAllowMethods(HttpHeaders.AccessControlAllowMethods),
    AccessControlAllowCredentials(HttpHeaders.AccessControlAllowCredentials),
    AccessControlAllowHeaders(HttpHeaders.AccessControlAllowHeaders),
    AccessControlRequestMethod(HttpHeaders.AccessControlRequestMethod),
    AccessControlRequestHeaders(HttpHeaders.AccessControlRequestHeaders),
    AccessControlExposeHeaders(HttpHeaders.AccessControlExposeHeaders),
    AccessControlMaxAge(HttpHeaders.AccessControlMaxAge),
    XHttpMethodOverride(HttpHeaders.XHttpMethodOverride),
    XForwardedHost(HttpHeaders.XForwardedHost),
    XForwardedServer(HttpHeaders.XForwardedServer),
    XForwardedProto(HttpHeaders.XForwardedProto),
    XForwardedFor(HttpHeaders.XForwardedFor),
    XForwardedPort(HttpHeaders.XForwardedPort),
    XRequestId(HttpHeaders.XRequestId),
    XCorrelationId(HttpHeaders.XCorrelationId),
    XTotalCount(HttpHeaders.XTotalCount),
    LastEventID(HttpHeaders.LastEventID),
}

enum class RASHttpMethods(val ktor: HttpMethod) {
    Get(HttpMethod.Get),
    Post(HttpMethod.Post),
    Put(HttpMethod.Put),
    Patch(HttpMethod.Patch),
    Delete(HttpMethod.Delete),
    Head(HttpMethod.Head),
    Options(HttpMethod.Options),
}