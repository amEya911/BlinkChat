# BlinkChat

**BlinkChat** is designed for private, real-time conversations where messages are automatically deleted after exiting the chat screen. The chat only starts when both people are present, making it feel like talking face-to-face, but online. You can join through guest mode by sharing a link or via mobile verification to chat with your contacts. There’s no message history, no data storage, just secure, one-on-one conversations that vanish instantly. It's ideal for confidential discussions, quick one-time chats, or anything where privacy really matters.

## Features

- **Private, real-time messaging**: Messages are automatically deleted when exiting the chat screen.
- **No message history**: Once the chat is over, everything vanishes.
- **Guest mode**: Start chatting with just a link.
- **Mobile verification**: Easy way to chat with contacts.
- **Focus on privacy**: No data storage, just secure, confidential conversations.

## Screenshots
Here are some screenshots of the game in action:
![Screenshot](https://github.com/user-attachments/assets/64e00838-e9b6-45ea-bcf7-e6a8098fd881)

## Current Progress

I am currently working on adding more features to **BlinkChat**, so stay tuned for updates.

## Want to Use the Project?

To get started with **BlinkChat**, clone the project with the following link:
`https://github.com/amEya911/BlinkChat.git`

After cloning, navigate to the `utils` folder and add a Kotlin object file named `Ids.kt`. In this file, add the following:

```kotlin
object Ids {
    const val IDENTITY_POOL_ID = "YOUR_IDENTITY_POOL_ID"
    const val ACCESS_TOKEN = "YOUR_FIREBASE_ACCESS_TOKEN"
    const val FCM_URL = "YOUR_FIREBASE_FCM_URL"
}
