let functions = require('firebase-functions');
let admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.firestore.document('notifications/{pushId}').onWrite((change, context) => {
        const message = change.after.exists ? change.after.data() : null;
		if (message === null) {
			return;
		}
        const senderUid = message.fromUid;
        const topicUid = message.topicUid;
		var payload;
		if (message.type === 'chat') {


           // console.log('notifying ' + topicUid + ' about ' + message.content + ' from ' + senderUid);
			
            payload = {
				data: {
					title: message.title,
					body: message.body,
					senderUid: senderUid,
					listingUid: topicUid,
					type: message.type,
					notification: '1',
				},
				android: {
					priority: 'high'
				},
				topic: topicUid
            };
		} else if (message.type === 'create') {
			payload = {
				data: {
					title: message.title,
					body: message.body,
					senderUid: senderUid,
					listingUid: message.listingUid,
					type: message.type,
					notification: '1',
				},
				android: {
					priority: 'high'
				},
				topic: topicUid
            };
		} else if (message.type === 'delete') {


           // console.log('notifying ' + topicUid + ' about ' + message.content + ' from ' + senderUid);
			
            payload = {
				data: {
					title: message.title,
					body: message.body,
					senderUid: senderUid,
					listingUid: topicUid,
					type: message.type,
					notification: '1',
				},
				android: {
					priority: 'high'
				},
				topic: topicUid
            };
		} else if (message.type === 'mute') {
			payload = {
				data: {
					title: 'Mugger Administration',
					body: 'You have been muted for ' + message.duration + ' hours.',
					senderUid: senderUid,
					type: message.type,
					notification: '1',
					until: message.until,
					listingUid: 'mute'
				},
				android: {
					priority: 'high'
				},
				token: message.instanceId
			}
		} else if (message.type === 'unmute') {
			payload = {
				data: {
					title: 'Mugger Administration',
					body: 'You have been unmuted',
					senderUid: senderUid,
					type: message.type,
					notification: '1',
					listingUid: 'mute'
				},
				android: {
					priority: 'high'
				},
				token: message.instanceId
			}
		} else if (message.type === 'role') {
			payload = {
				data: {
					title: 'Mugger Administration',
					body: 'Your role has been changed to ' + message.newRoleName,
					senderUid: senderUid,
					type: message.type,
					notification: '1',
					listingUid: 'role',
					newRoleName: message.newRoleName
				},
				android: {
					priority: 'high'
				},
				token: message.instanceId
			}
		}
            return admin.messaging().send(payload)
                .then(function (response) {
                    console.log("Successfully sent message:", response);
					change.after.ref.delete();
					return;
                })
                .catch(function (error) {
                    console.log("Error sending message:", error);
                });

    });
