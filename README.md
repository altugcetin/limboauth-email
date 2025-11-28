# LimboAuth-Email

LimboAuth fork with mandatory email registration and advanced email filtering for Velocity proxy.

This is a fork of [LimboAuth](https://github.com/Elytrium/LimboAuth) by Elytrium.

**Compatible with [LeaderOS](https://leaderos.net) server websites.**

## Features

### Email Registration
- Players must provide email during registration: `/register <password> <email>`
- Email format validation with configurable regex
- Email stored in database for account recovery

### Anti-Abuse Protection

**Domain Filtering**
- Blacklist Mode: Block 30+ disposable email services (tempmail, mailinator, guerrillamail, etc.)
- Whitelist Mode: Only allow specific email domains (gmail.com, hotmail.com, etc.)

**Gmail Protection**
- Block `+` alias trick (`email+random@gmail.com`)
- Normalize dot trick (`e.m.a.i.l@gmail.com` = `email@gmail.com`)

**Random Email Detection**
- Detects and blocks random strings like `xjkhsdqw@gmail.com`
- Uses consonant clustering and vowel ratio analysis
- Configurable minimum local part length

### Compatibility
- Velocity 3.4.0+ support
- MySQL, PostgreSQL, H2, SQLite support
- Full LimboAPI compatibility
- LeaderOS website integration support

## Commands

### Player Commands

| Command | Description |
|---------|-------------|
| `/register <password> <email>` | Register with password and email |
| `/login <password>` | Login to your account |
| `/changepassword <old> <new>` | Change your password |
| `/unregister <password> confirm` | Delete your account |
| `/premium <password> confirm` | Switch to premium account |
| `/2fa enable/disable` | Manage 2FA |

### Admin Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/forceregister <nick> <pass> <email>` | `limboauth.admin.forceregister` | Force register a player |
| `/forceunregister <nick>` | `limboauth.admin.forceunregister` | Force unregister a player |
| `/forcechangepassword <nick> <pass>` | `limboauth.admin.forcechangepassword` | Force change password |
| `/lauth reload` | `limboauth.admin.reload` | Reload configuration |

## Configuration

### Email Settings (config.yml)

```yaml
# Email validation regex
email-regex: "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"

# Blocked disposable email domains
blocked-email-domains:
  - tempmail.com
  - mailinator.com
  - guerrillamail.com
  - 10minutemail.com
  - yopmail.com

# Whitelist mode (leave empty to use blacklist)
allowed-email-domains: []

# Block emails with '+' (Gmail alias trick)
block-plus-emails: true

# Normalize Gmail addresses (remove dots)
normalize-gmail: true

# Minimum email local part length
min-email-local-length: 3

# Block random-looking email addresses
block-random-emails: true
```

## Installation

1. Download the latest release
2. Place the JAR in your Velocity `plugins` folder
3. Make sure [LimboAPI](https://github.com/Elytrium/LimboAPI) is installed
4. Restart your Velocity proxy
5. Configure `plugins/limboauth/config.yml`

## Database

This plugin is compatible with:
- H2 (default, file-based)
- MySQL / MariaDB
- PostgreSQL
- SQLite

The `email` column is automatically added to the `Accounts` table.

## Examples

### Accepted Emails
```
ahmet.yilmaz@gmail.com
mehmet123@hotmail.com
ayse_kaya@outlook.com
user@company.com
```

### Blocked Emails
```
random123@tempmail.com      - Disposable domain
xjkhsdqw@gmail.com          - Random string
email+spam@gmail.com        - Plus alias
a@gmail.com                 - Too short
bcdfgh@gmail.com            - No vowels
```

## Credits

- [Elytrium](https://elytrium.net/) - Original LimboAuth developers
- [LimboAPI](https://github.com/Elytrium/LimboAPI) - Virtual server library
- AstroAlchemist (Blocksmiths) - Email registration fork

## License

This project is licensed under the AGPL-3.0 License.
