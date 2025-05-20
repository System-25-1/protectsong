package com.example.protectsong

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.protectsong.databinding.ActivityUserListBinding
import com.example.protectsong.model.User
import com.example.protectsong.adapter.UserAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        firestore.collection("users").document(auth.currentUser?.uid ?: "").get()
            .addOnSuccessListener { document ->
                val isAdmin = document.getBoolean("isAdmin") ?: false
                if (!isAdmin) {
                    Toast.makeText(this, "접근 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    setupRecyclerView()
                    loadUsers()
                }
            }
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter()
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewUsers.adapter = adapter
    }

    private fun loadUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.mapNotNull { it.toObject(User::class.java) }
                adapter.submitList(users)
            }
            .addOnFailureListener {
                Toast.makeText(this, "불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
