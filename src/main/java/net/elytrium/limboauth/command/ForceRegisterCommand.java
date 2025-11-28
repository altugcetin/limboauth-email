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

package net.elytrium.limboauth.command;

import com.j256.ormlite.dao.Dao;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import net.elytrium.commons.kyori.serialization.Serializer;
import net.elytrium.limboauth.LimboAuth;
import net.elytrium.limboauth.Settings;
import net.elytrium.limboauth.model.RegisteredPlayer;
import net.elytrium.limboauth.model.SQLRuntimeException;
import net.kyori.adventure.text.Component;

public class ForceRegisterCommand extends RatelimitedCommand {

  private final LimboAuth plugin;
  private final Dao<RegisteredPlayer, String> playerDao;

  private final String successful;
  private final String notSuccessful;
  private final Component usage;
  private final Component takenNickname;
  private final Component incorrectNickname;
  private final Component invalidEmail;
  private final Component emailDomainBlocked;
  private final Component emailDomainNotAllowed;
  private final Component emailPlusNotAllowed;
  private final Component emailTooShort;
  private final Component emailLooksRandom;
  private final Component emailAlreadyUsed;
  private final Pattern emailPattern;

  public ForceRegisterCommand(LimboAuth plugin, Dao<RegisteredPlayer, String> playerDao) {
    this.plugin = plugin;
    this.playerDao = playerDao;

    this.successful = Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_SUCCESSFUL;
    this.notSuccessful = Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_NOT_SUCCESSFUL;
    this.usage = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_USAGE);
    this.takenNickname = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_TAKEN_NICKNAME);
    this.incorrectNickname = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.FORCE_REGISTER_INCORRECT_NICKNAME);
    this.invalidEmail = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_EMAIL_INVALID);
    this.emailDomainBlocked = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_EMAIL_DOMAIN_BLOCKED);
    this.emailDomainNotAllowed = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_EMAIL_DOMAIN_NOT_ALLOWED);
    this.emailPlusNotAllowed = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_EMAIL_PLUS_NOT_ALLOWED);
    this.emailTooShort = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_EMAIL_TOO_SHORT);
    this.emailLooksRandom = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_EMAIL_LOOKS_RANDOM);
    this.emailAlreadyUsed = LimboAuth.getSerializer().deserialize(Settings.IMP.MAIN.STRINGS.REGISTER_EMAIL_ALREADY_USED);
    this.emailPattern = Pattern.compile(Settings.IMP.MAIN.EMAIL_REGEX);
  }

  @Override
  public void execute(CommandSource source, String[] args) {
    if (args.length == 3) {
      String nickname = args[0];
      String password = args[1];
      String email = args[2];

      Serializer serializer = LimboAuth.getSerializer();
      try {
        if (!this.plugin.getNicknameValidationPattern().matcher(nickname).matches()) {
          source.sendMessage(this.incorrectNickname);
          return;
        }

        if (!this.emailPattern.matcher(email).matches()) {
          source.sendMessage(this.invalidEmail);
          return;
        }

        // Extract local part and domain
        int atIndex = email.indexOf('@');
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1).toLowerCase();

        // Block emails with '+'
        if (Settings.IMP.MAIN.BLOCK_PLUS_EMAILS && localPart.contains("+")) {
          source.sendMessage(this.emailPlusNotAllowed);
          return;
        }

        // Normalize Gmail addresses
        String normalizedLocalPart = localPart;
        if (Settings.IMP.MAIN.NORMALIZE_GMAIL && 
            (domain.equals("gmail.com") || domain.equals("googlemail.com"))) {
          normalizedLocalPart = localPart.replace(".", "");
        }

        // Check minimum length
        if (normalizedLocalPart.length() < Settings.IMP.MAIN.MIN_EMAIL_LOCAL_LENGTH) {
          source.sendMessage(this.emailTooShort);
          return;
        }

        // Check for random-looking emails
        if (Settings.IMP.MAIN.BLOCK_RANDOM_EMAILS && looksLikeRandomEmail(normalizedLocalPart)) {
          source.sendMessage(this.emailLooksRandom);
          return;
        }

        // Check if email is already used
        List<RegisteredPlayer> existingPlayers = this.playerDao.queryForEq(RegisteredPlayer.EMAIL_FIELD, email.toLowerCase());
        if (existingPlayers != null && !existingPlayers.isEmpty()) {
          source.sendMessage(this.emailAlreadyUsed);
          return;
        }

        // Check email domain
        if (!Settings.IMP.MAIN.ALLOWED_EMAIL_DOMAINS.isEmpty()) {
          boolean allowed = Settings.IMP.MAIN.ALLOWED_EMAIL_DOMAINS.stream()
              .anyMatch(allowedDomain -> domain.equalsIgnoreCase(allowedDomain));
          if (!allowed) {
            source.sendMessage(this.emailDomainNotAllowed);
            return;
          }
        } else {
          boolean blocked = Settings.IMP.MAIN.BLOCKED_EMAIL_DOMAINS.stream()
              .anyMatch(blockedDomain -> domain.equalsIgnoreCase(blockedDomain));
          if (blocked) {
            source.sendMessage(this.emailDomainBlocked);
            return;
          }
        }

        String lowercaseNickname = nickname.toLowerCase(Locale.ROOT);
        if (this.playerDao.idExists(lowercaseNickname)) {
          source.sendMessage(this.takenNickname);
          return;
        }

        RegisteredPlayer player = new RegisteredPlayer(nickname, "", "")
            .setPassword(password)
            .setEmail(email);
        this.playerDao.create(player);

        source.sendMessage(serializer.deserialize(MessageFormat.format(this.successful, nickname)));
      } catch (SQLException e) {
        source.sendMessage(serializer.deserialize(MessageFormat.format(this.notSuccessful, nickname)));
        throw new SQLRuntimeException(e);
      }
    } else {
      source.sendMessage(this.usage);
    }
  }

  @Override
  public boolean hasPermission(SimpleCommand.Invocation invocation) {
    return Settings.IMP.MAIN.COMMAND_PERMISSION_STATE.FORCE_REGISTER
        .hasPermission(invocation.source(), "limboauth.admin.forceregister");
  }

  private static boolean looksLikeRandomEmail(String localPart) {
    String lower = localPart.toLowerCase();
    String lettersOnly = lower.replaceAll("[^a-z]", "");
    
    if (lettersOnly.length() < 2 && localPart.length() > 4) {
      return true;
    }
    
    if (lettersOnly.isEmpty()) {
      return false;
    }

    int vowels = 0;
    int consonants = 0;
    int maxConsecutiveConsonants = 0;
    int currentConsonants = 0;
    String vowelChars = "aeiou";
    
    for (char c : lettersOnly.toCharArray()) {
      if (vowelChars.indexOf(c) >= 0) {
        vowels++;
        currentConsonants = 0;
      } else {
        consonants++;
        currentConsonants++;
        maxConsecutiveConsonants = Math.max(maxConsecutiveConsonants, currentConsonants);
      }
    }

    if (maxConsecutiveConsonants >= 5) {
      return true;
    }

    if (lettersOnly.length() >= 6 && vowels > 0) {
      double ratio = (double) consonants / vowels;
      if (ratio > 4.0) {
        return true;
      }
    }

    if (vowels == 0 && lettersOnly.length() >= 5) {
      return true;
    }

    return false;
  }
}
