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

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.cleanUpInactiveChatRooms = functions.firestore
    .document("chatRooms/{chatRoomId}")
    .onUpdate(async (change, context) => {
      const chatRoomId = context.params.chatRoomId;
      const newData = change.after.data();
      const previousData = change.before.data();

      if (!newData || !newData.activeUsers) return null;

      const {initiator, recipient} = newData.activeUsers;

      // Check if both users are inactive
      if (!initiator && !recipient) {
        const now = admin.firestore.Timestamp.now().toMillis();
        const lastUpdated = newData.lastUpdated ||
                            previousData.lastUpdated ||
                            now;

        // If already inactive for 30 seconds, delete the chat room
        if (now - lastUpdated > 30000) {
          console.log(`Deleting chatRoom: ${chatRoomId}`);
          return admin.firestore()
              .collection("chatRooms")
              .doc(chatRoomId)
              .delete();
        } else {
        // Otherwise, update the lastUpdated timestamp
          return change.after.ref.update({lastUpdated: now});
        }
      }

      return null;
    });


// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// exports.helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });
