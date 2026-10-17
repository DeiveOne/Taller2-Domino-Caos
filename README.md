# Dominó Caos (Android · Kotlin · Jetpack Compose · Firebase)

## Puesta en marcha
1. Abre la carpeta `DominoCaos` en Android Studio (Ladybug o superior, JDK 17) y espera el *Gradle Sync*.
   Si el sync falla por falta del wrapper: *File ▸ New ▸ New Project ▸ Empty Activity* con paquete
   `com.udistrital.dominocaos`, y copia encima `app/src`, `app/build.gradle.kts`, `build.gradle.kts` y `settings.gradle.kts`.
2. En la consola de Firebase crea un proyecto y registra la app Android con el paquete `com.udistrital.dominocaos`.
3. Descarga `google-services.json` y colócalo en `app/`.
4. Activa **Authentication ▸ Correo/contraseña** y crea la base **Cloud Firestore**.
5. Reglas de Firestore (desarrollo):
```
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {
    match /rooms/{room} { allow read, write: if request.auth != null; }
  }
}
```
6. Ejecuta en 2 emuladores/dispositivos: uno crea la sala (código de 5 letras), el otro se une; el anfitrión inicia.
7. Pruebas unitarias: `./gradlew testDebugUnitTest` (o clic derecho en `GameEngineTest`).

## Estructura (MVVM)
- `domain/` modelo + `GameEngine` (reglas puras, sin Android/Firebase)
- `data/` `RoomRepository` (Firestore en tiempo real + transacciones) y `AuthRepository`
- `viewmodel/` `AuthViewModel`, `LobbyViewModel`, `GameViewModel`
- `ui/components/` `DominoTile`, `BoardView`, `HandView`, `PlayerCard`, `PowerBar`, `ControlsBar`
- `ui/screens/` `AuthScreen`, `LobbyScreen`, `RoomScreen`
