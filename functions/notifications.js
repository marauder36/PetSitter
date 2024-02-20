const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendNotificationOnNewMessage = functions.database.ref('/messages/{messageId}')
    .onCreate((snapshot, context) => {
        const messageData = snapshot.val();
        const receiverId = messageData.receiver.id;
        const senderName = messageData.sender.name;
	const receiverType=messageData.receiverType;

        // Construct the notification payload
        const payload = {
            notification: {
                title: 'New Message Received!',
                body: `${senderName} sent you a message.`,
                // Add more custom data if needed
            }
        };

        // Retrieve the receiver's FCM token from your database
        admin.database().ref(`/receiverType/users/${receiverId}/fcmToken`).once('value')
            .then((tokenSnapshot) => {
                const receiverToken = tokenSnapshot.val();

                // Send the notification to the receiver
                return admin.messaging().sendToDevice(receiverToken, payload);
            })
            .catch((error) => {
                console.error('Error sending notification:', error);
            });
    });
