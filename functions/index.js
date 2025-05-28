import { onDocumentUpdated } from "firebase-functions/v2/firestore";
import { initializeApp } from "firebase-admin/app";
import { getFirestore } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";

initializeApp();

export const notifyOnStatusCompleteV2 = onDocumentUpdated(
  {
    document: "smsReports/{reportId}",
    region: "asia-northeast3", // ✅ 여기에 region 설정!
  },
  async (event) => {
    const before = event.data.before.data();
    const after = event.data.after.data();

    if (after.status === "처리완료") {
      const userId = after.uid;
      const reportTitle = after.building || "신고";

      try {
        const userDoc = await getFirestore().collection("users").doc(userId).get();
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

        await getMessaging().send(message);
        console.log("✅ 푸시 전송 성공:", fcmToken);
      } catch (error) {
        console.error("❌ 푸시 전송 실패:", error);
      }
    }
  }
);
