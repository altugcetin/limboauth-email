/*
 * Copyright (C) 2021 - 2024 Elytrium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.elytrium.limboauth.model;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.velocitypowered.api.proxy.Player;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import net.elytrium.limboauth.Settings;

@DatabaseTable(tableName = "Accounts")
public class RegisteredPlayer {

  public static final String ID_FIELD = "id";
  public static final String UUID_FIELD = "uuid";
  public static final String LOWERCASE_NICKNAME_FIELD = "username";
  public static final String NICKNAME_FIELD = "realname";
  public static final String EMAIL_FIELD = "email";
  public static final String HASH_FIELD = "password";
  public static final String LOGIN_DATE_FIELD = "lastlogin";
  public static final String CREDIT_FIELD = "credit";
  public static final String ISVERIFIED_FIELD = "isVerified";
  public static final String AUTHSTATUS = "authStatus";
  public static final String IP_FIELD = "creationIP";
  public static final String REG_DATE_FIELD = "creationDate";
  public static final String LOGIN_IP_FIELD = "ip";
  public static final String TOTP_TOKEN_FIELD = "TOTPTOKEN";
  public static final String PREMIUM_UUID_FIELD = "PREMIUMUUID";
  public static final String TOKEN_ISSUED_AT_FIELD = "ISSUEDTIME";

  private static final BCrypt.Hasher HASHER = BCrypt.withDefaults();

  @DatabaseField(generatedId = true, columnName = ID_FIELD)
  private int aid;

  @DatabaseField(canBeNull = true, columnName = UUID_FIELD)
  private String uuid = "";

  @DatabaseField(canBeNull = false, unique = true, columnName = LOWERCASE_NICKNAME_FIELD)
  private String lowercaseNickname;

  @DatabaseField(canBeNull = false, columnName = NICKNAME_FIELD)
  private String nickname;

  @DatabaseField(defaultValue = "your@email.com", canBeNull = false, columnName = EMAIL_FIELD)
  private String email = "";

  @DatabaseField(canBeNull = false, columnName = HASH_FIELD)
  private String hash = "";

  @DatabaseField(columnName = LOGIN_DATE_FIELD)
  private Long loginDate = System.currentTimeMillis();

  @DatabaseField(canBeNull = false, columnName = CREDIT_FIELD,
          columnDefinition = "DECIMAL(8,2) DEFAULT 0.00")
  private double credit;

  @DatabaseField(canBeNull = false, defaultValue = "1", columnName = ISVERIFIED_FIELD,
          columnDefinition = "ENUM('0', '1') DEFAULT '1'")
  private String isVerified;

  @DatabaseField(canBeNull = false, defaultValue = "0", columnName = AUTHSTATUS,
          columnDefinition = "ENUM('0', '1') DEFAULT '0'")
  private String authStatus;

  @DatabaseField(columnName = IP_FIELD)
  private String ip;

  @DatabaseField(columnName = REG_DATE_FIELD,
          dataType = DataType.DATE_STRING,
          format = "yyyy-MM-dd HH:mm:ss",
          columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
  private Date regDate;

  @DatabaseField(columnName = LOGIN_IP_FIELD)
  private String loginIp;

  @DatabaseField(columnName = TOTP_TOKEN_FIELD)
  private String totpToken = "";

  @DatabaseField(columnName = RegisteredPlayer.PREMIUM_UUID_FIELD)
  private String premiumUuid = "";

  @DatabaseField(columnName = TOKEN_ISSUED_AT_FIELD)
  private Long tokenIssuedAt = System.currentTimeMillis();

  @Deprecated
  public RegisteredPlayer(String nickname, String lowercaseNickname,
                          String hash, String ip, String totpToken, Date regDate, String uuid, String premiumUuid, String loginIp, Long loginDate) {
    this.uuid = uuid;
    this.lowercaseNickname = lowercaseNickname;
    this.nickname = nickname;
    this.hash = hash;
    this.ip = ip;
    this.totpToken = totpToken;
    this.regDate = regDate;
    this.premiumUuid = premiumUuid;
    this.loginIp = loginIp;
    this.loginDate = loginDate;
  }

  public RegisteredPlayer(Player player) {
    this(player.getUsername(), player.getUniqueId(), player.getRemoteAddress());
    this.regDate = new Date();
  }

  public RegisteredPlayer(String nickname, UUID uuid, InetSocketAddress ip) {
    this(nickname, uuid.toString(), ip.getAddress().getHostAddress());
    this.regDate = new Date();
  }

  public RegisteredPlayer(String nickname, String uuid, String ip) {
    this.nickname = nickname;
    this.lowercaseNickname = nickname.toLowerCase(Locale.ROOT);
    this.uuid = uuid;
    this.ip = ip;
    this.loginIp = ip;
    this.regDate = new Date();
  }

  public RegisteredPlayer() {

  }

  public static String genHash(String password) {
    return HASHER.hashToString(Settings.IMP.MAIN.BCRYPT_COST, password.toCharArray());
  }

  public RegisteredPlayer setNickname(String nickname) {
    this.nickname = nickname;
    this.lowercaseNickname = nickname.toLowerCase(Locale.ROOT);

    return this;
  }

  public String getNickname() {
    return this.nickname == null ? this.lowercaseNickname : this.nickname;
  }

  public String getLowercaseNickname() {
    return this.lowercaseNickname;
  }

  public RegisteredPlayer setPassword(String password) {
    this.hash = genHash(password);
    this.tokenIssuedAt = System.currentTimeMillis();

    return this;
  }

  public RegisteredPlayer setHash(String hash) {
    this.hash = hash;
    this.tokenIssuedAt = System.currentTimeMillis();

    return this;
  }

  public String getHash() {
    return this.hash == null ? "" : this.hash;
  }

  public RegisteredPlayer setIP(String ip) {
    this.ip = ip;

    return this;
  }

  public String getIP() {
    return this.ip == null ? "" : this.ip;
  }

  public RegisteredPlayer setTotpToken(String totpToken) {
    this.totpToken = totpToken;

    return this;
  }

  public String getTotpToken() {
    return this.totpToken == null ? "" : this.totpToken;
  }

  public RegisteredPlayer setRegDate(Date regDate) {
    this.regDate = regDate;
    return this;
  }

  public Date getRegDate() {
    return this.regDate == null ? new Date() : this.regDate;
  }

  public RegisteredPlayer setUuid(String uuid) {
    this.uuid = uuid;

    return this;
  }

  public String getUuid() {
    return this.uuid == null ? "" : this.uuid;
  }

  public RegisteredPlayer setPremiumUuid(String premiumUuid) {
    this.premiumUuid = premiumUuid;

    return this;
  }

  public RegisteredPlayer setPremiumUuid(UUID premiumUuid) {
    this.premiumUuid = premiumUuid.toString();

    return this;
  }

  public String getPremiumUuid() {
    return this.premiumUuid == null ? "" : this.premiumUuid;
  }

  public String getLoginIp() {
    return this.loginIp == null ? "" : this.loginIp;
  }

  public RegisteredPlayer setLoginIp(String loginIp) {
    this.loginIp = loginIp;

    return this;
  }

  public long getLoginDate() {
    return this.loginDate == null ? Long.MIN_VALUE : this.loginDate;
  }

  public RegisteredPlayer setLoginDate(Long loginDate) {
    this.loginDate = loginDate;

    return this;
  }

  public long getTokenIssuedAt() {
    return this.tokenIssuedAt == null ? Long.MIN_VALUE : this.tokenIssuedAt;
  }

  public RegisteredPlayer setTokenIssuedAt(Long tokenIssuedAt) {
    this.tokenIssuedAt = tokenIssuedAt;

    return this;
  }

  public String getEmail() {
    return this.email == null ? "" : this.email;
  }

  public RegisteredPlayer setEmail(String email) {
    this.email = email;

    return this;
  }
}
