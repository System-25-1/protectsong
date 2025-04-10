<?xml version="1.0" encoding="utf-8"?>
<androidx.drawerlayout.widget.DrawerLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/drawer_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <!-- 메인 콘텐츠 -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <!-- 툴바 -->
        <androidx.appcompat.widget.Toolbar
            android:id="@+id/toolbar"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="#002366"
            android:title="지키송"
            android:titleTextColor="@android:color/white" />

        <!-- 스크롤 가능한 콘텐츠 -->
        <ScrollView
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="16dp">

                <!-- 신고 버튼 영역 -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center">

                    <Button
                        android:id="@+id/btn_text_report"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="문자 신고" />

                    <Button
                        android:id="@+id/btn_emergency"
                        android:layout_width="100dp"
                        android:layout_height="100dp"
                        android:layout_margin="16dp"
                        android:backgroundTint="#8B0000"
                        android:text="긴급 신고"
                        android:textColor="@android:color/white" />

                    <Button
                        android:id="@+id/btn_call_report"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="전화 신고" />
                </LinearLayout>

                <!-- 휘장 + 기능 버튼 -->
                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center"
                    android:layout_marginTop="24dp">

                    <ToggleButton
                        android:id="@+id/btn_whistle"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textOff="호루라기"
                        android:textOn="호루라기 ON" />

                    <!-- 휘장 이미지 -->
                    <ImageView
                        android:layout_width="80dp"
                        android:layout_height="80dp"
                        android:src="@drawable/star_icon"
                        android:layout_marginHorizontal="24dp" />

                    <ToggleButton
                        android:id="@+id/btn_fake_call"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:textOff="위장전화"
                        android:textOn="위장전화 ON" />
                </LinearLayout>

                <!-- 공지사항 영역 -->
                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="공지"
                    android:background="#002366"
                    android:textColor="@android:color/white"
                    android:padding="8dp" />

                <androidx.recyclerview.widget.RecyclerView
                    android:id="@+id/notice_list"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginBottom="16dp" />

            </LinearLayout>
        </ScrollView>

        <!-- 하단 바 -->
        <com.google.android.material.bottomnavigation.BottomNavigationView
            android:id="@+id/bottom_navigation"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/white"
            app:menu="@menu/bottom_nav_menu" />

    </LinearLayout>

    <!-- 네비게이션 뷰 -->
    <com.google.android.material.navigation.NavigationView
        android:id="@+id/navigation_view"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_gravity="start"
        app:menu="@menu/drawer_menu" />

</androidx.drawerlayout.widget.DrawerLayout>
