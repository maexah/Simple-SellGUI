# Simple Sell GUI

Lightweight Paper plugin providing a chest-sized sell menu that reads prices directly from Essentials' `worth.yml` and pays players via Vault.

## Building

This project uses Gradle. From the repository root run:

```bash
gradle build
```

The plugin jar will be created in `build/libs/`.

## Usage

* Install Vault and an economy plugin (e.g., EssentialsX Economy) on Paper 1.21.10+.
* Place this plugin jar into the `plugins` folder and restart the server.
* Use `/sellgui` (alias `/sell`) to open the 54-slot sell inventory.
* Drop items into the inventory; the Sell button shows the current value when hovered.
* Click Sell to open a confirmation menu (green = confirm, red = cancel). Items are safely returned if the window is closed without selling.
