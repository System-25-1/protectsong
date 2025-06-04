const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifyOnStatusCompleteV2 = functions
  .region("asia-northeast3")
  .firestore
  .document("smsReports/{reportId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    if (before.status !== "처리완료" && after.status === "처리완료") {
      const userId = after.userId;
      const reportTitle = after.building || "신고";

      try {
        const userDoc = await admin.firestore().collection("users").doc(userId).get();
        const fcmToken = userDoc.data()?.fcmToken;

        if (!fcmToken) {
          console.log("⚠️ FCM 토큰이 없음");
          return;
        }

        const message = {
          token: fcmToken,
          notification: {
            title: "신고 처리 완료",
            body: `${reportTitle} 신고가 처리되었습니다.`,
          },
        };

        await admin.messaging().send(message);
        console.log("✅ 푸시 전송 성공:", fcmToken);
      } catch (error) {
        console.error("❌ 푸시 전송 실패:", error);
      }
    }
  });
