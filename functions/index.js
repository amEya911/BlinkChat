/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

// const {onRequest} = require("firebase-functions/v2/https");
// const logger = require("firebase-functions/logger");

const { onDocumentUpdated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

// Initialize Firebase Admin
initializeApp();
const db = getFirestore();

exports.cleanUpInactiveChatRooms = onDocumentUpdated("chatRooms/{chatRoomId}", async (event) => {
    const chatRoomId = event.params.chatRoomId;
    const newData = event.data.after.data();

    if (!newData || !newData.activeUsers) return null;

    const { initiator, recipient } = newData.activeUsers;

    // If both users are inactive, delete the chat room immediately
    if (!initiator && !recipient) {
        console.log(`Deleting chatRoom: ${chatRoomId}`);
        return db.collection("chatRooms").doc(chatRoomId).delete();
    }

    return null;
});


// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
