[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/A0A61IAEQ5)

# UTrade
A secure, lightweight, and exploit-free GUI trading plugin designed specifically for Minecraft 1.12.2 SMP servers.

UTrade provides a way for players to safely exchange items, economy money, and experience levels in a single double-chest interface.

# Features

- Secure 54-Slot GUI: A split-screen trade window where both players can safely offer items without dropping them on the ground.
- Vault Economy Support: Trade your server's digital currency directly within the GUI.
- Experience Level Trading: Safely wager and exchange XP levels.
- Anti-Scam Protection: If either player adds, removes, or shifts an item while the other player is "Ready", both players are immediately un-readied.
- Interactive Chat Invites: Uses the native 1.12.2 chat API to send clickable [ACCEPT] button.
- Safe Aborts: If a player crashes, logs out, or closes the window mid-trade, all items are instantly and safely returned to their owners.

# Requirements
- Server Version: Paper / Spigot 1.12.2
- Java Version: Java 8
- Dependencies: Vault (You must also have an economy provider installed)

# Installation
1. Download the latest `UTrade-x.x.jar` from the Releases page.
2. Place the JAR file into your server's `plugins` directory.
3. Ensure you have Vault installed.
4. Restart your server.

# Commands
- `/trade <player>`
- `/trade accept`
