# Pandabridge

## Installing

Install Forge into your Minecraft installation and copy `pandabridgemod.jar` to the `mods` folder of your server setup.

Configure the mod by adding `pandabridge.cfg` to the `config` folder, make sure it contains at least the following settings:

* host, scheme and your homeserver's address
* mainRoom, internal id for the main room
* hsKey (from appservice config)
* asKey (from appcervice config)

For example the file could contain the following:

```
host=https://yourmatrixserver.example.com
mainRoom=!asdInternalIdasdgadfs:yourmatrixserver.example.com
hsKey=fasuifUBHIASdbia
asKey=ADisnidoasdsaoni
```