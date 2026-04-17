# Mohamed Lovers Firebase Setup

This screen is implemented in the Android app, but Firebase must be configured in the project and console before it can work in production.

## Required app setup

1. Add your real `google-services.json` to `app/google-services.json`.
2. Enable **Anonymous Authentication** in Firebase Authentication.
3. Create a **Realtime Database**.
4. Enable **App Check**:
   - Debug builds: register the debug token from Logcat.
   - Release builds: use Play Integrity.

## Recommended Realtime Database rules

These rules keep each authenticated user limited to their own record and only allow monotonic counter growth. They are a strong baseline, but fully tamper-proof counting still requires a trusted backend or Cloud Functions.

```json
{
  "rules": {
    ".read": false,
    ".write": false,
    "mohamed_lovers": {
      "$roundKey": {
        "players": {
          "$uid": {
            ".read": "auth != null",
            ".write": "auth != null && auth.uid === $uid",
            ".validate": "newData.hasChildren(['uid', 'alias', 'countryCode', 'totalCount', 'winnerCode', 'updatedAt']) && newData.child('uid').val() === auth.uid && newData.child('alias').isString() && newData.child('alias').val().length <= 40 && newData.child('countryCode').val() === 'EG' && newData.child('winnerCode').isString() && newData.child('winnerCode').val().length <= 32 && newData.child('totalCount').isNumber() && newData.child('totalCount').val() >= (data.exists() ? data.child('totalCount').val() : 0) && newData.child('totalCount').val() <= (data.exists() ? data.child('totalCount').val() + 5000 : 5000)"
          }
        }
      }
    }
  }
}
```

## Current database shape

```text
mohamed_lovers/
  2026-04-17/
    players/
      <uid>/
        alias: "محب محمد 1A2B"
        countryCode: "EG"
        totalCount: 42
        uid: "<firebase-anon-uid>"
        updatedAt: <server timestamp>
        winnerCode: ""
```

## Important note

The app already uses:

- network time through `Kronos`
- anonymous Firebase auth
- App Check provider initialization
- local session batching and Firebase transaction updates on screen close

For a prize flow where counts must be impossible to spoof, move the increment logic to a trusted backend or Cloud Function and validate App Check plus auth there.
